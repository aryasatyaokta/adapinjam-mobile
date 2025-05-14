package id.co.bcaf.adapinjam.data.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.co.bcaf.adapinjam.R


class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM","From: ${message.from}")
        message.data.isNotEmpty().let {
            Log.d("FCM","Message data payload: ${message.data}")

            showNotification(message.data["title"], message.data["body"])
        }

        message.notification?.let {
            Log.d("FCM","Message Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String){
        super.onNewToken(token)
        Log.d("FCM", "Refrasehd Token: $token")
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "default_channel_id_v4"
        val channelName = "Default Channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = Uri.parse("android.resource://${packageName}/raw/notif2")
        // Buat channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_akun) // ganti dengan ikonmu
            .setContentTitle(title ?: "Notifikasi")
            .setContentText(message ?: "")
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())
    }
}