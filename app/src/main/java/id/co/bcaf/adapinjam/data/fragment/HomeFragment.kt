package id.co.bcaf.adapinjam.data.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.UserCustomerResponse
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.ui.adddetailcustomer.AddDetail
import id.co.bcaf.adapinjam.ui.historypengajuan.HistoryPengajuanActivity
import id.co.bcaf.adapinjam.ui.login.LoginActivity
import id.co.bcaf.adapinjam.ui.password.UpdatePasswordActivity
import id.co.bcaf.adapinjam.ui.pengajuan.PengajuanActivity
import id.co.bcaf.adapinjam.ui.plafon.PlafonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private lateinit var layoutSimulasi: LinearLayout
    private lateinit var etAmountSimulasi: EditText
    private lateinit var btnSubmitSimulasi: Button
    private lateinit var tvHasilSimulasi: TextView

    private lateinit var bgHomeCard: CardView
    private lateinit var hutangCard: CardView
    private lateinit var ajukanCard: CardView
    private lateinit var historyText: TextView
    private lateinit var viewAllText: TextView
    private lateinit var totalPinjamanCard: CardView
    private lateinit var labelDaftarPlafon2: TextView
    private lateinit var cardHasilSimulasi: CardView
    private lateinit var promoKhusus: CardView

    private lateinit var spinnerJenisPlafon: Spinner

    private lateinit var labelDaftarPlafon: TextView
    private lateinit var ajukanCard2: CardView
    private lateinit var rvAllPlafon2: RecyclerView

    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var etTenorSimulasi: MaterialAutoCompleteTextView

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

        layoutSimulasi = view.findViewById(R.id.layoutSimulasi)
        etAmountSimulasi = view.findViewById(R.id.etAmountSimulasi)
        etTenorSimulasi = view.findViewById(R.id.etTenorSimulasi)
        btnSubmitSimulasi = view.findViewById(R.id.btnSubmitSimulasi)
        tvHasilSimulasi = view.findViewById(R.id.tvHasilSimulasi)
        spinnerJenisPlafon = view.findViewById(R.id.spinnerJenisPlafon)

        bgHomeCard = view.findViewById(R.id.bgHomeCard)
        hutangCard = view.findViewById(R.id.hutangCard)
        ajukanCard = view.findViewById(R.id.ajukanCard)
        historyText = view.findViewById(R.id.historyText)
        viewAllText = view.findViewById(R.id. viewAllText)
        totalPinjamanCard = view.findViewById(R.id.totalPinjamanCard)
        labelDaftarPlafon2 = view.findViewById(R.id.labelDaftarPlafon2)
        labelDaftarPlafon = view.findViewById(R.id.labelDaftarPlafon)
        cardHasilSimulasi = view.findViewById(R.id.cardHasilSimulasi)
        promoKhusus = view.findViewById(R.id.promoKhusus)


        ajukanCard2 = view.findViewById(R.id.ajukanCard2)

        rvAllPlafon2 = view.findViewById(R.id.rvAllPlafon2)
        rvAllPlafon2.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        loadingAnimation = view.findViewById(R.id.loadingAnimation)

        rvAllPlafon = view.findViewById(R.id.rvAllPlafon)
        rvAllPlafon.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        loadPlafonSpinner()
        loadPlafon()
        loadSisaHutang()
        loadPengajuanTerbaru()

        spinnerJenisPlafon.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedJenis = parent?.getItemAtPosition(position).toString()
                updateTenorDropdown(selectedJenis)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Opsional: bisa dikosongkan
            }
        }

        val initialJenis = spinnerJenisPlafon.selectedItem?.toString() ?: "Bronze"
        updateTenorDropdown(initialJenis)

        val btnAjukanPinjaman: Button = view.findViewById(R.id.btnAjukanPinjaman)
        btnAjukanPinjaman.setOnClickListener {
            checkProfileBeforeAction {
                val intent = Intent(requireContext(), PengajuanActivity::class.java)
                startActivity(intent)
            }
        }

        val btnAjukanPinjaman2: Button = view.findViewById(R.id.btnAjukanPinjaman2)
        btnAjukanPinjaman2.setOnClickListener {
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

        etTenorSimulasi.setOnClickListener {
            etTenorSimulasi.showDropDown()
        }

        val token = sharedPrefManager.getToken()
        val hasToken = !token.isNullOrEmpty()

        if (hasToken) {
            layoutSimulasi.visibility = View.GONE
            rvAllPlafon2.visibility = View.GONE
            ajukanCard2.visibility = View.GONE
            labelDaftarPlafon2.visibility = View.GONE
            loadAllPlafon()
            loadPlafon()
            loadSisaHutang()
            loadPengajuanTerbaru()
        } else {
            // Tampilkan hanya yang diperlukan
            loadAllPlafon()
            layoutSimulasi.visibility = View.VISIBLE
            ajukanCard2.visibility = View.VISIBLE
            rvAllPlafon2.visibility = View.VISIBLE
            labelDaftarPlafon2.visibility = View.VISIBLE

            bgHomeCard.visibility = View.GONE
            hutangCard.visibility = View.GONE
            ajukanCard.visibility = View.GONE
            historyText.visibility = View.GONE
            viewAllText.visibility = View.GONE
            totalPinjamanCard.visibility = View.GONE
            labelDaftarPlafon.visibility = View.GONE
            rvAllPlafon.visibility = View.GONE

            tvJenisPlafon.text = "-"
            tvJumlahPlafon.text = "Rp -"
            tvSisaPlafon.text = "Rp -"
            tvSisaHutang.text = "Rp -"
            tvTotalPinjaman.text = "Rp -"
            tvTotalTenor.text = "- Bulan"
        }

        var current = ""

        etAmountSimulasi.addTextChangedListener(object : TextWatcher {
           override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etAmountSimulasi.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[Rp,.\\s]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = formatRupiah(parsed)

                        current = formatted
                        etAmountSimulasi.setText(formatted)
                        etAmountSimulasi.setSelection(formatted.length)
                    } else {
                        current = ""
                    }

                    etAmountSimulasi.addTextChangedListener(this)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSubmitSimulasi.setOnClickListener {
            val amountInput = etAmountSimulasi.text.toString().replace("[^\\d]".toRegex(), "")
            val amount = amountInput.toDoubleOrNull()
            val tenor = etTenorSimulasi.text.toString().toIntOrNull()
            val jenisPlafon = spinnerJenisPlafon.selectedItem.toString()

            if (amount == null || tenor == null) {
                Toast.makeText(requireContext(), "Masukkan jumlah dan tenor yang valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loadingAnimation.setAnimation(R.raw.loading_animation)
            loadingAnimation.playAnimation()
            loadingAnimation.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.getSimulasiPengajuan(jenisPlafon, amount, tenor)
                    }

                    delay(2000)
                    loadingAnimation.cancelAnimation()
                    loadingAnimation.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        val formattedAmount = formatRupiah(result.amount)
                        val angsuran = formatRupiah(result.angsuran)
                        val totalPembayaran = formatRupiah(result.totalPembayaran)
                        val admin = formatRupiah(result.biayaAdmin)
                        val danaCair = formatRupiah(result.danaCair)
                        val bungaAsDouble = result.bunga
                        val bungaFormatted = (bungaAsDouble / 100).toInt()  // Misal 500.0 / 100 = 5

                        val hasilText = """
                    Jumlah Pinjaman  : $formattedAmount
                    Tenor                       : ${result.tenor} bulan
                    Bunga                      : ${bungaFormatted}%
                    
                    Angsuran / bln       : $angsuran
                    Total Bayar             : $totalPembayaran
                    
                    Biaya Admin           : $admin
                    Dana Diterima        : $danaCair
                """.trimIndent()

                        tvHasilSimulasi.text = hasilText
                        cardHasilSimulasi.visibility = View.VISIBLE
                    } else {
                        tvHasilSimulasi.text = "❌ Gagal mengambil data simulasi"
                        cardHasilSimulasi.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    loadingAnimation.cancelAnimation()
                    loadingAnimation.visibility = View.GONE
                    tvHasilSimulasi.text = "⚠️ Error: ${e.message}"
                    cardHasilSimulasi.visibility = View.VISIBLE
                }
            }
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
                    val sisaPlafonValue = when (val plafon = userProfile?.sisaPlafon) {
                        is Number -> plafon.toDouble()
                        is String -> plafon.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }

                    val formattedPlafon = formatRupiah(jumlahPlafonValue)
                    val formattedSisa = formatRupiah(sisaPlafonValue)

                    tvJenisPlafon.text = jenisPlafon
                    tvJumlahPlafon.text = formattedPlafon
                    tvSisaPlafon.text = if (sisaPlafonValue > 0) formattedSisa else "Rp -"

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
        Log.d("TOKEN_CHECK", "Token = $token")

        if (token.isNullOrEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Login Diperlukan")
                .setMessage("Silakan login terlebih dahulu untuk melanjutkan.")
                .setPositiveButton("Ke Page Login") { _, _ ->
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
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
                val hasToken = !token.isNullOrEmpty()

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllPlafon()
                }

                if (!isAdded) return@launch

                if (response.isSuccessful && response.body() != null) {
                    val plafonList = response.body()!!
                    plafonAdapter = PlafonAdapter(plafonList)

                    if (hasToken) {
                        // Tampilkan ke RecyclerView versi login
                        rvAllPlafon.adapter = plafonAdapter
                        fadeIn(rvAllPlafon)
                        rvAllPlafon2.visibility = View.GONE
                    } else {
                        // Tampilkan ke RecyclerView versi belum login
                        rvAllPlafon2.adapter = plafonAdapter
                        fadeIn(rvAllPlafon2)
                        rvAllPlafon.visibility = View.GONE
                    }

                } else {
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

    private fun loadPlafonSpinner() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllPlafon()
                }

                if (!isAdded) return@launch

                if (response.isSuccessful && response.body() != null) {
                    val plafonList = response.body()!!

                    // Ambil hanya jenis plafon-nya untuk Spinner
                    val jenisPlafonList = plafonList.map { it.jenisPlafon }

                    val spinnerAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        jenisPlafonList
                    )
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerJenisPlafon.adapter = spinnerAdapter

                    // Set default pilihan & update tenor
                    if (jenisPlafonList.isNotEmpty()) {
                        updateTenorDropdown(jenisPlafonList[0].toString())
                    }

                    // Set listener (kalau belum diset)
                    spinnerJenisPlafon.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val selectedJenis = parent?.getItemAtPosition(position).toString()
                            updateTenorDropdown(selectedJenis)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal memuat jenis plafon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTenorDropdown(jenisPlafon: String) {
        val defaultTenorList = listOf(15, 18, 21, 24)

        val tenorOptionsMap = mapOf(
            "Bronze" to listOf(6, 9, 12, 15),
            "Silver" to listOf(9, 12, 15, 18),
            "Gold" to listOf(12, 15, 18, 21),
            "Platinum" to listOf(15, 18, 21, 24)
        )

        val normalizedPlafon = jenisPlafon.trim().capitalize()
        val tenorList = tenorOptionsMap[normalizedPlafon] ?: defaultTenorList
        val tenorStrings = tenorList.map { it.toString() }

        val tenorAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            tenorStrings
        )
        etTenorSimulasi.setAdapter(tenorAdapter)
        etTenorSimulasi.setText("") // reset isi tenor saat jenis plafon berubah
    }




}

