package pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.events.DeleteIngredientEvent
import pl.szczeliniak.kitchenassistant.android.events.DeleteStepEvent
import pl.szczeliniak.kitchenassistant.android.events.NewIngredientEvent
import pl.szczeliniak.kitchenassistant.android.events.NewStepEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.setTextOrDefault
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
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

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: ActivityShoppingListBinding
    private val shoppingListLoadingStateHandler: LoadingStateHandler<ShoppingList> =
        prepareShoppingListLoadingStateHandler()

    private val viewModel: ShoppingListActivityViewModel by viewModels()

    private val shoppingListId: Int
        get() {
            return intent.getIntExtra(SHOPPING_LIST_ID_EXTRA, -1)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                binding.activityShoppingListToolbarLayout.toolbar.init(this@ShoppingListActivity, data.title)
                binding.activityShoppingListTextviewDescription.setTextOrDefault(data.description)
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
    fun newIngredientEvent(event: NewIngredientEvent) {
        viewModel.load(shoppingListId)
    }

    @Subscribe
    fun newStepEvent(event: NewStepEvent) {
        viewModel.load(shoppingListId)
    }

    @Subscribe
    fun deleteIngredientEvent(event: DeleteIngredientEvent) {
        viewModel.load(shoppingListId)
    }

    @Subscribe
    fun deleteStepEvent(event: DeleteStepEvent) {
        viewModel.load(shoppingListId)
    }

}