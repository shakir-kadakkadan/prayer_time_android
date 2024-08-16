package shakir.swalah


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.Locale


class AlarmBroadCastReceiver : BroadcastReceiver() {


    val TAG = "AlarmBroadCastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            val name = intent?.getStringExtra("name")
            if (name == "dnd") {
                setSilentMode()
                return
            } else if (name == "offDND") {
                setSilentModeOff()
                Util.setNextAlarmDND(AppApplication.instance, offDND = false)
                return
            }
            val milli = intent?.getLongExtra("milli", 0) ?: 0
            val currentTimeMillis = System.currentTimeMillis()
            if (milli <= currentTimeMillis) {
                Log.d(TAG, "past time")
            }

            Log.d(
                TAG,

                "milli" + milli + "onReceive() called with: context = [" + context + "], intent = [" + intent + "]"
            );







            if (context != null) {


//                val array = arrayOf(
//                    PrayersType.FAJR,
//                    PrayersType.SUNRISE,
//                    PrayersType.ZUHR,
//                    PrayersType.ASR,
//                    PrayersType.MAGHRIB,
//                    PrayersType.ISHA
//                )
//                val date = GregorianCalendar()
//
//                val sharedPreferences = Util.getMySharedPreference(context)
//                val latitude = sharedPreferences
//                    .getDouble("latitude", 11.00)
//
//                val longitude = sharedPreferences
//                    .getDouble("longitude", 76.00)
//
//
//                val prayerTimes =
//                    TimeCalculator().date(date).location(latitude, longitude, 0.0, 0.0)
//                        .timeCalculationMethod(AngleCalculationType.KARACHI)
//                        .calculateTimes()
//
//
//                Log.d("dgfbdsfjjd", "$latitude $longitude $prayerTimes")


                showNoti(
                    context, intent?.getStringExtra("name") + " " + SimpleDateFormat(Util.timeFormat(), Locale.ENGLISH).format(milli).ltrEmbed()
                )

            }

            Handler().postDelayed({
                context?.let { Util.setNextAlarm(it) }
            }, 2500)
        } catch (e: Exception) {
            e.report()

        }


    }


}

fun setSilentModeOff() {
    println("setSilentMode")
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check for notification policy access
            val notificationManager = AppApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            println("setSilentMode notificationManager.isNotificationPolicyAccessGranted ${notificationManager.isNotificationPolicyAccessGranted}")
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)

            } else {
                // askDNDPermission()
            }
        } else {
            // For older Android versions
            val audioManager = AppApplication.instance.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL

        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("setSilentMode 3")
    }
    println("setSilentMode 2")
}


fun setSilentMode() {
    println("setSilentMode")
    try {
        Util.setNextAlarmDND(AppApplication.instance, offDND = true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check for notification policy access
            val notificationManager = AppApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            println("setSilentMode notificationManager.isNotificationPolicyAccessGranted ${notificationManager.isNotificationPolicyAccessGranted}")
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)

            } else {
                // askDNDPermission()
            }
        } else {
            // For older Android versions
            val audioManager = AppApplication.instance.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE

        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("setSilentMode 3")
    }
    println("setSilentMode 2")
}


