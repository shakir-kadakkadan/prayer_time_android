package shakir.swalah

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import shakir.swalah.db.AppDatabase

open class BaseActivity : AppCompatActivity() {
    lateinit var appDatabase: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appDatabase = AppDatabase(this)


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
}