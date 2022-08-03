package pl.szczeliniak.kitchenassistant.android.ui.fragments.recipesbycategory

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentRecipesByCategoryBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadRecipesEvent
import pl.szczeliniak.kitchenassistant.android.listeners.EndlessScrollRecyclerViewListener
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipesResponse
import pl.szczeliniak.kitchenassistant.android.ui.activities.addeditrecipe.AddEditRecipeActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.recipe.RecipeActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.choosedayplanforrecipe.ChooseDayPlanForRecipeDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.recipesfilter.RecipesFilterDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.RecipeItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.DebounceExecutor
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
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

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: FragmentRecipesByCategoryBinding
    private lateinit var recipesLoadingStateHandler: LoadingStateHandler<RecipesResponse>
    private lateinit var doActionAndResetRecipesLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var doActionLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var endlessScrollRecyclerViewListener: EndlessScrollRecyclerViewListener
    private lateinit var searchView: SearchView

    private var filter: RecipesFilterDialog.Filter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        savedInstanceState?.getParcelable<RecipesFilterDialog.Filter?>(FILTER_SAVED_STATE_EXTRA)?.let {
            filter = it
        }

        val viewModel: RecipesByCategoryFragmentViewModel by viewModels {
            RecipesByCategoryFragmentViewModel.provideFactory(recipesByCategoryFragmentViewModel, categoryId)
        }
        this.viewModel = viewModel

        binding = FragmentRecipesByCategoryBinding.inflate(inflater)
        binding.root.setOnRefreshListener { resetRecipes() }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        endlessScrollRecyclerViewListener = EndlessScrollRecyclerViewListener(
            binding.recyclerView.layoutManager as LinearLayoutManager
        ) { viewModel.loadRecipes(it, searchView.query.toString(), filter?.recipeTag) }
        binding.recyclerView.addOnScrollListener(endlessScrollRecyclerViewListener)

        return binding.root
    }

    private fun resetRecipes() {
        adapter.clear()
        endlessScrollRecyclerViewListener.reset()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipesLoadingStateHandler = prepareRecipesLoadingStateHandler()
        doActionAndResetRecipesLoadingStateHandler = prepareDoActionAndResetRecipesLoadingStateHandler()
        doActionLoadingStateHandler = prepareDoActionLoadingStateHandler()
        viewModel.recipes.observe(viewLifecycleOwner) { recipesLoadingStateHandler.handle(it) }
    }

    private fun prepareDoActionAndResetRecipesLoadingStateHandler(): LoadingStateHandler<Int> {
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

    private fun prepareDoActionLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.layout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.layout.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
            }
        })
    }

    private fun prepareRecipesLoadingStateHandler(): LoadingStateHandler<RecipesResponse> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<RecipesResponse> {
            override fun onInProgress() {
                binding.root.isRefreshing = true
                binding.layout.hideEmptyIcon()
            }

            override fun onFinish() {
                binding.root.isRefreshing = false
            }

            override fun onSuccess(data: RecipesResponse) {
                adapter.clear()
                endlessScrollRecyclerViewListener.maxPage = data.pagination.numberOfPages
                if (data.recipes.isEmpty()) {
                    binding.layout.showEmptyIcon(requireActivity())
                } else {
                    binding.layout.hideEmptyIcon()
                    data.recipes.forEach { recipe ->
                        adapter.add(RecipeItem(requireContext(), recipe, categoryId == null, {
                            RecipeActivity.start(requireContext(), it.id)
                        }, {
                            ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                                viewModel.delete(it.id).observe(viewLifecycleOwner) { r ->
                                    doActionAndResetRecipesLoadingStateHandler.handle(r)
                                }
                            }
                        }, {
                            AddEditRecipeActivity.start(requireContext(), it.id)
                        }, {
                            ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                                viewModel.setFavorite(it.id, !it.favorite).observe(viewLifecycleOwner) { r ->
                                    doActionAndResetRecipesLoadingStateHandler.handle(r)
                                }
                            }
                        }, {
                            ChooseDayPlanForRecipeDialog.show(
                                requireActivity().supportFragmentManager,
                                it.id,
                                ChooseDayPlanForRecipeDialog.OnDayPlanChosen { dayPlanId, recipeId ->
                                    viewModel.assignRecipeToDayPlan(recipeId, dayPlanId)
                                        .observe(viewLifecycleOwner) { r ->
                                            doActionLoadingStateHandler.handle(r)
                                        }
                                })
                        }))
                    }
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventBus.register(this)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_recipes, menu)
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

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter -> {
                RecipesFilterDialog.show(
                    requireActivity().supportFragmentManager,
                    RecipesFilterDialog.Filter(filter?.recipeTag),
                    RecipesFilterDialog.OnFilterChanged {
                        filter = it
                        resetRecipes()
                    }
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(FILTER_SAVED_STATE_EXTRA, filter)
    }

    private val categoryId: Int?
        get() {
            return requireArguments().get(CATEGORY_ID_EXTRA) as Int?
        }

    override fun onDestroy() {
        eventBus.unregister(this)
        super.onDestroy()
    }

    @Subscribe
    fun reloadRecipes(event: ReloadRecipesEvent) {
        endlessScrollRecyclerViewListener.reset()
    }

}