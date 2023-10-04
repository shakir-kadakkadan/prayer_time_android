package shakir.swalah

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.forEachIndexed
import androidx.recyclerview.widget.RecyclerView
import com.azan.TimeCalculator
import com.azan.types.AngleCalculationType
import com.azan.types.PrayersType
import shakir.swalah.databinding.AdapterMonthViewBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MonthAdapter(var latitude: Double, var longitude: Double, var timeFormat: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    inner class VH(val binding: AdapterMonthViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VH(AdapterMonthViewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return 100

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is VH){
            val holder=holder as VH
            val cal = Calendar.getInstance()
                .apply { add(Calendar.MONTH, position - 50); set(Calendar.DATE, 1) }
            holder.binding.textTest.setText(SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(cal.time))

            thread {
                val dateString = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(cal.time)
                val monthIndex = cal.get(Calendar.MONTH)
                val array = arrayOf(
                    PrayersType.FAJR,
                    PrayersType.SUNRISE,
                    PrayersType.ZUHR,
                    PrayersType.ASR,
                    PrayersType.MAGHRIB,
                    PrayersType.ISHA
                )


                val list = arrayListOf<String>()
                list.add("Date ")
                for (i in 0..5) {
                    list.add(AppApplication.getArabicNames(array[i].name) ?: "")
                }
                while (cal.get(Calendar.MONTH) == monthIndex) {
                    val date = GregorianCalendar().apply { time = cal.time }
                    val prayerTimes =
                        TimeCalculator().date(date).location(latitude, longitude, 0.0, 0.0)
                            .timeCalculationMethod(AngleCalculationType.KARACHI)
                            .calculateTimes()
                    list.add(cal.get(Calendar.DATE).toString())
                    for (i in 0..5) {
                        list.add(
                            SimpleDateFormat(timeFormat, Locale.ENGLISH).format(prayerTimes.getPrayTime(array[i]))
                        )
                    }
                    cal.add(Calendar.DATE, 1)
                }



                (holder.binding.root.context as? Activity)?.runOnUiThread {
                    try {
                        holder.binding. tableLayout.forEachIndexed { row, view ->
                            (view as TableRow).forEachIndexed { column, view ->
                                val tv = (view as TextView)
                                tv.gravity = Gravity.LEFT

                                tv.setText(list.getOrNull(((row) * 7) + (column)) ?: "")
                            }


                        }
                        holder.binding.textTest.setText(dateString)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        try {
                            holder.binding.textTest.setText(e.message)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }


        }

    }


}




