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
import android.widget.LinearLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import id.co.bcaf.adapinjam.data.model.GoogleAuthRequest
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.ui.adddetailcustomer.AddDetail
import id.co.bcaf.adapinjam.ui.password.ResetPasswordActivity
import id.co.bcaf.adapinjam.ui.password.UpdatePasswordActivity
import id.co.bcaf.adapinjam.utils.setupPasswordToggle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        sharedPrefManager = SharedPrefManager(this)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val edtUsername = findViewById<EditText>(R.id.Email)
        val edtPassword = findViewById<EditText>(R.id.Password)

        val btnForgotPass = findViewById<TextView>(R.id.ForgotPassword)
        btnForgotPass.setOnClickListener {
            val intent = Intent(this@LoginActivity, ResetPasswordActivity::class.java)
            startActivity(intent)
        }

        edtPassword.setupPasswordToggle()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Logging in...")
        progressDialog.setCancelable(false)

        loginViewModel.loginResult.observe(this) { result ->
            progressDialog.dismiss()
            result.onSuccess { token ->
                sharedPrefManager.setToken(token)
                Log.d("LoginActivity", "Token disimpan: $token")

                showSnackbar(findViewById(android.R.id.content), "Login berhasil")
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(5000)
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("544904337448-cc259lkh47qnsar5t3bupo1i6mi0kpdk.apps.googleusercontent.com") // Ganti dengan Web client ID dari Google Console
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<SignInButton>(R.id.btnGoogleSignIn).setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                Log.d("ID_TOKEN", idToken ?: "null")
                if (idToken != null) {
                    loginWithGoogleIdToken(idToken)
                } else {
                    Toast.makeText(this, "Token Google tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                e.printStackTrace()
                Toast.makeText(this, "Google Sign-In gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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

    private fun loginWithGoogleIdToken(idToken: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.loginWithGoogle(GoogleAuthRequest(idToken))
                sharedPrefManager.setToken(response.token)

                showSnackbar(findViewById(android.R.id.content), "Login berhasil, cek email. Password sudah terkirim")

                kotlinx.coroutines.delay(5000)
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, "Login Google gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }



}