fun showNoti(context: Context, title: String) {

    try {
        (context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    println("AlarmBroadCastReceiver : showNoti() called with: context = $context, title = $title")

    /* val intent = Intent(this, AlertDetails::class.java).apply {
 flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
}
val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
*/


    // val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


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

    val sp = Util.getMySharedPreference(AppApplication.instance)
    var prefKey = if (title.contains("الإقامة")) "iqama_sound" else
        if (title.contains("الفجر")) "fajr_sound" else "athan_sound"

    var prefKeyName = if (title.contains("الإقامة")) "iqama_soundName" else
        if (title.contains("الفجر")) "fajr_soundName"
        else "athan_soundName"
    var soundName = sp.getString(prefKeyName, null) ?: "Adhan"


    var path = if (sp.getString(prefKey, null) != null) {
        Uri.parse(sp.getString(prefKey, null))
    } else {
        if (title.contains("الإقامة")) {
            soundName = "mixkit_access_allowed_tone_2869"
            Uri.parse("android.resource://" + AppApplication.instance.packageName + "/" + R.raw.mixkit_access_allowed_tone_2869)
        } else {
            soundName = "message_tone"
            Uri.parse("android.resource://" + AppApplication.instance.packageName + "/" + R.raw.message_tone)
        }
    }

    //sunrise : dont play adhan; play any beeps
    if (title.contains("الشروق") && (soundName.endsWith(".mp3") || soundName.contains("athan"))) {
        soundName = "message_tone"
        path = Uri.parse("android.resource://" + AppApplication.instance.packageName + "/" + R.raw.message_tone)
    }


    println("path ${path}")

    var NotificationSoundUri: Uri? = null
    var channelName = "Adhan"
    if (sp.getBoolean("soundTypeIsAlarm", false)) {
        playSound(context, path)
        channelName = "default_channel"
    } else {
        NotificationSoundUri = path
        channelName = soundName + "_v1"
    }

    createNotificationChannel(context, channelName, NotificationSoundUri)


    var builder = NotificationCompat
        .Builder(context, channelName)
        .setSmallIcon(R.drawable.ic_stat_notifications)
        .setContentTitle(title)/* .setContentText(textContent)*/
        //.setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setAutoCancel(true)
        .setColor(Color.parseColor("#ff0C2752"))
        .setVisibility(VISIBILITY_PUBLIC)
        .setOnlyAlertOnce(true)
        .setSound(NotificationSoundUri)
        .setVibrate(longArrayOf(0, 0))

    val intent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }


// Create a PendingIntent
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 3333, intent, PendingIntent.FLAG_IMMUTABLE)


    val intentDismiss = Intent(context, NotificationDismissedReceiver::class.java)
    intentDismiss.action = "NOTIFICATION_DELETED_ACTION"
    val dismissPendingIntent = PendingIntent.getBroadcast(context, 2222, intentDismiss, PendingIntent.FLAG_IMMUTABLE)


// Set the content intent of the notification builder
    builder.setContentIntent(pendingIntent)
    builder.setDeleteIntent(dismissPendingIntent)



    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define
        notify(1111, builder.build())
    }


//    try {
//
//        if (title!="Sample"&& Util.openApp){
//            AppApplication.instance.acquireScreenCpuWakeLock()
//            val openAppIntent = Intent(context, MainActivity::class.java)
//            openAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            openAppIntent.putExtra("noMute",true)
//            println("sadasdasdsdasdasd 1")
//        }
//
//    } catch (e: Exception) {
//        println("sadasdasdsdasdasd 2")
//      e.printStackTrace()
//    }


}

class NotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            ringtone?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


var ringtone: Ringtone? = null
fun playSound(context: Context, uri: Uri?, minSound: Int = 15) {
    try {

        try {
            ringtone?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val sp = Util.getMySharedPreference(AppApplication.instance)
        var audio = sp.getInt("alarmVolumeSeekBar100", 75)
        if (audio < 15) audio = 15;

        println("audio / 100f ${audio / 100f}")

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != (audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) * (audio / 100f)).toInt()) audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM, (audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) * (audio / 100f)).toInt(), AudioManager.FLAG_SHOW_UI
        )




        println("printlnprintlnprintln R.raw.athan ${R.raw.athan} message_tone.mp3 ${R.raw.message_tone} mixkit_access_allowed_tone_2869.wav ${R.raw.mixkit_access_allowed_tone_2869}")







        if (uri != null) {

            ringtone = RingtoneManager.getRingtone(context!!.applicationContext, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone?.setAudioAttributes(
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                )
            } else {
                ringtone?.setStreamType(AudioManager.STREAM_ALARM)
            }

            ringtone?.play()

        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


private fun createNotificationChannel(context: Context?, channelName: String, NotificationSoundUri: Uri?) {
    context?.apply {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelName, channelName, importance).apply {
                description = "Show Adan Notification"
                setSound(null, null)
            }

            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val audioAttributes = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
            val vibrationPattern = longArrayOf(0, 500, 500, 500)
            channel.setSound(NotificationSoundUri, audioAttributes)

            notificationManager.createNotificationChannel(channel)
        }
    }
}