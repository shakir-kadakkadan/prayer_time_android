package shakir.swalah

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE)
        val lattt = sharedPreferences.getDouble("lattt", 0.0)
        val longgg = sharedPreferences.getDouble("longgg", 0.0)
        val location = sharedPreferences.getString("location", null)



        if (lattt == 0.0 && longgg == 0.0) {
            getGioIpDb()
        } else {
            onGetCordinates(lattt, longgg, location)
        }

        if (arrayList.size == 0) {
            getLocationFromCSV()
        }

        set.setOnClickListener {
            onGetCordinates(
                latEditText.text.toString().toDouble(),
                longEditText.text.toString().toDouble(),
                "Custom Location"
            )
        }
        close.setOnClickListener {
            locationSelector.visibility = View.GONE
            locationTV.visibility = View.VISIBLE
        }

        refresh.setOnClickListener {
            getGioIpDb()
        }

        locationTV.setOnClickListener {
            locationSelector.visibility = View.VISIBLE
            locationTV.visibility = View.GONE
        }


    }





    fun onGetCordinates(lattt: Double, longgg: Double, l: String? = "") {
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

        prayerTimeLL.forEachIndexed { index, view ->
            view.prayerName.setText(array[index].name)
            val prayTime = prayerTimes.getPrayTime(array[index])
            view.prayerTime.text =
                SimpleDateFormat("HH:mm", Locale.ENGLISH).format(prayTime)

            setAlarm(prayTime)
        }
        locationAC.setText("")
        locationAC.setHint(l)
        locationTV.setText(l)
        latEditText.setText(lattt.toString())
        longEditText.setText(longgg.toString())
        println("$lattt $longgg $l")

    }


    private fun setAlarm(prayTime: Date) {

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Alarm type
        val alarmType = AlarmManager.RTC


        val broadcastIntent = Intent(this, AlarmBroadCastReceiver::class.java)



        broadcastIntent.putExtras(
            Intent(this, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra("milli", prayTime.time)
        )


        val pendingAlarmIntent = PendingIntent.getBroadcast(
            this,
            prayTime.time.toInt(), broadcastIntent,
            PendingIntent.FLAG_ONE_SHOT
        )
        alarmManager.set(alarmType, prayTime.time, pendingAlarmIntent)
    }


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
                                onGetCordinates(lattt, longgg, it.city)
                                getSharedPreferences("sp", Context.MODE_PRIVATE)
                                    .edit()
                                    .putDouble("lattt", lattt)
                                    .putDouble("longgg", longgg)
                                    .putString("location", it?.city)
                                    .apply()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                {
                    it.printStackTrace()
                    onGetCordinates(11.0, 76.0, "Kerala")

                }
            )

    }


    fun SharedPreferences.Editor.putDouble(key: String, double: Double) =
        putLong(key, java.lang.Double.doubleToRawLongBits(double))

    fun SharedPreferences.getDouble(key: String, default: Double) =
        java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))


    val arrayList = arrayListOf<Cord>()

    fun getLocationFromCSV() {
        assets.open("worldcities.csv").bufferedReader().useLines {
            arrayList.clear()
            it.forEach {
                val splited = it.trim().split(',')
                arrayList.add(Cord(splited[0], splited[1].toDouble(), splited[2].toDouble()))
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
                        onGetCordinates(get.latitude, get.longitude, get.name)
                        getSharedPreferences("sp", Context.MODE_PRIVATE)
                            .edit()
                            .putDouble("lattt", get.latitude)
                            .putDouble("longgg", get.longitude)
                            .putString("location", get.name)
                            .apply()
                    }
                }
            }
        }
    }


}
