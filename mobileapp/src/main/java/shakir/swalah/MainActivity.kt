package shakir.swalah

/*import androidx.recyclerview.widget.RecyclerView*/


import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.ConfigurationCompat
import com.azan.TimeCalculator
import com.azan.types.AngleCalculationType
import com.azan.types.PrayersType
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import shakir.swalah.Util.isiqamaAlarmOn
import shakir.swalah.databinding.ActivityMainBinding
import shakir.swalah.models.Cord
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue


val INVALID_CORDINATE = Double.MAX_VALUE


class MainActivity : BaseActivity() {


    var toast: Toast? = null
    var toastText: String = ""

    //@thread


    //Lat range = -90 to +90
    //LOng range = -180 to +180


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adjustWithSystemWindow(binding.rootViewLL, binding.topSpacer, true)
        val receiver = ComponentName(applicationContext, BootCompleteReceiver::class.java)
        applicationContext.packageManager?.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )



        if (arrayList.size == 0) {
            getLocationFromCSV()
        }


        //qibla.isVisible = isMyTestDevice()


        /*  generateTone.setOnClickListener {
              startActivity(Intent(this, GenerateToneActivity::class.java))
          }*/


        binding.hijriDate.setText(UmmalquraCalendar().convertCaledarToDisplayDate_1())



        binding.speedDial.inflate(R.menu.menu_speed_dial)
        binding.speedDial.setOnActionSelectedListener {
            when (it.id) {
                R.id.qiblaFAB -> {
                    startActivity(Intent(this, QiblaActivity::class.java))
                }

                R.id.settingsFAB -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }

                R.id.monthView -> {
                    startActivity(Intent(this, MonthViewActivity::class.java))
                }


            }



            binding.speedDial.close(true)
            return@setOnActionSelectedListener true
        }


        binding.locationLL.setOnClickListener {
            startActivity(Intent(this, LocationSelectorAvtivity::class.java))
        }


    }


    fun millisecondsToHMS(milliseconds: Long): String {
        val totalSeconds = milliseconds.absoluteValue / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return ((if (milliseconds < 0) "" else "") + String.format("%02d:%02d:%02d", hours, minutes, seconds)).ltrEmbed()
    }


