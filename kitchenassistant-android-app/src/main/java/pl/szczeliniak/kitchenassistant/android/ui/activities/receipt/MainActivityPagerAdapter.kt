package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainActivityPagerAdapter(
    private val fragments: Array<Fragment>,
    supportFragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(supportFragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}