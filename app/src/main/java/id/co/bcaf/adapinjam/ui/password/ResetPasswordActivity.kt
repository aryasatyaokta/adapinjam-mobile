package id.co.bcaf.adapinjam.ui.password

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var emailReset: EditText
    private lateinit var btnUpdatedPassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        emailReset = findViewById(R.id.emailReset)
        btnUpdatedPassword = findViewById(R.id.btnUpdatedPassword)

        btnUpdatedPassword.setOnClickListener {
            val email = emailReset.text.toString().trim()

            // Validasi
            if (email.isEmpty()) {
                emailReset.error = "Email tidak boleh kosong"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailReset.error = "Format email tidak valid"
                return@setOnClickListener
            }

            // Kirim permintaan reset password
            sendResetPasswordRequest(email)
        }
    }

    private fun sendResetPasswordRequest(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBody = mapOf("email" to email)
                val response: Response<ResponseBody> = RetrofitClient.apiService.forgotPassword(requestBody)

                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Email reset password telah dikirim. Silakan cek email Anda.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Email tidak ditemukan atau terjadi kesalahan.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Gagal mengirim permintaan. Coba lagi nanti.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
