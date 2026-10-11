package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CapitalHomeFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        // Persist token for the active user if authenticated
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            updateUserFcmToken(uid, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val data = remoteMessage.data
        var title = remoteMessage.notification?.title
        var body = remoteMessage.notification?.body

        // Data payload takes precedence or acts as fallback for backgrounded notifications
        if (data.isNotEmpty()) {
            if (title.isNullOrEmpty()) {
                title = data["title"] ?: "Capital Home Update"
            }
            if (body.isNullOrEmpty()) {
                body = data["message"] ?: data["body"] ?: "You have a new update regarding your order or payment."
            }
        }

        if (title.isNullOrEmpty() && body.isNullOrEmpty()) {
            return
        }

        val type = data["type"] ?: "order_update"
        val orderId = data["order_id"] ?: ""
        val paymentRef = data["payment_reference"] ?: ""

        displayHighPriorityNotification(
            title = title ?: "Capital Home Residence",
            body = body ?: "New update available",
            type = type,
            orderId = orderId,
            paymentRef = paymentRef
        )
    }

    private fun displayHighPriorityNotification(
        title: String,
        body: String,
        type: String,
        orderId: String,
        paymentRef: String
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create high-importance channel with PUBLIC lockscreen visibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority notifications for Capital Home marketplace orders, M-Pesa payments, and approvals"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_type", type)
            if (orderId.isNotEmpty()) putExtra("order_id", orderId)
            if (paymentRef.isNotEmpty()) putExtra("payment_reference", paymentRef)
        }

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_capital_home_logo_1791459621552)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible on screen lock
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        manager.notify(notificationId, notificationBuilder.build())
    }

    private fun updateUserFcmToken(userId: String, token: String) {
        try {
            FirebaseFirestore.getInstance().collection("profiles")
                .document(userId)
                .update("fcm_token", token)
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to update FCM token for user $userId", e)
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Exception saving FCM token", t)
        }
    }

    companion object {
        private const val TAG = "CapitalHomeFCM"
        private const val CHANNEL_ID = "capital_home_orders_fcm_channel"
        private const val CHANNEL_NAME = "Capital Home Orders & M-Pesa Payments"

        fun registerCurrentToken(userId: String) {
            try {
                val fcm = com.google.firebase.messaging.FirebaseMessaging.getInstance()
                // If auto-init is disabled (e.g. in development/emulator or when FCM cloud messaging is not provisioned),
                // skip fetching token to avoid triggering background hard failure exceptions.
                if (!fcm.isAutoInitEnabled) {
                    Log.d(TAG, "FCM auto-init is disabled, skipping token retrieval")
                    return
                }

                fcm.token
                    .addOnSuccessListener { token ->
                        if (!token.isNullOrEmpty() && userId.isNotEmpty()) {
                            try {
                                FirebaseFirestore.getInstance().collection("profiles")
                                    .document(userId)
                                    .update("fcm_token", token)
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed updating fcm_token in Firestore: ${e.message}")
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // In virtualized emulator environments, Google Play Services or Firebase Cloud Messaging
                        // token registration may not be available or may encounter hard failure.
                        Log.w(TAG, "FCM token registration unavailable or delayed in current environment: ${exception.message}")
                    }
            } catch (t: Throwable) {
                Log.w(TAG, "FirebaseMessaging service unavailable on this device: ${t.message}")
            }
        }
    }
}
