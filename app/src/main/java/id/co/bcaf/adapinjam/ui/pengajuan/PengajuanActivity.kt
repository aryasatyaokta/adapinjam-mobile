package id.co.bcaf.adapinjam.ui.pengajuan

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresPermission
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

class PengajuanActivity : AppCompatActivity() {

    private lateinit var etJumlahPinjaman: EditText
    private lateinit var etJumlahTenor: EditText
    private lateinit var btnAjukan: Button
    private lateinit var sharedPref: SharedPrefManager

    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengajuan)

        etJumlahPinjaman = findViewById(R.id.etJumlahPinjaman)
        etJumlahTenor = findViewById(R.id.etJumlahTenor)
        btnAjukan = findViewById(R.id.btnAjukan)
        sharedPref = SharedPrefManager(this)

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
        val amount = etJumlahPinjaman.text.toString().toDoubleOrNull()
        val tenor = etJumlahTenor.text.toString().toIntOrNull()

        if (amount == null || tenor == null) {
            Toast.makeText(this, "Input tidak valid", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@PengajuanActivity, "Pengajuan gagal: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PengajuanActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showConfirmationDialog(latitude: Double, longitude: Double) {
        val amount = etJumlahPinjaman.text.toString().toDoubleOrNull()
        val tenor = etJumlahTenor.text.toString().toIntOrNull()

        if (amount == null || tenor == null) {
            Toast.makeText(this, "Input tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val message = """
        Apakah data Anda sudah benar untuk mengajukan pinjaman?
        
        Jumlah Pinjaman: Rp $amount
        Tenor: $tenor bulan
    """.trimIndent()

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Pengajuan")
            .setMessage(message)
            .setPositiveButton("Ya") { _, _ ->
                submitPengajuan(latitude, longitude)
            }
            .setNegativeButton("Batal", null)
            .show()
    }


}
