package id.co.bcaf.adapinjam.ui.historypinjaman

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.PinjamanWithJatuhTempo
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import kotlinx.coroutines.launch

class LunasPinjamanFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var tvEmptyMessage: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lunas_belum, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewPinjaman)
        tvEmptyMessage = view.findViewById(R.id.tvEmptyState)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        sharedPrefManager = SharedPrefManager(requireContext())

        val token = sharedPrefManager.getToken()

        if (!token.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    val tokenWithBearer = "Bearer $token"

                    val pengajuanList = RetrofitClient.apiService.getPengajuanHistory(tokenWithBearer)
                    val pinjamanList = RetrofitClient.apiService.getHistoryPinjaman(tokenWithBearer)

                    val lunasList = pinjamanList.filter { it.lunas }
                    val combinedList = mutableListOf<PinjamanWithJatuhTempo>()

                    lunasList.forEach { pinjaman ->
                        // Untuk pinjaman lunas, jatuhTempo set ke "-"
                        combinedList.add(PinjamanWithJatuhTempo(pinjaman, "-"))
                    }

                    if (combinedList.isNotEmpty()) {
                        recyclerView.adapter = HistoryPinjamanAdapter(combinedList)
                        tvEmptyMessage.visibility = View.GONE
                    } else {
                        tvEmptyMessage.text = "Tidak Ada Pinjaman"
                        tvEmptyMessage.visibility = View.VISIBLE
                    }

                } catch (e: Exception) {
                    tvEmptyMessage.text = "Gagal memuat data"
                    tvEmptyMessage.visibility = View.VISIBLE
                }
            }
        } else {
            tvEmptyMessage.text = "Login Aplikasi Dulu"
            tvEmptyMessage.visibility = View.VISIBLE
        }

        return view
    }
}
