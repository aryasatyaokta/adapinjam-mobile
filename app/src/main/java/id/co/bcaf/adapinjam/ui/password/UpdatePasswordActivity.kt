package id.co.bcaf.adapinjam.ui.password

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.UpdatePasswordRequest
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.ui.login.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdatePasswordActivity : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_password)

        sharedPrefManager = SharedPrefManager(this)

        val btnUpdatedPassword = findViewById<Button>(R.id.btnUpdatedPassword)
        val oldPasswordEditText = findViewById<EditText>(R.id.oldPassword)
        val newPasswordEditText = findViewById<EditText>(R.id.newPassword)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mengubah password...")
        progressDialog.setCancelable(false)

        btnUpdatedPassword.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()

            if (oldPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin mengubah password?")
                    .setPositiveButton("Ya") { _, _ ->
                        updatePassword(oldPassword, newPassword)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            } else {
                Toast.makeText(this, "Harap isi Password Lama dan Baru", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassword(oldPassword: String, newPassword: String) {
        val token = sharedPrefManager.getToken()
        if (token != null) {
            progressDialog.show()

            lifecycleScope.launch(Dispatchers.Main) {
                val response = withContext(Dispatchers.IO) {
                    val request = UpdatePasswordRequest(oldPassword, newPassword)
                    RetrofitClient.apiService.updatePassword("Bearer $token", request)
                }

                progressDialog.dismiss()

                if (response.isSuccessful) {
                    Toast.makeText(this@UpdatePasswordActivity, "Password berhasil diubah. Silakan login ulang.", Toast.LENGTH_SHORT).show()

                    // Hapus token dan arahkan ke LoginActivity
                    sharedPrefManager.clearToken()

                    val intent = Intent(this@UpdatePasswordActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@UpdatePasswordActivity, "Password gagal diubah", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }


}

