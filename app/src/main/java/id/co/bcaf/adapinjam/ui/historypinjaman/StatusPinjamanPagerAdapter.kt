package id.co.bcaf.adapinjam.ui.historypinjaman

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class StatusPinjamanPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2
    override fun createFragment(position: Int): Fragment {
        return if (position == 0) LunasPinjamanFragment() else BelumLunasPinjamanFragment()
    }
}
