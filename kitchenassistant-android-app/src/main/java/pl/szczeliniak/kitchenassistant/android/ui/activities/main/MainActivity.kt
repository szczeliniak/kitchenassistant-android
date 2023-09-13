package pl.szczeliniak.kitchenassistant.android.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.facebook.login.LoginManager
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityMainBinding
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.categories.CategoriesActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.login.LoginActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import javax.inject.Inject

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

        NavigationUI.setupWithNavController(
            binding.bottomNavView,
            (supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment).navController
        )

        binding.navView.setNavigationItemSelectedListener { onDrawerItemClicked(it.itemId) }

        binding.bottomNavView.selectedItemId = R.id.recipes
    }

    private fun onDrawerItemClicked(itemId: Int): Boolean {
        binding.root.closeDrawers()
        when (itemId) {
            R.id.recipes -> {
                binding.bottomNavView.selectedItemId = R.id.recipes
                return true
            }
            R.id.day_plans -> {
                binding.bottomNavView.selectedItemId = R.id.day_plans
                return true
            }
            R.id.categories -> {
                CategoriesActivity.start(this@MainActivity)
                return true
            }
            R.id.logout -> {
                if (localStorageService.logout()) {
                    LoginManager.getInstance().logOut()
                    LoginActivity.start(this)
                    finish()
                }
                return true
            }
        }
        return false
    }

}