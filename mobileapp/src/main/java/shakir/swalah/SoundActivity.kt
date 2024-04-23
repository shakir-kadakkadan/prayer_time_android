package shakir.swalah

import android.app.AlertDialog
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import shakir.swalah.databinding.ActivitySoundBinding


class SoundActivity : BaseActivity() {
    private lateinit var binding: ActivitySoundBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoundBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustWithSystemWindow(binding.rootViewLL, binding.topSpacer, true)


        binding.alarm.isChecked = sp.getBoolean("soundTypeIsAlarm", false) == true
        binding.notification.isChecked = !binding.alarm.isChecked
        binding.soundType.setOnCheckedChangeListener { group, checkedId ->
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
                sp.edit { putInt("alarmVolumeSeekBar100", seekBar!!.progress) }
                showNoti(this@SoundActivity, "Sample")
            }

        })


        binding.adhanSound.setOnClickListener {

            showSoundList("athan_")
        }
        binding.iqamaAudio.setOnClickListener {
            showSoundList("iqama_")
        }
        updateAudioNameTextViews()


    }

    fun updateAudioNameTextViews(){
        binding.adhanSoundAudioName.setText(sp.getString("${"athan_"}soundName", null))
        binding.iqamaAudioName.setText(sp.getString("${"iqama_"}soundName", null))
    }

    override fun onPause() {
        super.onPause()
        try {
            (this.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun showSoundList(key: String) {

        val appSoundList = arrayListOf(
            "athan" to R.raw.athan,
            "beep_beep_beep" to R.raw.beep_beep_beep,
            "message_tone" to R.raw.message_tone,
            "mixkit_access_allowed_tone_2869" to R.raw.mixkit_access_allowed_tone_2869,
            "complete_quest_requirement" to R.raw.complete_quest_requirement,
        )

        var prevSoundName = sp.getString("${key}soundName", null)
        var prevSound = sp.getString("${key}sound", null)

        val list: ArrayList<String> = arrayListOf()
        list.addAll(appSoundList.map { it.first })

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

                if (appSoundList.map { it.first }.contains(list.get(pos))) {
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

                showNoti(this, "Sample ${if (key == "iqama_") "(" + "الإقامة" + ")" else ""}")


            })
            .setCancelable(false)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->

                dialog.dismiss()

            })
            .setOnDismissListener {
                updateAudioNameTextViews()
            }
            .setOnCancelListener {
                updateAudioNameTextViews()
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
    lateinit var notificationSounds: ArrayList<Pair<String, Uri>>
    override fun onStart() {
        super.onStart()
        notificationSounds = getNotificationSounds(this)
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

}