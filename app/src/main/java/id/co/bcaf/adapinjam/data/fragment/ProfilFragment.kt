package id.co.bcaf.adapinjam.data.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.ui.EditProfil.EditProfilActivity
import id.co.bcaf.adapinjam.ui.login.LoginActivity
import id.co.bcaf.adapinjam.ui.password.UpdatePasswordActivity
import kotlinx.coroutines.launch

class ProfilFragment : Fragment() {

    private lateinit var sharedPrefManager: SharedPrefManager

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profil, container, false)
        sharedPrefManager = SharedPrefManager(requireContext())

        val btnLogout = view.findViewById<LinearLayout>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        val btnUpdatePass = view.findViewById<LinearLayout>(R.id.btnUpdatePassword)
        btnUpdatePass.setOnClickListener {
            Toast.makeText(requireContext(), "Tombol diklik", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), UpdatePasswordActivity::class.java)
            startActivity(intent)
        }

        val btnProfilSaya = view.findViewById<LinearLayout>(R.id.btnProfilSaya)
        btnProfilSaya.setOnClickListener {
            Toast.makeText(requireContext(), "Tombol diklik", Toast.LENGTH_SHORT).show()
            val token = sharedPrefManager.getToken()

            val fragment = ProfilSayaFragment()

            // Jika kamu perlu kirim token ke fragment:
            val bundle = Bundle()
            bundle.putString("token", token)
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment) // pastikan ID-nya sesuai container fragment-mu
                .addToBackStack(null)
                .commit()
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
}
