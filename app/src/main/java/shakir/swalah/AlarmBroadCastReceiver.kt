package shakir.swalah

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.R.id
import android.media.RingtoneManager
import android.R
import android.app.Notification
import android.app.PendingIntent
import android.net.Uri


class AlarmBroadCastReceiver : BroadcastReceiver() {

    val TAG = "AlarmBroadCastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        val longExtra = intent?.getLongExtra("milli", 0)?:0
        val currentTimeMillis = System.currentTimeMillis()
        if (longExtra<=currentTimeMillis){
            Log.d(TAG,"past time")
        }

        Log.d(TAG,

            "milli"+ longExtra +
            "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");



      /*  val pi = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val note = Notification(R.drawable.icon, "Alarm", System.currentTimeMillis())
        note.setLatestEventInfo(context, "Alarm", title + " (alarm)", pi)
        var alarmSound: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        }
        note.sound = alarmSound
        note.defaults = note.defaults or Notification.DEFAULT_VIBRATE
        note.flags = note.flags or Notification.FLAG_AUTO_CANCEL
        manager.notify(id, note)
*/



    }
}