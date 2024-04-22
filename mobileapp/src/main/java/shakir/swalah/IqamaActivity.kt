package shakir.swalah


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isVisible
import com.azan.astrologicalCalc.SimpleDate


import java.text.SimpleDateFormat
import java.util.*

import shakir.swalah.databinding.ActivityIqamaBinding
import shakir.swalah.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit


class IqamaActivity : BaseActivity() {
    private lateinit var binding: ActivityIqamaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIqamaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustWithSystemWindow(binding.rootViewLL, binding.topSpacer, true)
        val arrayView = arrayOf(
            binding.FAJR, binding.ZUHR, binding.ASR, binding.MAGHRIB, binding.ISHA
        )
        val names = arrayOf(
            "الفجر",
            "الظهر",
            "العصر",
            "المغرب",
            "العشاء",
        )

        val azan = getAthanObj(sp.getDouble("v2_latitude", 0.0), sp.getDouble("v2_longitude", 0.0))
        val today = SimpleDate(GregorianCalendar())
        val prayerTimes = azan.getAthanOfDate(today)
        val timeFormat = Util.timeFormat()


        val iqamaSettins = Util.getIqamaSettings()

        println("getIqamaSettings ${iqamaSettins.joinToString()}")

        iqamaSettins.forEachIndexed { index, iqama ->
            val view = arrayView[index]
            view.iqama.setText(names[index])


            fun setAfterOrAtText(setAction: Boolean = false) {
                println("changing in $index $iqama")

                if (iqama.isAfter)
                    view.afterOrAt.setText("after")
                else
                    view.afterOrAt.setText("at")

                view.afterTime.isVisible = iqama.isAfter
                view.afterTimeMinuteLabel.isVisible = iqama.isAfter
                view.atTime.isVisible = !iqama.isAfter
                val prayTime = prayerTimes[index]
                val fixed =
                    if (iqama.fixed <= 0) {
                        TimeUnit.MINUTES.toMillis(30).plus(prayTime.time)
                    } else
                        iqama.fixed


                view.atTime.setText(SimpleDateFormat(timeFormat, Locale.ENGLISH).format(Date(fixed)).ltrEmbed())

                val textWather = object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        try {
                            iqama.after = view.afterTime.text.toString().toInt()
                        } catch (e: Exception) {
                            iqama.after = 5
                        }
                        Util.saveIqamaSettings(iqamaSettins)
                        Util.setNextAlarm(this@IqamaActivity)
                    }

                }



                try {
                    view.afterTime.removeTextChangedListener(textWather)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                view.afterTime.setText(iqama.after.toString())
                view.atTime.setOnClickListener {
                    TimePickerFragment { HH, m ->
                        iqama.fixed = Calendar.getInstance().apply {
                            timeInMillis = prayTime.time
                            set(Calendar.HOUR_OF_DAY, HH)
                            set(Calendar.MINUTE, m)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        view.atTime.setText(SimpleDateFormat(timeFormat, Locale.ENGLISH).format(Date(iqama.fixed)).ltrEmbed())
                        Util.saveIqamaSettings(iqamaSettins)
                        Util.setNextAlarm(this@IqamaActivity)

                    }.apply {

                        arguments = Bundle().apply {
                            putLong("prayTime", fixed)
                        }
                    }.show(supportFragmentManager, "timePicker")
                }

                if (setAction) {
                    if (iqama.isAfter) {
                        view.afterTime.requestFocus()
                        view.afterTime.post {
                            view.afterTime.showKeyBoard()
                        }
                    } else {
                        view.afterTime.hideKeyboardView()
                        view.atTime.performClick()
                    }
                }


                view.afterTime.addTextChangedListener(textWather)


            }
            setAfterOrAtText(setAction = false)
            view.afterOrAt.setOnClickListener {
                iqama.isAfter = !iqama.isAfter
                setAfterOrAtText(setAction = true)
                Util.saveIqamaSettings(iqamaSettins)
                Util.setNextAlarm(this@IqamaActivity)
            }


        }


    }

}