//    fun updateCountdownTextView() {
//        val sharedPreferences = Util.getMySharedPreference(this)
//        val lastMilli = sharedPreferences.getLong("lastMilli", 0L)
//        val lastMilli_prev = sharedPreferences.getLong("lastMilli_prev", 0L)
//        val lastName = sharedPreferences.getString("lastName", "")
//        val lastName_prev = sharedPreferences.getString("lastName_prev", "")
//        if (lastMilli_prev != 0L && System.currentTimeMillis() - lastMilli_prev <= TimeUnit.MINUTES.toMillis(10) &&
//            TimeUnit.MILLISECONDS.toMinutes(lastMilli - System.currentTimeMillis()) > 30
//
//        ) {
//            binding.prayerTimeLl.countdown?.countdownTV?.setText(millisecondsToHMS(lastMilli_prev - System.currentTimeMillis()))
//            binding.prayerTimeLl.countdown?.countdownTVName?.setText(lastName_prev)
//        } else {
//            binding.prayerTimeLl.countdown?.countdownTV?.setText(millisecondsToHMS(lastMilli - System.currentTimeMillis()))
//            binding.prayerTimeLl.countdown?.countdownTVName?.setText(lastName)
//        }
//
//
//    }

    var lastUpdatedMinuteSinceEpoch = 0L
    val countDownTimer: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            var epochMinute = System.currentTimeMillis() / (1000 * 60)
            if (epochMinute != lastUpdatedMinuteSinceEpoch) {
                onMinuteUpdate()
                Handler().postDelayed({
                    onMinuteUpdate()
                }, 1000)
            }
            lastUpdatedMinuteSinceEpoch = epochMinute
            onSecondUpdate()

        }

        override fun onFinish() {

        }
    }

    fun onMinuteUpdate(isOnResume: Boolean = false) {
        try {
            if (sp.getBoolean("v2_set", false) == true) {
                onGetCordinates(
                    sp.getDouble("v2_latitude", 0.0),
                    sp.getDouble("v2_longitude", 0.0),
                    sp.getString("v2_locality", ""),
                )

                if (isOnResume) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 16)
                            } else {
                                ask_optimization_condetion_2_notification = true
                                optimization()
                            }
                        } else {
                            ask_optimization_condetion_2_notification = true
                            optimization()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    ask_optimization_condetion_1_location = true
                    optimization()
                }

            } else {
                if (isOnResume) {
                    startActivity(Intent(this, LocationSelectorAvtivity::class.java).apply {
                        putExtra("comeBack", true)
                    })
                    finish()
                }
            }


            currentIqamaMilli = null
            countdownDestinationMilii = null
            countdownColor = Color.WHITE
            if (currentPrayTime != null && currentPrayTime!!.second != 1 && isiqamaAlarmOn) {
                val iqsettings = Util.getIqamaSettings().get(if (currentPrayTime!!.second == 0) 0 else currentPrayTime!!.second - 1)
                if (iqsettings.isAfter) {
                    currentIqamaMilli = currentPrayTime!!.first.time + (TimeUnit.MINUTES.toMillis(iqsettings.after.toLong()))
                } else {
                    currentIqamaMilli = iqsettings.fixed
                }
            }

            val array = arrayOf(
                PrayersType.FAJR,
                PrayersType.SUNRISE,
                PrayersType.ZUHR,
                PrayersType.ASR,
                PrayersType.MAGHRIB,
                PrayersType.ISHA
            )

            val redMinuteAdhan = if (currentPrayTime!!.second == 1) 10L else 20L

            if (currentIqamaMilli != null && System.currentTimeMillis() > currentIqamaMilli!! && System.currentTimeMillis() <= (currentIqamaMilli!! + TimeUnit.MINUTES.toMillis(10))) {
                countdownColor = Color.RED
                countdownDestinationMilii = currentIqamaMilli!!
                countdownNameString = AppApplication.getArabicNames(array[currentPrayTime!!.second].name) + " (الإقامة) "
            } else if (currentIqamaMilli != null && System.currentTimeMillis() <= currentIqamaMilli!!) {
                countdownColor = Color.GREEN
                countdownDestinationMilii = currentIqamaMilli
                countdownNameString = AppApplication.getArabicNames(array[currentPrayTime!!.second].name) + " (الإقامة) "
            } else if (currentPrayTime != null && System.currentTimeMillis() <= currentPrayTime!!.first.time + TimeUnit.MINUTES.toMillis(redMinuteAdhan)) {
                countdownColor = Color.RED
                countdownDestinationMilii = currentPrayTime!!.first.time
                countdownNameString = AppApplication.getArabicNames(array[currentPrayTime!!.second].name)
            } else {
                countdownColor = Color.WHITE
                countdownDestinationMilii = nextPrayTime!!.first.time
                countdownNameString = AppApplication.getArabicNames(array[nextPrayTime!!.second].name)
            }


            onSecondUpdate()
        } catch (e: Exception) {
            e.report()
        }
    }

    fun onSecondUpdate() {

        try {//1713598080000 1713597628969


            if (countdownDestinationMilii != null) {
                binding.prayerTimeLl.countdown?.countdownTV?.setText(millisecondsToHMS((countdownDestinationMilii!! - System.currentTimeMillis())))
                binding.prayerTimeLl.countdown?.countdownTVName?.setText(countdownNameString)
                binding.prayerTimeLl.countdown?.countdownTV?.setTextColor(countdownColor)
                binding.prayerTimeLl.countdown?.countdownTVName?.setTextColor(countdownColor)
            } else {
                binding.prayerTimeLl.countdown?.countdownTV?.setText("")
                binding.prayerTimeLl.countdown?.countdownTVName?.setText("")
            }
        } catch (e: Exception) {
            e.report()
        }
    }


    override fun onPause() {
        super.onPause()
        countDownTimer.cancel()

    }

    override fun onResume() {
        super.onResume()
        countDownTimer.start()
        onMinuteUpdate(isOnResume = true)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ask_optimization_condetion_2_notification = true
        optimization()

    }

    override fun onStart() {
        super.onStart()
        try {
            window.decorView.postDelayed({
                try {
                    val crashlytics = Firebase.crashlytics
                    crashlytics.setCustomKeys {
                        key("LanguageTags", ConfigurationCompat.getLocales(getResources().getConfiguration()).toLanguageTags())
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 3000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var nextPrayTime: Pair<Date, Int>? = null
    var currentPrayTime: Pair<Date, Int>? = null
    var currentIqamaMilli: Long? = null
    var countdownColor = Color.WHITE
    var countdownDestinationMilii: Long? = null
    var countdownNameString: String? = null


    fun onGetCordinates(
        latitude: Double,
        longitude: Double,
        l: String? = "",
        cancelPrevisousPendingInten: Boolean = false
    ) {

        /*if (cancelPrevisousPendingInten)*/ /*todo : test the condetion*/

        val array = arrayOf(
            PrayersType.FAJR,
            PrayersType.SUNRISE,
            PrayersType.ZUHR,
            PrayersType.ASR,
            PrayersType.MAGHRIB,
            PrayersType.ISHA
        )
        val date = GregorianCalendar()
        val dateYest = GregorianCalendar().apply {
            add(Calendar.DATE, -1)
        }

        val dateTomorrow = GregorianCalendar().apply {
            add(Calendar.DATE, 1)
        }


        val prayerTimes =
            TimeCalculator().date(date).location(latitude, longitude, 0.0, 0.0)
                .timeCalculationMethod(AngleCalculationType.KARACHI)
                .calculateTimes()

        val prayerTimesYest =
            TimeCalculator().date(dateYest).location(latitude, longitude, 0.0, 0.0)
                .timeCalculationMethod(AngleCalculationType.KARACHI)
                .calculateTimes()

        val prayerTimesTomorrow =
            TimeCalculator().date(dateTomorrow).location(latitude, longitude, 0.0, 0.0)
                .timeCalculationMethod(AngleCalculationType.KARACHI)
                .calculateTimes()


        nextPrayTime = null


        arrayListOf(
            (0..5).map { prayerTimesYest.getPrayTime(array[it]) to it },
            (0..5).map { prayerTimes.getPrayTime(array[it]) to it },
            (0..5).map { prayerTimesTomorrow.getPrayTime(array[it]) to it },
        ).flatMap { it }.forEach {
            if (nextPrayTime == null && it.first.after(Date())) {
                nextPrayTime = it
            }
            if (it.first.before(Date())) {
                currentPrayTime = it
            }
        }


        val timeFormat = Util.timeFormat()

        arrayOf(binding.prayerTimeLl.FAJR, binding.prayerTimeLl.SUNRISE, binding.prayerTimeLl.ZUHR, binding.prayerTimeLl.ASR, binding.prayerTimeLl.MAGHRIB, binding.prayerTimeLl.ISHA).forEachIndexed { index, view ->
            view.prayerName.setText(AppApplication.getArabicNames(array[index].name))
            val prayTime = prayerTimes.getPrayTime(array[index])
            view.prayerTime.text =
                SimpleDateFormat(timeFormat, Locale.ENGLISH).format(prayTime).ltrEmbed()
        }




        binding.prayerTimeLl.MIDNIGHT.prayerName.setText("Mid night")
        binding.prayerTimeLl.THIRDNIGHT.prayerName.setText("Third night(m)")
        binding.prayerTimeLl.THIRDNIGHTISHA.prayerName.setText("Third night(i)")
        binding.prayerTimeLl.MIDNIGHT.prayerTime.setText(SimpleDateFormat(timeFormat, Locale.ENGLISH).format(Date(prayerTimes.getPrayTime(array[0]).time.plus(prayerTimesYest.getPrayTime(array[4]).time).div(2))).ltrEmbed())
        binding.prayerTimeLl.THIRDNIGHT.prayerTime.setText(
            SimpleDateFormat(timeFormat, Locale.ENGLISH).format(
                Date(
                    prayerTimes.getPrayTime(array[0]).time.minus(
                        prayerTimes.getPrayTime(array[0]).time.minus(prayerTimesYest.getPrayTime(array[4]).time).div(3)
                    )
                )
            ).ltrEmbed()
        )

        binding.prayerTimeLl.THIRDNIGHTISHA.prayerTime.setText(
            SimpleDateFormat(timeFormat, Locale.ENGLISH).format(
                Date(
                    prayerTimes.getPrayTime(array[0]).time.minus(
                        prayerTimes.getPrayTime(array[0]).time.minus(prayerTimesYest.getPrayTime(array[5]).time).div(3)
                    )
                )
            ).ltrEmbed()
        )

        Util.setNextAlarm(this)

        binding.locationTV.setText(if (l.isNullOrBlank()) "My Location" else l)
        println("$latitude $longitude $l")

    }


    val arrayList = arrayListOf<Cord>()

    fun getLocationFromCSV() {
        assets.open("worldcities.csv").bufferedReader().useLines {
            arrayList.clear()
            it.forEach {
                val splited = it.trim().split(',')
                arrayList.add(Cord(splited[0], splited[1].toDouble(), splited[2].toDouble()))
            }
        }
        /*        locationAC.setAdapter(
                    ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        arrayList.map { it.name })
                )
                locationAC.threshold = 1
                locationAC.setOnItemClickListener { parent, view, position, id ->
                    val get = arrayList.find { it.name == locationAC.text.toString() }
                    get?.let {

                        sp.edit()
                            .putDouble("latitude", get.latitude)
                            .putDouble("longitude", get.longitude)
                            .putString("locality", get.name)
                            .putBoolean("isLocationSet", true)
                            .apply()
                        onGetCordinates(get.latitude, get.longitude, get.name, true)
                    }
                }*/
    }


    var optiDialog: AlertDialog? = null
    var ask_optimization_condetion_1_location = false
    var ask_optimization_condetion_2_notification = false
    fun optimization() {

        if (ask_optimization_condetion_1_location && ask_optimization_condetion_2_notification) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) {
                    if (sp.getBoolean("stopOptimizeBatteryIgnored", false) != true) {
                        if (optiDialog == null)
                            optiDialog = AlertDialog.Builder(this/*, R.style.MyAlertDialogTheme*/)
                                .setTitle("Warning")
                                .setMessage("Battery optimization mode is enabled. It can interrupt Adhan notifications and alarms. Please Allow \"Stop optimising Battery Usage\"")
                                .setPositiveButton("OK") { dialog, which ->
                                    dialog.dismiss()
                                    startActivity(with(Intent()) {
                                        action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                        setData(Uri.parse("package:${BuildConfig.APPLICATION_ID}"))
                                    })

                                }
                                .setNegativeButton("Ignore") { dialog, which ->
                                    dialog.dismiss()
                                    sp.edit { putBoolean("stopOptimizeBatteryIgnored", true) }
                                }
                                .create()
                        optiDialog?.show()
                    }


                }
            }
        }


    }


}

fun UmmalquraCalendar?.convertCaledarToDisplayDate_1(): String {
    if (this != null) {
        val locale = Locale("ar", "KW")
        val nf = NumberFormat.getInstance(locale)
        nf.isGroupingUsed = false

        return " ${this.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale)}،  ${
            nf.format(
                this[Calendar.DATE]
            )
        } ${
            this.getDisplayName(
                Calendar.MONTH,
                Calendar.LONG,
                Locale("ar")
            )
        } ${nf.format(this.get(Calendar.YEAR))} هـ "

    } else return ""
}

