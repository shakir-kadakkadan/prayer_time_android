package shakir.swalah

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import shakir.swalah.databinding.ActivitySoundBinding
import java.io.File


class SoundActivity : BaseActivity() {
    private lateinit var binding: ActivitySoundBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoundBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustWithSystemWindow(binding.rootViewLL, binding.topSpacer, true)
        binding.backButton.setOnClickListener { finish() }

        binding.alarm.isChecked = sp.getBoolean("soundTypeIsAlarm", false) == true
        binding.notification.isChecked = !binding.alarm.isChecked
        binding.soundType.setOnCheckedChangeListener { group, checkedId ->
            stopPlay()
            if (checkedId == R.id.alarm) {
                sp.edit { putBoolean("soundTypeIsAlarm", true) }
            }
            if (checkedId == R.id.notification) {
                sp.edit { putBoolean("soundTypeIsAlarm", false) }
            }
            enableDisableAlarmVolume()
            showNoti(this@SoundActivity, "Sample")
        }
        enableDisableAlarmVolume()
        binding.alarmVolumeSeekBar.progress = sp.getInt("alarmVolumeSeekBar100", 75)
        binding.alarmVolumeSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                stopPlay()
                sp.edit { putInt("alarmVolumeSeekBar100", seekBar!!.progress) }
                showNoti(this@SoundActivity, "Sample")
            }

        })


        binding.adhanSound.setOnClickListener {
            stopPlay()
            showSoundList("athan_")
        }
        binding.iqamaAudio.setOnClickListener {
            stopPlay()
            showSoundList("iqama_")
        }

        binding.fajrSound.setOnClickListener {
            stopPlay()
            showSoundList("fajr_")
        }




        updateAudioNameTextViews(1)

        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(volumeChangeReceiver, filter)

        binding.root.setOnClickListener {
            stopPlay()
        }


    }

    fun updateAudioNameTextViews(rety: Int) {
        stopPlay()
        binding.adhanSoundAudioName.setText(sp.getString("${"athan_"}soundName", null))
        binding.iqamaAudioName.setText(sp.getString("${"iqama_"}soundName", null))
        var fajrrr = sp.getString("${"fajr_"}soundName", null)
        binding.fajrSoundAudioName.setText(fajrrr)
        if (fajrrr.isNullOrBlank()) {
            try {
                val key = "fajr_"
                val dir = File(filesDir, "adhanMp3s")
                val fileList = try {
                    dir.listFiles().filter { it.isFile }
                } catch (e: Exception) {
                    emptyList()
                }
                fileList.find { it.name.contains("fajr", ignoreCase = true) }?.let {
                    val path = FileProvider.getUriForFile(this, "${this.packageName}.provider", it)
                    sp.edit(commit = true) {
                        putString("${key}sound", path.toString())
                        putString("${key}soundName", it.name)
                    }
                }
                if (rety > 0)
                    updateAudioNameTextViews(rety = rety-1)
            } catch (e: Exception) {
             e.printStackTrace()
            }

        }


    }

    override fun onPause() {
        super.onPause()
        stopPlay()
    }


    fun showSoundList(key: String) {


        val dir = File(filesDir, "adhanMp3s")
        val fileList = try {
            dir.listFiles().filter { it.isFile }
        } catch (e: Exception) {
            emptyList()
        }

        println("fileList ${fileList.map { it.path }.joinToString(",")}")

        val appSoundList = arrayListOf(
            "message_tone" to R.raw.message_tone,
            "mixkit_access_allowed_tone_2869" to R.raw.mixkit_access_allowed_tone_2869,
            "beep_beep_beep" to R.raw.beep_beep_beep,
            "athan" to R.raw.athan,
        )

        var prevSoundName = sp.getString("${key}soundName", null)
        var prevSound = sp.getString("${key}sound", null)

        val list: ArrayList<String> = arrayListOf()
        list.addAll(appSoundList.map { it.first })
        list.addAll(fileList.map { it.name })
        list.addAll(notificationSounds.map { it.first })
        var pos = -1
        if (sp.getString("${key}soundName", null) != null) {
            pos = list.indexOf(sp.getString("${key}soundName", null))
        }
        AlertDialog.Builder(this)
            .setSingleChoiceItems(list.toTypedArray(), pos, DialogInterface.OnClickListener { dialog, which ->
                pos = which
//                println("which $pos")
//                if (appSoundList.map { it.first }.contains(list.get(pos))) {
//                    val path = Uri.parse("android.resource://" + AppApplication.instance.packageName + "/" + appSoundList.get(pos).second)
//                    playSound(this@SoundActivity, path)
//                } else {
//                    playSound(this@SoundActivity, notificationSounds.first { it.first == list.get(pos) }.second)
//                }

                if (fileList.find { it.name == list.get(pos) } != null) {
                    val path = FileProvider.getUriForFile(this, "${this.packageName}.provider", fileList.find { it.name == list.get(pos) }!!)
                    sp.edit(commit = true) {
                        putString("${key}sound", path.toString())
                        putString("${key}soundName", list.get(pos))
                    }
                } else if (appSoundList.map { it.first }.contains(list.get(pos))) {
                    val path = Uri.parse("android.resource://" + AppApplication.instance.packageName + "/" + appSoundList.get(pos).second)
                    sp.edit(commit = true) {
                        putString("${key}sound", path.toString())
                        putString("${key}soundName", list.get(pos))
                    }
                } else {
                    sp.edit(commit = true) {
                        putString("${key}sound", notificationSounds.first { it.first == list.get(pos) }.second.toString())
                        putString("${key}soundName", list.get(pos))
                    }
                }

                showNoti(this, "Sample ${if (key == "iqama_") "(" + "الإقامة" + ")" else ""}${if (key == "fajr_") "(" + "الفجر" + ")" else ""}")


            })
            .setCancelable(false)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->

                dialog.dismiss()

            })
            .setOnDismissListener {
                updateAudioNameTextViews(0)
            }
            .setOnCancelListener {
                updateAudioNameTextViews(0)
            }

            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                sp.edit(commit = true) {
                    putString("${key}sound", prevSound)
                    putString("${key}soundName", prevSoundName)
                }
            })
            .show()


    }

    val notificationSounds: ArrayList<Pair<String, Uri>> = arrayListOf()
    override fun onStart() {
        super.onStart()
        try {
            notificationSounds.clear()
            notificationSounds.addAll(getNotificationSounds(this))
        } catch (e: Exception) {
            e.report()
        }
        downloadSounds(force = true) {
            runOnUiThread {
                try {
                    binding.downloading.setText(it)
                } catch (e: Exception) {

                }
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(volumeChangeReceiver)
    }


    private fun getNotificationSounds(context: Context): ArrayList<Pair<String, Uri>> {
        val notificationSoundsList = ArrayList<Pair<String, Uri>>()


        // Create a RingtoneManager to retrieve the notification sounds
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(RingtoneManager.TYPE_NOTIFICATION)

        // Retrieve a Cursor containing the list of notification sounds
        val cursor = ringtoneManager.cursor

        // Iterate through the Cursor and add the notification sound titles to the list
        var p = 0
        while (cursor.moveToNext()) {
            val notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val notificationUri = ringtoneManager.getRingtoneUri(cursor.position)
            notificationSoundsList.add(notificationTitle to notificationUri)

        }

        // Close the Cursor to free up resources
        cursor.close()
        return notificationSoundsList
    }

    fun enableDisableAlarmVolume() {
        if (binding.alarm.isChecked) {
            binding.alarmVolumeLL.alpha = 1f
            binding.alarmVolumeSeekBar.isEnabled = true
        } else {
            binding.alarmVolumeLL.alpha = .25f
            binding.alarmVolumeSeekBar.isEnabled = false
        }
    }




    fun updateseekBarOnUpdateVolumeKey() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            binding.alarmVolumeSeekBar.progress = ((streamVolume / max.toDouble()) * binding.alarmVolumeSeekBar.max).toInt()

            println("streamVolume ${streamVolume} ${binding.alarmVolumeSeekBar}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var volumeChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                if (binding.alarm.isChecked) {
                    updateseekBarOnUpdateVolumeKey()
                }

            }
        }
    }


}