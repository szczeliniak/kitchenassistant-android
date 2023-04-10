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
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditingredient.AddEditIngredientDialog
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
    private val ingredientsAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRecipeIngredientsBinding.inflate(inflater)
        binding.recyclerView.adapter = ingredientsAdapter
        binding.buttonAddIngredient.setOnClickListener { showAddIngredientDialog() }
        return binding.root
    }

    private fun showAddIngredientDialog() {
        recipe?.let {
            val group = it.ingredientGroups.first()
            AddEditIngredientDialog.show(
                requireActivity().supportFragmentManager,
                it.id,
                group.id,
                group.name,
                ArrayList(it.ingredientGroups)
            )
        }
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
            ingredientsAdapter.clear()
            if (r.ingredientGroups.flatMap { it.ingredients }.none()) {
                binding.root.showEmptyIcon(requireActivity())
            } else {
                binding.root.hideEmptyIcon()
                r.ingredientGroups.forEach { ingredientGroup ->
                    if (ingredientGroup.ingredients.isNotEmpty()) {
                        ingredientsAdapter.add(IngredientGroupHeaderItem(ingredientGroup))
                        ingredientGroup.ingredients.forEach { ingredient ->
                            ingredientsAdapter.add(IngredientItem(requireContext(), r.id, ingredient, { recipeId, i ->
                                ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                                    viewModel.delete(recipeId, ingredientGroup.id, i.id).observe(viewLifecycleOwner) {
                                        deleteIngredientStateHandler.handle(it)
                                    }
                                }
                            }, { recipeId, i ->
                                AddEditIngredientDialog.show(
                                    requireActivity().supportFragmentManager,
                                    recipeId,
                                    ingredientGroup.id,
                                    ingredientGroup.name,
                                    ArrayList(r.ingredientGroups),
                                    i
                                )
                            }, { _, i ->
                                AddIngredientToShoppingListDialog.show(
                                    requireActivity().supportFragmentManager,
                                    i.name,
                                    i.quantity,
                                    r.id
                                )
                            }))
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