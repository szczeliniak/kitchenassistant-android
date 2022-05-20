package pl.szczeliniak.kitchenassistant.android.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityMainBinding
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.archivedshoppinglists.ArchivedShoppingListsActivity
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

        binding.bottomNavView.selectedItemId = R.id.receipts
    }

    private fun onDrawerItemClicked(itemId: Int): Boolean {
        binding.root.closeDrawers()
        when (itemId) {
            R.id.receipts -> {
                binding.bottomNavView.selectedItemId = R.id.receipts
                return true
            }
            R.id.shopping_lists -> {
                binding.bottomNavView.selectedItemId = R.id.shopping_lists
                return true
            }
            R.id.archived_shopping_lists -> {
                ArchivedShoppingListsActivity.start(this@MainActivity)
                return true
            }
            R.id.categories -> {
                CategoriesActivity.start(this@MainActivity)
                return true
            }
            R.id.logout -> {
                if (localStorageService.logout()) {
                    LoginActivity.start(this)
                    finish()
                }
                return true
            }
        }
        return false
    }

}