package pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.events.NewShoppingListItemEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.dialogs.addshoppinglistitem.AddShoppingListItemDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.ShoppingListItemItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.*
import javax.inject.Inject

@AndroidEntryPoint
class ShoppingListActivity : AppCompatActivity() {

    companion object {
        private const val SHOPPING_LIST_ID_EXTRA = "SHOPPING_LIST_ID_EXTRA"

        fun start(context: Context, shoppingListId: Int) {
            val intent = Intent(context, ShoppingListActivity::class.java)
            intent.putExtra(SHOPPING_LIST_ID_EXTRA, shoppingListId)
            context.startActivity(intent)
        }
    }

    private val itemsAdapter = GroupAdapter<GroupieViewHolder>()

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: ActivityShoppingListBinding
    private val shoppingListLoadingStateHandler: LoadingStateHandler<ShoppingList> =
        prepareShoppingListLoadingStateHandler()
    private val deleteShoppingListItemStateHandler: LoadingStateHandler<Int> =
        prepareDeleteShoppingListItemStateHandler()

    private val viewModel: ShoppingListActivityViewModel by viewModels()

    private val shoppingListId: Int
        get() {
            return intent.getIntExtra(SHOPPING_LIST_ID_EXTRA, -1)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingListBinding.inflate(layoutInflater)
        binding.activityShoppingListRecyclerViewItems.adapter = itemsAdapter
        setContentView(binding.root)

        binding.fragmentReceiptsFabAddShoppingListItem.setOnClickListener {
            AddShoppingListItemDialog.newInstance(shoppingListId)
                .show(supportFragmentManager, AddShoppingListItemDialog.TAG)
        }

        viewModel.shoppingList.observe(this) { shoppingListLoadingStateHandler.handle(it) }
        viewModel.load(shoppingListId)
    }

    private fun prepareShoppingListLoadingStateHandler(): LoadingStateHandler<ShoppingList> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<ShoppingList> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@ShoppingListActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(this@ShoppingListActivity)
            }

            override fun onSuccess(data: ShoppingList) {
                binding.activityShoppingListToolbarLayout.toolbar.init(this@ShoppingListActivity, data.name)
                binding.activityShoppingListTextviewDescription.setTextOrDefault(data.description)

                itemsAdapter.clear()
                if (data.items.isEmpty()) {
                    binding.activityShoppingListLayoutItems.showEmptyIcon(this@ShoppingListActivity)
                } else {
                    binding.activityShoppingListLayoutItems.hideEmptyIcon()
                    data.items.forEach { item ->
                        itemsAdapter.add(
                            ShoppingListItemItem(
                                this@ShoppingListActivity, shoppingListId, item
                            ) { shoppingListId, shoppingListItem ->
                                viewModel.deleteShoppingListItem(shoppingListId, shoppingListItem.id)
                                    .observe(this@ShoppingListActivity) {
                                        deleteShoppingListItemStateHandler.handle(it)
                                    }
                            })
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
                binding.root.hideProgressSpinner(this@ShoppingListActivity)
            }

            override fun onSuccess(data: Int) {
                viewModel.load(shoppingListId)
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

    @Subscribe
    fun newShoppingListItemEvent(event: NewShoppingListItemEvent) {
        viewModel.load(shoppingListId)
    }

}