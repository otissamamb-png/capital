package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.*
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ResidenceRepository(
    val db: FirebaseFirestore,
    val context: Context? = null
) {

    companion object {
        fun create(context: Context): ResidenceRepository {
            val dbId = context.getString(R.string.firestore_database_id)
            return ResidenceRepository(FirebaseFirestore.getInstance(dbId), context.applicationContext)
        }
    }

    private val auth: FirebaseAuth?
        get() = try {
            Firebase.auth
        } catch (_: Throwable) {
            null
        }
    private val prefs = context?.getSharedPreferences("capital_home_auth_prefs", Context.MODE_PRIVATE)

    fun getCurrentSessionUser(): SessionUser? {
        val uid = prefs?.getString("session_uid", null) ?: return null
        val email = prefs?.getString("session_email", "") ?: ""
        val name = prefs?.getString("session_name", "") ?: ""
        val role = prefs?.getString("session_role", "resident") ?: "resident"
        val phone = prefs?.getString("session_phone", "") ?: ""
        val roomId = prefs?.getString("session_room_id", "") ?: ""
        return SessionUser(id = uid, email = email, name = name, role = role, phone = phone, roomId = roomId)
    }

    fun saveSession(user: SessionUser) {
        prefs?.edit()
            ?.putString("session_uid", user.id)
            ?.putString("session_email", user.email)
            ?.putString("session_name", user.name)
            ?.putString("session_role", user.role)
            ?.putString("session_phone", user.phone)
            ?.putString("session_room_id", user.roomId)
            ?.apply()
    }

    fun signOut() {
        prefs?.edit()?.clear()?.apply()
        try {
            auth?.signOut()
        } catch (_: Throwable) {}
    }

    fun requireUserId(): String {
        return getCurrentUserId()
            ?: throw IllegalStateException("User must be signed in before accessing database.")
    }

    fun getCurrentUserId(): String? {
        return getCurrentSessionUser()?.id ?: auth?.currentUser?.uid
    }

    private fun hashPassword(password: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ==========================================
    // PROFILES
    // ==========================================
    fun observeProfile(userId: String): Flow<UserProfile?> {
        return db.collection("profiles").document(userId)
            .snapshots()
            .map { snap ->
                if (snap.exists()) snap.toObject(UserProfile::class.java)?.copy(id = snap.id) else null
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.GET, "profiles/$userId")
                emit(null)
            }
    }

    suspend fun getProfileOnce(userId: String): UserProfile? {
        return try {
            val snap = db.collection("profiles").document(userId).get().await()
            if (snap.exists()) snap.toObject(UserProfile::class.java)?.copy(id = snap.id) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        val uid = profile.auth_user_id.ifBlank { requireUserId() }
        return try {
            val docRef = db.collection("profiles").document(uid)
            val data = mutableMapOf<String, Any>(
                "auth_user_id" to uid,
                "full_name" to profile.full_name,
                "phone" to profile.phone,
                "email" to (getCurrentSessionUser()?.email ?: profile.email),
                "profile_photo" to profile.profile_photo,
                "room_id" to profile.room_id,
                "role" to profile.role,
                "account_status" to profile.account_status,
                "updated_at" to FieldValue.serverTimestamp()
            )
            if (profile.created_at != null) {
                data["created_at"] = profile.created_at
            } else {
                data["created_at"] = FieldValue.serverTimestamp()
            }
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "profiles/$uid")
            Result.failure(e)
        }
    }

    // ==========================================
    // AUTHENTICATION & RESIDENT SIGNUP
    // ==========================================
    suspend fun signInWithEmailPassword(email: String, password: String): Result<Unit> {
        return try {
            val trimmedEmail = email.trim().lowercase()
            val inputHash = hashPassword(password)

            // Try Firebase Auth if available, but do not fail if not configured
            var firebaseAuthSuccess = false
            try {
                auth?.signInWithEmailAndPassword(trimmedEmail, password)?.await()
                firebaseAuthSuccess = true
            } catch (_: Throwable) {}

            // Verify in Firestore profiles collection
            val snap = db.collection("profiles")
                .whereEqualTo("email", trimmedEmail)
                .get().await()

            if (!snap.isEmpty) {
                val doc = snap.documents.first()
                val storedHash = doc.getString("password_hash")
                val uid = doc.getString("auth_user_id") ?: doc.id
                val name = doc.getString("full_name") ?: "Resident"
                val role = doc.getString("role") ?: "resident"
                val phone = doc.getString("phone") ?: ""
                val roomId = doc.getString("room_id") ?: ""

                // Verify password hash or allow if matched
                if (storedHash.isNullOrBlank() || storedHash == inputHash || firebaseAuthSuccess) {
                    saveSession(SessionUser(id = uid, email = trimmedEmail, name = name, role = role, phone = phone, roomId = roomId))
                    return Result.success(Unit)
                } else {
                    return Result.failure(Exception("Incorrect password. Please verify your password and try again."))
                }
            } else {
                // Check if logging in as default resident
                if (trimmedEmail == "alex@capitalhome.co.ke" || trimmedEmail == "resident@capitalhome.com") {
                    val uid = "res_seed_alex"
                    saveSession(SessionUser(id = uid, email = trimmedEmail, name = "Alex Kimani", role = "resident", phone = "+254 712 345 678", roomId = "204"))
                    return Result.success(Unit)
                }
                return Result.failure(Exception("No account registered with $trimmedEmail. Please sign up to create your room."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAdmin(email: String, password: String): Result<Unit> {
        return try {
            val trimmedEmail = email.trim().lowercase()
            if (trimmedEmail != "okindatechhub@gmail.com") {
                return Result.failure(Exception("Unauthorized: Only registered administrators can access the admin portal."))
            }

            if (password != "Okinda3078") {
                return Result.failure(Exception("Invalid administrator password."))
            }

            val uid = "admin_okinda_3078"
            saveSession(SessionUser(
                id = uid,
                email = trimmedEmail,
                name = "System Administrator",
                role = "admin",
                phone = "+254 700 000 000"
            ))

            // Ensure administrator profile exists in database
            try {
                db.collection("profiles").document(uid).set(
                    mapOf(
                        "auth_user_id" to uid,
                        "full_name" to "System Administrator",
                        "email" to trimmedEmail,
                        "phone" to "+254 700 000 000",
                        "role" to "admin",
                        "account_status" to "active",
                        "password_hash" to hashPassword(password),
                        "created_at" to FieldValue.serverTimestamp(),
                        "updated_at" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: Exception) {
                Log.w("ResidenceRepository", "Admin profile update warning: ${e.message}")
            }

            logAdminAction("admin_login", "auth", uid, "Administrator logged into administration dashboard")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isRoomNumberRegistered(roomNumber: String): Boolean {
        return try {
            val snap = db.collection("rooms")
                .whereEqualTo("room_number", roomNumber.trim())
                .get().await()
            !snap.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun registerResidentWithRoom(
        fullName: String,
        phone: String,
        email: String,
        password: String,
        roomNumber: String,
        roomType: String,
        roomStatus: String,
        coverImageUrl: String
    ): Result<Unit> {
        return try {
            val trimmedRoom = roomNumber.trim()
            val trimmedEmail = email.trim().lowercase()
            val trimmedName = fullName.trim()

            // 1. Validate room number uniqueness
            if (isRoomNumberRegistered(trimmedRoom)) {
                return Result.failure(Exception("Room $trimmedRoom is already registered. Please verify your room number."))
            }

            // 2. Validate email uniqueness
            try {
                val existingSnap = db.collection("profiles")
                    .whereEqualTo("email", trimmedEmail)
                    .get().await()
                if (!existingSnap.isEmpty) {
                    return Result.failure(Exception("An account with email $trimmedEmail is already registered. Please log in instead."))
                }
            } catch (e: Exception) {
                Log.w("ResidenceRepository", "Email check warning: ${e.message}")
            }

            // 3. Generate persistent resident ID
            val uid = "usr_" + System.currentTimeMillis() + "_" + (1000..9999).random()

            // Optional Firebase Auth attempt (gracefully handle OPERATION_NOT_ALLOWED)
            try {
                auth?.createUserWithEmailAndPassword(trimmedEmail, password)?.await()
            } catch (e: Throwable) {
                Log.d("ResidenceRepository", "Firebase Auth registration note: ${e.message}")
            }

            val price = if (roomType.contains("New", ignoreCase = true)) 25000L else 15000L
            val normalizedStatus = if (roomStatus.equals("Vacant", ignoreCase = true)) "Vacant" else "Occupied"

            // 4. Create Room document in rooms collection
            val roomDocRef = db.collection("rooms").document()
            val roomData = mapOf(
                "room_number" to trimmedRoom,
                "room_type" to roomType,
                "price_per_semester" to price,
                "status" to normalizedStatus,
                "room_status" to normalizedStatus,
                "cover_image_url" to coverImageUrl,
                "image_url" to coverImageUrl,
                "resident_id" to uid,
                "occupant_name" to trimmedName,
                "occupant_id" to uid,
                "floor" to if (trimmedRoom.toIntOrNull()?.let { it >= 200 } == true) "2nd Floor" else "1st Floor",
                "description" to "$roomType at Capital Home Residence registered by $trimmedName.",
                "features" to listOf("Single Room", "Wi-Fi Included", "Study Desk", "Wardrobe"),
                "created_at" to FieldValue.serverTimestamp(),
                "updated_at" to FieldValue.serverTimestamp()
            )
            roomDocRef.set(roomData).await()

            // 5. Create Profile document in profiles collection
            val passwordHash = hashPassword(password)
            val profileDocRef = db.collection("profiles").document(uid)
            val profileData = mapOf(
                "auth_user_id" to uid,
                "full_name" to trimmedName,
                "phone" to phone.trim(),
                "email" to trimmedEmail,
                "profile_photo" to "",
                "room_id" to roomDocRef.id,
                "role" to "resident",
                "account_status" to "active",
                "password_hash" to passwordHash,
                "created_at" to FieldValue.serverTimestamp(),
                "updated_at" to FieldValue.serverTimestamp()
            )
            profileDocRef.set(profileData).await()

            // 6. Save active session to device storage so user remains logged in
            val sessionUser = SessionUser(
                id = uid,
                email = trimmedEmail,
                name = trimmedName,
                role = "resident",
                phone = phone.trim(),
                roomId = roomDocRef.id
            )
            saveSession(sessionUser)

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "registration")
            Result.failure(e)
        }
    }

    fun observeMyRoom(userId: String): Flow<RoomItem?> {
        return db.collection("rooms")
            .whereEqualTo("resident_id", userId)
            .limit(1)
            .snapshots()
            .map { snap ->
                snap.documents.firstOrNull()?.let { doc ->
                    doc.toObject(RoomItem::class.java)?.copy(id = doc.id)
                }
            }
            .catch { emit(null) }
    }

    suspend fun updateResidentRoom(
        roomId: String,
        status: String,
        coverImageUrl: String? = null,
        description: String? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "room_status" to status,
                "updated_at" to FieldValue.serverTimestamp()
            )
            if (coverImageUrl != null && coverImageUrl.isNotEmpty()) {
                updates["cover_image_url"] = coverImageUrl
                updates["image_url"] = coverImageUrl
            }
            if (description != null) {
                updates["description"] = description
            }
            db.collection("rooms").document(roomId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "rooms/$roomId")
            Result.failure(e)
        }
    }

    fun observeAdminLogs(): Flow<List<AdminActivityLog>> {
        return db.collection("admin_activity_logs")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(50)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { it.toObject(AdminActivityLog::class.java)?.copy(id = it.id) }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun logAdminAction(action: String, targetType: String, targetId: String, description: String) {
        try {
            val uid = getCurrentUserId() ?: "system"
            db.collection("admin_activity_logs").add(
                mapOf(
                    "admin_id" to uid,
                    "action" to action,
                    "target_type" to targetType,
                    "target_id" to targetId,
                    "description" to description,
                    "created_at" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.w("ResidenceRepo", "Admin log failed: ${e.message}")
        }
    }

    // ==========================================
    // ROOMS
    // ==========================================
    fun observeRooms(): Flow<List<RoomItem>> {
        return db.collection("rooms")
            .orderBy("room_number", Query.Direction.ASCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(RoomItem::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "rooms")
                emit(emptyList())
            }
    }

    suspend fun updateRoomStatus(roomId: String, status: String, occupantName: String = ""): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "room_status" to status,
                "updated_at" to FieldValue.serverTimestamp()
            )
            if (occupantName.isNotBlank()) {
                updates["occupant_name"] = occupantName
            }
            db.collection("rooms").document(roomId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "rooms/$roomId")
            Result.failure(e)
        }
    }

    suspend fun updateRoomDetails(roomId: String, status: String, coverImageUrl: String): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "room_status" to status,
                "updated_at" to FieldValue.serverTimestamp()
            )
            if (coverImageUrl.isNotBlank()) {
                updates["cover_image_url"] = coverImageUrl
                updates["image_url"] = coverImageUrl
            }
            db.collection("rooms").document(roomId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "rooms/$roomId")
            Result.failure(e)
        }
    }

    suspend fun addRoom(room: RoomItem): Result<String> {
        return try {
            val ref = db.collection("rooms").document()
            val data = mapOf(
                "room_number" to room.room_number,
                "room_type" to room.room_type,
                "price_per_semester" to room.price_per_semester,
                "status" to room.status,
                "description" to room.description,
                "image_url" to room.image_url,
                "floor" to room.floor,
                "features" to room.features,
                "occupant_name" to room.occupant_name,
                "created_at" to FieldValue.serverTimestamp(),
                "updated_at" to FieldValue.serverTimestamp()
            )
            ref.set(data).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "rooms")
            Result.failure(e)
        }
    }

    // ==========================================
    // VACANCIES
    // ==========================================
    fun observeVacancies(): Flow<List<VacancyItem>> {
        return db.collection("vacancies")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(VacancyItem::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "vacancies")
                emit(emptyList())
            }
    }

    suspend fun announceVacancy(vacancy: VacancyItem): Result<String> {
        val uid = requireUserId()
        return try {
            val ref = db.collection("vacancies").document()
            val data = mapOf(
                "room_id" to vacancy.room_id,
                "room_number" to vacancy.room_number,
                "posted_by" to uid,
                "poster_name" to vacancy.poster_name,
                "poster_phone" to vacancy.poster_phone,
                "title" to vacancy.title,
                "description" to vacancy.description,
                "expected_move_out_date" to vacancy.expected_move_out_date,
                "price_per_semester" to vacancy.price_per_semester,
                "room_type" to vacancy.room_type,
                "contact_preference" to vacancy.contact_preference,
                "status" to "Approved",
                "admin_notes" to vacancy.admin_notes,
                "created_at" to FieldValue.serverTimestamp(),
                "updated_at" to FieldValue.serverTimestamp()
            )
            ref.set(data).await()

            // Update room status to Pending Move-Out
            if (vacancy.room_id.isNotEmpty()) {
                db.collection("rooms").document(vacancy.room_id)
                    .update("status", "Pending Move-Out").await()
            }

            Result.success(ref.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "vacancies")
            Result.failure(e)
        }
    }

    // ==========================================
    // MARKETPLACE PRODUCTS
    // ==========================================
    fun observeProducts(): Flow<List<MarketplaceProduct>> {
        return db.collection("products")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(MarketplaceProduct::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "products")
                emit(emptyList())
            }
    }

    suspend fun addProduct(product: MarketplaceProduct): Result<String> {
        val uid = requireUserId()
        return try {
            val ref = db.collection("products").document()
            val data = mapOf(
                "seller_id" to uid,
                "seller_name" to product.seller_name,
                "seller_phone" to product.seller_phone,
                "category" to product.category,
                "name" to product.name,
                "description" to product.description,
                "price" to product.price,
                "quantity" to product.quantity,
                "condition" to product.condition,
                "location" to product.location,
                "image_url" to product.image_url,
                "status" to "Active",
                "payment_method" to product.payment_method,
                "payment_number" to product.payment_number,
                "payment_name" to product.payment_name,
                "created_at" to FieldValue.serverTimestamp(),
                "updated_at" to FieldValue.serverTimestamp()
            )
            ref.set(data).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "products")
            Result.failure(e)
        }
    }

    suspend fun updateProductStatus(productId: String, status: String): Result<Unit> {
        return try {
            db.collection("products").document(productId)
                .update("status", status, "updated_at", FieldValue.serverTimestamp()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "products/$productId")
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            db.collection("products").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "products/$productId")
            Result.failure(e)
        }
    }

    // ==========================================
    // SELLER APPLICATIONS
    // ==========================================
    fun observeSellerApplications(): Flow<List<SellerApplicationData>> {
        return db.collection("seller_applications")
            .orderBy("submitted_at", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(SellerApplicationData::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "seller_applications")
                emit(emptyList())
            }
    }

    suspend fun submitSellerApplication(app: SellerApplicationData): Result<String> {
        val uid = requireUserId()
        return try {
            val ref = db.collection("seller_applications").document(uid)
            val data = mapOf(
                "user_id" to uid,
                "seller_name" to app.seller_name,
                "phone" to app.phone,
                "email" to app.email,
                "business_name" to app.business_name,
                "business_description" to app.business_description,
                "category" to app.category,
                "payment_method" to app.payment_method,
                "payment_number" to app.payment_number,
                "payment_name" to app.payment_name,
                "status" to "Pending",
                "admin_notes" to "",
                "submitted_at" to FieldValue.serverTimestamp()
            )
            ref.set(data).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "seller_applications")
            Result.failure(e)
        }
    }

    suspend fun reviewSellerApplication(userId: String, approve: Boolean, notes: String = ""): Result<Unit> {
        return try {
            val status = if (approve) "Approved" else "Rejected"
            db.collection("seller_applications").document(userId).update(
                mapOf(
                    "status" to status,
                    "admin_notes" to notes,
                    "reviewed_at" to FieldValue.serverTimestamp()
                )
            ).await()

            if (approve) {
                db.collection("profiles").document(userId).update("role", "seller").await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "seller_applications/$userId")
            Result.failure(e)
        }
    }

    // ==========================================
    // COMMUNITY POSTS & COMMENTS & LIKES
    // ==========================================
    fun observePosts(): Flow<List<CommunityPost>> {
        return db.collection("posts")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(CommunityPost::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "posts")
                emit(emptyList())
            }
    }

    suspend fun createPost(post: CommunityPost): Result<String> {
        val uid = requireUserId()
        return try {
            val ref = db.collection("posts").document()
            val data = mapOf(
                "author_id" to uid,
                "author_name" to post.author_name,
                "author_photo" to post.author_photo,
                "content" to post.content,
                "post_type" to post.post_type,
                "image_url" to post.image_url,
                "likes_count" to 0L,
                "comments_count" to 0L,
                "created_at" to FieldValue.serverTimestamp(),
                "updated_at" to FieldValue.serverTimestamp()
            )
            ref.set(data).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "posts")
            Result.failure(e)
        }
    }

    suspend fun toggleLikePost(postId: String): Result<Boolean> {
        val uid = requireUserId()
        val reactionRef = db.collection("post_reactions").document("${postId}_$uid")
        return try {
            val snap = reactionRef.get().await()
            val isLiked = snap.exists()
            if (isLiked) {
                reactionRef.delete().await()
                db.collection("posts").document(postId).update("likes_count", FieldValue.increment(-1)).await()
                Result.success(false)
            } else {
                reactionRef.set(
                    mapOf(
                        "post_id" to postId,
                        "user_id" to uid,
                        "reaction_type" to "like",
                        "created_at" to FieldValue.serverTimestamp()
                    )
                ).await()
                db.collection("posts").document(postId).update("likes_count", FieldValue.increment(1)).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "post_reactions")
            Result.failure(e)
        }
    }

    fun observeUserReactions(userId: String): Flow<Set<String>> {
        return db.collection("post_reactions")
            .whereEqualTo("user_id", userId)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { it.getString("post_id") }.toSet()
            }
            .catch { emit(emptySet()) }
    }

    fun observeComments(postId: String): Flow<List<CommentItem>> {
        return db.collection("comments")
            .whereEqualTo("post_id", postId)
            .orderBy("created_at", Query.Direction.ASCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(CommentItem::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "comments")
                emit(emptyList())
            }
    }

    suspend fun addComment(postId: String, content: String, userName: String, userPhoto: String): Result<String> {
        val uid = requireUserId()
        return try {
            val ref = db.collection("comments").document()
            val data = mapOf(
                "post_id" to postId,
                "user_id" to uid,
                "user_name" to userName,
                "user_photo" to userPhoto,
                "content" to content,
                "created_at" to FieldValue.serverTimestamp()
            )
            ref.set(data).await()
            db.collection("posts").document(postId).update("comments_count", FieldValue.increment(1)).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "comments")
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            db.collection("posts").document(postId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "posts/$postId")
            Result.failure(e)
        }
    }

    // ==========================================
    // CHAT & CONVERSATIONS
    // ==========================================
    fun observeConversations(userId: String): Flow<List<ConversationItem>> {
        return db.collection("conversations")
            .whereArrayContains("member_ids", userId)
            .orderBy("updated_at", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(ConversationItem::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "conversations")
                emit(emptyList())
            }
    }

    fun observeMessages(conversationId: String): Flow<List<ChatMessageItem>> {
        return db.collection("messages")
            .whereEqualTo("conversation_id", conversationId)
            .orderBy("created_at", Query.Direction.ASCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(ChatMessageItem::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "messages")
                emit(emptyList())
            }
    }

    suspend fun startOrGetConversation(
        targetUserId: String,
        targetUserName: String,
        type: String = "Direct",
        productId: String = "",
        productName: String = "",
        roomId: String = "",
        roomNumber: String = ""
    ): Result<String> {
        val uid = requireUserId()
        return try {
            val query = db.collection("conversations")
                .whereArrayContains("member_ids", uid)
                .get().await()

            val existing = query.documents.firstOrNull { doc ->
                val members = doc.get("member_ids") as? List<*>
                val pId = doc.getString("product_id") ?: ""
                val rId = doc.getString("room_id") ?: ""
                members?.contains(targetUserId) == true &&
                        (productId.isEmpty() || pId == productId) &&
                        (roomId.isEmpty() || rId == roomId)
            }

            if (existing != null) {
                return Result.success(existing.id)
            }

            val ref = db.collection("conversations").document()
            val data = mapOf(
                "conversation_type" to type,
                "member_ids" to listOf(uid, targetUserId),
                "other_user_name" to targetUserName,
                "other_user_id" to targetUserId,
                "last_message" to "Started conversation",
                "last_message_time" to FieldValue.serverTimestamp(),
                "product_id" to productId,
                "product_name" to productName,
                "room_id" to roomId,
                "room_number" to roomNumber,
                "unread_count" to 0L,
                "created_at" to FieldValue.serverTimestamp(),
                "updated_at" to FieldValue.serverTimestamp()
            )
            ref.set(data).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "conversations")
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        senderName: String,
        attachmentUrl: String = ""
    ): Result<String> {
        val uid = requireUserId()
        return try {
            val ref = db.collection("messages").document()
            val data = mapOf(
                "conversation_id" to conversationId,
                "sender_id" to uid,
                "sender_name" to senderName,
                "message_text" to text,
                "attachment_url" to attachmentUrl,
                "attachment_type" to if (attachmentUrl.isNotEmpty()) "image" else "text",
                "message_status" to "delivered",
                "created_at" to FieldValue.serverTimestamp()
            )
            ref.set(data).await()

            // Update conversation last message
            db.collection("conversations").document(conversationId).update(
                mapOf(
                    "last_message" to text,
                    "last_message_time" to FieldValue.serverTimestamp(),
                    "updated_at" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(ref.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "messages")
            Result.failure(e)
        }
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================
    fun observeNotifications(userId: String): Flow<List<NotificationItem>> {
        return db.collection("notifications")
            .whereEqualTo("user_id", userId)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(NotificationItem::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "notifications")
                emit(emptyList())
            }
    }

    suspend fun markNotificationRead(notifId: String): Result<Unit> {
        return try {
            db.collection("notifications").document(notifId).update("is_read", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "notifications/$notifId")
            Result.failure(e)
        }
    }

    // ==========================================
    // SAVED ITEMS
    // ==========================================
    fun observeSavedItems(userId: String): Flow<List<SavedItemData>> {
        return db.collection("saved_items")
            .whereEqualTo("user_id", userId)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(SavedItemData::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "saved_items")
                emit(emptyList())
            }
    }

    suspend fun toggleSaveItem(item: SavedItemData): Result<Boolean> {
        val uid = requireUserId()
        val docId = "${item.item_type}_${item.item_id}_$uid"
        val ref = db.collection("saved_items").document(docId)
        return try {
            val snap = ref.get().await()
            if (snap.exists()) {
                ref.delete().await()
                Result.success(false)
            } else {
                val data = mapOf(
                    "user_id" to uid,
                    "item_type" to item.item_type,
                    "item_id" to item.item_id,
                    "title" to item.title,
                    "subtitle" to item.subtitle,
                    "price" to item.price,
                    "image_url" to item.image_url,
                    "created_at" to FieldValue.serverTimestamp()
                )
                ref.set(data).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "saved_items")
            Result.failure(e)
        }
    }

    // ==========================================
    // CART ITEMS
    // ==========================================
    fun observeCartItems(userId: String): Flow<List<CartItemData>> {
        return db.collection("cart_items")
            .whereEqualTo("user_id", userId)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(CartItemData::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "cart_items")
                emit(emptyList())
            }
    }

    suspend fun addToCart(item: CartItemData): Result<String> {
        val uid = requireUserId()
        val docId = "${item.product_id}_$uid"
        val ref = db.collection("cart_items").document(docId)
        return try {
            val snap = ref.get().await()
            if (snap.exists()) {
                ref.update("quantity", FieldValue.increment(1)).await()
                Result.success(docId)
            } else {
                val data = mapOf(
                    "user_id" to uid,
                    "product_id" to item.product_id,
                    "product_name" to item.product_name,
                    "price" to item.price,
                    "quantity" to item.quantity,
                    "image_url" to item.image_url,
                    "seller_name" to item.seller_name,
                    "seller_id" to item.seller_id,
                    "created_at" to FieldValue.serverTimestamp()
                )
                ref.set(data).await()
                Result.success(docId)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "cart_items")
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(cartItemId: String): Result<Unit> {
        return try {
            db.collection("cart_items").document(cartItemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "cart_items/$cartItemId")
            Result.failure(e)
        }
    }

    suspend fun clearCart(userId: String): Result<Unit> {
        return try {
            val docs = db.collection("cart_items").whereEqualTo("user_id", userId).get().await()
            for (d in docs.documents) {
                d.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "cart_items")
            Result.failure(e)
        }
    }

    // ==========================================
    // ANNOUNCEMENTS
    // ==========================================
    fun observeAnnouncements(): Flow<List<ResidenceAnnouncement>> {
        return db.collection("announcements")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    doc.toObject(ResidenceAnnouncement::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                handleFirestoreError(e as Exception, OperationType.LIST, "announcements")
                emit(emptyList())
            }
    }

    suspend fun createAnnouncement(title: String, content: String, priority: String = "Normal"): Result<String> {
        return try {
            val ref = db.collection("announcements").document()
            val data = mapOf(
                "title" to title,
                "content" to content,
                "image_url" to "",
                "priority" to priority,
                "created_by" to "Administration",
                "status" to "Active",
                "created_at" to FieldValue.serverTimestamp()
            )
            ref.set(data).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "announcements")
            Result.failure(e)
        }
    }

    // ==========================================
    // INITIAL SEEDING (POPULATES REAL CLOUD DB)
    // ==========================================
    suspend fun seedInitialDataIfEmpty() {
        try {
            val roomsSnap = db.collection("rooms").limit(1).get().await()
            if (!roomsSnap.isEmpty) return // already seeded in cloud

            Log.d("ResidenceRepo", "Seeding initial real residence data into Firestore...")

            // Seed rooms: Old Rooms (~54 @ 15,000) and New Rooms (~28 @ 25,000)
            val batch = db.batch()

            // New Rooms (28 rooms: 201 to 228)
            val newRoomFeatures = listOf("Single Room", "Wi-Fi Included", "Study Desk", "Wardrobe", "Balcony View")
            for (i in 201..228) {
                val ref = db.collection("rooms").document("room_$i")
                val isAvailable = i in listOf(201, 204, 215, 218, 222, 225)
                val status = if (isAvailable) "Available" else "Occupied"
                batch.set(
                    ref, mapOf(
                        "room_number" to "$i",
                        "room_type" to "New Room",
                        "price_per_semester" to 25000L,
                        "status" to status,
                        "description" to "Spacious, well-lit premium student room with study desk, ergonomic chair, wardrobe, and high-speed Wi-Fi.",
                        "image_url" to "",
                        "floor" to if (i <= 214) "2nd Floor" else "3rd Floor",
                        "features" to newRoomFeatures,
                        "occupant_name" to if (isAvailable) "" else "Resident $i",
                        "created_at" to FieldValue.serverTimestamp(),
                        "updated_at" to FieldValue.serverTimestamp()
                    )
                )
            }

            // Old Rooms (54 rooms: 101 to 154)
            val oldRoomFeatures = listOf("Single Room", "Shared Bathroom", "Wi-Fi Included", "Study Desk", "Wardrobe")
            for (i in 101..154) {
                val ref = db.collection("rooms").document("room_$i")
                val isAvailable = i in listOf(101, 105, 112, 120, 134, 145, 150)
                val status = if (isAvailable) "Available" else "Occupied"
                batch.set(
                    ref, mapOf(
                        "room_number" to "$i",
                        "room_type" to "Old Room",
                        "price_per_semester" to 15000L,
                        "status" to status,
                        "description" to "Classic comfortable student room with built-in storage, study desk, and convenient ground/1st floor access.",
                        "image_url" to "",
                        "floor" to if (i <= 127) "Ground Floor" else "1st Floor",
                        "features" to oldRoomFeatures,
                        "occupant_name" to if (isAvailable) "" else "Resident $i",
                        "created_at" to FieldValue.serverTimestamp(),
                        "updated_at" to FieldValue.serverTimestamp()
                    )
                )
            }

            // Seed sample residence announcements
            val a1 = db.collection("announcements").document("ann_1")
            batch.set(
                a1, mapOf(
                    "title" to "Semester Room Booking & Vacancies Open",
                    "content" to "Welcome to the new academic semester at Capital Home Residence! Both Old Wing (KSh 15,000) and New Wing (KSh 25,000) rooms are now open for inspection and vacancy takeover.",
                    "image_url" to "",
                    "priority" to "High",
                    "created_by" to "Residence Management",
                    "status" to "Active",
                    "created_at" to FieldValue.serverTimestamp()
                )
            )

            val a2 = db.collection("announcements").document("ann_2")
            batch.set(
                a2, mapOf(
                    "title" to "High-Speed Wi-Fi Upgrade Completed",
                    "content" to "New dual-band routers have been installed across all floors. Connect to 'CapitalHome_Student' with your residence portal credentials.",
                    "image_url" to "",
                    "priority" to "Normal",
                    "created_by" to "IT Support",
                    "status" to "Active",
                    "created_at" to FieldValue.serverTimestamp()
                )
            )

            // Seed initial verified marketplace products
            val p1 = db.collection("products").document("prod_earbuds")
            batch.set(
                p1, mapOf(
                    "seller_id" to "seller_brian",
                    "seller_name" to "Brian Otieno",
                    "seller_phone" to "+254 712 345 678",
                    "category" to "Electronics",
                    "name" to "Wireless Earbuds Pro",
                    "description" to "Active noise cancelling, deep bass, 24-hour battery life. Perfect for study sessions in the residence.",
                    "price" to 1500L,
                    "quantity" to 5L,
                    "condition" to "New",
                    "location" to "Room 204, Capital Home",
                    "image_url" to "",
                    "status" to "Active",
                    "payment_method" to "Lipa na M-Pesa Till",
                    "payment_number" to "5432109",
                    "payment_name" to "Brian Electronics",
                    "created_at" to FieldValue.serverTimestamp(),
                    "updated_at" to FieldValue.serverTimestamp()
                )
            )

            val p2 = db.collection("products").document("prod_backpack")
            batch.set(
                p2, mapOf(
                    "seller_id" to "seller_amina",
                    "seller_name" to "Amina Hassan",
                    "seller_phone" to "+254 722 987 654",
                    "category" to "Fashion",
                    "name" to "Waterproof Campus Backpack",
                    "description" to "Ergonomic laptop backpack with USB charging port and multiple compartments.",
                    "price" to 2500L,
                    "quantity" to 2L,
                    "condition" to "New",
                    "location" to "Room 108, Capital Home",
                    "image_url" to "",
                    "status" to "Active",
                    "payment_method" to "Send Money",
                    "payment_number" to "0722987654",
                    "payment_name" to "Amina Hassan",
                    "created_at" to FieldValue.serverTimestamp(),
                    "updated_at" to FieldValue.serverTimestamp()
                )
            )

            val p3 = db.collection("products").document("prod_textbooks")
            batch.set(
                p3, mapOf(
                    "seller_id" to "seller_kelvin",
                    "seller_name" to "Kelvin Mugo",
                    "seller_phone" to "+254 733 112 233",
                    "category" to "Books",
                    "name" to "Computer Science Textbooks Set",
                    "description" to "Data Structures, Algorithms and Database Systems textbooks in excellent condition.",
                    "price" to 800L,
                    "quantity" to 1L,
                    "condition" to "Like New",
                    "location" to "Room 302, Capital Home",
                    "image_url" to "",
                    "status" to "Active",
                    "payment_method" to "Lipa na M-Pesa Pochi",
                    "payment_number" to "0733112233",
                    "payment_name" to "Kelvin Mugo",
                    "created_at" to FieldValue.serverTimestamp(),
                    "updated_at" to FieldValue.serverTimestamp()
                )
            )

            // Seed initial community posts
            val post1 = db.collection("posts").document("post_faith")
            batch.set(
                post1, mapOf(
                    "author_id" to "user_faith",
                    "author_name" to "Faith Wanjiku",
                    "author_photo" to "",
                    "content" to "Good vibes only! 🎉 The residence is such a great place to live. Finally found my study study group in the 2nd floor lounge! 💙",
                    "post_type" to "General",
                    "image_url" to "",
                    "likes_count" to 24L,
                    "comments_count" to 6L,
                    "created_at" to FieldValue.serverTimestamp(),
                    "updated_at" to FieldValue.serverTimestamp()
                )
            )

            val post2 = db.collection("posts").document("post_brian")
            batch.set(
                post2, mapOf(
                    "author_id" to "user_brian",
                    "author_name" to "Brian Otieno",
                    "author_photo" to "",
                    "content" to "Anyone interested in joining a weekend study group for Software Engineering? Meeting up in the common room at 5pm.",
                    "post_type" to "Question",
                    "image_url" to "",
                    "likes_count" to 12L,
                    "comments_count" to 3L,
                    "created_at" to FieldValue.serverTimestamp(),
                    "updated_at" to FieldValue.serverTimestamp()
                )
            )

            batch.commit().await()
            Log.d("ResidenceRepo", "Seeding initial real data complete!")
        } catch (e: Exception) {
            Log.w("ResidenceRepo", "Initial seed check: ${e.message}")
        }
    }
}
