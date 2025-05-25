package id.co.bcaf.adapinjam.ui.pengajuan

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.PengajuanRequest
import id.co.bcaf.adapinjam.data.model.PengajuanResponse
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class PengajuanActivity : AppCompatActivity() {

    private lateinit var etJumlahPinjaman: EditText
    private lateinit var etJumlahTenor: EditText
    private lateinit var btnAjukan: Button
    private lateinit var sharedPref: SharedPrefManager
    private lateinit var tvSisaPlafon: TextView

    private var allowedTenors: List<Int> = listOf()
    private var maxPlafon: Double = 0.0

    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengajuan)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        tvSisaPlafon = findViewById(R.id.tvSisaPlafon)

        etJumlahPinjaman = findViewById(R.id.etJumlahPinjaman)
        etJumlahPinjaman.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etJumlahPinjaman.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[Rp,.\\s]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = formatRupiah(parsed)

                        current = formatted
                        etJumlahPinjaman.setText(formatted)
                        etJumlahPinjaman.setSelection(formatted.length)
                    } else {
                        current = ""
                    }

                    etJumlahPinjaman.addTextChangedListener(this)
                }
            }
        })

        etJumlahTenor = findViewById(R.id.etJumlahTenor)
        btnAjukan = findViewById(R.id.btnAjukan)
        sharedPref = SharedPrefManager(this)

        loadSisaPlafon()

        btnAjukan.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                getLocationAndSubmit()
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocationAndSubmit() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    showConfirmationDialog(location.latitude, location.longitude)
                } else {
                    Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
    }


    private fun submitPengajuan(latitude: Double, longitude: Double) {
        val amountStr = etJumlahPinjaman.text.toString().replace("[Rp,.\\s]".toRegex(), "")
        val amount = amountStr.toDoubleOrNull()
        val tenor = etJumlahTenor.text.toString().toIntOrNull()

        if (amount == null || tenor == null) {
            Toast.makeText(this, "Input tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount > maxPlafon) {
            Toast.makeText(this, "Jumlah pinjaman melebihi plafon yang tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        if (!allowedTenors.contains(tenor)) {
            Toast.makeText(this, "Tenor tidak sesuai dengan jenis plafon Anda", Toast.LENGTH_SHORT).show()
            return
        }

        val token = sharedPref.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val request = PengajuanRequest(amount, tenor, latitude, longitude)

        // Jalankan API di coroutine scope
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.createPengajuan("Bearer $token", request)
                }
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@PengajuanActivity, "Pengajuan berhasil", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Pengajuan gagal: ${response.message()}"
                    if (errorMsg.contains("masih ada yang direview", ignoreCase = true)) {
                        Toast.makeText(this@PengajuanActivity, "Pengajuan anda masih ada yang direview. Silahkan pantau terus pengajuan anda.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@PengajuanActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@PengajuanActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showConfirmationDialog(latitude: Double, longitude: Double) {
        val amountStr = etJumlahPinjaman.text.toString().replace("[Rp,.\\s]".toRegex(), "")
        val amount = amountStr.toDoubleOrNull()
        val tenor = etJumlahTenor.text.toString().toIntOrNull()

        if (amount == null || tenor == null) {
            Toast.makeText(this, "Input tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val token = SharedPrefManager(this).getToken()
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val preview = RetrofitClient.apiService.previewPengajuan(
                    amount,
                    tenor,
                    "Bearer $token"
                )

                val bungaFormatted = if (preview.bunga % 1 == 0.0) {
                    preview.bunga.toInt().toString()
                } else {
                    preview.bunga.toString()
                }

                val message = """
                Apakah data Anda sudah benar untuk mengajukan pinjaman?

                Jumlah Pinjaman : ${formatRupiah(preview.amount)}
                Tenor           : ${preview.tenor} bulan
                Bunga           : $bungaFormatted%
                Angsuran        : ${formatRupiah(preview.angsuran)}
                Biaya Admin     : ${formatRupiah(preview.biayaAdmin)}
                Dana Diterima   : ${formatRupiah(preview.totalDanaDidapat)}
            """.trimIndent()

                AlertDialog.Builder(this@PengajuanActivity) // Ganti jika bukan di MainActivity
                    .setTitle("Konfirmasi Pengajuan")
                    .setMessage(message)
                    .setPositiveButton("Ya") { _, _ ->
                        submitPengajuan(latitude, longitude)
                    }
                    .setNegativeButton("Batal", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(this@PengajuanActivity, "Gagal mengambil data preview: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatRupiah(amount: Number): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getNumberInstance(localeID)
        return "Rp ${numberFormat.format(amount)}"
    }

    private fun loadSisaPlafon() {
        val token = sharedPref.getToken()

        if (token.isNullOrEmpty()) {
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
                    val sisaPlafonValue = (userProfile?.sisaPlafon as? Number)?.toDouble() ?: 0.0
                    val jenisPlafon = userProfile?.plafon?.jenisPlafon ?: "Default"

                    // Tampilkan sisa plafon dan jenis plafon
                    tvSisaPlafon.text = "Sisa Plafon (${jenisPlafon}): ${formatRupiah(sisaPlafonValue)}"

                    // Atur placeholder jumlah pinjaman
                    etJumlahPinjaman.hint = "Jumlah Pinjaman"

                    // Tentukan opsi tenor berdasarkan jenis plafon
                    val tenorOptions = when (jenisPlafon.lowercase(Locale.ROOT)) {
                        "bronze" -> listOf(6, 9, 12, 15)
                        "silver" -> listOf(9, 12, 15, 18)
                        "gold" -> listOf(12, 15, 18, 21)
                        "platinum" -> listOf(15, 18, 21, 24)
                        else -> listOf(15, 18, 21, 24)
                    }

                    val tenorAdapter = ArrayAdapter(this@PengajuanActivity, android.R.layout.simple_dropdown_item_1line, tenorOptions)
                    (etJumlahTenor as? AutoCompleteTextView)?.apply {
                        setAdapter(tenorAdapter)
                        setOnClickListener { showDropDown() }
                        hint = "Pilih tenor"
                    }
                } else {
                    tvSisaPlafon.text = "Rp -"
                }
            } catch (e: Exception) {
                tvSisaPlafon.text = "Rp -"
            }
        }
    }

}
