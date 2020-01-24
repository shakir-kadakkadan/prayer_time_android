package shakir.swalah

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_generate_tone.*

class GenerateToneActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_tone)
        var a = -1
        prev.setOnClickListener {
            generateTone(a--)
        }
        next.setOnClickListener {
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
            intTone.setText(a.toString())

            toneGen1?.startTone(a, duration.text.toString().toInt());
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