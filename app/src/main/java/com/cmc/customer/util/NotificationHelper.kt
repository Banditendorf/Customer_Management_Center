package com.cmc.customer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cmc.customer.MainActivity
import com.cmc.customer.R

object NotificationHelper {

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelWithSound(
                context,
                "stock_channel",
                "Stok Bildirimleri",
                R.raw.samsung_spaceline_notification
            )

            createChannelWithSound(
                context,
                "maintenance_channel",
                "BakÄ±m Bildirimleri",
                R.raw.samsung_spaceline_notification
            )

            createChannelWithSound(
                context,
                "system_channel",
                "Sistem Bildirimleri",
                R.raw.samsung_spaceline_notification
            )
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        type: String,
        data: Map<String, String>
    ) {
        val category = data["category"] ?: inferCategoryFromType(type)
        val channelId = when (category) {
            "stock" -> "stock_channel"
            "maintenance" -> "maintenance_channel"
            else -> "system_channel"
        }

        val intent = createIntentForType(context, type, data)
        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() and 0xFFFFFFF).toInt(), // gÃ¼venli ID
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.cmc_logo) // küçük ikon önerisi SVG olabilir
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(context).notify(
            (System.currentTimeMillis() and 0xFFFFFFF).toInt(),
            builder.build()
        )
    }

    private fun createChannelWithSound(context: Context, id: String, name: String, soundRes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/raw/${context.resources.getResourceEntryName(soundRes)}")

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(soundUri, audioAttributes)
                description = name
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createIntentForType(context: Context, type: String, data: Map<String, String>): Intent {
        val intent = Intent(context, MainActivity::class.java)

        when (type) {
            "stock_critical" -> {
                intent.putExtra("navigateTo", "materialDetail")
                intent.putExtra("materialCode", data["code"])
            }
            "maintenance_upcoming", "maintenance_done", "maintenance_planned", "task_assigned" -> {
                intent.putExtra("navigateTo", "maintenanceDetail")
                intent.putExtra("maintenanceId", data["maintenanceId"])
            }
            "maintenance_overdue" -> {
                intent.putExtra("navigateTo", "machineDetail")
                intent.putExtra("machineId", data["machineId"])
            }
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return intent
    }


    private fun inferCategoryFromType(type: String): String {
        return when (type) {
            "task_assigned",
            in listOf("maintenance_upcoming", "maintenance_done", "maintenance_planned", "maintenance_overdue") -> "maintenance"
            in listOf("stock_critical") -> "stock"
            else -> "system"
        }
    }

}
