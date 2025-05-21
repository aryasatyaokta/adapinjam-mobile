package id.co.bcaf.adapinjam.data.fragment

import HistoryPengajuanAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import kotlinx.coroutines.launch


class HistoryFragment(private val statusFilter: String?) : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history_pengajuan, container, false)
        recyclerView = view.findViewById(R.id.rvRiwayat)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        recyclerView.layoutManager = LinearLayoutManager(context)

        sharedPrefManager = SharedPrefManager(requireContext())
        val token = sharedPrefManager.getToken()

        if (!token.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    val data = RetrofitClient.apiService.getPengajuanHistory("Bearer $token")
                    val filtered = when (statusFilter) {
                        "DISBURSEMENT" -> data.filter { it.status == "DISBURSEMENT" }
                        "REVIEW" -> data.filter { it.status in listOf("BCKT_MARKETING", "BCKT_BRANCHMANAGER", "BCKT_BACKOFFICE") }
                        "REJECT" -> data.filter { it.status == "REJECT" }
                        else -> data
                    }
                    if (filtered.isNotEmpty()) {
                        recyclerView.adapter = HistoryPengajuanAdapter(filtered)
                        tvEmptyState.visibility = View.GONE
                    } else {
                        tvEmptyState.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    tvEmptyState.visibility = View.VISIBLE
                }
            }
        } else {
            tvEmptyState.visibility = View.VISIBLE
        }

        return view
    }
}

