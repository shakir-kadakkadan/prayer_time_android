package shakir.swalah

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MonthAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    inner class VH(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VH(parent.inflate(R.layout.adapter_month_view))
    }

    override fun getItemCount(): Int {
        return 0

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }


}




