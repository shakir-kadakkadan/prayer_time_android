package shakir.swalah

import android.app.AlarmManager
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
import kotlin.concurrent.thread

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustWithSystemWindow(binding.rootViewLL, binding.topSpacer, true)
        try {
            binding.battery.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !(getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
            binding.batteryLine.isVisible = binding.battery.isVisible
        } catch (e: Exception) {
            e.printStackTrace()
        }



        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager =
                    getSystemService(Context.ALARM_SERVICE) as AlarmManager
                binding.alarmPermission.isVisible = !alarmManager.canScheduleExactAlarms()
            } else {
                binding.alarmPermission.isVisible = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.alarmPermissionLine.isVisible = binding.alarmPermission.isVisible
        binding.alarmPermission.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:$packageName")
                    )
                )
            } catch (e: Exception) {
              e.printStackTrace()
            }
        }



        binding.battery.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) {
                    val optiDialog = AlertDialog.Builder(this/*, R.style.MyAlertDialogTheme*/)
                        .setTitle("Warning")
                        .setMessage("Battery optimization mode is enabled. It can interrupt Adhan notifications and alarms. Please Allow \"Stop optimising Battery Usage\"")
                        .setPositiveButton("OK") { dialog, which ->
                            dialog.dismiss()
                            try {
                                startActivity(with(Intent()) {
                                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                    setData(Uri.parse("package:${BuildConfig.APPLICATION_ID}"))
                                })
                            } catch (e: Exception) {
                                e.report()
                                toast(e.message)
                            }

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
        binding.openApp.isChecked = Util.openApp

        binding.adhanAlarm.setOnCheckedChangeListener { buttonView, isChecked ->
            Util.isadhanAlarmOn = isChecked
            Util.setNextAlarm(this@SettingsActivity)
        }
        binding.iqamaAlarm.setOnCheckedChangeListener { buttonView, isChecked ->
            Util.isiqamaAlarmOn = isChecked
            Util.setNextAlarm(this@SettingsActivity)
        }

        binding.openApp.setOnCheckedChangeListener { buttonView, isChecked ->
            Util.openApp = isChecked
            Util.setNextAlarm(this@SettingsActivity)
        }


        binding.contactUs.setOnClickListener {
            thread {
                try {
                    var url = try {
                        sendGetRequest("https://install4-default-rtdb.asia-southeast1.firebasedatabase.app/athan_app_contact_us_url.json")?.replace("\"", "")
                    } catch (e: Exception) {
                        null
                    }
                    if (url.isNullOrBlank()) {
                        url = "https://wa.me/918129625121?text=This message is regarding أَذَان app.\n"
                    }
                    runOnUiThread {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(url)
                            startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


        }

        binding.sound.setOnClickListener {
            startActivity(Intent(this@SettingsActivity, SoundActivity::class.java))
        }

        binding.share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val appLink = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, (" أَذَان \u200E Lite \n") + "Prayer Time and Notifications,  Qibla: \n$appLink")
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }


        var showMidNight = sp.getInt("showMidNight", 1)
        var arr = arrayOf("Show", "Hide")
        binding.midnightValue.setText(arr[showMidNight])
        binding.midnight.setOnClickListener {
            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(binding.midnightT.text.toString())
                .setSingleChoiceItems(arr, showMidNight) { dialog, which ->
                    showMidNight = which
                    binding.midnightValue.setText(arr[which])
                    dialog.dismiss()
                    sp.edit().putInt("showMidNight", showMidNight).commit()
                }
                .show()
        }

        var showThirdNight = sp.getInt("showThirdNight", 0)
        var arr2 = arrayOf("Isha Based", "Magrib Based", "Hide")
        binding.thirdNightValue.setText(arr2[showThirdNight])
        binding.thirdNight.setOnClickListener {
            AlertDialog.Builder(this@SettingsActivity)
                .setTitle(binding.thirdNightT.text.toString())
                .setSingleChoiceItems(arr2, showThirdNight) { dialog, which ->
                    showThirdNight = which
                    binding.thirdNightValue.setText(arr2[which])
                    dialog.dismiss()
                    sp.edit().putInt("showThirdNight", showThirdNight).commit()
                }
                .show()
        }


    }

}