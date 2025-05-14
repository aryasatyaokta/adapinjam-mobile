package id.co.bcaf.adapinjam.ui.EditProfil

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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

    private lateinit var fotoProfil: ImageView
    private lateinit var btnUploadFoto: Button
    private var selectedImageUri: Uri? = null

    private var customerId: String? = null
    private val REQUEST_IMAGE_CAPTURE = 1002
    private var photoFile: File? = null

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

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jenisKelaminSpinner.adapter = adapter

        tanggalLahir.setOnClickListener { showDatePicker() }
        btnBack.setOnClickListener { onBackPressed() }
        btnSubmit.setOnClickListener { showConfirmationDialog() }

        fotoProfil = findViewById(R.id.fotoProfil)
        btnUploadFoto = findViewById(R.id.btnUploadFoto)

        btnUploadFoto.setOnClickListener {
            openGalleryForImage()
        }

        val token = intent.getStringExtra("token")

        observeViewModel()
        getProfileData()  // Fetch profile data when the activity is created
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            tanggalLahir.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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
        Log.d("EditProfilActivity", "Token yang digunakan: $token")
        if (token != null) {
            viewModel.fetchProfileData(token)
        } else {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.fetchProfileResult.observe(this) { result ->
            result.onSuccess {
                updateUI(it)
            }
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
            .load(profile.fotoUrl)  // asumsi ini adalah URL dari server
            .placeholder(R.drawable.ic_image)  // ganti sesuai kebutuhan
            .error(R.drawable.ic_image)
            .into(fotoProfil)

    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            fotoProfil.setImageURI(selectedImageUri)

            // Kirim ke server (jika diperlukan langsung)
            selectedImageUri?.let {
                uploadImageToServer(it)
            }
        }
    }

    private fun uploadImageToServer(uri: Uri) {
        val token = sharedPref.getToken() ?: return
        val id = customerId ?: return  // pastikan ID sudah ada
        val file = File(getRealPathFromURI(uri))
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        viewModel.uploadFotoProfil(token, id, body)
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val filePath = columnIndex?.let { cursor.getString(it) } ?: ""
        cursor?.close()
        return filePath
    }

    private fun openCameraForImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = createImageFile()

        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",  // pastikan sesuai
            photoFile!!
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun createImageFile(): File {
        val fileName = "JPEG_${System.currentTimeMillis()}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDir!!)
    }


}
