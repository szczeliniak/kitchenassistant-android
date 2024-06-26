package pl.szczeliniak.cookbook.android.ui.fragments.recipesbycategory

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.cookbook.android.R
import pl.szczeliniak.cookbook.android.databinding.FragmentRecipesByCategoryBinding
import pl.szczeliniak.cookbook.android.listeners.EndlessScrollRecyclerViewListener
import pl.szczeliniak.cookbook.android.network.LoadingStateHandler
import pl.szczeliniak.cookbook.android.network.requests.AddRecipeToDayPlanRequest
import pl.szczeliniak.cookbook.android.network.responses.RecipesResponse
import pl.szczeliniak.cookbook.android.ui.activities.recipe.RecipeActivity
import pl.szczeliniak.cookbook.android.ui.dialogs.choosedayplanforrecipe.ChooseDayForRecipeDialog
import pl.szczeliniak.cookbook.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.cookbook.android.ui.listitems.RecipeItem
import pl.szczeliniak.cookbook.android.ui.utils.DebounceExecutor
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class RecipesByCategoryFragment : Fragment() {

    companion object {
        private const val FILTER_SAVED_STATE_EXTRA = "FILTER_SAVED_STATE_EXTRA"
        private const val CATEGORY_ID_EXTRA = "CATEGORY_ID_EXTRA"

        fun create(id: Int?): RecipesByCategoryFragment {
            val bundle = Bundle()
            id?.let { bundle.putInt(CATEGORY_ID_EXTRA, it) }
            val fragment = RecipesByCategoryFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var viewModel: RecipesByCategoryFragmentViewModel
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val debounceExecutor = DebounceExecutor(500)

    @Inject
    lateinit var recipesByCategoryFragmentViewModel: RecipesByCategoryFragmentViewModel.Factory

    private lateinit var binding: FragmentRecipesByCategoryBinding
    private lateinit var loadRecipesLoadingStateHandler: LoadingStateHandler<RecipesResponse>
    private lateinit var reloadRecipesLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var addRecipeToDayPlanLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var endlessScrollRecyclerViewListener: EndlessScrollRecyclerViewListener
    private lateinit var searchView: SearchView

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.fragment_recipes, menu)
            searchView = menu.findItem(R.id.search).actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    debounceExecutor.execute { resetRecipes() }
                    return true
                }
            })
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return false
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewModel: RecipesByCategoryFragmentViewModel by viewModels {
            RecipesByCategoryFragmentViewModel.provideFactory(recipesByCategoryFragmentViewModel, categoryId)
        }
        this.viewModel = viewModel

        binding = FragmentRecipesByCategoryBinding.inflate(inflater)
        binding.root.setOnRefreshListener { resetRecipes() }
        binding.recyclerView.adapter = adapter
        adapter.clear()
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        endlessScrollRecyclerViewListener =
            EndlessScrollRecyclerViewListener(binding.recyclerView.layoutManager as LinearLayoutManager, {
                viewModel.loadRecipes(it, searchView.query.toString().ifEmpty { null })
            }, { adapter.clear() })
        binding.recyclerView.addOnScrollListener(endlessScrollRecyclerViewListener)

        return binding.root
    }

    private fun resetRecipes() {
        adapter.clear()
        endlessScrollRecyclerViewListener.reset()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadRecipesLoadingStateHandler = loadRecipesLoadingStateHandler()
        reloadRecipesLoadingStateHandler = deleteRecipeLoadingStateHandler()
        addRecipeToDayPlanLoadingStateHandler = addRecipeToDayPlanLoadingStateHandler()
        viewModel.recipes.observe(viewLifecycleOwner) { loadRecipesLoadingStateHandler.handle(it) }
    }

    private fun deleteRecipeLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.layout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.layout.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                resetRecipes()
            }
        })
    }

    private fun addRecipeToDayPlanLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.layout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.layout.hideProgressSpinner()
            }

        })
    }

    private fun loadRecipesLoadingStateHandler(): LoadingStateHandler<RecipesResponse> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<RecipesResponse> {
            override fun onInProgress() {
                binding.root.isRefreshing = true
                binding.layout.hideEmptyIcon()
            }

            override fun onFinish() {
                binding.root.isRefreshing = false
            }

            override fun onSuccess(data: RecipesResponse) {
                endlessScrollRecyclerViewListener.maxPage = data.recipes.totalNumberOfPages
                if (data.recipes.items.isEmpty()) {
                    binding.layout.showEmptyIcon(requireActivity())
                } else {
                    binding.layout.hideEmptyIcon()
                    data.recipes.items.forEach { recipe ->
                        adapter.add(RecipeItem(requireContext(), recipe, {
                            RecipeActivity.start(requireContext(), it.id)
                        }, {
                            ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                                viewModel.setFavorite(it.id, !it.favorite).observe(viewLifecycleOwner) { r ->
                                    reloadRecipesLoadingStateHandler.handle(r)
                                }
                            }
                        }, {
                            ChooseDayForRecipeDialog.show(requireActivity().supportFragmentManager,
                                ChooseDayForRecipeDialog.OnDayChosen { date ->
                                    viewModel.assignRecipeToDayPlan(
                                        AddRecipeToDayPlanRequest(date, recipe.id)
                                    ).observe(viewLifecycleOwner) { r ->
                                        addRecipeToDayPlanLoadingStateHandler.handle(r)
                                    }
                                })
                        }))
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        activity?.addMenuProvider(menuProvider)
    }

    override fun onPause() {
        activity?.removeMenuProvider(menuProvider)
        super.onPause()
    }

    private val categoryId: Int?
        get() {
            val id = requireArguments().getInt(CATEGORY_ID_EXTRA, -1)
            return if (id > 0) id else null
        }

}