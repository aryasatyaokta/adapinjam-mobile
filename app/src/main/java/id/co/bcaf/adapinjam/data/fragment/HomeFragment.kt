package id.co.bcaf.adapinjam.data.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
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

        rvAllPlafon = view.findViewById(R.id.rvAllPlafon)
        rvAllPlafon.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        loadAllPlafon()

        loadPlafon()

        val btnAjukanPinjaman: Button = view.findViewById(R.id.btnAjukanPinjaman)
        btnAjukanPinjaman.setOnClickListener {
            checkProfileBeforeAction {
                val intent = Intent(requireContext(), PengajuanActivity::class.java)
                startActivity(intent)
            }
        }

        return view
    }

    private fun loadPlafon() {
        val token = sharedPrefManager.getToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getCustomerProfile("Bearer $token")
                }

                if (response.isSuccessful) {
                    val userProfile: UserCustomerResponse? = response.body()
                    val jenisPlafon = userProfile?.plafon?.jenisPlafon ?: "-"
                    val jumlahPlafonValue = (userProfile?.plafon?.jumlahPlafon as? Number)?.toLong() ?: 0L
                    val formattedJumlahPlafon = NumberFormat.getNumberInstance(Locale("in", "ID")).format(jumlahPlafonValue)

                    tvJenisPlafon.text = jenisPlafon
                    tvJumlahPlafon.text = "Rp $formattedJumlahPlafon"
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat data plafon", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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


}
