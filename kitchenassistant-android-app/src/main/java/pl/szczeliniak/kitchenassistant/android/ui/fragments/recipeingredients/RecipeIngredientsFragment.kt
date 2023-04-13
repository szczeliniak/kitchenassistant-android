package pl.szczeliniak.kitchenassistant.android.ui.fragments.recipeingredients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentRecipeIngredientsBinding
import pl.szczeliniak.kitchenassistant.android.events.RecipeSavedEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditingredient.AddEditIngredientGroupDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.addingredienttoshoppinglist.AddIngredientToShoppingListDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.fragments.RecipeActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.listitems.IngredientGroupHeaderItem
import pl.szczeliniak.kitchenassistant.android.ui.listitems.IngredientItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class RecipeIngredientsFragment : RecipeActivityFragment() {

    companion object {
        fun create(): RecipeIngredientsFragment {
            return RecipeIngredientsFragment()
        }
    }

    private lateinit var binding: FragmentRecipeIngredientsBinding
    private lateinit var deleteIngredientStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: RecipeIngredientsFragmentViewModel by viewModels()
    private val ingredientGroupsAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRecipeIngredientsBinding.inflate(inflater)
        binding.recyclerView.adapter = ingredientGroupsAdapter
        binding.buttonAddIngredient.setOnClickListener { showAddIngredientGroupDialog() }
        return binding.root
    }

    private fun showAddIngredientGroupDialog() {
        recipe?.let { AddEditIngredientGroupDialog.show(requireActivity().supportFragmentManager, it.id, null) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        deleteIngredientStateHandler = prepareDeleteIngredientLoadingStateHandler()
        loadData()
    }

    private fun prepareDeleteIngredientLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireContext(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(RecipeSavedEvent())
            }
        })
    }

    private fun loadData() {
        recipe?.let { r ->
            ingredientGroupsAdapter.clear()
            if (r.ingredientGroups.flatMap { it.ingredients }.none()) {
                binding.root.showEmptyIcon(requireActivity())
            } else {
                binding.root.hideEmptyIcon()
                r.ingredientGroups.forEach { ingredientGroup ->
                    if (ingredientGroup.ingredients.isNotEmpty()) {
                        ingredientGroupsAdapter.add(
                            IngredientGroupHeaderItem(
                                ingredientGroup.id, ingredientGroup.name, r.id, requireActivity().supportFragmentManager
                            )
                        )
                        ingredientGroup.ingredients.forEach { ingredient ->
                            ingredientGroupsAdapter.add(IngredientItem(requireContext(),
                                r.id,
                                ingredient,
                                { recipeId, i ->
                                    ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                                        viewModel.delete(recipeId, ingredientGroup.id, i.id)
                                            .observe(viewLifecycleOwner) {
                                                deleteIngredientStateHandler.handle(it)
                                            }
                                    }
                                },
                                { recipeId, i ->
                                    AddIngredientToShoppingListDialog.show(
                                        requireActivity().supportFragmentManager, i.name, i.quantity, recipeId
                                    )
                                })
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onRecipeChanged() {
        loadData()
    }

}