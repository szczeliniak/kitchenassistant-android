package pl.szczeliniak.kitchenassistant.android.ui.activities.addshoppinglist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityAddEditShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListsEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.ShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatEditTextUtils.Companion.getTextOrNull
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.toast
import javax.inject.Inject

@AndroidEntryPoint
class AddEditShoppingListActivity : AppCompatActivity() {

    companion object {
        private const val SHOPPING_LIST_EXTRA = "SHOPPING_LIST_EXTRA"

        fun start(context: Context) {
            val intent = Intent(context, AddEditShoppingListActivity::class.java)
            context.startActivity(intent)
        }

        fun start(context: Context, shoppingList: ShoppingList) {
            val intent = Intent(context, AddEditShoppingListActivity::class.java)
            intent.putExtra(SHOPPING_LIST_EXTRA, shoppingList)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var localStorageService: LocalStorageService

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddEditShoppingListActivityViewModel by viewModels()

    private lateinit var binding: ActivityAddEditShoppingListBinding

    private val saveShoppingListLoadingStateHandler = prepareSaveShoppingListLoadingStateHandler()

    private val shoppingList: ShoppingList?
        get() {
            return intent.getParcelableExtra(SHOPPING_LIST_EXTRA)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
    }

    private fun initLayout() {
        binding = ActivityAddEditShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        shoppingList?.let {
            binding.shoppingListName.setText(it.name)
            binding.shoppingListDescription.setText(it.description)
            binding.toolbarLayout.toolbar.init(this, R.string.title_activity_edit_shopping_list)
        } ?: kotlin.run {
            binding.toolbarLayout.toolbar.init(this, R.string.title_activity_new_shopping_list)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_add_shopping_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.activity_add_shopping_list_menu_item_save) {
            saveShoppingList()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun prepareSaveShoppingListLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddEditShoppingListActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(this@AddEditShoppingListActivity)
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadShoppingListsEvent())
                if (shoppingList == null) {
                    ShoppingListActivity.start(this@AddEditShoppingListActivity, data)
                }
                finish()
            }
        })
    }

    private fun saveShoppingList() {
        if (!validateData()) {
            return
        }
        shoppingList?.let { list ->
            viewModel.updateShoppingList(list.id, UpdateShoppingListRequest(name!!, description))
                .observe(this) { saveShoppingListLoadingStateHandler.handle(it) }
        } ?: kotlin.run {
            viewModel.addShoppingList(AddShoppingListRequest(name!!, description, localStorageService.getId()))
                .observe(this) { saveShoppingListLoadingStateHandler.handle(it) }
        }
    }

    private fun validateData(): Boolean {
        if (name.isNullOrEmpty()) {
            toast(R.string.message_shopping_list_name_is_empty)
            return false
        }
        return true
    }

    private val name: String?
        get() {
            return binding.shoppingListName.getTextOrNull()
        }

    private val description: String?
        get() {
            return binding.shoppingListDescription.getTextOrNull()
        }

}