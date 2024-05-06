package pl.szczeliniak.cookbook.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.cookbook.android.R
import pl.szczeliniak.cookbook.android.databinding.ListItemRecipeBinding
import pl.szczeliniak.cookbook.android.network.responses.RecipesResponse
import pl.szczeliniak.cookbook.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide

class RecipeItem(
    private val context: Context,
    private val recipe: RecipesResponse.Recipe,
    private val onClicked: (recipe: RecipesResponse.Recipe) -> Unit,
    private val onAddRemoveFromFavouritesClicked: (recipe: RecipesResponse.Recipe) -> Unit,
    private val onAssignToDayPanClicked: (recipe: RecipesResponse.Recipe) -> Unit
) : BindableItem<ListItemRecipeBinding>() {

    override fun bind(binding: ListItemRecipeBinding, position: Int) {
        binding.recipeName.text = recipe.name
        binding.recipeCategory.fillOrHide(recipe.category?.name, binding.recipeCategory)
        binding.recipeAuthor.fillOrHide(recipe.author, binding.recipeAuthor)
        binding.root.setOnClickListener { onClicked(recipe) }
        binding.buttonMore.setOnClickListener { showPopupMenu(it) }
        binding.recipeIsFavorite.visibility = if (recipe.favorite) View.VISIBLE else View.INVISIBLE
    }

    override fun getLayout(): Int {
        return R.layout.list_item_recipe
    }

    override fun initializeViewBinding(view: View): ListItemRecipeBinding {
        return ListItemRecipeBinding.bind(view)
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.recipe_item)

        popupMenu.menu.findItem(R.id.add_remove_from_favorites).title =
            context.getString(if (recipe.favorite) R.string.label_button_remove_from_favorites else R.string.label_button_add_to_favorites)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add_remove_from_favorites -> {
                    onAddRemoveFromFavouritesClicked(recipe)
                    return@setOnMenuItemClickListener true
                }

                R.id.add_to_day_plan -> {
                    onAssignToDayPanClicked(recipe)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

}