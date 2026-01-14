package shakir.swalah

import android.app.NotificationManager
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit


open class BaseActivity : AppCompatActivity() {
    //    lateinit var appDatabase: AppDatabase
    val sp: SharedPreferences
        get() {
            return Util.getMySharedPreference(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        appDatabase = AppDatabase(this)


    }

    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onStart() {
        super.onStart()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }


    fun adjustWithSystemWindow(
        root: View,
        topView: View? = null,
        isTopHt: Boolean? = null,
        bottomView: View? = null,
        isBottomHt: Boolean? = null,
        callback: ((top: Int, bottom: Int) -> Unit)? = null
    ) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
        }
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            root.setOnApplyWindowInsetsListener { v, insets ->
                if (isTopHt == true)
                    topView?.layoutParams?.height = insets.systemWindowInsetTop
                else if (isTopHt == false) {
                    topView?.setPadding(0, insets.systemWindowInsetTop, 0, 0)
                }

                if (isBottomHt == true)
                    bottomView?.layoutParams?.height = insets.systemWindowInsetBottom
                else if (isBottomHt == false) {
                    bottomView?.setPadding(0, 0, 0, insets.systemWindowInsetBottom)
                }
                callback?.invoke(insets.systemWindowInsetTop, insets.systemWindowInsetBottom)
                return@setOnApplyWindowInsetsListener insets
            }
        } else { /* NOT NEEDED IN API19 below*/
            val top =
                resources.getDimensionPixelSize(
                    resources.getIdentifier(
                        "status_bar_height",
                        "dimen",
                        "android"
                    )
                )
            if (isTopHt == true)
                topView?.layoutParams?.height =
                    top
            else if (isTopHt == false) {
                topView?.setPadding(
                    0,
                    top,
                    0,
                    0
                )
            }

            callback?.invoke(top, 0)


        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

    }

    fun downloadSounds(force: Boolean = false, aaaaa: (String) -> Unit) {


        try {
            if (sp.getBoolean("force_delete_require",true)){
                val temp = File(filesDir, "temp")
                val dir = File(filesDir, "adhanMp3s")

                try {
                    temp.deleteRecursively()
                } catch (e: Exception) {

                }

                try {
                    dir.deleteRecursively()
                } catch (e: Exception) {
                }
                sp.edit().putBoolean("force_delete_require",false).commit()
            }
        } catch (e: Exception) {

        }


        var enteredDownloadQueue=false
        try {
            val lastDownloaded = sp.getLong("lastDownloaded", 0L)
            if (force || TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastDownloaded) > 25) {
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            try {
                                // Fetch audio list from Firebase Hosting
                                val audioListJson = sendGetRequest("https://prayer-time-shakir.web.app/audio-list.json")
                                val jsonArray = org.json.JSONArray(audioListJson)

                                val temp = File(filesDir, "temp")
                                val dir = File(filesDir, "adhanMp3s")
                                if (!dir.exists()) dir.mkdir()

                                for (i in 0 until jsonArray.length()) {
                                    try {
                                        val audioObj = jsonArray.getJSONObject(i)
                                        val name = audioObj.getString("name")
                                        val url = audioObj.getString("url")
                                        val size = audioObj.getLong("size")

                                        // Create safe filename from name
                                        val fileName = "${name}.mp3"
                                        val mp3 = File(dir, fileName)

                                        // Download if file doesn't exist or size doesn't match
                                        if (!mp3.exists() || mp3.length() != size) {
                                            enteredDownloadQueue=true
                                            aaaaa.invoke("Downloading... $name")
                                            downloadFileGET(url, temp)

                                            if (temp.exists() && temp.length() > 0) {
                                                temp.copyTo(mp3, overwrite = true)
                                                temp.delete()
                                                println("Downloaded: ${mp3.path} (${mp3.length()} bytes)")
                                            }
                                        } else {
                                            println("Already exists: ${mp3.path} (${mp3.length()} bytes)")
                                        }

                                        if (i == jsonArray.length() - 1&&enteredDownloadQueue) {
                                            aaaaa.invoke("Download Completed")
                                        }
                                    } catch (e: Exception) {
                                        aaaaa.invoke("Download Failed: ${e.message}")
                                        e.printStackTrace()
                                    }
                                }

                                sp.edit(commit = true) {
                                    putLong("lastDownloaded", System.currentTimeMillis())
                                }

                            } catch (e: Exception) {
                                aaaaa.invoke("Downloading Failed\nPlease check your internet connection")
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    fun stopPlay() {

        try {
            ringtone?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            (getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        stopPlay()

//        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            updateseekBarOnUpdateVolumeKey()
//        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            updateseekBarOnUpdateVolumeKey()
//        }

        return super.onKeyDown(keyCode, event)

    }


    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        stopPlay()
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        stopPlay()
        return super.onKeyUp(keyCode, event)

    }




}