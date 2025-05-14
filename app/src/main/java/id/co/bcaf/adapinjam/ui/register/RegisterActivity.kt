package id.co.bcaf.adapinjam.ui.register

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.ui.login.LoginActivity
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.data.viewModel.RegisterViewModel
import id.co.bcaf.adapinjam.utils.setupPasswordToggle

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sharedPrefManager = SharedPrefManager(this)
        registerViewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val edtUsername = findViewById<EditText>(R.id.Email)
        val edtPassword = findViewById<EditText>(R.id.Password)
        val edtName = findViewById<EditText>(R.id.Name)

        edtPassword.setupPasswordToggle()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Registering...")
        progressDialog.setCancelable(false)

        registerViewModel.registerResult.observe(this) { result ->
            progressDialog.dismiss()
            result.onSuccess { token ->
                sharedPrefManager.setToken(token)
                showSnackbar(findViewById(android.R.id.content), "Registrasi berhasil! Silakan cek email untuk verifikasi.")
                window.decorView.postDelayed({
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }, 5000) // 5000 = 5 detik
            }

            result.onFailure { e ->
                showSnackbar(findViewById(android.R.id.content), e.message ?: "Registrasi gagal", isError = true)
            }
        }

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
                    // 🔔 Tampilkan dialog konfirmasi sebelum submit
                    AlertDialog.Builder(this)
                        .setTitle("Konfirmasi")
                        .setMessage("Apakah data Anda sudah benar?")
                        .setPositiveButton("Ya") { _, _ ->
                            progressDialog.show()
                            registerViewModel.register(username, password, name)
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                }
            }
        }


        findViewById<TextView>(R.id.Login).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showSnackbar(view: View, message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(
            if (isError) Color.parseColor("#D32F2F") else Color.parseColor("#388E3C")
        )
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }
}
