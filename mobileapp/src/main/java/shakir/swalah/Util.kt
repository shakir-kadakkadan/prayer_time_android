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

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.concurrent.TimeUnit

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
            (0..5).forEachIndexed { index, prayersType ->
                arrayOf(false, true).forEach { _isIqama ->
                    val isIqama = _isIqama && index != 1

                    if ((isIqama && Util.isiqamaAlarmOn) || (!isIqama && Util.isadhanAlarmOn)) {
                        var milli = prayerTimes[prayersType].time
                        if (isIqama) {
                            val iqsettings = Util.getIqamaSettings().get(if (index == 0) 0 else index - 1)
                            if (iqsettings.isAfter) {
                                milli = milli + (TimeUnit.MINUTES.toMillis(iqsettings.after.toLong()))
                            } else {
                                milli = iqsettings.fixed
                            }

                        }
                        if (milli >= System.currentTimeMillis()) {

                            var arabicNames = AppApplication.getArabicNames(prayersType)
                            if (isIqama) {
                                arabicNames = arabicNames + " " + "(الإقامة)"
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
//                               milli=System.currentTimeMillis()+TimeUnit.MINUTES.toMillis(1)


                            val pendingIntent =
                                createPendingIntent(context, milli, arabicNames, uniqueIndexForParayer)


                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    alarmType,
                                    milli,
                                    pendingIntent
                                )
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    alarmManager.setExact(alarmType, milli, pendingIntent)
                                } else {
                                    alarmManager.set(alarmType, milli, pendingIntent)
                                }
                            }



                            println("settttttt milli $milli arabicNames $arabicNames uniqueIndexForParayer $uniqueIndexForParayer $isIqama")



                            var lastMilli_prev=sharedPreferences.getLong("lastMilli_copy",0L)
                            var lastName_prev=sharedPreferences.getString("lastName_copy","")

                            sharedPreferences
                                .edit()
                                .putLong("lastMilli", milli)
                                .putLong("lastMilli_copy", milli)
                                .putString("lastName", arabicNames)
                                .putString("lastName_copy", arabicNames)
                                .apply {
                                    if (lastMilli_prev!=0L&&lastMilli_prev!=milli){
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
        Log.d(
            "TESTSTSTST",
            "createPendingIntent() called with: context = [" + context + "], milli = [" + milli + "], arabicNames = [" + arabicNames + "], uniqueIndexForParayer = [" + uniqueIndexForParayer + "]"
        );
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


}


data class Iqama(val index: Int, var isAfter: Boolean = true, var after: Int, var fixed: Long)




