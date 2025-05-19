package id.co.bcaf.adapinjam.data.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.UserCustomerResponse
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.ui.adddetailcustomer.AddDetail
import id.co.bcaf.adapinjam.ui.historypengajuan.HistoryPengajuanActivity
import id.co.bcaf.adapinjam.ui.password.UpdatePasswordActivity
import id.co.bcaf.adapinjam.ui.pengajuan.PengajuanActivity
import id.co.bcaf.adapinjam.ui.plafon.PlafonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var tvJenisPlafon: TextView
    private lateinit var tvJumlahPlafon: TextView
    private lateinit var tvSisaHutang: TextView
    private lateinit var tvSisaPlafon: TextView

    private lateinit var tvTotalPinjaman: TextView
    private lateinit var tvTotalTenor: TextView

    private lateinit var rvAllPlafon: RecyclerView
    private lateinit var plafonAdapter: PlafonAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        sharedPrefManager = SharedPrefManager(requireContext())

        tvJenisPlafon = view.findViewById(R.id.jenisPlafon)
        tvJumlahPlafon = view.findViewById(R.id.jumlahPlafon)
        tvSisaHutang = view.findViewById(R.id.tvSisaHutangHome)
        tvSisaPlafon = view.findViewById(R.id.tvSisaPlafonHome)

        tvTotalPinjaman = view.findViewById(R.id.totalPengajuanHome)
        tvTotalTenor = view.findViewById(R.id.totalTenorPengajuanHome)


        rvAllPlafon = view.findViewById(R.id.rvAllPlafon)
        rvAllPlafon.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        loadAllPlafon()
        loadPlafon()
        loadSisaHutang()
        loadPengajuanTerbaru()

        val btnAjukanPinjaman: Button = view.findViewById(R.id.btnAjukanPinjaman)
        btnAjukanPinjaman.setOnClickListener {
            checkProfileBeforeAction {
                val intent = Intent(requireContext(), PengajuanActivity::class.java)
                startActivity(intent)
            }
        }

        val viewAll = view.findViewById<TextView>(R.id.viewAllText)
        viewAll.setOnClickListener {
            val intent = Intent(requireContext(), HistoryPengajuanActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun loadPlafon() {
        val token = sharedPrefManager.getToken()

        if (token.isNullOrEmpty()) {
            tvJenisPlafon.text = "-"
            tvJumlahPlafon.text = "Rp -"
            tvSisaPlafon.text = "Rp -"
            return
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getCustomerProfile("Bearer $token")
                }

                if (response.isSuccessful) {
                    val userProfile = response.body()
                    val jenisPlafon = userProfile?.plafon?.jenisPlafon ?: "-"
                    val jumlahPlafonValue = (userProfile?.plafon?.jumlahPlafon as? Number)?.toDouble() ?: 0.0
                    val formattedPlafon = formatRupiah(jumlahPlafonValue)

                    tvJenisPlafon.text = jenisPlafon
                    tvJumlahPlafon.text = formattedPlafon
                    tvSisaPlafon.text = if (jumlahPlafonValue > 0) formattedPlafon else "Rp -"

                    fadeIn(tvJenisPlafon)
                    fadeIn(tvJumlahPlafon)
                    fadeIn(tvSisaPlafon)
                } else {
                    tvJenisPlafon.text = "-"
                    tvJumlahPlafon.text = "Rp -"
                    tvSisaPlafon.text = "Rp -"
                }
            } catch (e: Exception) {
                tvJenisPlafon.text = "-"
                tvJumlahPlafon.text = "Rp -"
                tvSisaPlafon.text = "Rp -"
            }
        }
    }

    private fun loadSisaHutang() {
        val token = sharedPrefManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                // Ambil data di IO dispatcher
                val pinjamanList = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getHistoryPinjaman("Bearer $token")
                }

                // Hitung total sisa hutang
                val totalHutang = pinjamanList.sumOf { it.sisaPokokHutang ?: 0.0 }

                // Tampilkan di UI
                withContext(Dispatchers.Main) {
                    tvSisaHutang.text = if (totalHutang > 0) formatRupiah(totalHutang) else "Rp -"
                    fadeIn(tvSisaHutang)
                }
            } catch (e: Exception) {
                // Tangani jika error (misal gagal koneksi, token salah, dll)
                withContext(Dispatchers.Main) {
                    tvSisaHutang.text = "Rp -"
                    Log.e("LoadHutang", "Error: ${e.message}", e)
                }
            }
        }
    }


    private fun checkProfileBeforeAction(onProfileComplete: (() -> Unit)? = null) {
        val token = sharedPrefManager.getToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.checkProfile("Bearer $token")
                }

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful -> {
                            onProfileComplete?.invoke()
                        }
                        response.code() == 428 -> {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Lengkapi Profil")
                                .setMessage("Profil Anda belum lengkap. Silakan lengkapi terlebih dahulu.")
                                .setPositiveButton("Lengkapi Sekarang") { _, _ ->
                                    startActivity(Intent(requireContext(), AddDetail::class.java))
                                }
                                .setCancelable(false)
                                .show()
                        }
                        else -> {
                            Toast.makeText(
                                requireContext(),
                                "Gagal memeriksa status profil: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadAllPlafon() {
        lifecycleScope.launch {
            try {
                val token = sharedPrefManager.getToken()
                if (token.isNullOrEmpty()) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllPlafon("Bearer $token")
                }

                if (response.isSuccessful && response.body() != null) {
                    if (!isAdded) return@launch

                    val plafonList = response.body()!!
                    plafonAdapter = PlafonAdapter(plafonList)
                    rvAllPlafon.adapter = plafonAdapter
                    fadeIn(rvAllPlafon)
                } else {
                    if (!isAdded) return@launch

                    Log.e("Plafon", "Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "Gagal memuat daftar plafon", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatRupiah(amount: Double): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getNumberInstance(localeID)
        return "Rp ${numberFormat.format(amount)}"
    }

    private fun loadPengajuanTerbaru() {
        val token = sharedPrefManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val historyList = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getPengajuanHistory("Bearer $token")
                }

                if (historyList.isNotEmpty()) {
                    // Asumsikan item terakhir adalah yang terbaru (pastikan backend urutkan descending)
                    val latest = historyList.last()

                    withContext(Dispatchers.Main) {
                        tvTotalPinjaman.text = formatRupiah(latest.amount.toDouble())
                        tvTotalTenor.text = "${latest.tenor} Bulan"
                        fadeIn(tvTotalPinjaman)
                        fadeIn(tvTotalTenor)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        tvTotalPinjaman.text = "Rp -"
                        tvTotalTenor.text = "- Bulan"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("PengajuanTerbaru", "Error: ${e.message}", e)
                    tvTotalPinjaman.text = "Rp -"
                    tvTotalTenor.text = "- Bulan"
                }
            }
        }
    }

    private fun fadeIn(view: View, duration: Long = 500) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setListener(null)
    }

}

