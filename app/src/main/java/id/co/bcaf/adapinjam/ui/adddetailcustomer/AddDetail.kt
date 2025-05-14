package id.co.bcaf.adapinjam.ui.adddetailcustomer

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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

    private val CAMERA_REQUEST_CODE = 100
    private var imageUri: Uri? = null
    private var imageFile: File? = null

    private val GALLERY_REQUEST_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_detail)

        val btnUploadFoto = findViewById<Button>(R.id.btnUploadFoto)
        btnUploadFoto.setOnClickListener {
            openGallery()
        }


        val btnAmbilFoto = findViewById<Button>(R.id.btnAmbilFoto)
        btnAmbilFoto.setOnClickListener {
            openCamera()
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
            }
        }


        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Set an OnClickListener to go back to HomeActivity
        btnBack.setOnClickListener {
            // Navigate to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Optional: This will finish AddDetail activity and remove it from the back stack
        }

        sharedPrefManager = SharedPrefManager(this)
        progressDialog = ProgressDialog(this)

        tanggalLahir = findViewById(R.id.TanggalLahir)
        jenisKelamin = findViewById(R.id.JenisKelamin)

        // Set up the Spinner for Jenis Kelamin (Gender)
        val genderOptions = listOf("Pria", "Wanita")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jenisKelamin.adapter = genderAdapter

        // Set up the Date Picker for Tanggal Lahir
        tanggalLahir.setOnClickListener {
            showDatePickerDialog()
        }

        val btnSubmit = findViewById<Button>(R.id.btnAddDetail)

        btnSubmit.setOnClickListener {
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
                nik = nik,
                tempatLahir = tempatLahir,
                tanggalLahir = tanggalLahirStr,
                jenisKelamin = jenisKelaminStr,
                noTelp = noTelp,
                alamat = alamat,
                namaIbuKandung = ibuKandung,
                pekerjaan = pekerjaan,
                gaji = gaji,
                noRek = noRek,
                statusRumah = statusRumah
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
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Format the date as a string (e.g., "dd/MM/yyyy")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            calendar.set(selectedYear, selectedMonth, selectedDay)
            val selectedDate = dateFormat.format(calendar.time)
            tanggalLahir.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun submitCustomerDetails(token: String, customerRequest: UserCustomerRequest) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.apiService.addCustomerDetails("Bearer $token", customerRequest)
                if (response.isSuccessful && response.body() != null) {
                    val customerId = response.body()!!.id
                    if (imageUri != null) {
                        uploadCustomerPhoto(token, customerId.toString(), imageUri!!)
                    } else {
                        progressDialog.dismiss()
                        showSnackbar("Customer details added successfully")
                        startActivity(Intent(this@AddDetail, HomeActivity::class.java))
                        finish()
                    }
                } else {
                    progressDialog.dismiss()
                    showSnackbar("Error: ${response.message()}", true)
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                showSnackbar("Exception: ${e.message}", true)
            }
        }
    }


    private fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(if (isError) Color.RED else Color.GREEN)
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        imageFile = File.createTempFile("photo_", ".jpg", cacheDir)
        imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile!!)

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageView = findViewById<ImageView>(R.id.imagePreview)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    imageView.setImageURI(imageUri)
                }
                GALLERY_REQUEST_CODE -> {
                    imageUri = data?.data
                    imageView.setImageURI(imageUri)
                }
            }
        }
    }

    private fun uploadCustomerPhoto(token: String, customerId: String, uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Copy isi dari URI ke file sementara
                val inputStream = contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
                val outputStream = tempFile.outputStream()

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // 2. Buat MultipartBody.Part dari tempFile
                val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                // 3. Panggil API upload
                val response = RetrofitClient.apiService.uploadFoto("Bearer $token", customerId, body)

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if (response.isSuccessful) {
                        val responseText = response.body()?.string() ?: "Upload berhasil"
                        showSnackbar(responseText)
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
                    Log.e("UPLOAD_ERROR", "Exception saat upload", e)
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Izin diberikan
        } else {
            Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    fun compressImage(context: Context, uri: Uri): File {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        val file = File(context.cacheDir, "compressed_image.jpg")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out) // quality 70%
        out.flush()
        out.close()
        return file
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

}
