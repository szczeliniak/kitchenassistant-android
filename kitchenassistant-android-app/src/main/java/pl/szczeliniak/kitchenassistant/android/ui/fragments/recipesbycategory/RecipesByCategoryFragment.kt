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
import pl.szczeliniak.kitchenassistant.android.events.DayPlanEditedEvent
import pl.szczeliniak.kitchenassistant.android.events.RecipeDeletedEvent
import pl.szczeliniak.kitchenassistant.android.events.RecipeSavedEvent
import pl.szczeliniak.kitchenassistant.android.listeners.EndlessScrollRecyclerViewListener
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddRecipeToDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipesResponse
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.services.RecipeService
import pl.szczeliniak.kitchenassistant.android.ui.activities.addeditrecipe.AddEditRecipeActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.recipe.RecipeActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.choosedayplanforrecipe.ChooseDayForRecipeDialog
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

    @Inject
    lateinit var localStorageService: LocalStorageService

    private lateinit var binding: FragmentRecipesByCategoryBinding
    private lateinit var loadRecipesLoadingStateHandler: LoadingStateHandler<RecipesResponse>
    private lateinit var loadPhotoLoadingStateHandler: LoadingStateHandler<RecipeService.DownloadedPhoto>
    private lateinit var reloadRecipesLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var addRecipeToDayPlanLoadingStateHandler: LoadingStateHandler<Int>
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
        adapter.clear()
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        endlessScrollRecyclerViewListener = EndlessScrollRecyclerViewListener(
            binding.recyclerView.layoutManager as LinearLayoutManager,
            {
                viewModel.loadRecipes(
                    it,
                    searchView.query.toString(),
                    filter?.recipeTag,
                    filter?.onlyFavorites ?: false
                )
            },
            { adapter.clear() }
        )
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
        loadPhotoLoadingStateHandler = loadPhotoLoadingStateHandler()
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

            override fun onSuccess(data: Int) {
                eventBus.post(DayPlanEditedEvent())
            }
        })
    }

    private fun loadPhotoLoadingStateHandler(): LoadingStateHandler<RecipeService.DownloadedPhoto> {
        return LoadingStateHandler(
            requireActivity(),
            object : LoadingStateHandler.OnStateChanged<RecipeService.DownloadedPhoto> {
                override fun onInProgress() {}
                override fun onFinish() {}
                override fun onSuccess(data: RecipeService.DownloadedPhoto) {
                    findRecipeItemByPhotoName(data.photoName)?.let {
                        (adapter.getItem(it) as RecipeItem).photo = data.file
                        adapter.notifyItemChanged(it)
                    }
                }
            })
    }

    private fun findRecipeItemByPhotoName(photoName: String): Int? {
        for (i in 0 until adapter.itemCount) {
            if ((adapter.getItem(i) as RecipeItem).photoName == photoName) {
                return i
            }
        }
        return null
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
                                    reloadRecipesLoadingStateHandler.handle(r)
                                }
                            }
                        }, {
                            AddEditRecipeActivity.start(requireContext(), it.id)
                        }, {
                            ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                                viewModel.setFavorite(it.id, !it.favorite).observe(viewLifecycleOwner) { r ->
                                    reloadRecipesLoadingStateHandler.handle(r)
                                }
                            }
                        }, {
                            ChooseDayForRecipeDialog.show(
                                requireActivity().supportFragmentManager,
                                ChooseDayForRecipeDialog.OnDayChosen { date ->
                                    viewModel.assignRecipeToDayPlan(
                                        recipe.id,
                                        AddRecipeToDayPlanRequest(localStorageService.getId(), date)
                                    )
                                        .observe(viewLifecycleOwner) { r ->
                                            addRecipeToDayPlanLoadingStateHandler.handle(r)
                                        }
                                })
                        }))
                    }

                    data.recipes.forEach { recipe ->
                        recipe.photoName?.let { photoName ->
                            viewModel.loadPhoto(recipe.id, photoName).observe(viewLifecycleOwner) {
                                loadPhotoLoadingStateHandler.handle(it)
                            }
                        }
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

    @Deprecated("")
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

    @Deprecated("")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter -> {
                RecipesFilterDialog.show(
                    requireActivity().supportFragmentManager,
                    RecipesFilterDialog.Filter(filter?.onlyFavorites ?: false, filter?.recipeTag),
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
    fun onRecipeSaved(event: RecipeSavedEvent) {
        endlessScrollRecyclerViewListener.reset()
    }

    @Subscribe
    fun onRecipeDeleted(event: RecipeDeletedEvent) {
        endlessScrollRecyclerViewListener.reset()
    }

}