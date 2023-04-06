package pl.szczeliniak.kitchenassistant.android.ui.fragments.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentRecipesBinding
import pl.szczeliniak.kitchenassistant.android.events.CategorySavedEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.ui.activities.addeditrecipe.AddEditRecipeActivity
import pl.szczeliniak.kitchenassistant.android.ui.adapters.FragmentPagerAdapter
import pl.szczeliniak.kitchenassistant.android.ui.fragments.recipesbycategory.RecipesByCategoryFragment
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class RecipesFragment : Fragment() {

    companion object {
        fun create(): RecipesFragment {
            return RecipesFragment()
        }
    }

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: RecipesFragmentViewModel by viewModels()

    private lateinit var binding: FragmentRecipesBinding
    private lateinit var categoriesLoadingStateHandler: LoadingStateHandler<List<Category>>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRecipesBinding.inflate(inflater)

        binding.buttonAddRecipe.setOnClickListener { AddEditRecipeActivity.start(requireContext()) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoriesLoadingStateHandler = prepareCategoriesLoadingStateHandler()
        viewModel.categories.observe(viewLifecycleOwner) { categoriesLoadingStateHandler.handle(it) }
    }

    private fun prepareCategoriesLoadingStateHandler(): LoadingStateHandler<List<Category>> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<List<Category>> {
            override fun onInProgress() {
                binding.layout.showProgressSpinner(activity)
                binding.layout.hideEmptyIcon()
            }

            override fun onFinish() {
                binding.layout.hideProgressSpinner()
            }

            override fun onSuccess(data: List<Category>) {
                if (data.isEmpty()) {
                    binding.layout.showEmptyIcon(requireActivity())
                } else {
                    binding.layout.hideEmptyIcon()
                    val tabs = data.map { CategoryTab(it.id, it.name) }.toMutableList()
                    tabs.add(CategoryTab(null, getString(R.string.label_category_tab_all)))
                    initPager(tabs)
                }
            }
        })
    }

    private fun initPager(tabs: List<CategoryTab>) {
        binding.viewPager.adapter = FragmentPagerAdapter(
            tabs.map { RecipesByCategoryFragment.create(it.categoryId) }.toTypedArray(),
            childFragmentManager,
            lifecycle
        )
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabs[position].tabName
        }.attach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        eventBus.register(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        eventBus.unregister(this)
        super.onDestroy()
    }

    @Subscribe
    fun onCategorySaved(event: CategorySavedEvent) {
        viewModel.reloadCategories()
    }

    data class CategoryTab(val categoryId: Int?, val tabName: String)

}