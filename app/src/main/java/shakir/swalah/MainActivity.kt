package shakir.swalah

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.azan.TimeCalculator
import com.azan.types.AngleCalculationType
import com.azan.types.PrayersType
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.prayer_time_ll.*
import kotlinx.android.synthetic.main.pt_layout.view.*
import shakir.swalah.db.GeoCoded
import shakir.swalah.models.Cord
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class MainActivity : MainActivityLocation() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onStart() {
        super.onStart()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }


    override fun onLocationServiceResult(location: Location) {
        thread {
            val latitude = location.latitude
            val longitude = location.longitude
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? = try {
                geocoder.getFromLocation(latitude, longitude, 1)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            var locality = addresses?.getOrNull(0)?.locality ?: ""
            var subLocality = addresses?.getOrNull(0)?.subLocality ?: ""
            var countryName = addresses?.getOrNull(0)?.countryName ?: ""
            var isNear = false

            try {
                val bundle = Bundle()
                bundle.putString("LocationType", "LocationService")
                bundle.putDouble("Latitude", latitude)
                bundle.putDouble("Longitude", longitude)
                bundle.putString("Locality", locality)
                bundle.putString("SubLocality", subLocality)
                bundle.putString("Country", countryName)
                firebaseAnalytics.logEvent("Location", bundle)
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }

            if (locality.isNotBlank()) {
                appDatabase.GeoCodedDao().insertAll(
                    GeoCoded(
                        latitude = latitude,
                        longitude = longitude,
                        locality = locality,
                        subLocality = subLocality,
                        countryName = countryName
                    )
                )
            } else {
                var nearest: GeoCoded? = null
                var nearestMeter: Float? = null
                val floatArray = FloatArray(5)
                val km5 = (5 * 1000).toFloat()
                appDatabase.GeoCodedDao().getAll().forEach {
                    Location.distanceBetween(
                        latitude, longitude, it.latitude, it.longitude, floatArray
                    )
                    if (floatArray[0] < km5 && (nearestMeter == null || floatArray[0] < nearestMeter!!)) {
                        nearestMeter = floatArray[0]
                        nearest = it
                    }

                }

                if (nearest != null) {
                    locality = nearest?.locality ?: ""
                    subLocality = nearest?.subLocality ?: ""
                    countryName = nearest?.countryName ?: ""
                    isNear = true
                } else {

                }




            }


            getSharedPreferences("sp", Context.MODE_PRIVATE)
                .edit()
                .putDouble("lattt", latitude)
                .putDouble("longgg", longitude)
                .putString("location", if (locality.isBlank()) "My Location" else locality)
                .putBoolean("isLocationSet", true)
                .commit()
            runOnUiThread {
                onGetCordinates(latitude, longitude, locality, true)
                isRotationNeed = false
                Toast.makeText(
                    this,
                    "${if (locality.isBlank()) "Unknown Location.\nPlease Check Your Network Settings & Location" else {
                        if (isNear) "Near $locality" else locality
                    }}, $subLocality, $countryName\n$latitude,  $longitude",
                    Toast.LENGTH_LONG
                ).show()
            }


        }
    }

    //@thread


    //Lat range = -90 to +90
    //LOng range = -180 to +180
    val INVALID_CORDINATE = Double.MAX_VALUE

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

        val sharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE)
        val lattt = sharedPreferences.getDouble("lattt", INVALID_CORDINATE)
        val longgg = sharedPreferences.getDouble("longgg", INVALID_CORDINATE)
        var location = sharedPreferences.getString("location", null)



        if (lattt == INVALID_CORDINATE || longgg == INVALID_CORDINATE/*FIRST TIME*/) {
            getGioIpDb()
        } else if (location.isNullOrBlank()) {
            requestForGPSLocation()
        } else {
            onGetCordinates(lattt, longgg, location, true)
        }

        if (arrayList.size == 0) {
            getLocationFromCSV()
        }

        set.setOnClickListener {

            /*Crashlytics.getInstance().crash()*/
            /*  if (true) {
                  getSharedPreferences("sp", Context.MODE_PRIVATE)
                      .edit().clear().commit()
                  finish()
                  startActivity(intent)
                  return@setOnClickListener
              }*/


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
            getSharedPreferences("sp", Context.MODE_PRIVATE)
                .edit()
                .putDouble("lattt", latitude)
                .putDouble("longgg", longitude)
                .putString("location", locality)
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
            sharedPreferences.edit().putInt("locationTV_VISIBILITY", locationLL.visibility)
                .apply()
        }

        refresh.setOnClickListener {
            requestForGPSLocation()
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
            sharedPreferences.edit().putInt("locationTV_VISIBILITY", locationLL.visibility)
                .apply()
            try {
                firebaseAnalytics.logEvent(
                    "click",
                    Bundle().apply { putString("click", "locationLL") })
            } catch (e: Exception) {

            }
        }
        if (sharedPreferences.getInt("locationTV_VISIBILITY", View.VISIBLE) == View.VISIBLE) {
            locationLL.visibility = View.VISIBLE
            locationSelector.visibility = View.GONE
            LL_close_refresh.visibility = View.GONE
        } else {
            locationLL.visibility = View.GONE
            locationSelector.visibility = View.VISIBLE
            LL_close_refresh.visibility = View.VISIBLE
        }


    }


    fun onGetCordinates(
        lattt: Double,
        longgg: Double,
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
            TimeCalculator().date(date).location(lattt, longgg, 0.0, 0.0)
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

        Util.setNextAlarm(this, true)
        locationAC.setText("")
        locationAC.setHint(l)
        locationTV.setText(l)
        latEditText.setText(lattt.toString())
        longEditText.setText(longgg.toString())
        println("$lattt $longgg $l")

    }


    val TAG = "MainActivityAlarm"
    /*  private fun setAlarm(
          prayTime: Date,
          name: String?,
          arabicNames: String?
      ) {
          Log.d(TAG, "setAlarm() called with: prayTime = [" + prayTime + "]");

          val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

          // Alarm type
          val alarmType = AlarmManager.RTC

          val uniqueIndexForParayer = Util.getUniqueIndexForParayer(name)
          val broadcastIntent = Intent(this, AlarmBroadCastReceiver::class.java).apply {
              setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
              putExtra("milli", prayTime.time)
              putExtra("name", arabicNames)
              putExtra("index", uniqueIndexForParayer)
          }


          *//* broadcastIntent.putExtras(
             Intent(this, MainActivity::class.java)
                 .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                 .putExtra("milli", prayTime.time)
                 .putExtra("name", name)
         )*//*


        val pendingAlarmIntent = PendingIntent.getBroadcast(
            this,
            uniqueIndexForParayer, broadcastIntent,
            PendingIntent.FLAG_ONE_SHOT
        )


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(alarmType, prayTime.time, pendingAlarmIntent)
        } else {
            alarmManager.set(alarmType, prayTime.time, pendingAlarmIntent)
        }


    }
*/

    /*  @SuppressLint("CheckResult")
      fun getIPStackCordinates() {

          AppApplication.restService.ipStack()
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(
                  {
                      try {
                          location.setText(it?.city)
                          it.latitude?.let { it1 -> it.longitude?.let { it2 -> onGetCordinates(it1, it2) } }
                      } catch (e: Exception) {
                          e.printStackTrace()
                      }
                  },
                  {
                      it.printStackTrace()
                  }
              )

      }*/


    private fun animateRotate() {
        if (isRotationNeed) {
            ObjectAnimator
                .ofFloat(refresh, View.ROTATION, 0f, 360f)
                .setDuration(600)
                .apply {
                    repeatCount = 100
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                            if (!isRotationNeed) {
                                this@apply.cancel()
                            }
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            if (!isRotationNeed) {
                                this@apply.cancel()
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {

                        }

                    })
                }

                .start()



            ObjectAnimator
                .ofFloat(locationButton, View.ROTATION, 0f, 360f)
                .setDuration(600)
                .apply {
                    repeatCount = 100
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                            if (!isRotationNeed) {
                                this@apply.cancel()
                            }
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            if (!isRotationNeed) {
                                this@apply.cancel()
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {

                        }

                    })
                }

                .start()


        }

    }

    var isRotationNeed = false

    @SuppressLint("CheckResult")
    fun getGioIpDb() {
        isRotationNeed = true
        animateRotate()
        AppApplication.restService.gioIpDB()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    try {

                        it.latitude?.let { latitude ->
                            it.longitude?.let { longitude ->
                                getSharedPreferences("sp", Context.MODE_PRIVATE)
                                    .edit()
                                    .putDouble("lattt", latitude)
                                    .putDouble("longgg", longitude)
                                    .putString("location", it?.city)
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
                    requestForGPSLocation()
                },
                {
                    it.printStackTrace()

                    val sharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE)
                    val lattt = sharedPreferences.getDouble("lattt", INVALID_CORDINATE)
                    val longgg = sharedPreferences.getDouble("longgg", INVALID_CORDINATE)
                    val location = sharedPreferences.getString("location", null)
                    if (lattt == INVALID_CORDINATE && longgg == INVALID_CORDINATE) {
                        onGetCordinates(11.0, 76.0, "Kerala", true)
                    } else {
                        onGetCordinates(lattt, longgg, location, true)
                    }


                    requestForGPSLocation()
                }
            )

    }

    fun requestForGPSLocation() {
        hideKeyboardView()
        isRotationNeed = true
        animateRotate()
        startLocationServiceInitialisation()
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

                getSharedPreferences("sp", Context.MODE_PRIVATE)
                    .edit()
                    .putDouble("lattt", get.latitude)
                    .putDouble("longgg", get.longitude)
                    .putString("location", get.name)
                    .putBoolean("isLocationSet", true)
                    .apply()
                onGetCordinates(get.latitude, get.longitude, get.name, true)

            }
        }


    }


    /*fun testAudio(context: Context) {
        if (BuildConfig.DEBUG) {
            val broadcastIntent = Intent(context, AlarmBroadCastReceiver::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("milli", System.currentTimeMillis())
                putExtra("name", "test" + System.currentTimeMillis())
                putExtra("index", System.currentTimeMillis())
            }


            val pendingAlarmIntent = PendingIntent.getBroadcast(
                context,
                (System.currentTimeMillis()).toInt(), broadcastIntent,
                PendingIntent.FLAG_ONE_SHOT
            )


            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Alarm type
            val alarmType = AlarmManager.RTC

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    alarmType,
                    (System.currentTimeMillis()),
                    pendingAlarmIntent
                )
            } else {
                alarmManager.set(
                    alarmType,
                    (System.currentTimeMillis()),
                    pendingAlarmIntent
                )
            }

        }

    }
*/

}
