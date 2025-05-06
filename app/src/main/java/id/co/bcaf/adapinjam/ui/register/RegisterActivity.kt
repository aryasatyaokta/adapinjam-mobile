package id.co.bcaf.adapinjam.ui.register

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
import id.co.bcaf.adapinjam.ui.home.HomeActivity
import id.co.bcaf.adapinjam.ui.login.LoginActivity
import id.co.bcaf.adapinjam.data.viewModel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerViewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val edtUsername = findViewById<EditText>(R.id.Email)
        val edtPassword = findViewById<EditText>(R.id.Password)
        val edtName = findViewById<EditText>(R.id.Name)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Registering...")
        progressDialog.setCancelable(false)

        registerViewModel.registerResult.observe(this) { result ->
            progressDialog.dismiss()
            result.onSuccess { token ->
                getSharedPreferences("MyPrefs", MODE_PRIVATE).edit()
                    .putString("auth_token", token)
                    .apply()

                showSnackbar(findViewById(android.R.id.content), "Registrasi berhasil")

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
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
                    progressDialog.show()
                    registerViewModel.register(username, password, name)
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
