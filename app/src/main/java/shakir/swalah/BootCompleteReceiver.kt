package shakir.swalah

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import shakir.swalah.alarm.ut

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            context?.let { Util.setNextAlarm(it) }
        }

    }
}