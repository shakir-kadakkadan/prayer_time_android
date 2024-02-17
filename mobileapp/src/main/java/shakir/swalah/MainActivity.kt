package shakir.swalah

/*import androidx.recyclerview.widget.RecyclerView*/


import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import shakir.swalah.databinding.ActivityMainBinding
import shakir.swalah.models.Cord
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale


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


    override fun onResume() {
        super.onResume()





        if (sp.getBoolean("v2_set", false) == true) {
            onGetCordinates(
                sp.getDouble("v2_latitude", 0.0),
                sp.getDouble("v2_longitude", 0.0),
                sp.getString("v2_locality", ""),
            )

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

        } else {
            startActivity(Intent(this, LocationSelectorAvtivity::class.java).apply {
                putExtra("comeBack", true)
            })
            finish()
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
    }


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
        val prayerTimes =
            TimeCalculator().date(date).location(latitude, longitude, 0.0, 0.0)
                .timeCalculationMethod(AngleCalculationType.KARACHI)
                .calculateTimes()

        val prayerTimesYest =
            TimeCalculator().date(dateYest).location(latitude, longitude, 0.0, 0.0)
                .timeCalculationMethod(AngleCalculationType.KARACHI)
                .calculateTimes()


        var nextPrayTime: PrayersType? = null

        val timeFormat = Util.timeFormat()

        binding.prayerTimeLl.FAJR

        arrayOf(binding.prayerTimeLl.FAJR, binding.prayerTimeLl.SUNRISE, binding.prayerTimeLl.ZUHR, binding.prayerTimeLl.ASR, binding.prayerTimeLl.MAGHRIB, binding.prayerTimeLl.ISHA).forEachIndexed { index, view ->
            view.prayerName.setText(AppApplication.getArabicNames(array[index].name))
            val prayTime = prayerTimes.getPrayTime(array[index])
            view.prayerTime.text =
                SimpleDateFormat(timeFormat, Locale.ENGLISH).format(prayTime).ltrEmbed()

            if (prayTime.time >= System.currentTimeMillis() && nextPrayTime == null) {
                nextPrayTime = array[index]
            }

            view.root.setOnClickListener {
                /* testAudio(this)*/
            }


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

