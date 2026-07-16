package com.cmc.customer.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.cmc.customer.util.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Yeni Token: $token")
        // TODO: Firestore'a token'Ä± yaz
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"] ?: "Yeni Bildirim"
        val body = message.data["body"] ?: "Detay belirtilmedi"
        val type = message.data["type"] ?: "generic"

        Log.d("FCM", "Gelen Bildirim: $type - $title")

        NotificationHelper.showNotification(applicationContext, title, body, type, message.data)
    }
}
