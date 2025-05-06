package id.co.bcaf.adapinjam

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.co.bcaf.adapinjam.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Memulai LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
