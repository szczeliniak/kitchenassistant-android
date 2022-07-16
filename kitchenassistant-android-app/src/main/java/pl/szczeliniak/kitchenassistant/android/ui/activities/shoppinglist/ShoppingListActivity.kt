package pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListEvent
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListsEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingListDetails
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditshoppinglistitem.AddEditShoppingListItemDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.ShoppingListItemItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.setTextOrDefault
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils
import javax.inject.Inject

@AndroidEntryPoint
class ShoppingListActivity : AppCompatActivity() {

    companion object {
        private const val SHOPPING_LIST_ID_EXTRA = "SHOPPING_LIST_ID_EXTRA"
        private const val SHOW_ARCHIVED_MENU_ITEM_EXTRA = "SHOW_ARCHIVED_MENU_ITEM_EXTRA"

        fun start(context: Context, shoppingListId: Int, showArchivedMenuItem: Boolean? = null) {
            val intent = Intent(context, ShoppingListActivity::class.java)
            intent.putExtra(SHOPPING_LIST_ID_EXTRA, shoppingListId)
            showArchivedMenuItem?.let { intent.putExtra(SHOW_ARCHIVED_MENU_ITEM_EXTRA, it) }
            context.startActivity(intent)
        }
    }

    private val itemsAdapter = GroupAdapter<GroupieViewHolder>()

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var shoppingListActivityViewModelFactory: ShoppingListActivityViewModel.Factory

    private lateinit var binding: ActivityShoppingListBinding
    private val shoppingListLoadingStateHandler: LoadingStateHandler<ShoppingListDetails> =
        prepareShoppingListLoadingStateHandler()
    private val deleteShoppingListItemStateHandler: LoadingStateHandler<Int> =
        prepareDeleteShoppingListItemStateHandler()
    private val archiveShoppingListStateHandler: LoadingStateHandler<Int> =
        prepareArchiveShoppingListStateHandler()
    private val changeShoppingListItemStateStateHandler: LoadingStateHandler<Int> =
        prepareChangeShoppingListItemStateStateHandler()

    private val viewModel: ShoppingListActivityViewModel by viewModels {
        ShoppingListActivityViewModel.provideFactory(shoppingListActivityViewModelFactory, shoppingListId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
        viewModel.shoppingList.observe(this) { shoppingListLoadingStateHandler.handle(it) }
    }

    private fun initLayout() {
        binding = ActivityShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.adapter = itemsAdapter

        binding.buttonAddShoppingListItem.setOnClickListener {
            AddEditShoppingListItemDialog.show(supportFragmentManager, shoppingListId)
        }
    }

    private fun prepareShoppingListLoadingStateHandler(): LoadingStateHandler<ShoppingListDetails> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<ShoppingListDetails> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@ShoppingListActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: ShoppingListDetails) {
                binding.toolbarLayout.toolbar.init(this@ShoppingListActivity, data.name)
                binding.shoppingListDescription.setTextOrDefault(data.description)
                binding.shoppingListDate.setTextOrDefault(LocalDateUtils.stringify(data.date))

                itemsAdapter.clear()
                if (data.items.isEmpty()) {
                    binding.shoppingListItemsLayout.showEmptyIcon(this@ShoppingListActivity)
                } else {
                    binding.shoppingListItemsLayout.hideEmptyIcon()
                    data.items.forEach { item ->
                        itemsAdapter.add(
                            ShoppingListItemItem(
                                this@ShoppingListActivity, shoppingListId, item, { shoppingListId, shoppingListItem ->
                                    ConfirmationDialog.show(supportFragmentManager) {
                                        viewModel.deleteItem(shoppingListId, shoppingListItem.id)
                                            .observe(this@ShoppingListActivity) {
                                                deleteShoppingListItemStateHandler.handle(it)
                                            }
                                    }
                                }, { shoppingListId, shoppingListItem ->
                                    AddEditShoppingListItemDialog.show(
                                        supportFragmentManager,
                                        shoppingListId,
                                        shoppingListItem
                                    )
                                }, { shoppingListId, shoppingListItem, isChecked ->
                                    viewModel.changeItemState(
                                        shoppingListId,
                                        shoppingListItem.id,
                                        isChecked
                                    ).observe(this@ShoppingListActivity) {
                                        changeShoppingListItemStateStateHandler.handle(it)
                                    }
                                }
                            ))
                    }
                }
            }
        })
    }

    private fun prepareDeleteShoppingListItemStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@ShoppingListActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadShoppingListEvent())
            }
        })
    }

    private fun prepareChangeShoppingListItemStateStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@ShoppingListActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {}
        })
    }

    private fun prepareArchiveShoppingListStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@ShoppingListActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadShoppingListsEvent())
                finish()
            }
        })
    }

    override fun onStart() {
        eventBus.register(this)
        super.onStart()
    }

    override fun onStop() {
        eventBus.unregister(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_shopping_list, menu)
        if (!showArchivedMenuItem) {
            menu?.removeItem(R.id.archive)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.archive) {
            ConfirmationDialog.show(supportFragmentManager) {
                viewModel.archive(shoppingListId).observe(this) { archiveShoppingListStateHandler.handle(it) }
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe
    fun reloadShoppingListEvent(event: ReloadShoppingListEvent) {
        viewModel.reload()
    }

    private val shoppingListId: Int
        get() {
            return intent.getIntExtra(SHOPPING_LIST_ID_EXTRA, -1)
        }

    private val showArchivedMenuItem: Boolean
        get() {
            return intent.getBooleanExtra(SHOW_ARCHIVED_MENU_ITEM_EXTRA, true)
        }

}