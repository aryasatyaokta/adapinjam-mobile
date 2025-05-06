package id.co.bcaf.adapinjam.data.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.UserCustomerResponse
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var nameProfile: TextView
    private lateinit var emailProfile: TextView

    private lateinit var nik: TextView
    private lateinit var tempatTglLahir: TextView
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

        nameProfile = view.findViewById(R.id.nameProfile)
        emailProfile = view.findViewById(R.id.emailProfile)

        nik = view.findViewById(R.id.nik)
        tempatTglLahir = view.findViewById(R.id.tempatTglLahir)
        noTelp = view.findViewById(R.id.noTelp)
        alamat = view.findViewById(R.id.alamat)
        namaIbu = view.findViewById(R.id.namaIbu)
        pekerjaan = view.findViewById(R.id.pekerjaan)
        gaji = view.findViewById(R.id.gaji)
        noRekening = view.findViewById(R.id.noRekening)
        statusRumah = view.findViewById(R.id.statusRumah)

        loadingProfile = view.findViewById(R.id.loadingProfile)

        getProfileData()

        return view
    }

    private fun getProfileData() {
        val token = sharedPrefManager.getToken()
        Log.d("ProfileFragment", "Token: $token")

        if (token == null) {
            Toast.makeText(context, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        loadingProfile.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getCustomerProfile("Bearer $token")
                }

                if (response.isSuccessful) {
                    val data: UserCustomerResponse? = response.body()
                    data?.let { updateUI(it) }
                } else {
                    Toast.makeText(context, "Gagal ambil data profil", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                loadingProfile.visibility = View.GONE
            }
        }
    }

    private fun updateUI(data: UserCustomerResponse) {
        nameProfile.text = data.user?.name ?: "-"
        emailProfile.text = data.user?.email ?: "-"
        nik.text = data.nik ?: "-"
        tempatTglLahir.text = data.tempatTglLahir ?: "-"
        noTelp.text = data.noTelp ?: "-"
        alamat.text = data.alamat ?: "-"
        namaIbu.text = data.namaIbuKandung ?: "-"
        pekerjaan.text = data.pekerjaan ?: "-"
        gaji.text = data.gaji ?: "-"
        noRekening.text = data.noRek ?: "-"
        statusRumah.text = data.statusRumah ?: "-"
    }
}
