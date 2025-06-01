package id.co.bcaf.adapinjam.ui.adddetailcustomer

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.UserCustomerRequest
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.ui.home.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AddDetail : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var progressDialog: ProgressDialog

    private lateinit var tanggalLahir: EditText
    private lateinit var jenisKelamin: Spinner

    private val REQUEST_KTP = 201
    private val REQUEST_SELFIE = 202

    private var uriKtp: Uri? = null
    private var uriSelfie: Uri? = null

    private var fileKtp: File? = null
    private var fileSelfie: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_detail)

        sharedPrefManager = SharedPrefManager(this)
        progressDialog = ProgressDialog(this)

        tanggalLahir = findViewById(R.id.TanggalLahir)
        jenisKelamin = findViewById(R.id.JenisKelamin)

        val genderOptions = listOf("Pria", "Wanita")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jenisKelamin.adapter = genderAdapter

        tanggalLahir.setOnClickListener {
            showDatePickerDialog()
        }

        val gajiField = findViewById<EditText>(R.id.Gaji)

        gajiField.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    gajiField.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[Rp,.\\s]".toRegex(), "")
                    val parsed = cleanString.toDoubleOrNull() ?: 0.0
                    val localeID = Locale("in", "ID")
                    val formatted = NumberFormat.getCurrencyInstance(localeID).apply {
                        maximumFractionDigits = 0
                    }.format(parsed)

                    current = formatted
                    gajiField.setText(formatted)
                    gajiField.setSelection(formatted.length)

                    gajiField.addTextChangedListener(this)
                }
            }
        })


        findViewById<Button>(R.id.btnKtpPicker).setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_KTP)
        }

        findViewById<Button>(R.id.btnSelfiePicker).setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_SELFIE)
        }


        findViewById<Button>(R.id.btnAddDetail).setOnClickListener {
            submitForm()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 200)
            }
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun submitForm() {
        val nikField = findViewById<EditText>(R.id.Nik)
        val tempatLahirField = findViewById<EditText>(R.id.TempatLahir)
        val noTelpField = findViewById<EditText>(R.id.Telepon)
        val alamatField = findViewById<EditText>(R.id.Alamat)
        val ibuKandungField = findViewById<EditText>(R.id.IbuKandung)
        val pekerjaanField = findViewById<EditText>(R.id.Pekerjaan)
        val gajiField = findViewById<EditText>(R.id.Gaji)
        val noRekField = findViewById<EditText>(R.id.Rekening)
        val statusRumahField = findViewById<EditText>(R.id.StatusRumah)

        val nik = nikField.text.toString().trim()
        val tempatLahir = tempatLahirField.text.toString().trim()
        val tanggalLahirStr = tanggalLahir.text.toString().trim()
        val jenisKelaminStr = jenisKelamin.selectedItem.toString()
        val noTelp = noTelpField.text.toString().trim()
        val alamat = alamatField.text.toString().trim()
        val ibuKandung = ibuKandungField.text.toString().trim()
        val pekerjaan = pekerjaanField.text.toString().trim()
        val gajiStr = gajiField.text.toString().trim()
        val noRek = noRekField.text.toString().trim()
        val statusRumah = statusRumahField.text.toString().trim()

        when {
            nik.isEmpty() -> {
                nikField.error = "NIK harus diisi"
                nikField.requestFocus()
                return
            }
            tempatLahir.isEmpty() -> {
                tempatLahirField.error = "Tempat lahir harus diisi"
                tempatLahirField.requestFocus()
                return
            }
            tanggalLahirStr.isEmpty() -> {
                tanggalLahir.error = "Tanggal lahir harus diisi"
                tanggalLahir.requestFocus()
                return
            }
            noTelp.isEmpty() -> {
                noTelpField.error = "Nomor telepon harus diisi"
                noTelpField.requestFocus()
                return
            }
            alamat.isEmpty() -> {
                alamatField.error = "Alamat harus diisi"
                alamatField.requestFocus()
                return
            }
            ibuKandung.isEmpty() -> {
                ibuKandungField.error = "Nama ibu kandung harus diisi"
                ibuKandungField.requestFocus()
                return
            }
            pekerjaan.isEmpty() -> {
                pekerjaanField.error = "Pekerjaan harus diisi"
                pekerjaanField.requestFocus()
                return
            }
            gajiStr.isEmpty() -> {
                gajiField.error = "Gaji harus diisi"
                gajiField.requestFocus()
                return
            }
            noRek.isEmpty() -> {
                noRekField.error = "Nomor rekening harus diisi"
                noRekField.requestFocus()
                return
            }
            statusRumah.isEmpty() -> {
                statusRumahField.error = "Status rumah harus diisi"
                statusRumahField.requestFocus()
                return
            }
            uriKtp == null -> {
                showSnackbar("Silakan upload foto KTP", true)
                return
            }
            uriSelfie == null -> {
                showSnackbar("Silakan upload foto selfie", true)
                return
            }
        }

        val gaji = gajiStr.replace("[Rp,.\\s]".toRegex(), "").toIntOrNull() ?: 0

        val customerRequest = UserCustomerRequest(
            nik = nik, tempatLahir = tempatLahir, tanggalLahir = tanggalLahirStr,
            jenisKelamin = jenisKelaminStr, noTelp = noTelp, alamat = alamat,
            namaIbuKandung = ibuKandung, pekerjaan = pekerjaan, gaji = gaji,
            noRek = noRek, statusRumah = statusRumah
        )

        // Konfirmasi sebelum kirim
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah data Anda sudah benar semua?")
            .setPositiveButton("Ya") { _, _ ->
                val token = sharedPrefManager.getToken() ?: ""
                if (token.isNotEmpty()) {
                    progressDialog.setMessage("Submitting...")
                    progressDialog.show()
                    submitCustomerDetails(token, customerRequest)
                } else {
                    showSnackbar("Token is missing", true)
                }
            }
            .setNegativeButton("Periksa Lagi", null)
            .show()
    }


    private fun submitCustomerDetails(token: String, customerRequest: UserCustomerRequest) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.apiService.addCustomerDetails("Bearer $token", customerRequest)
                if (response.isSuccessful && response.body() != null) {
                    val customerId = response.body()!!.id

                    // Tampilkan Toast
                    Toast.makeText(this@AddDetail, "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show()

                    // Lanjut ke upload foto
                    uploadCustomerPhoto(token, customerId.toString())
                } else {
                    progressDialog.dismiss()
                    showSnackbar("Gagal menambahkan data: ${response.message()}", true)
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                showSnackbar("Exception: ${e.message}", true)
            }
        }
    }


    private fun uploadCustomerPhoto(token: String, customerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val partKtp = uriKtp?.let { createMultipart("fotoKtp", it) }
                val partSelfie = uriSelfie?.let { createMultipart("fotoSelfie", it) }

                val response = RetrofitClient.apiService.uploadFoto("Bearer $token", customerId, partKtp, partSelfie)

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if (response.isSuccessful) {
                        showSnackbar("Upload berhasil")
                        startActivity(Intent(this@AddDetail, HomeActivity::class.java))
                        finish()
                    } else {
                        showSnackbar("Upload gagal: ${response.errorBody()?.string()}", true)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    showSnackbar("Upload error: ${e.message}", true)
                }
            }
        }
    }

    private fun createMultipart(fieldName: String, uri: Uri): MultipartBody.Part {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File.createTempFile(fieldName, ".jpg", cacheDir)
        val outputStream = file.outputStream()
        inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data!!
            when (requestCode) {
                REQUEST_KTP -> {
                    uriKtp = uri
                    fileKtp = File(uri.path!!)
                    findViewById<ImageView>(R.id.imageKtp).setImageURI(uriKtp)
                }
                REQUEST_SELFIE -> {
                    uriSelfie = uri
                    fileSelfie = File(uri.path!!)
                    findViewById<ImageView>(R.id.imageSelfie).setImageURI(uriSelfie)
                }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(this, { _, y, m, d ->
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            calendar.set(y, m, d)
            tanggalLahir.setText(dateFormat.format(calendar.time))
        }, year, month, day)
        dialog.show()
    }

    private fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(if (isError) Color.RED else Color.GREEN)
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Izin kamera diberikan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatRupiah(number: String): String {
        return try {
            val cleanString = number.replace("[Rp,.\\s]".toRegex(), "")
            val parsed = cleanString.toDoubleOrNull() ?: 0.0
            val localeID = Locale("in", "ID")
            val formatter = NumberFormat.getCurrencyInstance(localeID)
            formatter.maximumFractionDigits = 0
            formatter.format(parsed)
        } catch (e: Exception) {
            "Rp 0"
        }
    }

}

