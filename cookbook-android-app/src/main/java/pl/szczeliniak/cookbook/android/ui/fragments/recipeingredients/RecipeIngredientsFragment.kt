package pl.szczeliniak.cookbook.android.ui.fragments.recipeingredients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.cookbook.android.databinding.FragmentRecipeIngredientsBinding
import pl.szczeliniak.cookbook.android.ui.fragments.RecipeActivityFragment
import pl.szczeliniak.cookbook.android.ui.listitems.GroupHeaderItem
import pl.szczeliniak.cookbook.android.ui.listitems.IngredientItem
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon

@AndroidEntryPoint
class RecipeIngredientsFragment : RecipeActivityFragment() {

    companion object {
        fun create(): RecipeIngredientsFragment {
            return RecipeIngredientsFragment()
        }
    }

    private lateinit var binding: FragmentRecipeIngredientsBinding

    private val ingredientsAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRecipeIngredientsBinding.inflate(inflater)
        binding.recyclerView.adapter = ingredientsAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadData()
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
                        ingredientGroup.name?.let { ingredientsAdapter.add(GroupHeaderItem(it)) }
                        ingredientGroup.ingredients.forEach { ingredient ->
                            ingredientsAdapter.add(
                                IngredientItem(
                                    ingredient
                                )
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