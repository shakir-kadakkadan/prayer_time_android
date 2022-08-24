package shakir.swalah

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_location.view.*
import shakir.swalah.models.Cord

class LocListAdapter(val function: (Cord) -> Unit) : RecyclerView.Adapter<LocListAdapter.VH>() {


    val arrayList = arrayListOf<Cord>()
    inner class VH(val item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent.inflate(R.layout.adapter_location))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.locationTV.setText(arrayList.get(position).name)
        holder.itemView.setOnClickListener {
            function?.invoke(arrayList.get(position))
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}