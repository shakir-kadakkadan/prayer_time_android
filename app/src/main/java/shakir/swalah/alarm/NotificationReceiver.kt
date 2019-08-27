package shakir.swalah.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioAttributes.USAGE_ALARM
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build

import androidx.core.app.NotificationCompat
import shakir.swalah.BuildConfig
import shakir.swalah.R

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "Alarm"
            val descriptionText = "Alarm details"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("AlarmId", name, importance)
            mChannel.description = descriptionText
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }


        val path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/raw/message_tone.mp3")
        val ringtone = RingtoneManager.getRingtone(context!!.applicationContext, path)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ringtone.setAudioAttributes(AudioAttributes.Builder().setUsage(USAGE_ALARM).build())
        }else{
            ringtone.setStreamType(AudioManager.STREAM_ALARM)
        }

        ringtone.play()
        // Create the notification to be shown
        val mBuilder = NotificationCompat.Builder(context!!, "AlarmId")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Alarm")
            .setContentText("Here\'s your alarm!")
            .setAutoCancel(true)
            .setSound(null)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Get the Notification manager service
        val am = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Generate an Id for each notification
        val id = System.currentTimeMillis() / 1000

        // Show a notification
        am.notify(id.toInt(), mBuilder.build())
    }
}