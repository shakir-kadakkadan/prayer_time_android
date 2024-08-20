package shakir.swalah

import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.crashlytics.FirebaseCrashlytics

import shakir.swalah.compass.CompassSensorManager

import shakir.swalah.databinding.ActivityQiblaBinding
import shakir.swalah.databinding.ActivityQiblaIntroBinding
import java.util.*


class QiblaIntroActivity : BaseActivity() {




    lateinit var binding: ActivityQiblaIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityQiblaIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Glide.with(this).asGif().load(R.drawable.unnamed).into( binding.imageView);
        binding.ok2.setOnClickListener {
            startActivity(Intent(this,QiblaActivity::class.java))
            finish()
        }
        binding.ok.setOnClickListener {
            startActivity(Intent(this,QiblaActivity::class.java))
            finish()
        }
    }



}
