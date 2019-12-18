package shakir.swalah

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.azan.TimeCalculator
import com.azan.types.AngleCalculationType
import com.azan.types.PrayersType
import java.text.SimpleDateFormat
import java.util.*

object Util {


    fun getNextUniqueIndex(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("sp", Context.MODE_PRIVATE)
        val currentInt = sharedPreferences.getInt("nextUniqueIndex", 0)
        sharedPreferences.edit().putInt("nextUniqueIndex", currentInt + 1).apply()
        return currentInt
    }


    fun setNextAlarm(context: Context, tommorrow: Boolean = false) {
        try {
            Util.cancelLastPendingIntent(context)
            val array = arrayOf(
                PrayersType.FAJR,
                PrayersType.SUNRISE,
                PrayersType.ZUHR,
                PrayersType.ASR,
                PrayersType.MAGHRIB,
                PrayersType.ISHA
            )
            val date = GregorianCalendar()
            if (tommorrow)
                date.add(Calendar.DATE, 1)

            val sharedPreferences = context.getSharedPreferences("sp", Context.MODE_PRIVATE)
            val lattt = sharedPreferences
                .getDouble("lattt", 11.00)

            val longgg = sharedPreferences
                .getDouble("longgg", 76.00)


            val prayerTimes =
                TimeCalculator().date(date).location(lattt, longgg, 0.0, 0.0)
                    .timeCalculationMethod(AngleCalculationType.KARACHI)
                    .calculateTimes()


            Log.d("dgfbdsfjjd", "$lattt $longgg $prayerTimes")


            array.forEachIndexed { index, prayersType ->
                val prayTime = prayerTimes.getPrayTime(array[index])
                val milli = prayTime.time
                if (milli >= System.currentTimeMillis()) {

                    val arabicNames = AppApplication.getArabicNames(array[index].name)


                    val lastMilli = sharedPreferences
                        .getLong("lastMilli", 0)

                    val lastName = sharedPreferences
                        .getString("lastName", null)

                    if (lastMilli == milli && lastName == arabicNames) {
                        Log.d("dsfghsdfsh", "set return@setNextAlarm")
                        return@setNextAlarm
                    }


                    SimpleDateFormat("HH:mm", Locale.ENGLISH).format(prayTime)
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    // Alarm type
                    val alarmType = AlarmManager.RTC

                    val uniqueIndexForParayer = getNextUniqueIndex(context)


                    val pendingIntent =
                        createPendingIntent(context, milli, arabicNames, uniqueIndexForParayer)


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            alarmType,
                            milli,
                            pendingIntent
                        )
                    } else {
                        alarmManager.set(alarmType, milli, pendingIntent)
                    }



                    sharedPreferences
                        .edit()
                        .putLong("lastMilli", milli)
                        .putString("lastName", arabicNames)
                        .putInt("lastuniqueIndexForParayer", uniqueIndexForParayer)
                        .apply()


                    return@setNextAlarm
                }

            }

            //if execution reached here . it means
            //no alarm set because no more prayer today
            //set alarm for next date
            setNextAlarm(context, tommorrow = true)

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }


    fun cancelLastPendingIntent(context: Context) {

        try {
            val sharedPreferences = context.getSharedPreferences("sp", Context.MODE_PRIVATE)

            val lastMilli = sharedPreferences.getLong("lastMilli", 0)
            val lastName = sharedPreferences.getString("lastName", null)
            val lastuniqueIndexForParayer = sharedPreferences.getInt("lastuniqueIndexForParayer", 0)


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
                .remove("lastuniqueIndexForParayer")
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun createPendingIntent(
        context: Context,
        milli: Long,
        arabicNames: String?,
        uniqueIndexForParayer: Int
    ): PendingIntent? {
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
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }


}