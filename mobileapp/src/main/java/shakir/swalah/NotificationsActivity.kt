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
import kotlinx.android.synthetic.main.activity_notifications.*
import shakir.swalah.Util.is_24_hourFormat
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        adjustWithSystemWindow(rootViewLL, topSpacer, true)

    }

}