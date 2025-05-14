package id.co.bcaf.adapinjam.ui.EditProfil

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.fragment.ProfilSayaFragment
import id.co.bcaf.adapinjam.data.model.UserCustomerRequest
import id.co.bcaf.adapinjam.data.model.UserCustomerResponse
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.data.viewModel.EditProfilViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*

class EditProfilActivity : AppCompatActivity() {

    private lateinit var viewModel: EditProfilViewModel
    private lateinit var sharedPref: SharedPrefManager

    private lateinit var nik: EditText
    private lateinit var tempatLahir: EditText
    private lateinit var tanggalLahir: EditText
    private lateinit var jenisKelaminSpinner: Spinner
    private lateinit var telepon: EditText
    private lateinit var alamat: EditText
    private lateinit var ibuKandung: EditText
    private lateinit var pekerjaan: EditText
    private lateinit var gaji: EditText
    private lateinit var rekening: EditText
    private lateinit var statusRumah: EditText
    private lateinit var btnBack: ImageView
    private lateinit var btnSubmit: Button
    private lateinit var imagePreview: ImageView

    private var customerId: String? = null
    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 102

    private var imageUri: Uri? = null
    private var imageFile: File? = null

    private val genderOptions = arrayOf("Laki-laki", "Perempuan")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profil)

        sharedPref = SharedPrefManager(this)
        viewModel = ViewModelProvider(this)[EditProfilViewModel::class.java]

        nik = findViewById(R.id.NIK)
        tempatLahir = findViewById(R.id.TempatLahir)
        tanggalLahir = findViewById(R.id.TanggalLahir)
        jenisKelaminSpinner = findViewById(R.id.JenisKelamin)
        telepon = findViewById(R.id.Telepon)
        alamat = findViewById(R.id.Alamat)
        ibuKandung = findViewById(R.id.IbuKandung)
        pekerjaan = findViewById(R.id.Pekerjaan)
        gaji = findViewById(R.id.Gaji)
        rekening = findViewById(R.id.Rekening)
        statusRumah = findViewById(R.id.StatusRumah)
        btnBack = findViewById(R.id.btnBack)
        btnSubmit = findViewById(R.id.btnAddDetail)
        imagePreview = findViewById(R.id.fotoProfil)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jenisKelaminSpinner.adapter = adapter

        tanggalLahir.setOnClickListener { showDatePicker() }
        btnBack.setOnClickListener { onBackPressed() }
        btnSubmit.setOnClickListener { showConfirmationDialog() }

        findViewById<Button>(R.id.btnUploadFoto).setOnClickListener { openGallery() }
        findViewById<Button>(R.id.btnAmbilFoto).setOnClickListener { openCamera() }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
            }
        }

        observeViewModel()
        getProfileData()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        imageFile = File.createTempFile("photo_", ".jpg", cacheDir)
        imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile!!)

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

//    private fun createImageFile(): File {
//        val fileName = "IMG_${System.currentTimeMillis()}"
//        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File.createTempFile(fileName, ".jpg", storageDir!!)
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    imageUri?.let {
                        imagePreview.setImageURI(it)
                        uploadImageToServer(it)
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        imageUri = selectedImageUri
                        imagePreview.setImageURI(imageUri)
                        uploadImageToServer(imageUri!!)
                    }
                }
            }
        }
    }

    private fun uploadImageToServer(uri: Uri) {
        val token = sharedPref.getToken() ?: return
        val id = customerId ?: return

        val filePath = uriToFile(uri)
        if (filePath != null) {
            val requestFile = filePath.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", filePath.name, requestFile)
            viewModel.uploadFotoProfil(token, id, body)
        } else {
            Toast.makeText(this, "Gagal membaca file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uriToFile(uri: Uri): File? {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            tanggalLahir.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin merubah data?")
            .setPositiveButton("Ya") { _, _ -> submitData() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun submitData() {
        val request = UserCustomerRequest(
            nik = nik.text.toString(),
            tempatLahir = tempatLahir.text.toString(),
            tanggalLahir = tanggalLahir.text.toString(),
            jenisKelamin = jenisKelaminSpinner.selectedItem.toString(),
            noTelp = telepon.text.toString(),
            alamat = alamat.text.toString(),
            namaIbuKandung = ibuKandung.text.toString(),
            pekerjaan = pekerjaan.text.toString(),
            gaji = gaji.text.toString().toIntOrNull() ?: 0,
            noRek = rekening.text.toString(),
            statusRumah = statusRumah.text.toString()
        )

        val token = sharedPref.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.editProfile(token, request)
    }

    private fun getProfileData() {
        val token = sharedPref.getToken()
        if (token != null) {
            viewModel.fetchProfileData(token)
        } else {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.fetchProfileResult.observe(this) { result ->
            result.onSuccess { updateUI(it) }
            result.onFailure {
                Toast.makeText(this, "Gagal memuat profil: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.editProfileResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ProfilSayaFragment::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                })
                finish()
            }
            result.onFailure {
                Toast.makeText(this, "Gagal memperbarui profil: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(profile: UserCustomerResponse) {
        customerId = profile.id.toString()
        nik.setText(profile.nik)
        tempatLahir.setText(profile.tempatLahir)
        tanggalLahir.setText(profile.tanggalLahir)
        jenisKelaminSpinner.setSelection(if (profile.jenisKelamin == "Laki-laki") 0 else 1)
        telepon.setText(profile.noTelp)
        alamat.setText(profile.alamat)
        ibuKandung.setText(profile.namaIbuKandung)
        pekerjaan.setText(profile.pekerjaan)
        gaji.setText(profile.gaji.toString())
        rekening.setText(profile.noRek)
        statusRumah.setText(profile.statusRumah)

        Glide.with(this)
            .load(profile.fotoUrl)
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
            .into(imagePreview)
    }
}
