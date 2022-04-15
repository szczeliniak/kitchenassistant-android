package pl.szczeliniak.kitchenassistant.android.ui.activities.archivedshoppinglists

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityArchviedShoppingListsBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListsEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.ui.activities.addshoppinglist.AddEditShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.ShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.shoppinglistsfilter.ShoppingListsFilterDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.ShoppingListItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ActivityUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ActivityUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ArchivedShoppingListsActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ArchivedShoppingListsActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: ArchivedShoppingListsActivityViewModel by viewModels()
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val saveShoppingListLoadingStateHandler = prepareLoadShoppingListsStateHandler()
    private val deleteShoppingListLoadingStateHandler = prepareDeleteShoppingListLoadingStateHandler()

    private lateinit var binding: ActivityArchviedShoppingListsBinding
    private var filter: ShoppingListsFilterDialog.Filter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
        viewModel.shoppingLists.observe(this) {
            saveShoppingListLoadingStateHandler.handle(it)
        }
        viewModel.filter.observe(this) {
            this.filter = it
            viewModel.reloadShoppingLists(it.name, it?.date)
        }
    }

    private fun initLayout() {
        binding = ActivityArchviedShoppingListsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarLayout.toolbar.init(this, R.string.title_activity_archived_shopping_lists)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )
        binding.root.setOnRefreshListener { viewModel.reloadShoppingLists(filter?.name, filter?.date) }
    }

    private fun prepareLoadShoppingListsStateHandler(): LoadingStateHandler<List<ShoppingList>> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<List<ShoppingList>> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@ArchivedShoppingListsActivity)
            }

            override fun onFinish() {
                binding.root.isRefreshing = false
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: List<ShoppingList>) {
                adapter.clear()
                if (data.isEmpty()) {
                    binding.layout.showEmptyIcon(this@ArchivedShoppingListsActivity)
                } else {
                    binding.layout.hideEmptyIcon()
                    data.forEach { shoppingList ->
                        adapter.add(ShoppingListItem(this@ArchivedShoppingListsActivity, shoppingList, {
                            ShoppingListActivity.start(this@ArchivedShoppingListsActivity, shoppingList.id)
                        }, {
                            viewModel.delete(it.id).observe(this@ArchivedShoppingListsActivity) { r ->
                                deleteShoppingListLoadingStateHandler.handle(r)
                            }
                        }, {
                            AddEditShoppingListActivity.start(this@ArchivedShoppingListsActivity, it)
                        }))
                    }
                }
            }
        })
    }

    private fun prepareDeleteShoppingListLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(
            this@ArchivedShoppingListsActivity,
            object : LoadingStateHandler.OnStateChanged<Int> {
                override fun onInProgress() {
                    binding.layout.showProgressSpinner(this@ArchivedShoppingListsActivity)
                }

                override fun onFinish() {
                    binding.layout.hideProgressSpinner()
                }

                override fun onSuccess(data: Int) {
                    adapter.clear()
                    viewModel.reloadShoppingLists(filter?.name, filter?.date)
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
        menuInflater.inflate(R.menu.activity_archived_shopping_lists, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.activity_archived_shopping_lists_menu_item_filter) {
            ShoppingListsFilterDialog.show(supportFragmentManager,
                ShoppingListsFilterDialog.Filter(filter?.name, filter?.date),
                ShoppingListsFilterDialog.OnFilterChanged { viewModel.changeFilter(it) })
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe
    fun reloadReceiptEvent(event: ReloadShoppingListsEvent) {
        viewModel.reloadShoppingLists(filter?.name, filter?.date)
    }

}