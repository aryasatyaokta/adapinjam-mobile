package id.co.bcaf.adapinjam.ui.adddetailcustomer

import android.app.Activity
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
import java.text.SimpleDateFormat
import java.util.*

class AddDetail : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var progressDialog: ProgressDialog

    private lateinit var tanggalLahir: EditText
    private lateinit var jenisKelamin: Spinner

    private val REQUEST_KTP_CAMERA = 101
    private val REQUEST_SELFIE_CAMERA = 102
    private val REQUEST_KTP_GALLERY = 103
    private val REQUEST_SELFIE_GALLERY = 104

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
        val nik = findViewById<EditText>(R.id.Nik).text.toString()
        val tempatLahir = findViewById<EditText>(R.id.TempatLahir).text.toString()
        val tanggalLahirStr = tanggalLahir.text.toString()
        val jenisKelaminStr = jenisKelamin.selectedItem.toString()
        val noTelp = findViewById<EditText>(R.id.Telepon).text.toString()
        val alamat = findViewById<EditText>(R.id.Alamat).text.toString()
        val ibuKandung = findViewById<EditText>(R.id.IbuKandung).text.toString()
        val pekerjaan = findViewById<EditText>(R.id.Pekerjaan).text.toString()
        val gaji = findViewById<EditText>(R.id.Gaji).text.toString().toIntOrNull() ?: 0
        val noRek = findViewById<EditText>(R.id.Rekening).text.toString()
        val statusRumah = findViewById<EditText>(R.id.StatusRumah).text.toString()

        val customerRequest = UserCustomerRequest(
            nik = nik, tempatLahir = tempatLahir, tanggalLahir = tanggalLahirStr, jenisKelamin = jenisKelaminStr, noTelp = noTelp,
            alamat = alamat, namaIbuKandung = ibuKandung, pekerjaan = pekerjaan, gaji = gaji, noRek = noRek, statusRumah = statusRumah
        )

        val token = sharedPrefManager.getToken() ?: ""
        if (token.isNotEmpty()) {
            progressDialog.setMessage("Submitting...")
            progressDialog.show()
            submitCustomerDetails(token, customerRequest)
        } else {
            showSnackbar("Token is missing", true)
        }
    }

    private fun submitCustomerDetails(token: String, customerRequest: UserCustomerRequest) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.apiService.addCustomerDetails("Bearer $token", customerRequest)
                if (response.isSuccessful && response.body() != null) {
                    val customerId = response.body()!!.id
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

    private fun openCamera(requestCode: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = File.createTempFile("img_", ".jpg", cacheDir)
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

        when (requestCode) {
            REQUEST_KTP_CAMERA -> {
                uriKtp = uri
                fileKtp = file
            }
            REQUEST_SELFIE_CAMERA -> {
                uriSelfie = uri
                fileSelfie = file
            }
        }

        startActivityForResult(intent, requestCode)
    }

    private fun openGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, requestCode)
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
}

