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
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.UserCustomerResponse
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.data.viewModel.ProfileViewModel
import id.co.bcaf.adapinjam.ui.login.LoginActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

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

    private lateinit var loadingProfile: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        sharedPrefManager = SharedPrefManager(requireContext())
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        nameProfile = view.findViewById(R.id.nameProfile)
        emailProfile = view.findViewById(R.id.emailProfile)

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

        loadingProfile = view.findViewById(R.id.loadingProfile)

        val btnLogout = view.findViewById<ImageView>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
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

        return view
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

    private fun performLogout() {
        val token = sharedPrefManager.getToken()
        if (token != null) {
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.logout("Bearer $token")
                    if (response.isSuccessful) {
                        // Clear token and navigate to LoginActivity
                        sharedPrefManager.clearToken() // Clear all saved data (token, etc.)
                        Toast.makeText(context, "Logout berhasil", Toast.LENGTH_SHORT).show()

                        // Arahkan ke LoginActivity
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(context, "Logout gagal", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutConfirmation() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Konfirmasi Logout")
        builder.setMessage("Apakah Anda ingin keluar aplikasi?")
        builder.setPositiveButton("Ya") { dialog, _ ->
            dialog.dismiss()
            performLogout()
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }


}
