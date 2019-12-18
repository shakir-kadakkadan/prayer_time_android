/*
package shakir.swalah

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.text.view.*
import shakir.swalah.db.Logs

class LogAdapter(val all: List<Logs>) : RecyclerView.Adapter<LogAdapter.VH>() {


    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.text, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return all.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
                holder.itemView.text123.setText(all.get(position).text)
    }

}
*/
