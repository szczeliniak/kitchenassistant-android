package pl.szczeliniak.cookbook.android.ui.fragments.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.cookbook.android.R
import pl.szczeliniak.cookbook.android.databinding.FragmentRecipesBinding
import pl.szczeliniak.cookbook.android.network.LoadingStateHandler
import pl.szczeliniak.cookbook.android.network.responses.CategoriesResponse
import pl.szczeliniak.cookbook.android.ui.adapters.FragmentPagerAdapter
import pl.szczeliniak.cookbook.android.ui.fragments.recipesbycategory.RecipesByCategoryFragment
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner

@AndroidEntryPoint
class RecipesFragment : Fragment() {

    companion object {
        fun create(): RecipesFragment {
            return RecipesFragment()
        }
    }

    private val viewModel: RecipesFragmentViewModel by viewModels()

    private lateinit var binding: FragmentRecipesBinding
    private lateinit var categoriesLoadingStateHandler: LoadingStateHandler<List<CategoriesResponse.Category>>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRecipesBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoriesLoadingStateHandler = prepareCategoriesLoadingStateHandler()
        viewModel.categories.observe(viewLifecycleOwner) { categoriesLoadingStateHandler.handle(it) }
    }

    private fun prepareCategoriesLoadingStateHandler(): LoadingStateHandler<List<CategoriesResponse.Category>> {
        return LoadingStateHandler(
            requireActivity(),
            object : LoadingStateHandler.OnStateChanged<List<CategoriesResponse.Category>> {
                override fun onInProgress() {
                    binding.layout.showProgressSpinner(activity)
                    binding.layout.hideEmptyIcon()
                }

                override fun onFinish() {
                    binding.layout.hideProgressSpinner()
                }

                override fun onSuccess(data: List<CategoriesResponse.Category>) {
                    val tabs = mutableListOf<CategoryTab>()
                    tabs.addAll(data.map { CategoryTab(it.id, it.name) })
                    tabs.add(CategoryTab(null, getString(R.string.label_category_tab_all)))
                    initPager(tabs)
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

    data class CategoryTab(val categoryId: Int?, val tabName: String)

}