package shakir.swalah

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_settings.*
import shakir.swalah.Util.is_24_hourFormat
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        adjustWithSystemWindow(rootViewLL, topSpacer, true)
        hour24Value.setText(if (Util.is_24_hourFormat) "24 Hour" else "12 Hour")
        showAMPMValue.setText(SimpleDateFormat(Util.timeFormat(), Locale.ENGLISH).format(Date()).ltrEmbed())
        hour24.setOnClickListener {
            val selected = if (Util.is_24_hourFormat) 1 else 0
            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(hour24key.text.toString())
                .setSingleChoiceItems(arrayOf("12 Hour", "24 Hour"), selected) { dialog, which ->
                    Util.is_24_hourFormat = if (which == 0) false else true
                    hour24Value.setText(if (Util.is_24_hourFormat) "24 Hour" else "12 Hour")
                    showAMPMValue.setText(SimpleDateFormat(Util.timeFormat(), Locale.ENGLISH).format(Date()).ltrEmbed())
                    if (is_24_hourFormat) {
                        showAMPMKey.alpha = .25f
                    } else {
                        showAMPMKey.alpha = 1f
                    }
                    dialog.dismiss()

                }
                .show()
        }

        if (is_24_hourFormat) {
            showAMPMKey.alpha = .25f
        } else {
            showAMPMKey.alpha = 1f
        }
        showAMPM.setOnClickListener {
            if (!is_24_hourFormat) {
                val selected = if (Util.isAMPMShow) 0 else 1
                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle(showAMPMKey.text.toString())
                    .setSingleChoiceItems(arrayOf("Show", "Hide"), selected) { dialog, which ->
                        Util.isAMPMShow = if (which == 0) true else false
                        showAMPMValue.setText(SimpleDateFormat(Util.timeFormat(), Locale.ENGLISH).format(Date()).ltrEmbed())
                        dialog.dismiss()
                    }
                    .show()
            }

        }






        battery.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !(getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
        batteryLine.isVisible = battery.isVisible
        battery.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) {
                    val optiDialog = AlertDialog.Builder(this/*, R.style.MyAlertDialogTheme*/)
                        .setTitle("Warning")
                        .setMessage("Battery optimization mode is enabled. It can interrupt Adhan notifications and alarms. Please Allow \"Stop optimising Battery Usage\"")
                        .setPositiveButton("OK") { dialog, which ->
                            dialog.dismiss()
                            startActivity(with(Intent()) {
                                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                setData(Uri.parse("package:${BuildConfig.APPLICATION_ID}"))
                            })

                        }
                        .setNegativeButton("Ignore") { dialog, which ->
                            dialog.dismiss()
                        }
                        .create()
                    optiDialog?.show()


                }
            }

        }

        notifications.setOnClickListener {
            startActivity(Intent(this,NotificationsActivity::class.java))
        }


        iqama.setOnClickListener {
            startActivity(Intent(this,IqamaActivity::class.java))
        }



    }

}