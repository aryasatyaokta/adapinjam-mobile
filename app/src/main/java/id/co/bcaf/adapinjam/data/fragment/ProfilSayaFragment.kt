package id.co.bcaf.adapinjam.data.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.dhaval2404.imagepicker.ImagePicker
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.UserCustomerResponse
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.data.viewModel.ProfileViewModel
import id.co.bcaf.adapinjam.ui.EditProfil.EditProfilActivity
import id.co.bcaf.adapinjam.ui.login.LoginActivity
import id.co.bcaf.adapinjam.ui.profile.ProfilActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.jvm.java

class ProfilSayaFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var sharedPrefManager: SharedPrefManager

    private lateinit var nameProfile: TextView
    private lateinit var emailProfile: TextView

    private lateinit var nik: TextView
    private lateinit var tempatLahir: TextView
    private lateinit var tanggalLahir: TextView
    private lateinit var jenisKelamin: TextView
    private lateinit var noTelp: TextView
    private lateinit var alamat: TextView
    private lateinit var namaIbu: TextView
    private lateinit var pekerjaan: TextView
    private lateinit var gaji: TextView
    private lateinit var noRekening: TextView
    private lateinit var statusRumah: TextView
    private lateinit var btnBack: ImageView
    private lateinit var loadingProfile: ProgressBar

    private lateinit var ImageKtp: ImageView
    private lateinit var ImageSelfie: ImageView

    private lateinit var imageViewProfile: ImageView

    private var customerId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profil_saya, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefManager = SharedPrefManager(requireContext())
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        nameProfile = view.findViewById(R.id.nameProfile)
        emailProfile = view.findViewById(R.id.emailProfile)

        ImageKtp = view.findViewById(R.id.fotoKtpEdit)
        ImageSelfie = view.findViewById(R.id.fotoSelfieEdit)

        nik = view.findViewById(R.id.nik)
        tempatLahir = view.findViewById(R.id.tempatLahir)
        tanggalLahir = view.findViewById(R.id.tanggalLahir)
        jenisKelamin = view.findViewById(R.id.jenisKelamin)
        noTelp = view.findViewById(R.id.noTelp)
        alamat = view.findViewById(R.id.alamat)
        namaIbu = view.findViewById(R.id.namaIbu)
        pekerjaan = view.findViewById(R.id.pekerjaan)
        gaji = view.findViewById(R.id.gaji)
        noRekening = view.findViewById(R.id.noRekening)
        statusRumah = view.findViewById(R.id.statusRumah)
        btnBack = view.findViewById(R.id.btnBack)

        loadingProfile = view.findViewById(R.id.loadingProfile)

        imageViewProfile = view.findViewById(R.id.imageViewProfile)
        val imageViewEditIcon = view.findViewById<ImageView>(R.id.imageViewEditIcon)

        imageViewProfile.setOnClickListener { showImagePicker() }
        imageViewEditIcon.setOnClickListener { showImagePicker() }

        val btnEdit = view.findViewById<View>(R.id.btnEditProfile)
        btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditProfilActivity::class.java)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        profileViewModel.profileData.observe(viewLifecycleOwner) { profile ->
            loadingProfile.visibility = View.GONE
            if (profile != null) {
                updateUI(profile)
            } else {
                Toast.makeText(context, "Gagal ambil data profil", Toast.LENGTH_SHORT).show()
            }
        }

        profileViewModel.error.observe(viewLifecycleOwner) { error ->
            loadingProfile.visibility = View.GONE
            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
        }

        getProfileData()
    }

    private fun getProfileData() {
        val token = sharedPrefManager.getToken()
        if (token != null) {
            loadingProfile.visibility = View.VISIBLE
            profileViewModel.fetchProfileData("Bearer $token")
        } else {
            Toast.makeText(context, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(profile: UserCustomerResponse) {
        val fotoKtp = profile.fotoKtp // pastikan field ini sesuai dengan nama di model
        if (!fotoKtp.isNullOrEmpty()) {
            Glide.with(this)
                .load(fotoKtp)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_image) // gunakan drawable default
                .error(R.drawable.ic_image)
                .into(ImageKtp)
        }

        val fotoSelfie = profile.fotoSelfie // pastikan field ini sesuai dengan nama di model
        if (!fotoSelfie.isNullOrEmpty()) {
            Glide.with(this)
                .load(fotoSelfie)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_image) // gunakan drawable default
                .error(R.drawable.ic_image)
                .into(ImageSelfie)
        }
        customerId = profile.id?.toString()
        nameProfile.text = profile.user?.name ?: "-"
        emailProfile.text = profile.user?.email ?: "-"
        nik.text = profile.nik ?: "-"
        tempatLahir.text = profile.tempatLahir ?: "-"
        tanggalLahir.text = profile.tanggalLahir ?: "-"
        jenisKelamin.text = profile.jenisKelamin ?: "-"
        noTelp.text = profile.noTelp ?: "-"
        alamat.text = profile.alamat ?: "-"
        namaIbu.text = profile.namaIbuKandung ?: "-"
        pekerjaan.text = profile.pekerjaan ?: "-"
        gaji.text = profile.gaji ?: "-"
        noRekening.text = profile.noRek ?: "-"
        statusRumah.text = profile.statusRumah ?: "-"
        val fotoProfil = profile.fotoProfil // sesuaikan field-nya
        if (!fotoProfil.isNullOrEmpty()) {
            Glide.with(this)
                .load(fotoProfil)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop() // untuk membuat gambar menjadi bulat
                .into(imageViewProfile)
        }
    }

    private fun showImagePicker() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                val uri = data.data
                uri?.let {
                    imageViewProfile.setImageURI(uri)
                    uploadImageToServer(uri)
                }
            }
        }


    private fun uploadImageToServer(uri: Uri) {
        val context = requireContext()
        val token = sharedPrefManager.getToken()
        val id = customerId

        if (token.isNullOrEmpty() || id.isNullOrEmpty()) {
            Toast.makeText(context, "Token atau ID tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = DocumentFile.fromSingleUri(context, uri)?.name ?: "profile_image.jpg"
        val file = File(context.cacheDir, fileName)

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.uploadProfil(
                    "Bearer $token", id, multipartBody
                )
                if (response.isSuccessful) {
                    Toast.makeText(context, "Upload berhasil", Toast.LENGTH_SHORT).show()
                    getProfileData()
                } else {
                    Toast.makeText(context, "Gagal upload: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

