package id.co.bcaf.adapinjam.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.fragment.HomeFragment
import id.co.bcaf.adapinjam.data.fragment.RiwayatFragment
import id.co.bcaf.adapinjam.ui.pengajuan.PengajuanActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnAjukan = findViewById<Button>(R.id.ajukanButton)
        btnAjukan.setOnClickListener {
            val intent = Intent(this, PengajuanActivity::class.java)
            startActivity(intent)
            finish()
        }

//        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
//
//        // Load default fragment
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, HomeFragment())
//            .commit()
//
//        bottomNavigation.setOnNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_home -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, HomeFragment())
//                        .commit()
//                    true
//                }
//                R.id.nav_riwayat -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, RiwayatFragment())
//                        .commit()
//                    true
//                }
//                else -> false
//            }
//        }
    }
}
