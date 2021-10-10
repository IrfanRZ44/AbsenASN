package id.exomatik.absenasn.services.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.exomatik.absenasn.R
import id.exomatik.absenasn.utils.Constant

class MyFirebaseMessaging : FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage) {
        notif(p0)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    private var mNotificationManager: NotificationManager? = null
    private fun notif(remoteMessage: RemoteMessage) {
        val mBuilder = NotificationCompat.Builder(
            applicationContext, Constant.appName
        )

        val intent = Intent(remoteMessage.notification?.clickAction)
        intent.action = remoteMessage.notification?.clickAction
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText(remoteMessage.notification?.body)
        bigText.setBigContentTitle(remoteMessage.notification?.title)
        bigText.setSummaryText(remoteMessage.notification?.body)
        mBuilder.setContentIntent(pendingIntent)
        mBuilder.setSmallIcon(R.drawable.ic_logo_notif)
        mBuilder.setContentTitle(remoteMessage.notification?.title)
        mBuilder.setContentText(remoteMessage.notification?.body)
        mBuilder.setStyle(bigText)
        val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mBuilder.setSound(uri)
        mBuilder.setAutoCancel(true)
        mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        // === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId: String = resources.getString(R.string.app_name)
            val channel = NotificationChannel(
                channelId,
                resources.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.lightColor = Color.BLUE
            channel.enableLights(true)
            val uriSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            channel.setSound(uriSound, Notification.AUDIO_ATTRIBUTES_DEFAULT)
            channel.enableVibration(true)
            mNotificationManager?.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }
        mNotificationManager?.notify(0, mBuilder.build())
    }
}