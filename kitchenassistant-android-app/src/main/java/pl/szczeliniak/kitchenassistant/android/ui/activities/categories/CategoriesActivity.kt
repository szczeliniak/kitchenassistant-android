package pl.szczeliniak.kitchenassistant.android.ui.activities.categories

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityCategoriesBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListsEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.ui.listitems.CategoryItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.*
import javax.inject.Inject

@AndroidEntryPoint
class CategoriesActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CategoriesActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: CategoriesActivityViewModel by viewModels()
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val loadCategoriesLoadingStateHandler = prepareLoadCategoriesStateHandler()
    private val deleteCategoryLoadingStateHandler = prepareDeleteCategoryLoadingStateHandler()

    private lateinit var binding: ActivityCategoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
        viewModel.categories.observe(this) {
            loadCategoriesLoadingStateHandler.handle(it)
        }
        viewModel.reloadCategories()
    }

    private fun initLayout() {
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarLayout.toolbar.init(
            this,
            R.string.title_activity_categories
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )
    }

    private fun prepareLoadCategoriesStateHandler(): LoadingStateHandler<List<Category>> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<List<Category>> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@CategoriesActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(this@CategoriesActivity)
            }

            override fun onSuccess(data: List<Category>) {
                adapter.clear()
                if (data.isEmpty()) {
                    binding.layout.showEmptyIcon(this@CategoriesActivity)
                } else {
                    binding.layout.hideEmptyIcon()
                    data.forEach { category ->
                        adapter.add(CategoryItem(this@CategoriesActivity, category, {
                            viewModel.delete(it.id).observe(this@CategoriesActivity) { r ->
                                deleteCategoryLoadingStateHandler.handle(r)
                            }
                        }, {

                        }))
                    }
                }
            }
        })
    }

    private fun prepareDeleteCategoryLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(
            this@CategoriesActivity,
            object : LoadingStateHandler.OnStateChanged<Int> {
                override fun onInProgress() {
                    binding.layout.showProgressSpinner(this@CategoriesActivity)
                }

                override fun onFinish() {
                    binding.layout.hideProgressSpinner(this@CategoriesActivity)
                }

                override fun onSuccess(data: Int) {
                    adapter.clear()
                    viewModel.reloadCategories()
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
    fun reloadReceiptEvent(event: ReloadShoppingListsEvent) {
        viewModel.reloadCategories()
    }

}