package shakir.swalah


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Space
import android.widget.TextView


/**
 * Created by Shakir on 8-04-2020.
 */
 class SpinnerZeroHideAdapter(
        val array: List<String?>?,
        val spinner_item_layout_res: Int,
        val drop_down_layout_res: Int,
        val hintString: String? = null
) : BaseAdapter() {


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (position == 0) {
            return Space(parent.context)
        } else {
            val row =
                    LayoutInflater.from(parent.context).inflate(drop_down_layout_res, parent, false)
            val textView = row.findViewById<View>(android.R.id.text1) as TextView
            textView.text = array?.getOrNull(position - 1)?.toString()
            return row
        }


    }

    override fun getCount(): Int {
        return array?.size?.plus(1) ?: 0
    }

    /** DON'T CALL GET ITEM*/
    override fun getItem(position: Int): Any {
        return position
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val row = convertView ?: LayoutInflater.from(parent.context).inflate(
                spinner_item_layout_res,
                parent,
                false
        )
        val textViewQ: TextView
        textViewQ = row.findViewById<View>(android.R.id.text1) as TextView
        if (position == 0) {
            textViewQ.setText("")
            textViewQ.hint = hintString?:""
        } else {
            textViewQ.text = array?.getOrNull(position - 1)?.toString()

        }
        return row
    }


}


