package shakir.swalah

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics


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



    fun notification() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 16)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}