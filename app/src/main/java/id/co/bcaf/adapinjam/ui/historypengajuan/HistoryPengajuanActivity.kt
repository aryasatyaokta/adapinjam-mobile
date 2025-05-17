package id.co.bcaf.adapinjam.ui.historypengajuan

import HistoryPengajuanAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import kotlinx.coroutines.launch

class HistoryPengajuanActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_pengajuan)

        recyclerView = findViewById(R.id.rvRiwayat)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }


        sharedPrefManager = SharedPrefManager(this)
        val token = sharedPrefManager.getToken()

        if (!token.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    val data = RetrofitClient.apiService.getPengajuanHistory("Bearer $token")
                    if (data.isNotEmpty()) {
                        recyclerView.adapter = HistoryPengajuanAdapter(data)
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
    }
}
