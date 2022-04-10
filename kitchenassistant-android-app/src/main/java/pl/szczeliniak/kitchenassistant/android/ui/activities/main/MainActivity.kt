package pl.szczeliniak.kitchenassistant.android.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityMainBinding
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.archivedshoppinglists.ArchivedShoppingListsActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.categories.CategoriesActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.login.LoginActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.main.fragments.receipts.ReceiptsFragment
import pl.szczeliniak.kitchenassistant.android.ui.activities.main.fragments.shoppinglists.ShoppingListsFragment
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import javax.inject.Inject

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarLayout.toolbar.init(this, R.drawable.icon_menu) {
            binding.drawerLayout.open()
        }

        binding.bottomNavView.setOnItemSelectedListener { onNavigationItemChanged(it.itemId) }
        binding.navView.setNavigationItemSelectedListener { onDrawerItemClicked(it.itemId) }

        setFragment(savedInstanceState)
    }

    private fun onNavigationItemChanged(itemId: Int): Boolean {
        when (itemId) {
            R.id.nav_bottom_item_receipts -> {
                setCurrentFragment(ReceiptsFragment.create())
                return true
            }
            R.id.nav_bottom_item_shopping_lists -> {
                setCurrentFragment(ShoppingListsFragment.create())
                return true
            }
        }
        return false
    }

    private fun setCurrentFragment(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        replace(R.id.fragment_container, fragment)
        commit()
    }

    private fun onDrawerItemClicked(itemId: Int): Boolean {
        binding.root.closeDrawers()
        when (itemId) {
            R.id.menu_nav_view_item_receipts -> {
                binding.bottomNavView.selectedItemId = R.id.nav_bottom_item_receipts
                return true
            }
            R.id.menu_nav_view_item_shopping_lists -> {
                binding.bottomNavView.selectedItemId = R.id.nav_bottom_item_shopping_lists
                return true
            }
            R.id.menu_nav_view_item_archived_shopping_lists -> {
                ArchivedShoppingListsActivity.start(this@MainActivity)
                return true
            }
            R.id.menu_nav_view_item_categories -> {
                CategoriesActivity.start(this@MainActivity)
                return true
            }
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

    private fun setFragment(savedInstanceState: Bundle?) {
        val fragmentClassName = savedInstanceState?.getString(SAVED_FRAGMENT_NAME)
        if (fragmentClassName == null) {
            setCurrentFragment(ReceiptsFragment.create())
        }
        when (fragmentClassName) {
            ReceiptsFragment::class.java.simpleName -> {
                setCurrentFragment(ReceiptsFragment.create())
            }
            ShoppingListsFragment::class.java.simpleName -> {
                setCurrentFragment(ShoppingListsFragment.create())
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