package id.co.bcaf.adapinjam.ui.plafon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlafonActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlafonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plafon)

        recyclerView = findViewById(R.id.rvPlafon)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fetchPlafons()
    }

    private fun fetchPlafons() {
        val apiService = RetrofitClient.apiService
        val sharedPrefManager = SharedPrefManager(this) // buat instance

        lifecycleScope.launch {
            try {
                val token = sharedPrefManager.getToken()

                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@PlafonActivity, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    apiService.getAllPlafon("Bearer $token")
                }

                if (response.isSuccessful && response.body() != null) {
                    adapter = PlafonAdapter(response.body()!!)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@PlafonActivity, "Gagal memuat plafon", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@PlafonActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
