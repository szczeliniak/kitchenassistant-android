package pl.szczeliniak.kitchenassistant.android.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityMainBinding
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.login.LoginActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.main.fragments.ReceiptsFragment
import pl.szczeliniak.kitchenassistant.android.ui.utils.init
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val SAVED_FRAGMENT_NAME = "SAVED_FRAGMENT_NAME"

        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var localStorageService: LocalStorageService

    private lateinit var binding: ActivityMainBinding

    private val receiptsFragment = ReceiptsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activityMainToolbarLayout.toolbar.init(this, R.drawable.icon_menu) {
            binding.activityMainDrawerLayout.open()
        }

        binding.activityMainBottomNavView.setOnItemSelectedListener { onNavigationItemChanged(it.itemId) }
        binding.activityMainNavView.setNavigationItemSelectedListener { onDrawerItemClicked(it.itemId) }

        setInitialFragment(savedInstanceState)
    }

    private fun onNavigationItemChanged(itemId: Int): Boolean {
        when (itemId) {
            R.id.nav_bottom_item_receipts -> {
                setCurrentFragment(receiptsFragment)
                return true
            }
        }
        return false
    }

    private fun setCurrentFragment(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        replace(R.id.activity_main_fragment_container, fragment)
        commit()
    }

    private fun onDrawerItemClicked(itemId: Int): Boolean {
        binding.root.closeDrawers()
        when (itemId) {
            R.id.menu_nav_view_item_logout -> {
                logout()
                return true
            }
        }
        return false
    }

    private fun logout() {
        if (localStorageService.logout()) {
            LoginActivity.start(this)
            finish()
        }
    }

    private fun setInitialFragment(savedInstanceState: Bundle?) {
        val fragmentClassName = savedInstanceState?.getString(SAVED_FRAGMENT_NAME)
        if (fragmentClassName == null) {
            setCurrentFragment(receiptsFragment)
        }
        when (fragmentClassName) {
            ReceiptsFragment::class.java.simpleName -> {
                setCurrentFragment(receiptsFragment)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (supportFragmentManager.fragments.size > 0) {
            outState.putString(SAVED_FRAGMENT_NAME, supportFragmentManager.fragments[0]::class.java.simpleName)
        }
    }

}