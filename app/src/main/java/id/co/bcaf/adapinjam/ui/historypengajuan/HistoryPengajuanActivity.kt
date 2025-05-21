package id.co.bcaf.adapinjam.ui.historypengajuan

import HistoryPengajuanAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.fragment.HistoryFragment
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import kotlinx.coroutines.launch

class HistoryPengajuanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_pengajuan)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val adapter = object : FragmentStateAdapter(this) {
            private val statusFilters = listOf(
                Pair("Disetujui", "DISBURSEMENT"),
                Pair("Direview", "REVIEW"),
                Pair("Ditolak", "REJECT")
            )

            override fun getItemCount() = statusFilters.size
            override fun createFragment(position: Int): Fragment {
                return HistoryFragment(statusFilters[position].second)
            }
        }

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Disetujui"
                1 -> tab.text = "Direview"
                2 -> tab.text = "Ditolak"
            }
        }.attach()
    }
}
