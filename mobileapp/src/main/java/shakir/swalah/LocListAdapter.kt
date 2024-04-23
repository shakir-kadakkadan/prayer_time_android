package shakir.swalah

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import shakir.swalah.databinding.AdapterLocationBinding
import shakir.swalah.models.Cord

class LocListAdapter(val function: (Cord) -> Unit) : RecyclerView.Adapter<LocListAdapter.VH>() {


    val arrayList = arrayListOf<Cord>()
    inner class VH(val binding: AdapterLocationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(AdapterLocationBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.locationTV.setText(arrayList.getOrNull(position)?.name)
        holder.binding.root.setOnClickListener {
            arrayList.getOrNull(position)?.let { it1 -> function?.invoke(it1) }
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}