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
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityAddShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.events.NewShoppingListEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.ShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.toast
import javax.inject.Inject

@AndroidEntryPoint
class AddShoppingListActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AddShoppingListActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var localStorageService: LocalStorageService

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddShoppingListActivityViewModel by viewModels()

    private lateinit var binding: ActivityAddShoppingListBinding

    private val saveShoppingListLoadingStateHandler = prepareSaveShoppingListLoadingStateHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
    }

    private fun initLayout() {
        binding = ActivityAddShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.activityAddShoppingListToolbar.toolbar.init(this, R.string.activity_new_shopping_list_name)
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
                binding.root.showProgressSpinner(this@AddShoppingListActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(this@AddShoppingListActivity)
            }

            override fun onSuccess(data: Int) {
                eventBus.post(NewShoppingListEvent())
                ShoppingListActivity.start(this@AddShoppingListActivity, data)
                finish()
            }
        })
    }

    private fun saveShoppingList() {
        if (!validateData()) {
            return
        }
        viewModel.addShoppingList(AddShoppingListRequest(name, description, localStorageService.getId()))
            .observe(this) { saveShoppingListLoadingStateHandler.handle(it) }
    }

    private fun validateData(): Boolean {
        if (name.isEmpty()) {
            toast(R.string.toast_shopping_list_name_is_empty)
            return false
        }
        return true
    }

    private val name: String
        get() {
            return binding.activityAddShoppingEdittextName.text.toString()
        }

    private val description: String
        get() {
            return binding.activityAddShoppingListEdittextDescription.text.toString()
        }

}