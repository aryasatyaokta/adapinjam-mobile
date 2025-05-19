import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.PengajuanHistoryResponse
import java.text.NumberFormat
import java.util.Locale

class HistoryPengajuanAdapter(private val list: List<PengajuanHistoryResponse>) :
    RecyclerView.Adapter<HistoryPengajuanAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAmount: TextView = view.findViewById(R.id.tvTotalPinjamanValue)
        val tvTenor: TextView = view.findViewById(R.id.tvTenorValue)
        val tvDana: TextView = view.findViewById(R.id.tvTotalDanaValue)
        val tvAdmin: TextView = view.findViewById(R.id.tvAdminValue)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pengajuan_history, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvAmount.text = formatRupiah(item.amount)
        holder.tvTenor.text = "${item.tenor} Bulan"
        holder.tvDana.text = formatRupiah(item.totalDanaDidapat)
        holder.tvAdmin.text = formatRupiah(item.biayaAdmin)

        when (item.status) {
            "BCKT_MARKETING", "BCKT_BRANCHMANAGER", "BCKT_BACKOFFICE" -> {
                holder.tvStatus.text = "Direview"
                holder.tvStatus.setTextColor(Color.parseColor("#F3C623"))
            }
            "REJECT" -> {
                holder.tvStatus.text = "Ditolak"
                holder.tvStatus.setTextColor(Color.parseColor("#FF0000"))
            }
            "DISBURSEMENT" -> {
                holder.tvStatus.text = "Disetujui (Cair)"
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            }
            else -> {
                holder.tvStatus.text = item.status
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.black))
            }
        }
    }
    override fun getItemCount(): Int = list.size

    private fun formatRupiah(amount: Number): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getNumberInstance(localeID)
        return "Rp ${numberFormat.format(amount)}"
    }
}