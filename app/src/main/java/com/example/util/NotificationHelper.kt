package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    const val CHANNEL_ID = "market_updates_channel"
    private const val CHANNEL_NAME = "Naseeb Lal Market Updates"
    private const val CHANNEL_DESC = "Notices, rent alerts, and market announcements for Naseeb Lal Market"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = 0xFFD97706.toInt()
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showUpdateNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        createNotificationChannel(context)

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted, skipping notification popup.")
                return
            }
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Decode market logo for large icon
            val largeLogoBitmap: Bitmap? = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.market_logo)
            } catch (_: Exception) {
                null
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_market)
                .setSubText("Naseeb Lal Market")
                .setColor(0xFFD97706.toInt()) // Brand Gold/Amber
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            if (largeLogoBitmap != null) {
                builder.setLargeIcon(largeLogoBitmap)
            }

            val notification = builder.build()

            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.i(TAG, "Notification posted successfully: $title - $message")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display notification: ${e.message}")
        }
    }
}
