package shakir.swalah

/*import androidx.recyclerview.widget.RecyclerView*/

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.azan.TimeCalculator
import com.azan.types.AngleCalculationType
import com.azan.types.PrayersType
import com.crashlytics.android.Crashlytics
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.prayer_time_ll.*
import kotlinx.android.synthetic.main.pt_layout.view.*
import shakir.swalah.models.Cord
import java.text.SimpleDateFormat
import java.util.*


val INVALID_CORDINATE = Double.MAX_VALUE


class MainActivity : MainActivityLocation() {

    override fun onLocationCallBack(
        location: Location,
        locality: String,
        isNear: Boolean,
        subLocality: String,
        countryName: String
    ) {
        with(location) {
            onGetCordinates(latitude, longitude, locality, true)


            val s =
                "${if (locality.isBlank()) "Unknown Location.\nPlease Check Your Network Settings & Location" else {
                    if (isNear) "Near $locality" else locality
                }}, $subLocality, $countryName\n$latitude,  $longitude"
            if (toastText.contains(
                    "Unknown Location",
                    true
                ) && s.contains("Unknown Location") && toast != null
            ) {
                //repeated toast
            } else {
                try {
                    toast?.cancel()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                toastText = s
                toast = Toast.makeText(
                    this@MainActivity,
                    toastText,
                    Toast.LENGTH_LONG
                )
                toast?.show()
            }
        }


    }


    var toast: Toast? = null
    var toastText: String = ""

    //@thread


    //Lat range = -90 to +90
    //LOng range = -180 to +180


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adjustWithSystemWindow(rootViewLL, topSpacer, true)
        val receiver = ComponentName(applicationContext, BootCompleteReceiver::class.java)
        applicationContext.packageManager?.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )



        if (arrayList.size == 0) {
            getLocationFromCSV()
        }

        set.setOnClickListener {
            it.hideKeyboardView()
            val latitude = latEditText.text.toString().toDoubleOrNull()
            val longitude = longEditText.text.toString().toDoubleOrNull()
            var locality = locationAC.text.toString()
            if (latitude == null || latitude < -90 || latitude > 90) {
                latEditText.setText("")
                toast("Invalid Latitude")
                return@setOnClickListener
            }
            if (longitude == null || longitude < -180 || longitude > 180) {
                longEditText.setText("")
                toast("Invalid Longitude")
                return@setOnClickListener
            }
            if (locality.isNullOrBlank()) locality = "Custom Location"
            sp.edit()
                .putDouble("latitude", latitude)
                .putDouble("longitude", longitude)
                .putString("locality", locality)
                .putBoolean("isLocationSet", true)
                .apply()
            onGetCordinates(
                latitude,
                longEditText.text.toString().toDouble(),
                locality,
                true
            )

            try {
                val bundle = Bundle()
                bundle.putString("LocationType", "SetFromEditText")
                bundle.putDouble("Latitude", latitude)
                bundle.putDouble("Longitude", longitude)
                bundle.putString("Locality", locality)
                firebaseAnalytics.logEvent("Location", bundle)
                firebaseAnalytics.logEvent(
                    "click",
                    Bundle().apply { putString("click", "set") })
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }
        }
        close.setOnClickListener {
            try {
                firebaseAnalytics.logEvent(
                    "click",
                    Bundle().apply { putString("click", "close") })
            } catch (e: Exception) {

            }
            it.hideKeyboardView()
            LL_close_refresh.visibility = View.GONE
            locationSelector.visibility = View.GONE
            locationLL.visibility = View.VISIBLE
            sp.edit().putInt("locationTV_VISIBILITY", locationLL.visibility)
                .apply()
        }

        refresh.setOnClickListener {
            requestForGPSLocationWithRotationAnimation()
            try {
                firebaseAnalytics.logEvent(
                    "click",
                    Bundle().apply { putString("click", "refresh") })
            } catch (e: Exception) {

            }
        }

        locationLL.setOnClickListener {
            locationSelector.visibility = View.VISIBLE
            locationLL.visibility = View.GONE
            LL_close_refresh.visibility = View.VISIBLE
            sp.edit().putInt("locationTV_VISIBILITY", locationLL.visibility)
                .apply()
            try {
                firebaseAnalytics.logEvent(
                    "click",
                    Bundle().apply { putString("click", "locationLL") })
            } catch (e: Exception) {

            }
        }

        //qibla.isVisible = isMyTestDevice()
        qibla.setOnClickListener {
            startActivity(Intent(this, QiblaActivity::class.java))
        }


        generateTone.setOnClickListener {
            startActivity(Intent(this, GenerateToneActivity::class.java))
        }


    }


    override fun onResume() {
        super.onResume()
        val latitude = sp.getDouble("latitude", INVALID_CORDINATE)
        val longitude = sp.getDouble("longitude", INVALID_CORDINATE)
        var locality = sp.getString("locality", null)

        if (latitude == INVALID_CORDINATE || longitude == INVALID_CORDINATE/*FIRST TIME*/) {
            Log.d("hgdhag", "FIRST TIME")
            getGioIpDb()
        } else if (locality.isNullOrBlank()) {
            Log.d("hgdhag", "2nd TIME LOCATION BLANK")
            onGetCordinates(latitude, longitude, locality, true)
            requestLocationRepeatLoop()
        } else {
            Log.d("hgdhag", "2nd TIME LOCATION HAS VALUE $locality")
            onGetCordinates(latitude, longitude, locality, true)
        }

        if (sp.getInt("locationTV_VISIBILITY", View.VISIBLE) == View.VISIBLE) {
            locationLL.visibility = View.VISIBLE
            locationSelector.visibility = View.GONE
            LL_close_refresh.visibility = View.GONE
        } else {
            locationLL.visibility = View.GONE
            locationSelector.visibility = View.VISIBLE
            LL_close_refresh.visibility = View.VISIBLE
        }

        optimization()
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
        val prayerTimes =
            TimeCalculator().date(date).location(latitude, longitude, 0.0, 0.0)
                .timeCalculationMethod(AngleCalculationType.KARACHI)
                .calculateTimes()


        var nextPrayTime: PrayersType? = null


        arrayOf(FAJR, SUNRISE, ZUHR, ASR, MAGHRIB, ISHA).forEachIndexed { index, view ->
            view.prayerName.setText(AppApplication.getArabicNames(array[index].name))
            val prayTime = prayerTimes.getPrayTime(array[index])
            view.prayerTime.text =
                SimpleDateFormat("HH:mm", Locale.ENGLISH).format(prayTime)

            if (prayTime.time >= System.currentTimeMillis() && nextPrayTime == null) {
                nextPrayTime = array[index]
            }

            view.setOnClickListener {
                /* testAudio(this)*/
            }


        }

        Util.setNextAlarm(this)
        locationAC.setText("")
        locationAC.setHint(if (l.isNullOrBlank()) "My Location" else l)
        locationTV.setText(if (l.isNullOrBlank()) "My Location" else l)
        latEditText.setText(latitude.toString())
        longEditText.setText(longitude.toString())
        println("$latitude $longitude $l")

    }

    @SuppressLint("CheckResult")
    fun getGioIpDb() {
        isIconRotationNeed = true
        animateRotate()
        AppApplication.restService.gioIpDB()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    try {

                        it.latitude?.let { latitude ->
                            it.longitude?.let { longitude ->
                                sp.edit()
                                    .putDouble("latitude", latitude)
                                    .putDouble("longitude", longitude)
                                    .putString("locality", it?.city)
                                    .putBoolean("isLocationSet", false)
                                    .apply()
                                onGetCordinates(latitude, longitude, it.city, true)
                                try {
                                    val bundle = Bundle()
                                    bundle.putString("LocationType", "IPLocation")
                                    bundle.putDouble("Latitude", latitude)
                                    bundle.putDouble("Longitude", longitude)
                                    bundle.putString("Locality", it?.city)
                                    firebaseAnalytics.logEvent("Location", bundle)
                                } catch (e: Exception) {
                                    Crashlytics.logException(e)
                                }

                            }


                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    requestForGPSLocationWithRotationAnimation()
                },
                {
                    it.printStackTrace()


                    val latitude = sp.getDouble("latitude", INVALID_CORDINATE)
                    val longitude = sp.getDouble("longitude", INVALID_CORDINATE)
                    val location = sp.getString("locality", null)
                    if (latitude == INVALID_CORDINATE && longitude == INVALID_CORDINATE) {
                        onGetCordinates(11.0, 76.0, "Kerala", true)
                    } else {
                        onGetCordinates(latitude, longitude, location, true)
                    }


                    requestForGPSLocationWithRotationAnimation()
                }
            )

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
        locationAC.setAdapter(
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
        }
    }


    fun optimization() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID))
                startActivity(with(Intent()) {
                    action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    setData(Uri.parse("package:${BuildConfig.APPLICATION_ID}"))
                })
        }
    }




}
