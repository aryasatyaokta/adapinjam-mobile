package id.co.bcaf.adapinjam.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.fragment.HistoryPinjamanFragment
import id.co.bcaf.adapinjam.data.fragment.HomeFragment
import id.co.bcaf.adapinjam.data.fragment.RiwayatFragment
import id.co.bcaf.adapinjam.ui.pengajuan.PengajuanActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Tampilkan HomeFragment saat pertama kali
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Listener untuk bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener() { item ->
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
                // Tambahkan menu lainnya jika perlu
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
