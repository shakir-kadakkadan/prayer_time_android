package shakir.swalah

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEachIndexed
import com.azan.TimeCalculator
import com.azan.types.AngleCalculationType
import com.azan.types.PrayersType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.pt_layout.view.*
import shakir.swalah.models.Cord
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    //Lat range = -90 to +90
    //LOng range = -180 to +180
    val INVALID_CORDINATE = Double.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val receiver = ComponentName(applicationContext, BootCompleteReceiver::class.java)
        applicationContext.packageManager?.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val sharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE)
        val lattt = sharedPreferences.getDouble("lattt", INVALID_CORDINATE)
        val longgg = sharedPreferences.getDouble("longgg", INVALID_CORDINATE)
        val location = sharedPreferences.getString("location", null)



        if (lattt == INVALID_CORDINATE || longgg == INVALID_CORDINATE) {
            getGioIpDb()
        } else {
            onGetCordinates(lattt, longgg, location, false)
        }

        if (arrayList.size == 0) {
            getLocationFromCSV()
        }

        set.setOnClickListener {


            getSharedPreferences("sp", Context.MODE_PRIVATE)
                .edit()
                .putDouble("lattt", lattt)
                .putDouble("longgg", longgg)
                .putString("location", "Select City")
                .apply()
            onGetCordinates(
                latEditText.text.toString().toDouble(),
                longEditText.text.toString().toDouble(),
                "Custom Location",
                true
            )
        }
        close.setOnClickListener {
            LL_close_refresh.visibility = View.GONE
            locationSelector.visibility = View.GONE
            locationTV.visibility = View.VISIBLE
            sharedPreferences.edit().putInt("locationTV_VISIBILITY", locationTV.visibility).apply()
        }

        refresh.setOnClickListener {
            getGioIpDb()
        }

        locationTV.setOnClickListener {
            locationSelector.visibility = View.VISIBLE
            locationTV.visibility = View.GONE
            LL_close_refresh.visibility = View.VISIBLE
            sharedPreferences.edit().putInt("locationTV_VISIBILITY", locationTV.visibility).apply()
        }
        if (sharedPreferences.getInt("locationTV_VISIBILITY", View.GONE) == View.VISIBLE) {
            locationTV.visibility = View.VISIBLE
            locationSelector.visibility = View.GONE
            LL_close_refresh.visibility = View.GONE
        } else {
            locationTV.visibility = View.GONE
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

        if (cancelPrevisousPendingInten)
            Util.cancelLastPendingIntent(this)
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
        prayerTimeLL.forEachIndexed { index, view ->
            view.prayerName.setText(AppApplication.getArabicNames(array[index].name))
            val prayTime = prayerTimes.getPrayTime(array[index])
            view.prayerTime.text =
                SimpleDateFormat("HH:mm", Locale.ENGLISH).format(prayTime)

            if (prayTime.time >= System.currentTimeMillis() && nextPrayTime == null) {
                nextPrayTime = array[index]
            }

            view.setOnClickListener {
                testAudio(this)
            }


        }

        Util.setNextAlarm(this)
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


    @SuppressLint("CheckResult")
    fun getGioIpDb() {

        AppApplication.restService.gioIpDB()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    try {

                        it.latitude?.let { lattt ->
                            it.longitude?.let { longgg ->
                                getSharedPreferences("sp", Context.MODE_PRIVATE)
                                    .edit()
                                    .putDouble("lattt", lattt)
                                    .putDouble("longgg", longgg)
                                    .putString("location", it?.city)
                                    .apply()
                                onGetCordinates(lattt, longgg, it.city, true)

                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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

                getSharedPreferences("sp", Context.MODE_PRIVATE)
                    .edit()
                    .putDouble("lattt", get.latitude)
                    .putDouble("longgg", get.longitude)
                    .putString("location", get.name)
                    .apply()
                onGetCordinates(get.latitude, get.longitude, get.name, true)

            }
        }


    }


    fun testAudio(context: Context) {
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
