package shakir.swalah

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import shakir.swalah.databinding.ActivityGenerateToneBinding


class GenerateToneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateToneBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateToneBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_generate_tone)
        var a = -1
        binding.prev.setOnClickListener {
            generateTone(a--)
        }
        binding.next.setOnClickListener {
            generateTone(a++)
        }


    }

    var toneGen1: ToneGenerator? = null
    fun generateTone(a: Int) {
        stopTone()

        try {
            toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (e: Exception) {
        }
        try {
            binding.intTone.setText(a.toString())

            toneGen1?.startTone(a, binding.duration.text.toString().toInt());
        } catch (e: Exception) {
            try {
                toneGen1?.startTone(a)
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }

        }
    }


    fun stopTone() {
        try {
            toneGen1?.stopTone()
        } catch (e: Exception) {
         /*   Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()*/
            e.printStackTrace()
        }
    }

}