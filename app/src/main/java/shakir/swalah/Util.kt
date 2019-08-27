package shakir.swalah

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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


    fun setNextAlarm(context: Context) {
        val array = arrayOf(
            PrayersType.FAJR,
            PrayersType.SUNRISE,
            PrayersType.ZUHR,
            PrayersType.ASR,
            PrayersType.MAGHRIB,
            PrayersType.ISHA
        )
        val date = GregorianCalendar()

        val sharedPreferences = context.getSharedPreferences("sp", Context.MODE_PRIVATE)
        val lattt = sharedPreferences
            .getDouble("lattt", 11.00)

        val longgg = sharedPreferences
            .getDouble("longgg", 76.00)


        val prayerTimes =
            TimeCalculator().date(date).location(lattt, longgg, 0.0, 0.0)
                .timeCalculationMethod(AngleCalculationType.KARACHI)
                .calculateTimes()

        array.forEachIndexed { index, prayersType ->
            val prayTime = prayerTimes.getPrayTime(array[index])
            val milli = prayTime.time
            if (milli >= System.currentTimeMillis()) {

                val arabicNames = AppApplication.getArabicNames(array[index].name)


                val lastMilli = sharedPreferences
                    .getLong("lastMilli", 0)

                val lastName = sharedPreferences
                    .getString("lastName", null)

                if (lastMilli == milli && lastName == arabicNames)
                    return@setNextAlarm


                SimpleDateFormat("HH:mm", Locale.ENGLISH).format(prayTime)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                // Alarm type
                val alarmType = AlarmManager.RTC

                val uniqueIndexForParayer = getNextUniqueIndex(context)


                val broadcastIntent = Intent(context, AlarmBroadCastReceiver::class.java).apply {
                    setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("milli", milli)
                    putExtra("name", arabicNames)
                    putExtra("index", uniqueIndexForParayer)
                }


                val pendingAlarmIntent = PendingIntent.getBroadcast(
                    context,
                    uniqueIndexForParayer, broadcastIntent,
                    PendingIntent.FLAG_ONE_SHOT
                )


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        alarmType,
                        milli,
                        pendingAlarmIntent
                    )
                } else {
                    alarmManager.set(alarmType, milli, pendingAlarmIntent)
                }



                sharedPreferences
                    .edit()
                    .putLong("lastMilli", milli)
                    .putString("lastName", arabicNames)
                    .putInt("lastuniqueIndexForParayer", uniqueIndexForParayer)
                    .apply()


                return@forEachIndexed
            }

        }


    }


    fun cancelLastPendingIntent(context: Context) {

        try {
            val sharedPreferences = context.getSharedPreferences("sp", Context.MODE_PRIVATE)

            val lastMilli = sharedPreferences.getInt("lastMilli", 0)
            val lastName = sharedPreferences.getString("lastName", null)
            val lastuniqueIndexForParayer = sharedPreferences.getInt("lastuniqueIndexForParayer", 0)

            val broadcastIntent = Intent(context, AlarmBroadCastReceiver::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("milli", lastMilli)
                putExtra("name", lastName)
                putExtra("index", lastuniqueIndexForParayer)
            }


            val pendingAlarmIntent = PendingIntent.getBroadcast(
                context,
                lastuniqueIndexForParayer, broadcastIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            pendingAlarmIntent.cancel()

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


}