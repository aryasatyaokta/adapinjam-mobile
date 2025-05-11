package id.co.bcaf.adapinjam.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.fragment.HistoryPinjamanFragment
import id.co.bcaf.adapinjam.data.fragment.HomeFragment
import id.co.bcaf.adapinjam.data.fragment.ProfilFragment
import id.co.bcaf.adapinjam.data.fragment.ProfilSayaFragment
import id.co.bcaf.adapinjam.data.fragment.RiwayatFragment
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.ui.login.LoginActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefManager = SharedPrefManager(this)
        val token = sharedPrefManager.getToken()
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_riwayat -> {
                    loadFragment(RiwayatFragment())
                    true
                }
                R.id.nav_pinjaman -> {
                    loadFragment(HistoryPinjamanFragment())
                    true
                }
                R.id.nav_akun -> {
                    loadFragment(ProfilFragment())
                    true
                }
                else -> false
            }
        }
    }


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
