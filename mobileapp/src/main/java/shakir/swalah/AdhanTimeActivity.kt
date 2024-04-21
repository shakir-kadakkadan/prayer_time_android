package shakir.swalah

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import com.azan.Method
import shakir.swalah.databinding.ActivityAdhanTimeSettingsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AdhanTimeActivity : BaseActivity() {
    private lateinit var binding: ActivityAdhanTimeSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdhanTimeSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustWithSystemWindow(binding.rootViewLL, binding.topSpacer, true)
        binding.hour24Value.setText(if (Util.is_24_hourFormat) "24 Hour" else "12 Hour")
        binding.showAMPMValue.setText(SimpleDateFormat(Util.timeFormat(), Locale.ENGLISH).format(Date()).ltrEmbed())
        binding.hour24.setOnClickListener {
            val selected = if (Util.is_24_hourFormat) 1 else 0
            AlertDialog.Builder(this@AdhanTimeActivity)
                .setTitle(binding.hour24key.text.toString())
                .setSingleChoiceItems(arrayOf("12 Hour", "24 Hour"), selected) { dialog, which ->
                    Util.is_24_hourFormat = if (which == 0) false else true
                    binding.hour24Value.setText(if (Util.is_24_hourFormat) "24 Hour" else "12 Hour")
                    binding.showAMPMValue.setText(SimpleDateFormat(Util.timeFormat(), Locale.ENGLISH).format(Date()).ltrEmbed())
                    if (Util.is_24_hourFormat) {
                        binding.showAMPMKey.alpha = .25f
                    } else {
                        binding.showAMPMKey.alpha = 1f
                    }
                    dialog.dismiss()

                }
                .show()
        }

        if (Util.is_24_hourFormat) {
            binding.showAMPMKey.alpha = .25f
        } else {
            binding.showAMPMKey.alpha = 1f
        }
        binding.showAMPM.setOnClickListener {
            if (!Util.is_24_hourFormat) {
                val selected = if (Util.isAMPMShow) 0 else 1
                AlertDialog.Builder(this@AdhanTimeActivity)
                    .setTitle(binding.showAMPMKey.text.toString())
                    .setSingleChoiceItems(arrayOf("Show", "Hide"), selected) { dialog, which ->
                        Util.isAMPMShow = if (which == 0) true else false
                        binding.showAMPMValue.setText(SimpleDateFormat(Util.timeFormat(), Locale.ENGLISH).format(Date()).ltrEmbed())
                        dialog.dismiss()
                    }
                    .show()
            }

        }

        var tm = sp.getInt("timeMethods", 0)
        binding.caculationMethodsSelected.setText(timeMethods[tm].second)
        binding.caculationMethodsDesc.setText(timeMethods[tm].third.replace(timeMethods[tm].second,"").trimIndent())
        binding.caculationMethods.setOnClickListener {
            AlertDialog.Builder(this@AdhanTimeActivity)
                .setTitle("Calculation Methods")
                .setSingleChoiceItems(timeMethods.map { it.second }.toTypedArray(), tm) { dialog, which ->
                    tm = which
                    sp.edit(commit = true) {
                        putInt("timeMethods", tm)
                    }
                    binding.caculationMethodsSelected.setText(timeMethods[tm].second)
                    binding.caculationMethodsDesc.setText(timeMethods[tm].third.replace(timeMethods[tm].second,"").trimIndent())
                    dialog.dismiss()
                    Util.setNextAlarm(this@AdhanTimeActivity)
                }
                .show()
        }


    }

}


val timeMethods: Array<Triple<Method, String, String>> = arrayOf(

    Triple(
        Method.KARACHI_SHAF, "University of Islamic Sciences, Karachi (Shaf'i)",
        "University of Islamic Sciences, Karachi (Shaf'i)\n" +
                "Fajr Angle      = 18\n" +
                "Ishaa Angle = 18\n" +
                "Used in: Iran, Kuwait, parts of Europe",
    ),
    Triple(
        Method.KARACHI_HANAF, "University of Islamic Sciences, Karachi (Hanafi)", "University of Islamic Sciences, Karachi (Hanafi)\n" +
                "Fajr Angle = 18\n" +
                "Ishaa Angle = 18\n" +
                "Used in: Afghanistan, Bangladesh, India"
    ),

    Triple(
        Method.EGYPT_SURVEY, "Egyptian General Authority of Survey", "Egyptian General Authority of Survey\n" +
                "Fajr Angle = 20\n" +
                "Ishaa Angle = 18\n" +
                "Used in: Indonesia, Iraq, Jordan, Lebanon, Malaysia, Singapore, Syria, parts of Africa, parts of United States"
    ),

    Triple(
        Method.NORTH_AMERICA, "Islamic Society of North America",
        "Islamic Society of North America\n" +
                "Fajr Angle = 15\n" +
                "Ishaa Angle = 15\n" +
                "Used in: Canada, Parts of UK, parts of United States",
    ),
    Triple(
        Method.MUSLIM_LEAGUE, "Muslim World League (MWL)", "Muslim World League (MWL)\n" +
                "Fajr Angle = 18\n" +
                "Ishaa Angle = 17\n" +
                "Used in: parts of Europe, Far East, parts of United States"
    ),
    Triple(
        Method.UMM_ALQURRA, "Om Al-Qurra University",
        "Om Al-Qurra University\n" +
                "Fajr Angle = 19\n" +
                "Ishaa Interval = 90 minutes from Al-Maghrib prayer but set to 120 during Ramadan.\n" +
                "Used in: Saudi Arabia",
    ),

    Triple(
        Method.FIXED_ISHAA, "Fixed Ishaa Angle Interval (always 90)",
        "Fixed Ishaa Angle Interval (always 90)\n" +
                "Fajr Angle = 19.5\n" +
                "Ishaa Interval = 90 minutes from Al-Maghrib prayer.\n" +
                "Used in: Bahrain, Oman, Qatar, United Arab Emirates",
    ),


    )



