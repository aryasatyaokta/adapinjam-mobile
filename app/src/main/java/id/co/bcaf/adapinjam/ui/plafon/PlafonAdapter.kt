package id.co.bcaf.adapinjam.ui.plafon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.Plafon
import java.text.NumberFormat
import java.util.*

class PlafonAdapter(private val plafonList: List<Plafon>) :
    RecyclerView.Adapter<PlafonAdapter.PlafonViewHolder>() {

    class PlafonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJenisPlafon: TextView = itemView.findViewById(R.id.tvJenisPlafon)
        val tvJumlahPlafon: TextView = itemView.findViewById(R.id.tvJumlahPlafon)
        val imgPlafon: ImageView = itemView.findViewById(R.id.imgPlafon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlafonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plafon, parent, false)
        return PlafonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlafonViewHolder, position: Int) {
        val plafon = plafonList[position]
        holder.tvJenisPlafon.text = plafon.jenisPlafon
        holder.tvJumlahPlafon.text = "Rp. ${
            NumberFormat.getNumberInstance(Locale("in", "ID"))
                .format(plafon.jumlahPlafon)
        }"

        val imageRes = when (plafon.jenisPlafon?.lowercase()) {
            "bronze" -> R.drawable.bronze
            "silver" -> R.drawable.silver
            "gold" -> R.drawable.gold
            "platinum" -> R.drawable.platinum
            else -> R.drawable.default_plafon
        }

        holder.imgPlafon.setImageResource(imageRes)
    }

    override fun getItemCount(): Int = plafonList.size
}
