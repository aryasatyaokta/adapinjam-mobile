package id.co.bcaf.adapinjam.ui.login

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.ui.home.HomeActivity
import id.co.bcaf.adapinjam.ui.register.RegisterActivity
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.data.viewModel.LoginViewModel
import kotlinx.coroutines.launch
import android.app.ProgressDialog
import android.graphics.Color
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.ui.adddetailcustomer.AddDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        sharedPrefManager = SharedPrefManager(this)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val edtUsername = findViewById<EditText>(R.id.Email)
        val edtPassword = findViewById<EditText>(R.id.Password)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Logging in...")
        progressDialog.setCancelable(false)

        loginViewModel.loginResult.observe(this) { result ->
            progressDialog.dismiss()
            result.onSuccess { token ->
                sharedPrefManager.setToken(token)
                Log.d("LoginActivity", "Token disimpan: $token")

                showSnackbar(findViewById(android.R.id.content), "Login berhasil")

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            result.onFailure { e ->
                showSnackbar(findViewById(android.R.id.content), e.message ?: "Login gagal", isError = true)
            }
        }

        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString()

            when {
                username.isEmpty() -> {
                    edtUsername.error = "Email harus diisi"
                    edtUsername.requestFocus()
                }
                password.isEmpty() -> {
                    edtPassword.error = "Password harus diisi"
                    edtPassword.requestFocus()
                }
                else -> {
                    progressDialog.show()
                    loginViewModel.login(username, password)
                }
            }
        }

        findViewById<TextView>(R.id.Register).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
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
