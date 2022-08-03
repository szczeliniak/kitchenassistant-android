package pl.szczeliniak.kitchenassistant.android.ui.fragments.dayplaningredients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentDayPlanIngredientsBinding
import pl.szczeliniak.kitchenassistant.android.events.DayPlanReloadedEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanRecipe
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanIngredientGroupHeaderItem
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanIngredientItem
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanRecipeHeaderItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class DayPlanIngredientsFragment : Fragment() {

    companion object {
        private const val DAY_PLAN_ID_EXTRA = "DAY_PLAN_ID_EXTRA"

        fun create(id: Int): DayPlanIngredientsFragment {
            val bundle = Bundle()
            id.let { bundle.putInt(DAY_PLAN_ID_EXTRA, it) }
            val fragment = DayPlanIngredientsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var factory: DayPlanIngredientsFragmentViewModel.Factory

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: DayPlanIngredientsFragmentViewModel by viewModels {
        DayPlanIngredientsFragmentViewModel.provideFactory(factory, dayPlanId)
    }
    private val recipesAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var recipesLoadingStateHandler: LoadingStateHandler<List<DayPlanRecipe>>
    private lateinit var binding: FragmentDayPlanIngredientsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDayPlanIngredientsBinding.inflate(inflater)
        binding.recyclerView.adapter = recipesAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recipesLoadingStateHandler = prepareRecipesLoadingStateHandler()
        viewModel.recipes.observe(requireActivity()) { recipesLoadingStateHandler.handle(it) }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun prepareRecipesLoadingStateHandler(): LoadingStateHandler<List<DayPlanRecipe>> {
        return LoadingStateHandler(requireContext(), object : LoadingStateHandler.OnStateChanged<List<DayPlanRecipe>> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: List<DayPlanRecipe>) {
                recipesAdapter.clear()
                if (data.isEmpty()) {
                    binding.root.showEmptyIcon(requireActivity())
                } else {
                    binding.root.hideEmptyIcon()
                }
                data.forEach { recipe ->
                    recipesAdapter.add(DayPlanRecipeHeaderItem(recipe))
                    recipe.ingredientGroups.forEach { ingredientGroup ->
                        if (ingredientGroup.ingredients.isNotEmpty()) {
                            recipesAdapter.add(DayPlanIngredientGroupHeaderItem(ingredientGroup))
                            ingredientGroup.ingredients.forEach { ingredient ->
                                recipesAdapter.add(DayPlanIngredientItem(ingredient))
                            }
                        }
                    }
                }
            }
        })
    }

    private val dayPlanId: Int
        get() {
            return requireArguments().getInt(DAY_PLAN_ID_EXTRA)
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
    fun dayPlanReloadedEvent(event: DayPlanReloadedEvent) {
        viewModel.reload()
    }

}