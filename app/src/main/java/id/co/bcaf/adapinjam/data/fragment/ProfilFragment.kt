package id.co.bcaf.adapinjam.data.fragment

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.ui.EditProfil.EditProfilActivity
import id.co.bcaf.adapinjam.ui.adddetailcustomer.AddDetail
import id.co.bcaf.adapinjam.ui.login.LoginActivity
import id.co.bcaf.adapinjam.ui.password.UpdatePasswordActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class ProfilFragment : Fragment() {

    private lateinit var sharedPrefManager: SharedPrefManager

    private lateinit var btnLoginProfil : LinearLayout
    private lateinit var lineTop : View

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profil, container, false)
        sharedPrefManager = SharedPrefManager(requireContext())

        btnLoginProfil = view.findViewById(R.id.btnLogin)
        lineTop = view.findViewById(R.id.lineTop)

        btnLoginProfil.setOnClickListener {
            context?.let {
                AlertDialog.Builder(it)
                    .setTitle("Login Diperlukan")
                    .setMessage("Silakan login terlebih dahulu untuk melanjutkan.")
                    .setPositiveButton("Ke Page Login") { _, _ ->
                        val intent = Intent(it, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    .setNegativeButton("Batal", null)
                    .setCancelable(false)
                    .show()
            }
        }

        val btnLogout = view.findViewById<LinearLayout>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        val btnUpdatePass = view.findViewById<LinearLayout>(R.id.btnUpdatePasswordProfil)
        btnUpdatePass.setOnClickListener {
            val intent = Intent(requireContext(), UpdatePasswordActivity::class.java)
            startActivity(intent)
        }

        val btnProfilSaya = view.findViewById<LinearLayout>(R.id.btnProfilSaya)
        btnProfilSaya.setOnClickListener {
            checkProfileBeforeOpenDetail()
        }

        val btnBantuan = view.findViewById<LinearLayout>(R.id.btnBantuan)
        btnBantuan.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Bantuan")
                .setMessage("Apakah Anda yakin membutuhkan bantuan?")
                .setPositiveButton("Ya") { _, _ ->
                    val phoneNumber = "6281646886785" // format internasional TANPA +
                    val message = "Halo, saya butuh bantuan dengan aplikasi Adapinjam"
                    val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${URLEncoder.encode(message, "UTF-8")}"

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setPackage("com.whatsapp") // memastikan intent langsung ke WhatsApp
                    intent.data = Uri.parse(url)

                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(requireContext(), "WhatsApp tidak ditemukan di perangkat ini", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }



        val token = sharedPrefManager.getToken()
        val hasToken = !token.isNullOrEmpty()

        if (hasToken) {
            btnUpdatePass.visibility = View.VISIBLE
            btnProfilSaya.visibility = View.VISIBLE
            btnLogout.visibility = View.VISIBLE
            btnBantuan.visibility = View.VISIBLE
            btnLoginProfil.visibility = View.GONE
            lineTop.visibility = View.GONE
        } else {
            btnUpdatePass.visibility = View.GONE
            btnProfilSaya.visibility = View.GONE
            btnLogout.visibility = View.GONE
            btnBantuan.visibility = View.GONE
            btnLoginProfil.visibility = View.VISIBLE
            lineTop.visibility = View.VISIBLE
        }

        return view
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

    private fun performLogout() {
        val token = sharedPrefManager.getToken()
        if (token != null) {
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.logout("Bearer $token")
                    if (response.isSuccessful) {
                        sharedPrefManager.clearToken()
                        Toast.makeText(context, "Logout berhasil", Toast.LENGTH_SHORT).show()
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

    private fun checkProfileBeforeOpenDetail() {
        val token = sharedPrefManager.getToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.checkProfile("Bearer $token")
                }

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful -> {
                            // Profil lengkap → buka ProfilSayaFragment
                            val fragment = ProfilSayaFragment()
                            val bundle = Bundle()
                            bundle.putString("token", token)
                            fragment.arguments = bundle

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }

                        response.code() == 428 -> {
                            // Profil belum lengkap → tampilkan dialog
                            android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Lengkapi Profil")
                                .setMessage("Profil Anda belum lengkap. Silakan lengkapi terlebih dahulu.")
                                .setPositiveButton("Lengkapi Sekarang") { _, _ ->
                                    startActivity(Intent(requireContext(), AddDetail::class.java))
                                }
                                .setCancelable(false)
                                .show()
                        }

                        else -> {
                            Toast.makeText(
                                requireContext(),
                                "Gagal memeriksa status profil: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
