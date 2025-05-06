package id.co.bcaf.adapinjam.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.RegisterRequest
import id.co.bcaf.adapinjam.data.model.RegisterResponse
import id.co.bcaf.adapinjam.ui.home.HomeActivity
import id.co.bcaf.adapinjam.ui.login.LoginActivity
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response
import android.app.ProgressDialog
import android.graphics.Color
import android.view.View
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private val apiService = RetrofitClient.apiService // Mengambil apiService dari RetrofitClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val edtUsername = findViewById<EditText>(R.id.Email)
        val edtPassword = findViewById<EditText>(R.id.Password)
        val edtName = findViewById<EditText>(R.id.Name)

        btnRegister.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString()
            val name = edtName.text.toString()

            when {
                username.isEmpty() -> {
                    edtUsername.error = "Email harus diisi"
                    edtUsername.requestFocus()
                }
                password.isEmpty() -> {
                    edtPassword.error = "Password harus diisi"
                    edtPassword.requestFocus()
                }
                name.isEmpty() -> {
                    edtName.error = "Nama harus diisi"
                    edtName.requestFocus()
                }
                else -> {
                    register(username, password, name)
                }
            }
        }

        val loginText = findViewById<TextView>(R.id.Login)
        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showSnackbar(view: View, message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(Color.parseColor("#D32F2F")) // Red
        } else {
            snackbar.setBackgroundTint(Color.parseColor("#388E3C")) // Green
        }
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }

    private fun register(username: String, password: String, name: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Registering...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        lifecycleScope.launch {
            try {
                val response: Response<RegisterResponse> = apiService.register(RegisterRequest(username, password, name))
                progressDialog.dismiss()

                if (response.isSuccessful) {
                    val token = response.body()?.token

                    if (!token.isNullOrEmpty()) {
                        // Simpan token
                        getSharedPreferences("MyPrefs", MODE_PRIVATE).edit()
                            .putString("auth_token", token)
                            .apply()

                        showSnackbar(findViewById(android.R.id.content), "Registrasi berhasil")

                        // Pindah ke Home
                        val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showSnackbar(findViewById(android.R.id.content), "Token tidak valid", isError = true)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    showSnackbar(findViewById(android.R.id.content), "Registrasi gagal: $errorBody", isError = true)
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                e.printStackTrace()
                showSnackbar(findViewById(android.R.id.content), "Gagal terhubung ke server", isError = true)
            }
        }
    }
}
