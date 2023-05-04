package shakir.swalah

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(val onTimeSet: (Int,Int) -> Unit) : DialogFragment(), TimePickerDialog.OnTimeSetListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        c.timeInMillis=arguments?.getLong("prayTime")?:System.currentTimeMillis()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        try {
            return TimePickerDialog(activity, TimePickerDialog.THEME_DEVICE_DEFAULT_LIGHT,this, hour, minute, Util.is_24_hourFormat)
        } catch (e: Exception) {
            return TimePickerDialog(activity, this, hour, minute, Util.is_24_hourFormat)
        }
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {

        onTimeSet.invoke(hourOfDay,minute)

    }
}