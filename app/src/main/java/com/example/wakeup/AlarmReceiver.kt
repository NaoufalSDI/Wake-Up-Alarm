package com.example.wakeup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private var ringtone: Ringtone? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "STOP_ALARM") {
            stopAlarm(context)
            return
        }
        val ringtoneName = intent.getStringExtra("ringtone") ?: "Default Ringtone"
        playAlarm(context, ringtoneName)

        createAndShowNotification(context)
    }

    private fun playAlarm(context: Context, ringtoneName: String) {
        if (ringtone != null) {
            return
        }

        val ringtoneUri: Uri = when (ringtoneName) {
            "old_fashioned_alarm" -> Uri.parse("android.resource://${context.packageName}/raw/old_fashioned_alarm")
            "reveille_loud" -> Uri.parse("android.resource://${context.packageName}/raw/reveille_loud")
            "ringtone_new_viral" -> Uri.parse("android.resource://${context.packageName}/raw/ringtone_new_viral")
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        ringtone?.play()
    }

    private fun stopAlarm(context: Context) {
        ringtone?.stop()
        ringtone = null

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(1001)
    }

    private fun createAndShowNotification(context: Context) {
        val stopAlarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "STOP_ALARM"
        }
        val stopAlarmPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            stopAlarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "alarm_channel"
            val channelName = "Alarm Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for alarm notifications"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(R.drawable.notifications_icon)
            .setContentTitle("Wake Up Alarm!")
            .setContentText("Your alarm is ringing, wake up. What are you waiting for!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.points, "Stop Alarm", stopAlarmPendingIntent) // Stop alarm action
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1001, notification)
    }
}

