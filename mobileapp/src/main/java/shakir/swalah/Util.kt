package shakir.swalah

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import com.azan.astrologicalCalc.SimpleDate
import shakir.swalah.AppApplication.Companion.sp

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object Util {

    const val iqamaSettingsSaved_PREF_KEY = "iqamaSettingsSaved3"

    fun getMySharedPreference(context: Context): SharedPreferences {
        return context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    }


    fun getNextUniqueIndex(context: Context): Int {
        val sharedPreferences = getMySharedPreference(context)
        val currentInt = sharedPreferences.getInt("nextUniqueIndex", 0)
        sharedPreferences.edit().putInt("nextUniqueIndex", currentInt + 1).apply()
        return currentInt
    }


    fun setNextAlarm(context: Context, tommorrow: Boolean = false) {
        println("setNextAlarmsetNextAlarmsetNextAlarm")

        try {
            Util.cancelLastPendingIntent(context)
            val sharedPreferences = getMySharedPreference(context)
            val latitude = sharedPreferences
                .getDouble("v2_latitude", 11.00)

            val longitude = sharedPreferences
                .getDouble("v2_longitude", 76.00)
            val date = GregorianCalendar()
            if (tommorrow)
                date.add(Calendar.DATE, 1)
            val dateS = SimpleDate(date)
            val azan = getAthanObj(latitude, longitude)
            val prayerTimes = azan.getAthanOfDate(dateS)

            val adhanType = 0
            val iqamaType = 1
            val suhoorType = -1
            var suhoorMinute = sp.getInt("suhoorMinute", 60)
            (0..5).forEachIndexed { index, prayersType ->
                arrayOf(suhoorType, adhanType, iqamaType).forEach { type ->
                    if ((type == iqamaType && Util.isiqamaAlarmOn && index != 1) || (type == adhanType && Util.isadhanAlarmOn) || (type == suhoorType && suhoorMinute > 0 && index == 5)) {
                        var milli = prayerTimes[prayersType].time
                        if (type == iqamaType) {
                            val iqsettings = Util.getIqamaSettings().get(if (index == 0) 0 else index - 1)
                            if (iqsettings.isAfter) {
                                milli = milli + (TimeUnit.MINUTES.toMillis(iqsettings.after.toLong()))
                            } else {
                                milli = iqsettings.fixed
                            }

                        }

                        if (type == suhoorType) {
                            milli = milli - (TimeUnit.MINUTES.toMillis(suhoorMinute.toLong()))
                        }


                        if (milli >= System.currentTimeMillis()) {

                            var arabicNames = AppApplication.getArabicNames(prayersType)
                            if (type == iqamaType) {
                                arabicNames = arabicNames + " " + "(الإقامة)"
                            }
                            if (type == suhoorType) {
                                arabicNames = "سَحُورٌ Time for Sahur"
                            }


                            val lastMilli = sharedPreferences
                                .getLong("lastMilli", 0)

                            val lastName = sharedPreferences
                                .getString("lastName", null)

                            if (lastMilli == milli && lastName == arabicNames) {
                                Log.d("dsfghsdfsh", "set return@setNextAlarm")
                                return@setNextAlarm
                            }


                            SimpleDateFormat("HH:mm", Locale.ENGLISH).format(Date(milli))
                            val alarmManager =
                                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                            // Alarm type
                            val alarmType = AlarmManager.RTC_WAKEUP

                            val uniqueIndexForParayer = 4444


//                           if (BuildConfig.isRunFromStudio)
//                               milli=System.currentTimeMillis()+TimeUnit.SECONDS.toMillis(10)


                            val pendingIntent =
                                createPendingIntent(context, milli, arabicNames, uniqueIndexForParayer)


                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                // No exact alarm permission, use inexact alarm
                                alarmManager.set(alarmType, milli, pendingIntent)
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    alarmType,
                                    milli,
                                    pendingIntent
                                )
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                alarmManager.setExact(alarmType, milli, pendingIntent)
                            } else {
                                alarmManager.set(alarmType, milli, pendingIntent)
                            }


                            var lastMilli_prev = sharedPreferences.getLong("lastMilli_copy", 0L)
                            var lastName_prev = sharedPreferences.getString("lastName_copy", "")

                            sharedPreferences
                                .edit()
                                .putLong("lastMilli", milli)
                                .putLong("lastMilli_copy", milli)
                                .putString("lastName", arabicNames)
                                .putString("lastName_copy", arabicNames)
                                .apply {
                                    if (lastMilli_prev != 0L && lastMilli_prev != milli) {
                                        putLong("lastMilli_prev", lastMilli_prev)
                                        putString("lastName_prev", lastName_prev)
                                    }
                                }
                                .putInt("lastUniqueIndexForPrayer", uniqueIndexForParayer)
                                .apply()


                            return@setNextAlarm
                        }
                    }
                }

            }

            //if execution reached here . it means
            //no alarm set because no more prayer today
            //set alarm for next date
            if (!tommorrow)
                setNextAlarm(context, tommorrow = true)

        } catch (e: Exception) {
            e.report()
        }


    }


    fun setNextAlarmDND(context: Context, tommorrow: Boolean = false) {


        try {
            val sharedPreferences = getMySharedPreference(context)

            val dnd = sharedPreferences
                .getBoolean("dnd", false)

            if (!dnd)
                return

            var dndMinutev2 = sharedPreferences
                .getInt("dndMinutev2", 1).plus(1).times(5)


            val dndManualMilli = sharedPreferences.getLong("dndManualMilli", 0L)
            val dndManualMilliEnd = dndManualMilli + TimeUnit.MINUTES.toMillis(10)


            val latitude = sharedPreferences
                .getDouble("v2_latitude", 11.00)

            val longitude = sharedPreferences
                .getDouble("v2_longitude", 76.00)
            val date = GregorianCalendar()
            if (tommorrow)
                date.add(Calendar.DATE, 1)
            val dateS = SimpleDate(date)
            val azan = getAthanObj(latitude, longitude)
            val prayerTimes = azan.getAthanOfDate(dateS)
            (0..5).forEachIndexed { index, prayersType ->
                arrayOf(false, true).forEach { _isIqama ->
                    val isIqama = _isIqama && index != 1

                    if (isIqama) {
                        var iqama_milli = prayerTimes[prayersType].time
                        if (isIqama) {
                            val iqsettings = Util.getIqamaSettings().get(if (index == 0) 0 else index - 1)
                            if (iqsettings.isAfter) {
                                iqama_milli = iqama_milli + (TimeUnit.MINUTES.toMillis(iqsettings.after.toLong()))
                            } else {
                                iqama_milli = iqsettings.fixed
                            }

                        }


                        iqama_milli = iqama_milli + TimeUnit.SECONDS.toMillis(10)
                        val iqama_milliEnd = iqama_milli + TimeUnit.MINUTES.toMillis(dndMinutev2.toLong()) - TimeUnit.SECONDS.toMillis(11)


                        fun setAlarm(milli: Long, action: String, uniqueIndexForParayer: Int) {

                            val alarmManager =
                                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                            // Alarm type
                            val alarmType = AlarmManager.RTC_WAKEUP


                            val pendingIntent =
                                createPendingIntent(context, milli, action, uniqueIndexForParayer)


                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                // No exact alarm permission, use inexact alarm
                                alarmManager.set(alarmType, milli, pendingIntent)
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    alarmType,
                                    milli,
                                    pendingIntent
                                )
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                alarmManager.setExact(alarmType, milli, pendingIntent)
                            } else {
                                alarmManager.set(alarmType, milli, pendingIntent)
                            }
                        }


                        var exit = false

                        if (dndManualMilliEnd > System.currentTimeMillis()) {
                            setAlarm(dndManualMilliEnd, "offDND", 555)
                            exit = true
                        } else {
                            sharedPreferences.edit().remove("dndManualMilli").commit()
                        }

                        if (iqama_milli > System.currentTimeMillis()) {
                            exit = true
                            setAlarm(iqama_milli, "dnd", 666)

                        }
                        if (iqama_milliEnd > System.currentTimeMillis()) {
                            exit = true
                            setAlarm(iqama_milliEnd, "offDND", 777)
                        }
                        if (exit) {
                            return@setNextAlarmDND
                        }

                    }

                }

            }

            //if execution reached here . it means
            //no alarm set because no more prayer today
            //set alarm for next date
            if (!tommorrow)
                setNextAlarmDND(context, tommorrow = true)

        } catch (e: Exception) {
            e.report()
        }


    }


    fun cancelLastPendingIntent(context: Context) {

        try {
            val sharedPreferences = getMySharedPreference(context)

            val lastMilli = sharedPreferences.getLong("lastMilli", 0)
            val lastName = sharedPreferences.getString("lastName", null)
            val lastuniqueIndexForParayer = sharedPreferences.getInt("lastUniqueIndexForPrayer", 0)


            Log.d("dsfghsdfsh", "last $lastMilli $lastName $lastuniqueIndexForParayer")

            val pendingIntent =
                createPendingIntent(context, lastMilli, lastName, lastuniqueIndexForParayer)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent?.cancel()

            sharedPreferences
                .edit()
                .remove("lastName")
                .remove("lastMilli")
                .remove("lastUniqueIndexForPrayer")
                .commit()
        } catch (e: Exception) {
            e.report()
        }
    }


    fun createPendingIntent(
        context: Context,
        milli: Long,
        arabicNames: String?,
        uniqueIndexForParayer: Int
    ): PendingIntent {

        val broadcastIntent = Intent(context, AlarmBroadCastReceiver::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("milli", milli)
            putExtra("name", arabicNames)
            putExtra("index", uniqueIndexForParayer)
        }




        return PendingIntent.getBroadcast(
            context,
            uniqueIndexForParayer, broadcastIntent,
            pFlagMutable(PendingIntent.FLAG_CANCEL_CURRENT)
        )
    }


    var is_24_hourFormat: Boolean
        get() {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            return sharedPreferences.getString("is_24_hourFormat", null)?.toBoolean() ?: DateFormat.is24HourFormat(AppApplication.instance)
        }
        set(value) {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            sharedPreferences.edit().putString("is_24_hourFormat", value.toString()).commit()
        }

    var isAMPMShow: Boolean
        get() {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            return sharedPreferences.getString("isAMPMShow", "false").toBoolean()
        }
        set(value) {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            sharedPreferences.edit().putString("isAMPMShow", value.toString()).commit()
        }


    var isadhanAlarmOn: Boolean
        get() {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            return sharedPreferences.getString("isadhanAlarmOn", "true").toBoolean()
        }
        set(value) {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            sharedPreferences.edit().putString("isadhanAlarmOn", value.toString()).commit()
        }


    var openApp: Boolean
        get() {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            return sharedPreferences.getString("openApp", "true").toBoolean()
        }
        set(value) {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            sharedPreferences.edit().putString("openApp", value.toString()).commit()
        }


    var isiqamaAlarmOn: Boolean
        get() {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            return sharedPreferences.getString("isiqamaAlarmOn_v2", "true").toBoolean()
        }
        set(value) {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            sharedPreferences.edit().putString("isiqamaAlarmOn_v2", value.toString()).commit()
        }


    fun timeFormat(): String {
        return if (is_24_hourFormat)
            "HH:mm"
        else if (isAMPMShow) "hh:mm a" else "hh:mm"
    }


    private var iqamaSettingsSavedString: String?
        get() {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            val iss = sharedPreferences.getString(iqamaSettingsSaved_PREF_KEY, null)
            println("issississ Read\n$iss")
            return iss
        }
        set(value) {
            val sharedPreferences = getMySharedPreference(AppApplication.instance)
            sharedPreferences.edit().putString(iqamaSettingsSaved_PREF_KEY, value).commit()
            val iss = sharedPreferences.getString(iqamaSettingsSaved_PREF_KEY, null)
            println("issississ Write\n$iss")

        }


    fun getIqamaSettings(): ArrayList<Iqama> {
        try {

            if (Util.iqamaSettingsSavedString?.isNotBlank() == true) {

                val arrayLis = arrayListOf<Iqama>()
                val ls = Util.iqamaSettingsSavedString!!.split("\n")
                for (i in 0..4) {
                    val ws = ls[i].split(",")
                    arrayLis.add(Iqama(ws[0].toInt(), ws[1].toBoolean(), ws[2].toInt(), ws[3].toLongOrNull() ?: 0L))
                }

                return arrayLis

            }


        } catch (e: Exception) {
            e.printStackTrace()

        }
        return arrayListOf(
            Iqama(0, true, 20, 0),
            Iqama(1, true, 20, 0),
            Iqama(2, true, 20, 0),
            Iqama(3, true, 5, 0),
            Iqama(4, true, 20, 0),
        )

    }


    fun saveIqamaSettings(list: ArrayList<Iqama>) {
        var s = ""
        list.forEach {
            if (s.isNotBlank())
                s = s + "\n"

            s = s + "${it.index},${it.isAfter},${it.after},${it.fixed}"
        }

        iqamaSettingsSavedString = s
    }


    suspend fun trackIPData() {
        try {
            delay(5000)
            val trackIPDataLastCalled = sp.getLong("trackIPDataLastCalled", 0L)
            val now = System.currentTimeMillis()
            if (now - trackIPDataLastCalled > TimeUnit.DAYS.toMillis(7)) {
                withContext(Dispatchers.IO) {
                    try {
                        sp.edit().putLong("trackIPDataLastCalled", now).commit()
                        // 1. Fetch IP data
                        val ipUrl = URL("https://pro.ip-api.com/json?key=yjfBZPLkt6Kkl3h&fields=58335")
                        val ipConn = ipUrl.openConnection() as HttpURLConnection
                        ipConn.requestMethod = "GET"
                        ipConn.connectTimeout = 10000
                        ipConn.readTimeout = 10000
                        val ipResponse = ipConn.inputStream.bufferedReader().readText()
                        ipConn.disconnect()

                        val ipData = JSONObject(ipResponse)

                        // 2. Build tracking payload


                        val payload = JSONObject().apply {
                            // merge all IP fields
                            ipData.keys().forEach { key -> put(key, ipData.get(key)) }
                            put("timestamp", System.currentTimeMillis())
                            put("timezone", TimeZone.getDefault().id)
                            put("language", Locale.getDefault().toLanguageTag())
                            put("screenWidth", AppApplication.instance.resources.displayMetrics.widthPixels)
                            put("screenHeight", AppApplication.instance.resources.displayMetrics.heightPixels)
                            put(
                                "appVersion", try {
                                    AppApplication.instance.packageManager.getPackageInfo(AppApplication.instance.packageName, 0).versionName
                                } catch (e: PackageManager.NameNotFoundException) {
                                    "unknown"
                                }
                            )
                            put("androidVersion", Build.VERSION.SDK_INT)
                            put("deviceModel", "${Build.MANUFACTURER} ${Build.MODEL}")
                        }

                        // 3. POST (new) or PUT (update existing) to Firebase REST API
                        val savedId = sp.getString("savedIdOfIPData", null)
                        val firebaseUrl = if (savedId != null)
                            URL("https://prayer-time-shakir.firebaseio.com/ip_details/$savedId.json")
                        else
                            URL("https://prayer-time-shakir.firebaseio.com/ip_details.json")

                        val fbConn = firebaseUrl.openConnection() as HttpURLConnection
                        fbConn.requestMethod = if (savedId != null) "PUT" else "POST"
                        fbConn.setRequestProperty("Content-Type", "application/json")
                        fbConn.doOutput = true
                        fbConn.connectTimeout = 10000
                        fbConn.readTimeout = 10000

                        OutputStreamWriter(fbConn.outputStream).use { it.write(payload.toString()) }

                        val responseCode = fbConn.responseCode
                        val responseBody = (if (responseCode in 200..299) fbConn.inputStream else fbConn.errorStream)
                            ?.bufferedReader()?.readText() ?: ""
                        fbConn.disconnect()

                        // On first POST, Firebase returns {"name":"-OmXxx..."} — save that id
                        if (savedId == null && responseCode in 200..299) {
                            val newId = JSONObject(responseBody).optString("name")
                            if (newId.isNotEmpty()) {
                                sp.edit().putString("savedIdOfIPData", newId).apply()
                            }
                        }

                        println("trackIPData response: $responseCode $responseBody")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("trackIPData" + " " + "Error tracking IP data")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}


data class Iqama(val index: Int, var isAfter: Boolean = true, var after: Int, var fixed: Long)




