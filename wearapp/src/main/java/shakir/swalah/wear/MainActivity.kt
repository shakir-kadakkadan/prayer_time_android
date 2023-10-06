package shakir.swalah.wear

/*import androidx.recyclerview.widget.RecyclerView*/

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationRequest
import android.os.*
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible

import androidx.fragment.app.FragmentActivity
import com.azan.TimeCalculator
import com.azan.types.AngleCalculationType
import com.azan.types.PrayersType
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import shakir.swalah.R
import shakir.swalah.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*


val INVALID_CORDINATE = Double.MAX_VALUE


class MainActivity : FragmentActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        getSharedPreferences("APP", MODE_PRIVATE)?.let {
            val lat = it.getString("lat", null)
            val longgg = it.getString("long", null)
            if (lat != null && longgg != null)
                onLocationRefresh(lat.toDouble(), longgg.toDouble())

        }

    }


    fun onLocationRefresh(lat: Double, longgg: Double) {


        onGetCordinates(lat, longgg, "")
    }

    override fun onResume() {
        super.onResume()
        refreshLocations()
    }


    var permissionCount = 2
    fun refreshLocations() {

        if (true)
            try {
                if ((--permissionCount) > 0 && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 15)
                    return
                }
                LocationServices.getFusedLocationProviderClient(this)
                    .getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, CancellationTokenSource().token)
                    .addOnSuccessListener { location ->

                        if (location?.latitude != null) {

                            getSharedPreferences("APP", MODE_PRIVATE)?.edit()?.let {
                                it.putString("lat", location.latitude.toString())
                                it.putString("long", location.longitude.toString())
                            }?.apply()

                            onLocationRefresh(location.latitude!!, location.longitude)
                        }

                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 15) {
            refreshLocations()
        }
    }


    fun onGetCordinates(
        latitude: Double,
        longitude: Double,
        l: String? = "",
        cancelPrevisousPendingInten: Boolean = false
    ) {

        if (::binding.isInitialized) {

            binding.progressBar.isVisible = false

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

            val timeFormat = "hh:mm"

            arrayOf(
                (binding.FAJR),
                (binding.SUNRISE),
                (binding.ZUHR),
                (binding.ASR),
                (binding.MAGHRIB),
                (binding.ISHA),
            ).forEachIndexed { index, view ->
                view.prayerName.setText(getArabicNames(array[index].name))
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

            //  Util.setNextAlarm(this)

            println("$latitude $longitude $l")

        }

    }


    fun getArabicNames(string: String?): String? {
        return when (string?.let { PrayersType.valueOf(it) }) {
            PrayersType.FAJR -> "الفجر"
            PrayersType.SUNRISE -> "الشروق"
            PrayersType.ZUHR -> "الظهر"
            PrayersType.ASR -> "العصر"
            PrayersType.MAGHRIB -> "المغرب"
            PrayersType.ISHA -> "العشاء"
            else -> null
        }
    }


    val LTR_EMBED = "\u202A"

    public fun String.ltrEmbed(): String {
        return LTR_EMBED + this + LTR_EMBED
    }


}

