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
import android.provider.OpenableColumns
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.view.KeyEvent
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import shakir.swalah.databinding.ActivitySoundBinding
import java.io.File
import kotlin.concurrent.thread


class SoundActivity : BaseActivity() {
    private lateinit var binding: ActivitySoundBinding

    private var pendingPickerKey: String? = null
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            val key = pendingPickerKey ?: return@registerForActivityResult
            val name = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                if (col >= 0) cursor.getString(col) else null
            } ?: uri.lastPathSegment ?: "local_file"
            thread {
                try {
                    val dir = File(filesDir, "adhanMp3s").also { if (!it.exists()) it.mkdir() }
                    val destFile = File(dir, name)
                    contentResolver.openInputStream(uri)!!.use { input ->
                        destFile.outputStream().use { input.copyTo(it) }
                    }
                    val fileUri = FileProvider.getUriForFile(this, "${packageName}.provider", destFile)
                    sp.edit(commit = true) {
                        putString("${key}sound", fileUri.toString())
                        putString("${key}soundName", name)
                    }
                    runOnUiThread {
                        updateAudioNameTextViews(0)
                        updateDownloadFolderSize()
                        showSoundList(key)
                        showNoti(this, "Sample ${
                            if (key == "suhoor_") "(" + "سَحُورٌ" + ")" else
                            if (key == "iqama_") "(" + "الإقامة" + ")" else ""}${if (key == "fajr_") "(" + "الفجر" + ")" else ""}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
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

        binding.suhoorAudio.setOnClickListener {
            stopPlay()
            showSoundList("suhoor_")
        }

        updateDownloadFolderSize()
        binding.clearDownloadsBtn.setOnClickListener {
            val dir = File(filesDir, "adhanMp3s")
            val sizeText = folderSizeText(dir)
            AlertDialog.Builder(this)
                .setTitle("Cached Audio")
                .setMessage("Delete all cached audio files? ($sizeText)")
                .setPositiveButton("Delete") { _, _ ->
                    val deletedNames = dir.listFiles()?.map { it.name }?.toSet() ?: emptySet()
                    dir.listFiles()?.forEach { it.delete() }
                    arrayOf("athan_", "iqama_", "fajr_", "suhoor_").forEach { key ->
                        if (sp.getString("${key}soundName", null)?.let { it in deletedNames } == true) {
                            sp.edit {
                                putString("${key}sound", null)
                                putString("${key}soundName", null)
                            }
                        }
                    }
                    updateDownloadFolderSize()
                    updateAudioNameTextViews(0)
                }
                .setNegativeButton("Cancel", null)
                .show()
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
        binding.suhoorAudioName.setText(sp.getString("${"suhoor_"}soundName", null))
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
                    updateAudioNameTextViews(rety = rety - 1)
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
            "panic" to R.raw.panic,
        )

        var prevSoundName = sp.getString("${key}soundName", null)
        var prevSound = sp.getString("${key}sound", null)

        val list: ArrayList<String> = arrayListOf()
        list.addAll(appSoundList.map { it.first })
        list.addAll(fileList.map { it.name })
        list.addAll(notificationSounds.map { it.first })
        val pos = if (prevSoundName != null) list.indexOf(prevSoundName) else -1

        // Sticky "Select local file" header — lives in setCustomTitle (never scrolls away)
        val dp16 = (16 * resources.displayMetrics.density).toInt()
        val headerLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val localFileRow = TextView(this).apply {
            text = "📁 Select local file..."
            setPadding(dp16 * 3, dp16, dp16, dp16)
            textSize = 16f
            isClickable = true
            isFocusable = true
            TypedValue().also { tv ->
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
                setBackgroundResource(tv.resourceId)
            }
        }
        headerLayout.addView(localFileRow)
        headerLayout.addView(
            android.view.View(this).apply { setBackgroundColor(0x1F000000) },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
        )

        var dialogRef: AlertDialog? = null
        localFileRow.setOnClickListener {
            pendingPickerKey = key
            dialogRef?.dismiss()
            filePickerLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            })
        }

        dialogRef = AlertDialog.Builder(this)
            .setCustomTitle(headerLayout)
            .setSingleChoiceItems(list.toTypedArray(), pos) { _, which ->
                val selectedName = list[which]
                val fileMatch = fileList.find { it.name == selectedName }
                val appSoundMatch = appSoundList.find { it.first == selectedName }
                val notifSoundMatch = notificationSounds.find { it.first == selectedName }

                if (fileMatch != null) {
                    val path = FileProvider.getUriForFile(this, "${packageName}.provider", fileMatch)
                    sp.edit(commit = true) {
                        putString("${key}sound", path.toString())
                        putString("${key}soundName", selectedName)
                    }
                } else if (appSoundMatch != null) {
                    val path = Uri.parse("android.resource://${packageName}/${appSoundMatch.second}")
                    sp.edit(commit = true) {
                        putString("${key}sound", path.toString())
                        putString("${key}soundName", selectedName)
                    }
                } else if (notifSoundMatch != null) {
                    sp.edit(commit = true) {
                        putString("${key}sound", notifSoundMatch.second.toString())
                        putString("${key}soundName", selectedName)
                    }
                }

                showNoti(this, "Sample ${
                    if (key == "suhoor_") "(" + "سَحُورٌ" + ")" else
                    if (key == "iqama_") "(" + "الإقامة" + ")" else ""}${if (key == "fajr_") "(" + "الفجر" + ")" else ""}")
            }
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setOnDismissListener { updateAudioNameTextViews(0) }
            .setOnCancelListener { updateAudioNameTextViews(0) }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                sp.edit(commit = true) {
                    putString("${key}sound", prevSound)
                    putString("${key}soundName", prevSoundName)
                }
            }
            .create()
        dialogRef.show()


    }

    val notificationSounds: ArrayList<Pair<String, Uri>> = arrayListOf()
    override fun onStart() {
        super.onStart()
        updateDownloadFolderSize()
        try {
            notificationSounds.clear()
            notificationSounds.addAll(getNotificationSounds(this))
        } catch (e: Exception) {
            e.report()
        }
        downloadSounds(force = true) {
            runOnUiThread {
                try {
                    binding.downloading.text = it
                    binding.downloading.visibility = if (it.isNullOrEmpty()) android.view.View.GONE else android.view.View.VISIBLE
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

    private fun folderSizeText(dir: File): String {
        val bytes = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        return when {
            bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024    -> "%.1f KB".format(bytes / 1_024.0)
            else              -> "$bytes B"
        }
    }

    fun updateDownloadFolderSize() {
        val dir = File(filesDir, "adhanMp3s")
        binding.cacheSizeText.text = folderSizeText(dir)
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