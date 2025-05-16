package id.co.bcaf.adapinjam.ui.historypinjaman

import android.view.LayoutInflater
import id.co.bcaf.adapinjam.R
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.co.bcaf.adapinjam.data.model.PinjamanHistoryResponse
import java.text.NumberFormat
import java.util.Locale

class HistoryPinjamanAdapter(private val list: List<PinjamanHistoryResponse>) :
    RecyclerView.Adapter<HistoryPinjamanAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val jumlah: TextView = view.findViewById(R.id.totalPinjaman)
        val angsuran: TextView = view.findViewById(R.id.angsuranBulan)
        val sisa: TextView = view.findViewById(R.id.sisaHutang)
        val bunga: TextView = view.findViewById(R.id.Bunga)
        val status: TextView = view.findViewById(R.id.statusLabel)
        val tenors: TextView = view.findViewById(R.id.Tenor)
        val sisaTenor: TextView = view.findViewById(R.id.sisaTenor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pinjaman_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pinjaman = list[position]

        holder.jumlah.text = formatRupiah(pinjaman.amount)
        holder.angsuran.text = formatRupiah(pinjaman.angsuran)
        holder.sisa.text = formatRupiah(pinjaman.sisaPokokHutang)
        holder.bunga.text = "${pinjaman.bunga.toInt()}%" // hilangkan .0
        holder.status.text = if (pinjaman.lunas) "Lunas" else "Belum Lunas"
        holder.tenors.text = "${pinjaman.tenor} Bulan"
        holder.sisaTenor.text = "${pinjaman.sisaTenor} Bulan"
    }

    private fun formatRupiah(amount: Double): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getNumberInstance(localeID)
        return "Rp ${numberFormat.format(amount)}"
    }

}