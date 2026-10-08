package com.example.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val id: String = "",
    val auth_user_id: String = "",
    val full_name: String = "",
    val phone: String = "",
    val email: String = "",
    val profile_photo: String = "",
    val room_id: String = "",
    val role: String = "resident", // resident, seller, admin
    val account_status: String = "active",
    val password_hash: String = "",
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)

data class SessionUser(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "resident",
    val phone: String = "",
    val roomId: String = ""
)

data class RoomItem(
    val id: String = "",
    val room_number: String = "",
    val room_type: String = "Old Room", // "Old Room" or "New Room"
    val price_per_semester: Long = 15000L,
    val status: String = "Occupied", // "Occupied", "Vacant", "Available", "Pending Move-Out"
    val description: String = "",
    val image_url: String = "",
    val cover_image_url: String = "",
    val floor: String = "1st Floor",
    val features: List<String> = emptyList(),
    val occupant_name: String = "",
    val occupant_id: String = "",
    val resident_id: String = "",
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)

data class VacancyItem(
    val id: String = "",
    val room_id: String = "",
    val room_number: String = "",
    val posted_by: String = "",
    val poster_name: String = "",
    val poster_phone: String = "",
    val title: String = "",
    val description: String = "",
    val expected_move_out_date: String = "",
    val price_per_semester: Long = 15000L,
    val room_type: String = "Old Room",
    val contact_preference: String = "Chat",
    val status: String = "Approved", // "Pending", "Approved", "Filled", "Rejected"
    val admin_notes: String = "",
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)

data class MarketplaceProduct(
    val id: String = "",
    val seller_id: String = "",
    val seller_name: String = "",
    val seller_phone: String = "",
    val category: String = "Electronics",
    val subcategory: String = "",
    val name: String = "",
    val description: String = "",
    val price: Long = 0L,
    val original_price: Long = 0L,
    val quantity: Long = 1L,
    val condition: String = "New", // "New", "Like New", "Used", "Refurbished"
    val brand: String = "",
    val model: String = "",
    val color: String = "",
    val size: String = "",
    val material: String = "",
    val additional_specs: String = "",
    val location: String = "Capital Home Residence",
    val delivery_option: String = "Pickup at Capital Home Residence",
    val image_url: String = "",
    val image_urls: List<String> = emptyList(),
    val status: String = "Active", // "Active", "Hidden", "Out of Stock", "Sold", "Deleted"
    val payment_method: String = "Send Money", // "Send Money", "Lipa na M-Pesa Pochi", "Lipa na M-Pesa Till", "Lipa M-Pesa Pay Bill"
    val payment_number: String = "",
    val payment_name: String = "",
    val rating: Double = 4.8,
    val review_count: Int = 12,
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
) {
    val discountPercent: Int
        get() = if (original_price > price && original_price > 0L) {
            (((original_price - price).toDouble() / original_price.toDouble()) * 100).toInt()
        } else 0

    val stockStatus: String
        get() = when {
            quantity <= 0L -> "Out of Stock"
            quantity <= 3L -> "Low Stock"
            else -> "In Stock"
        }
}

data class ProductImage(
    val id: String = "",
    val product_id: String = "",
    val image_url: String = "",
    val display_order: Int = 0,
    val is_cover: Boolean = false,
    val created_at: Timestamp? = null
)

data class OrderRecord(
    val id: String = "",
    val order_number: String = "",
    val buyer_id: String = "",
    val buyer_name: String = "",
    val buyer_phone: String = "",
    val seller_id: String = "",
    val seller_name: String = "",
    val product_id: String = "",
    val product_name: String = "",
    val product_image: String = "",
    val quantity: Long = 1L,
    val unit_price: Long = 0L,
    val total_amount: Long = 0L,
    val order_status: String = "Pending Payment", // "Pending Payment", "Payment Verification", "Payment Confirmed", "Processing", "Ready for Pickup", "Completed", "Cancelled"
    val payment_status: String = "Payment Pending", // "Payment Pending", "Reference Submitted", "Payment Under Review", "Payment Confirmed", "Payment Rejected", "Refund/Cancelled"
    val payment_method: String = "Send Money",
    val seller_payment_number: String = "",
    val seller_payment_name: String = "",
    val pickup_or_delivery: String = "Pickup at Capital Home Residence",
    val mpesa_reference: String = "",
    val rejection_reason: String = "",
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)

