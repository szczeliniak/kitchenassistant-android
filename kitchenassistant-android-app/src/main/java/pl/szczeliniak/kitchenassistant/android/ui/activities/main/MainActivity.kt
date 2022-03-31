package pl.szczeliniak.kitchenassistant.android.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityMainBinding
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.login.LoginActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.init
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var localStorageService: LocalStorageService

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarLayout.toolbar.init(this, R.drawable.icon_menu) {
            binding.drawerLayout.open()
        }

        binding.navigationView.setNavigationItemSelectedListener { onDrawerItemClicked(it.itemId) }

    }

    private fun onDrawerItemClicked(itemId: Int): Boolean {
        binding.root.closeDrawers()
        when (itemId) {
            R.id.activity_main_nav_drawer_item_logout -> {
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

}