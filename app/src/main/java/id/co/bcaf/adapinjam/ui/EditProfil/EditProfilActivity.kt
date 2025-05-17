package id.co.bcaf.adapinjam.ui.EditProfil

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
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
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
    private lateinit var imageKtp: ImageView
    private lateinit var imageSelfie: ImageView

    private var customerId: String? = null
    private val REQUEST_KTP_CAMERA = 101
    private val REQUEST_SELFIE_CAMERA = 102
    private val REQUEST_KTP_GALLERY = 103
    private val REQUEST_SELFIE_GALLERY = 104

    private var uriKtp: Uri? = null
    private var uriSelfie: Uri? = null

    private var fileKtp: File? = null
    private var fileSelfie: File? = null

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
        imageKtp = findViewById(R.id.fotoKtp)
        imageSelfie = findViewById(R.id.fotoSelfie)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jenisKelaminSpinner.adapter = adapter

        tanggalLahir.setOnClickListener { showDatePicker() }
        btnBack.setOnClickListener { onBackPressed() }
        btnSubmit.setOnClickListener { showConfirmationDialog() }

        findViewById<Button>(R.id.btnKtpCameraEdit).setOnClickListener {
            openCamera(REQUEST_KTP_CAMERA)
        }

        findViewById<Button>(R.id.btnKtpGalleryEdit).setOnClickListener {
            openGallery(REQUEST_KTP_GALLERY)
        }

        findViewById<Button>(R.id.btnSelfieCameraEdit).setOnClickListener {
            openCamera(REQUEST_SELFIE_CAMERA)
        }

        findViewById<Button>(R.id.btnSelfieGalleryEdit).setOnClickListener {
            openGallery(REQUEST_SELFIE_GALLERY)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
            }
        }
        observeViewModel()
        getProfileData()
    }

    private fun openCamera(requestCode: Int) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        when (requestCode) {
            REQUEST_KTP_CAMERA -> {
                fileKtp = File.createTempFile("ktp_", ".jpg", cacheDir)
                uriKtp = FileProvider.getUriForFile(this, "${packageName}.provider", fileKtp!!)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriKtp)
            }
            REQUEST_SELFIE_CAMERA -> {
                fileSelfie = File.createTempFile("selfie_", ".jpg", cacheDir)
                uriSelfie = FileProvider.getUriForFile(this, "${packageName}.provider", fileSelfie!!)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSelfie)
            }
            else -> return
        }

        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(cameraIntent, requestCode)
    }

    private fun openGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, requestCode)
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
                REQUEST_KTP_CAMERA -> {
                    uriKtp?.let {
                        imageKtp.setImageURI(it)
                        fileKtp = uriToFile(it)
                        uploadImageToServer()
                    }
                }
                REQUEST_SELFIE_CAMERA -> {
                    uriSelfie?.let {
                        imageSelfie.setImageURI(it)
                        fileSelfie = uriToFile(it)
                        uploadImageToServer()
                    }
                }
                REQUEST_KTP_GALLERY -> {
                    val selectedUri = data?.data
                    if (selectedUri != null) {
                        uriKtp = selectedUri
                        imageKtp.setImageURI(selectedUri)
                        fileKtp = uriToFile(selectedUri)
                        uploadImageToServer()
                    }
                }
                REQUEST_SELFIE_GALLERY -> {
                    val selectedUri = data?.data
                    if (selectedUri != null) {
                        uriSelfie = selectedUri
                        imageSelfie.setImageURI(selectedUri)
                        fileSelfie = uriToFile(selectedUri)
                        uploadImageToServer()
                    }
                }
            }
        }
    }

    private fun uploadImageToServer() {
        val token = sharedPref.getToken() ?: return
        val id = customerId ?: return

        val partKtp = fileKtp?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("fotoKtp", it.name, requestFile)
        }

        val partSelfie = fileSelfie?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("fotoSelfie", it.name, requestFile)
        }

        if (partKtp == null && partSelfie == null) {
            Toast.makeText(this, "Silakan unggah minimal satu foto", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.uploadFotoProfil(token, id, partKtp, partSelfie)
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
        viewModel.profileLiveData.observe(this) { response ->
            if (response != null) {
                customerId = response.id.toString()
                nik.setText(response.nik)
                tempatLahir.setText(response.tempatLahir)
                tanggalLahir.setText(response.tanggalLahir)
                jenisKelaminSpinner.setSelection(genderOptions.indexOf(response.jenisKelamin))
                telepon.setText(response.noTelp)
                alamat.setText(response.alamat)
                ibuKandung.setText(response.namaIbuKandung)
                pekerjaan.setText(response.pekerjaan)
                gaji.setText(response.gaji?.toString() ?: "")
                rekening.setText(response.noRek)
                statusRumah.setText(response.statusRumah)

                Glide.with(this)
                    .load(response.fotoKtp)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageKtp)

                Glide.with(this)
                    .load(response.fotoSelfie)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageSelfie)
            }
        }

        viewModel.uploadFotoResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Foto berhasil diupload", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, "Gagal upload foto", Toast.LENGTH_SHORT).show()
            }
        }


        viewModel.editProfileResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish() // kembali ke layar sebelumnya
            }.onFailure {
                Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
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
            .load(profile.fotoKtp)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
            .into(imageKtp)

        Glide.with(this)
            .load(profile.fotoSelfie)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
            .into(imageSelfie)
    }
}