data class PaymentRecord(
    val id: String = "",
    val order_id: String = "",
    val order_number: String = "",
    val buyer_id: String = "",
    val seller_id: String = "",
    val amount: Long = 0L,
    val payment_method: String = "Send Money",
    val seller_payment_details_snapshot: Map<String, String> = emptyMap(),
    val payment_status: String = "Payment Pending", // "Payment Pending", "Reference Submitted", "Payment Under Review", "Payment Confirmed", "Payment Rejected"
    val mpesa_reference: String = "",
    val submitted_at: Timestamp? = null,
    val verified_at: Timestamp? = null,
    val verified_by: String = "",
    val rejection_reason: String = "",
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)

data class SellerApplicationData(
    val id: String = "",
    val user_id: String = "",
    val seller_name: String = "",
    val phone: String = "",
    val email: String = "",
    val business_name: String = "",
    val business_description: String = "",
    val category: String = "General",
    val payment_method: String = "Send Money",
    val payment_number: String = "",
    val payment_name: String = "",
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val admin_notes: String = "",
    val submitted_at: Timestamp? = null,
    val reviewed_at: Timestamp? = null
)

data class CommunityPost(
    val id: String = "",
    val author_id: String = "",
    val author_name: String = "",
    val author_photo: String = "",
    val content: String = "",
    val post_type: String = "General", // "General", "Announcement", "Question", "Room", "Marketplace", "Event"
    val image_url: String = "",
    val likes_count: Long = 0L,
    val comments_count: Long = 0L,
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)

data class CommentItem(
    val id: String = "",
    val post_id: String = "",
    val user_id: String = "",
    val user_name: String = "",
    val user_photo: String = "",
    val content: String = "",
    val created_at: Timestamp? = null
)

data class PostReaction(
    val id: String = "",
    val post_id: String = "",
    val user_id: String = "",
    val reaction_type: String = "like",
    val created_at: Timestamp? = null
)

data class ConversationItem(
    val id: String = "",
    val conversation_type: String = "Direct", // "Direct", "Product", "Room", "Support"
    val member_ids: List<String> = emptyList(),
    val other_user_name: String = "",
    val other_user_id: String = "",
    val last_message: String = "",
    val last_message_time: Timestamp? = null,
    val product_id: String = "",
    val product_name: String = "",
    val room_id: String = "",
    val room_number: String = "",
    val unread_count: Long = 0L,
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)

data class ChatMessageItem(
    val id: String = "",
    val conversation_id: String = "",
    val sender_id: String = "",
    val sender_name: String = "",
    val message_text: String = "",
    val attachment_url: String = "",
    val attachment_type: String = "",
    val message_status: String = "sent",
    val created_at: Timestamp? = null
)

data class NotificationItem(
    val id: String = "",
    val user_id: String = "",
    val notification_type: String = "chat",
    val title: String = "",
    val message: String = "",
    val reference_type: String = "",
    val reference_id: String = "",
    val is_read: Boolean = false,
    val created_at: Timestamp? = null
)

data class SavedItemData(
    val id: String = "",
    val user_id: String = "",
    val item_type: String = "product", // "product" or "room"
    val item_id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val price: Long = 0L,
    val image_url: String = "",
    val created_at: Timestamp? = null
)

data class CartItemData(
    val id: String = "",
    val user_id: String = "",
    val product_id: String = "",
    val product_name: String = "",
    val price: Long = 0L,
    val quantity: Long = 1L,
    val image_url: String = "",
    val seller_name: String = "",
    val seller_id: String = "",
    val created_at: Timestamp? = null
)

data class ResidenceAnnouncement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val image_url: String = "",
    val priority: String = "Normal",
    val created_by: String = "Management",
    val status: String = "Active",
    val created_at: Timestamp? = null
)

data class AdminActivityLog(
    val id: String = "",
    val admin_id: String = "",
    val action: String = "",
    val target_type: String = "",
    val target_id: String = "",
    val description: String = "",
    val created_at: Timestamp? = null
)

data class ContentReport(
    val id: String = "",
    val reporter_id: String = "",
    val target_type: String = "",
    val target_id: String = "",
    val reason: String = "",
    val description: String = "",
    val status: String = "Pending",
    val admin_notes: String = "",
    val created_at: Timestamp? = null
)

