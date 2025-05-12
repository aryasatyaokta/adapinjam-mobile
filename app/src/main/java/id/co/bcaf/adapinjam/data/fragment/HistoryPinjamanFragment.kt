package id.co.bcaf.adapinjam.data.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.ui.historypinjaman.HistoryPinjamanAdapter
import kotlinx.coroutines.launch

class HistoryPinjamanFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history_pinjaman, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewPinjaman)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        sharedPrefManager = SharedPrefManager(requireContext())
        val token = sharedPrefManager.getToken()

        if (!token.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    val data = RetrofitClient.apiService.getHistoryPinjaman("Bearer $token")
                    recyclerView.adapter = HistoryPinjamanAdapter(data)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Gagal load data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}
