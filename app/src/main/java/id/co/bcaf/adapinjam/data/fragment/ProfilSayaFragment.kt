package id.co.bcaf.adapinjam.data.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.UserCustomerResponse
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.data.viewModel.ProfileViewModel
import id.co.bcaf.adapinjam.ui.EditProfil.EditProfilActivity
import id.co.bcaf.adapinjam.ui.login.LoginActivity
import id.co.bcaf.adapinjam.ui.profile.ProfilActivity
import kotlinx.coroutines.launch
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

    private lateinit var fotoKtp: ImageView

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

        fotoKtp = view.findViewById(R.id.fotoKtp)

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
        val fotoUrl = profile.fotoUrl // pastikan field ini sesuai dengan nama di model
        if (!fotoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(fotoUrl)
                .placeholder(R.drawable.ic_image) // gunakan drawable default
                .error(R.drawable.ic_image)
                .into(fotoKtp)
        }

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
    }
}

