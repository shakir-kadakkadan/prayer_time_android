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
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.ConfigurationCompat
import androidx.core.view.isVisible
import com.azan.Azan
import com.azan.astrologicalCalc.Location
import com.azan.astrologicalCalc.SimpleDate
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import shakir.swalah.Util.isiqamaAlarmOn
import shakir.swalah.databinding.ActivityMainBinding
import shakir.swalah.models.Cord
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
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
        showWhenLockedAndTurnScreenOn()
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

    private fun showWhenLockedAndTurnScreenOn() {
        val win = window
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        win.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
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


            val redMinuteAdhan = if (currentPrayTime!!.second == 1) 10L else 20L

            if (currentIqamaMilli != null && System.currentTimeMillis() > currentIqamaMilli!! && System.currentTimeMillis() <= (currentIqamaMilli!! + TimeUnit.MINUTES.toMillis(10))) {
                countdownColor = Color.RED
                countdownDestinationMilii = currentIqamaMilli!!
                countdownNameString = AppApplication.getArabicNames(currentPrayTime!!.second) + " (الإقامة) "
            } else if (currentIqamaMilli != null && System.currentTimeMillis() <= currentIqamaMilli!!) {
                countdownColor = Color.GREEN
                countdownDestinationMilii = currentIqamaMilli
                countdownNameString = AppApplication.getArabicNames(currentPrayTime!!.second) + " (الإقامة) "
            } else if (currentPrayTime != null && System.currentTimeMillis() <= currentPrayTime!!.first.time + TimeUnit.MINUTES.toMillis(redMinuteAdhan)) {
                countdownColor = Color.RED
                countdownDestinationMilii = currentPrayTime!!.first.time
                countdownNameString = AppApplication.getArabicNames(currentPrayTime!!.second)
            } else {
                countdownColor = Color.WHITE
                countdownDestinationMilii = nextPrayTime!!.first.time
                countdownNameString = AppApplication.getArabicNames(nextPrayTime!!.second)
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
        updator()

        try {
            ringtone?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    var updateDialog: AlertDialog? = null
    fun updator() {
        thread {
            try {
                val version_last_checked = sp.getLong("version_last_checked", 0L)
                if (System.currentTimeMillis() - version_last_checked > TimeUnit.DAYS.toMillis(1)) {


                    var apiVersion = sendGetRequest("https://install4-default-rtdb.asia-southeast1.firebasedatabase.app/adhan_app_version.json")?.replace("\"", "")?.toIntOrNull() ?: 0
                    println("apiVersion $apiVersion")

                    sp.edit {
                        putLong("version_last_checked", System.currentTimeMillis())
                    }

                    if (apiVersion > BuildConfig.VERSION_CODE) {
                        try {
                            runOnUiThread {
                                try {
                                    val version_shown_time = sp.getLong("version_shown_time", 0)
                                    val version_shown = sp.getInt("version_shown", 0)
                                    if (System.currentTimeMillis() - version_shown_time >= TimeUnit.DAYS.toMillis(1) || version_shown != apiVersion) {
                                        if (updateDialog == null)
                                            updateDialog = AlertDialog.Builder(this/*, R.style.MyAlertDialogTheme*/)
                                                .setTitle("Update")
                                                .setMessage("Update Available: A new version of the app is now available. Please update to enjoy the latest features and improvements.")
                                                .setPositiveButton("Update") { dialog, which ->
                                                    dialog.dismiss()
                                                    val url = "market://details?id=shakir.swalah"
                                                    val intent = Intent(Intent.ACTION_VIEW)
                                                    intent.data = Uri.parse(url)
                                                    startActivity(intent)

                                                }
                                                .setNegativeButton("Later") { dialog, which ->
                                                    dialog.dismiss()
                                                    sp.edit {
                                                        putInt("version_shown", apiVersion)
                                                        putLong("version_shown_time", System.currentTimeMillis())
                                                    }
                                                }
                                                .create()
                                        updateDialog?.show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                println("apiVersion $e")
                e.printStackTrace()
            }
        }
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

        downloadSounds()
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


        val today = SimpleDate(GregorianCalendar())
        val dateYest = SimpleDate(GregorianCalendar().apply {
            add(Calendar.DATE, -1)
        })
        val dateTomorrow = SimpleDate(GregorianCalendar().apply {
            add(Calendar.DATE, 1)
        })
        val azan = getAthanObj(latitude, longitude)
        val prayerTimes = azan.getAthanOfDate(today)
        val prayerTimesYest = azan.getAthanOfDate(dateYest)
        val prayerTimesTomorrow = azan.getAthanOfDate(dateTomorrow)

        nextPrayTime = null
        arrayListOf(
            (0..5).map { prayerTimesYest[it] to it },
            (0..5).map { prayerTimes[it] to it },
            (0..5).map { prayerTimesTomorrow[it] to it },
        ).flatMap { it }.forEach {
            println("nextPrayTimenextPrayTime ${it.first} ${it.second}")
            if (nextPrayTime == null && it.first.after(Date())) {
                nextPrayTime = it
            }
            if (it.first.before(Date())) {
                currentPrayTime = it
            }
        }


        val timeFormat = Util.timeFormat()

        arrayOf(binding.prayerTimeLl.FAJR, binding.prayerTimeLl.SUNRISE, binding.prayerTimeLl.ZUHR, binding.prayerTimeLl.ASR, binding.prayerTimeLl.MAGHRIB, binding.prayerTimeLl.ISHA).forEachIndexed { index, view ->
            view.prayerName.setText(AppApplication.getArabicNames(index))
            val prayTime = prayerTimes[index]
            view.prayerTime.text =
                SimpleDateFormat(timeFormat, Locale.ENGLISH).format(prayTime).ltrEmbed()
        }




        if (sp.getInt("showMidNight", 0) == 1) {
            binding.prayerTimeLl.MIDNIGHT.prayerName.setText("Mid night")
            binding.prayerTimeLl.MIDNIGHT.prayerTime.setText(SimpleDateFormat(timeFormat, Locale.ENGLISH).format(Date(prayerTimes[0].time.plus(prayerTimesYest[4].time).div(2))).ltrEmbed())
            binding.prayerTimeLl.MIDNIGHT.root.isVisible = true
        } else {
            binding.prayerTimeLl.MIDNIGHT.root.isVisible = false
        }

        if (sp.getInt("showThirdNight", 0) == 0) {
            binding.prayerTimeLl.THIRDNIGHTISHA.root.isVisible = true
            binding.prayerTimeLl.THIRDNIGHTISHA.prayerName.setText("Third night")
            binding.prayerTimeLl.THIRDNIGHTISHA.prayerTime.setText(
                SimpleDateFormat(timeFormat, Locale.ENGLISH).format(
                    Date(
                        prayerTimes[0].time.minus(
                            prayerTimes[0].time.minus(prayerTimesYest[5].time).div(3)
                        )
                    )
                ).ltrEmbed()
            )
        } else {
            binding.prayerTimeLl.THIRDNIGHTISHA.root.isVisible = false
        }

        if (sp.getInt("showThirdNight", 0) == 1) {
            binding.prayerTimeLl.THIRDNIGHT.prayerName.setText("Third night")
            binding.prayerTimeLl.THIRDNIGHT.root.isVisible = true
            binding.prayerTimeLl.THIRDNIGHT.prayerTime.setText(
                SimpleDateFormat(timeFormat, Locale.ENGLISH).format(
                    Date(
                        prayerTimes[0].time.minus(
                            prayerTimes[0].time.minus(prayerTimesYest[4].time).div(3)
                        )
                    )
                ).ltrEmbed()
            )
        } else {
            binding.prayerTimeLl.THIRDNIGHT.root.isVisible = false
        }











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
                                    try {
                                        dialog.dismiss()
                                        startActivity(with(Intent()) {
                                            action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                            setData(Uri.parse("package:${BuildConfig.APPLICATION_ID}"))
                                        })
                                    } catch (e: Exception) {
                                      e.report()
                                        toast(e.message)
                                    }

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


fun sendGetRequest(urlString: String): String? {
    val connection: HttpURLConnection?
    var response: String? = null

    try {
        // Create URL object
        val url = URL(urlString)

        // Open connection
        connection = url.openConnection() as HttpURLConnection

        // Set request method
        connection.requestMethod = "GET"

        // Connect to the server
        connection.connect()

        // Read the response
        val inputStream = connection.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        response = reader.readText()
        reader.close()
        connection.disconnect()

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return response
}

fun downloadFileGET(urlString: String, file: File) {
    val connection: HttpURLConnection?

    try {
        // Create URL object
        val url = URL(urlString)

        // Open connection
        connection = url.openConnection() as HttpURLConnection

        // Set request method
        connection.requestMethod = "GET"

        // Connect to the server
        connection.connect()

        // Read the binary data
        val inputStream = BufferedInputStream(connection.inputStream)
        val fileOutputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            fileOutputStream.write(buffer, 0, bytesRead)
        }
        fileOutputStream.close()
        inputStream.close()
        connection.disconnect()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}




fun getAthanObj(latitude: Double, longitude: Double): Azan {
    val defaultTimeZone: TimeZone = TimeZone.getDefault()
    var dstOffsetInHours: Int = 0
    dstOffsetInHours = if (defaultTimeZone.useDaylightTime()) {
        val dstOffsetInMillis = defaultTimeZone.dstSavings
        TimeUnit.MILLISECONDS.toHours(dstOffsetInMillis.toLong()).toInt()
    } else {
        0
    }
    val sp = Util.getMySharedPreference(AppApplication.instance)
    var tm = sp.getInt("timeMethods", 0)


    val gmtOffsetInMillis = defaultTimeZone.getRawOffset() / (3600000.0)
    val calendar = Calendar.getInstance(defaultTimeZone)
    //val isDST = defaultTimeZone.inDaylightTime(calendar.getTime())
    val location = Location(latitude, longitude, gmtOffsetInMillis, dstOffsetInHours)
    val azan = Azan(location, timeMethods[tm].first)
    return azan
}


fun Azan.getAthanOfDate(today: SimpleDate): List<Date> {
    return getPrayerTimes(today).times.mapIndexed { index, it ->
        val adj = AppApplication.sp.getInt("adjustment_$index", 0)
        Date(Date(today.year - 1900, today.month - 1, today.day, it.hour, it.minute, it.second).time + (adj * 60000))
    }
}







