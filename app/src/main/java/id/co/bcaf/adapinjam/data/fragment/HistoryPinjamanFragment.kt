package id.co.bcaf.adapinjam.data.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.ui.historypinjaman.HistoryPinjamanAdapter
import id.co.bcaf.adapinjam.ui.historypinjaman.StatusPinjamanPagerAdapter
import kotlinx.coroutines.launch

class HistoryPinjamanFragment : Fragment() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history_pinjaman, container, false)

        sharedPrefManager = SharedPrefManager(requireContext())

        viewPager = view.findViewById(R.id.viewPagerPinjaman)
        tabLayout = view.findViewById(R.id.tabLayoutStatus)

        viewPager.adapter = StatusPinjamanPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Lunas" else "Belum Lunas"
        }.attach()

        return view
    }
}

