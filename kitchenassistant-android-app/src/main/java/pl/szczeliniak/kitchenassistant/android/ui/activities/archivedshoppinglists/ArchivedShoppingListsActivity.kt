package pl.szczeliniak.kitchenassistant.android.ui.activities.archivedshoppinglists

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityArchviedShoppingListsBinding
import pl.szczeliniak.kitchenassistant.android.events.ShoppingListSavedEvent
import pl.szczeliniak.kitchenassistant.android.listeners.EndlessScrollRecyclerViewListener
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListsResponse
import pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.ShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.shoppinglistsfilter.ShoppingListsFilterDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.ShoppingListItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import javax.inject.Inject

@AndroidEntryPoint
class
ArchivedShoppingListsActivity : AppCompatActivity() {

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

    private lateinit var endlessScrollRecyclerViewListener: EndlessScrollRecyclerViewListener
    private lateinit var binding: ActivityArchviedShoppingListsBinding
    private var filter: ShoppingListsFilterDialog.Filter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
        viewModel.shoppingLists.observe(this) { saveShoppingListLoadingStateHandler.handle(it) }
        viewModel.filter.observe(this) {
            this.filter = it
            endlessScrollRecyclerViewListener.reset()
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

        endlessScrollRecyclerViewListener = EndlessScrollRecyclerViewListener(
            binding.recyclerView.layoutManager as LinearLayoutManager,
            { viewModel.reloadShoppingLists(it, null, filter?.date) },
            { adapter.clear() }
        )

        binding.recyclerView.addOnScrollListener(endlessScrollRecyclerViewListener)

        binding.refreshLayout.setOnRefreshListener { endlessScrollRecyclerViewListener.reset() }
    }

    private fun prepareLoadShoppingListsStateHandler(): LoadingStateHandler<ShoppingListsResponse> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<ShoppingListsResponse> {
            override fun onInProgress() {
                binding.refreshLayout.isRefreshing = true
            }

            override fun onFinish() {
                binding.refreshLayout.isRefreshing = false
            }

            override fun onSuccess(data: ShoppingListsResponse) {
                endlessScrollRecyclerViewListener.maxPage = data.shoppingLists.totalNumberOfPages
                if (data.shoppingLists.items.isEmpty()) {
                    binding.layout.showEmptyIcon(this@ArchivedShoppingListsActivity)
                } else {
                    binding.layout.hideEmptyIcon()
                    data.shoppingLists.items.forEach { shoppingList ->
                        adapter.add(ShoppingListItem(this@ArchivedShoppingListsActivity, shoppingList, {
                            ShoppingListActivity.start(this@ArchivedShoppingListsActivity, shoppingList.id, false)
                        }, null, null, null))
                    }
                }
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
        if (item.itemId == R.id.filter) {
            ShoppingListsFilterDialog.show(supportFragmentManager,
                ShoppingListsFilterDialog.Filter(filter?.date),
                ShoppingListsFilterDialog.OnFilterChanged { viewModel.changeFilter(it) })
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe
    fun onShoppingListSaved(event: ShoppingListSavedEvent) {
        endlessScrollRecyclerViewListener.reset()
    }

}