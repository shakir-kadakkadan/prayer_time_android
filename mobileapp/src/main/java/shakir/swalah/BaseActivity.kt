package shakir.swalah

import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder
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

    fun downloadSounds(force:Boolean=false) {
        try {
            val lastDownloaded=sp.getLong("lastDownloaded",0L)
            if (force||TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis()-lastDownloaded)>25){
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            try {
                                val s = sendGetRequest("https://firebasestorage.googleapis.com/v0/b/prayer-time-shakir.appspot.com/o/")
                                println("sssss")
                                println(s)
                                val jsonArray = JSONObject(s).getJSONArray("items")
                                for (i in 0 until jsonArray.length()) {
                                    try {
                                        val name = jsonArray.getJSONObject(i).getString("name")
                                        if (name.startsWith("adhan/")) {
                                            val temp = File(filesDir, "temp")
                                            val dir = File(filesDir, "adhanMp3s")
                                            if (!dir.exists()) dir.mkdir()
                                            val mp3 = File(dir, name.replaceFirst("adhan/",""))
                                            if (mp3.length() <= 0) {
                                                val token = JSONObject(sendGetRequest("https://firebasestorage.googleapis.com/v0/b/prayer-time-shakir.appspot.com/o/${URLEncoder.encode(name)}")).getString("downloadTokens")
                                                downloadFileGET("https://firebasestorage.googleapis.com/v0/b/prayer-time-shakir.appspot.com/o/${URLEncoder.encode(name)}?alt=media&token=$token", temp)
                                                temp.copyTo(mp3)
                                                temp.delete()
                                            }
                                            println("fileList mp3 ${mp3.path} ${mp3.length()}")
                                        }


                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                sp.edit(commit = true) {
                                    putLong("lastDownloaded",System.currentTimeMillis())
                                }

                            } catch (e: Exception) {
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


}