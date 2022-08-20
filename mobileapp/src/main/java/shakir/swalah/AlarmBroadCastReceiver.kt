package shakir.swalah


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import com.azan.TimeCalculator
import com.azan.types.AngleCalculationType
import com.azan.types.PrayersType
import java.text.SimpleDateFormat
import java.util.*


class AlarmBroadCastReceiver : BroadcastReceiver() {

    val CHANNEL_MAME = "adan"

    val TAG = "AlarmBroadCastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        val milli = intent?.getLongExtra("milli", 0) ?: 0
        val currentTimeMillis = System.currentTimeMillis()
        if (milli <= currentTimeMillis) {
            Log.d(TAG, "past time")
        }

        Log.d(
            TAG,

            "milli" + milli +
                    "onReceive() called with: context = [" + context + "], intent = [" + intent + "]"
        );







        if (context != null) {


            val array = arrayOf(
                PrayersType.FAJR,
                PrayersType.SUNRISE,
                PrayersType.ZUHR,
                PrayersType.ASR,
                PrayersType.MAGHRIB,
                PrayersType.ISHA
            )
            val date = GregorianCalendar()

            val sharedPreferences = Util.getMySharedPreference(context)
            val latitude = sharedPreferences
                .getDouble("latitude", 11.00)

            val longitude = sharedPreferences
                .getDouble("longitude", 76.00)


            val prayerTimes =
                TimeCalculator().date(date).location(latitude, longitude, 0.0, 0.0)
                    .timeCalculationMethod(AngleCalculationType.KARACHI)
                    .calculateTimes()


            Log.d("dgfbdsfjjd", "$latitude $longitude $prayerTimes")


            showNoti(
                context, intent?.getStringExtra("name") + " " +
                        SimpleDateFormat(Util.timeFormat(), Locale.ENGLISH).format(milli).ltrEmbed()
            )

        }

        Handler().postDelayed({
            context?.let { Util.setNextAlarm(it) }
        }, 2500)


    }

    fun showNoti(context: Context, title: String) {

        /* val intent = Intent(this, AlertDetails::class.java).apply {
     flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
 }
 val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
*/




        createNotificationChannel(context)

        // val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            <
            (audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) * .75)
        )
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                (audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) * .75).toInt(),
                AudioManager.FLAG_SHOW_UI
            )


        /*   val mediaPlayer = MediaPlayer.create(context, R.raw.message_tone)
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
               mediaPlayer.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
           }else {
              // mediaPlayer.setStreamType(AudioManager.STREAM_ALARM)
           }
         //  mediaPlayer.prepareAsync()
           mediaPlayer.setOnPreparedListener(object :MediaPlayer.OnPreparedListener{
               override fun onPrepared(mp: MediaPlayer?) {
                   mp?.start()
               }

           })*/


        val path =
            Uri.parse("android.resource://" + AppApplication.instance.packageName + "/" + R.raw.message_tone)
        val ringtone = RingtoneManager.getRingtone(context!!.applicationContext, path)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ringtone.setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
            )
        } else {
            ringtone.setStreamType(AudioManager.STREAM_ALARM)
        }

        ringtone.play()


        var builder = NotificationCompat.Builder(context, CHANNEL_MAME)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            /* .setContentText(textContent)*/
            //.setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVisibility(VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setSound(null)
            .setVibrate(longArrayOf(0, 0))


        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(0, builder.build())
        }
    }


    private fun createNotificationChannel(context: Context?) {
        context?.apply {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_MAME, CHANNEL_MAME, importance).apply {
                    description = "Show Adan Notification"
                    setSound(null, null)
                }

                // Register the channel with the system
                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                notificationManager.createNotificationChannel(channel)
            }
        }
    }


}