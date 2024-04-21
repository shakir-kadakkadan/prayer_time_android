package shakir.swalah

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import shakir.swalah.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustWithSystemWindow(binding.rootViewLL, binding.topSpacer, true)





        binding.battery.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !(getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
        binding.batteryLine.isVisible = binding.battery.isVisible
        binding.battery.setOnClickListener {
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

        binding.adhanTimeSettings.setOnClickListener {
            startActivity(Intent(this, AdhanTimeActivity::class.java))
        }


        binding.iqama.setOnClickListener {
            startActivity(Intent(this, IqamaActivity::class.java))
        }

        binding.adhanAlarm.isChecked = Util.isadhanAlarmOn
        binding.iqamaAlarm.isChecked = Util.isiqamaAlarmOn

        binding.adhanAlarm.setOnCheckedChangeListener { buttonView, isChecked ->
            Util.isadhanAlarmOn = isChecked
            Util.setNextAlarm(this@SettingsActivity)
        }
        binding.iqamaAlarm.setOnCheckedChangeListener { buttonView, isChecked ->
            Util.isiqamaAlarmOn = isChecked
            Util.setNextAlarm(this@SettingsActivity)
        }


        binding.contactUs.setOnClickListener {
            val url = "https://wa.me/918129625121?text=This message is regarding أَذَان app.\n"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)

        }

        binding.sound.setOnClickListener {
            startActivity(Intent(this@SettingsActivity, SoundActivity::class.java))
        }

        binding.share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val appLink = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, (" أَذَان \u200E Lite \n") +"Prayer Time and Notifications,  Qibla: \n$appLink")
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }


    }

}