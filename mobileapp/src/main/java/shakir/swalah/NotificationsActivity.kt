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

import shakir.swalah.Util.is_24_hourFormat
import shakir.swalah.databinding.ActivityMainBinding
import shakir.swalah.databinding.ActivityNotificationsBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : BaseActivity() {
    private lateinit var binding: ActivityNotificationsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustWithSystemWindow(binding.rootViewLL, binding.topSpacer, true)

    }

}