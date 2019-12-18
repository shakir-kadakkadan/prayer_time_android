package shakir.swalah

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent



class TimeChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == "android.intent.action.TIME_SET") {
            context?.let { Util.setNextAlarm(it) }
        }
    }
}