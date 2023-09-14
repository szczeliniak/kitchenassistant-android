package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemIngredientBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipeResponse

class IngredientItem(
    private val context: Context,
    private val recipeId: Int,
    private val ingredient: RecipeResponse.Recipe.IngredientGroup.Ingredient,
    private val onDeleteClicked: (recipeId: Int, ingredient: RecipeResponse.Recipe.IngredientGroup.Ingredient) -> Unit,
) :
    BindableItem<ListItemIngredientBinding>() {

    override fun bind(binding: ListItemIngredientBinding, position: Int) {
        binding.ingredientName.text = ingredient.name
        binding.ingredientQuantity.text = ingredient.quantity ?: "---"
        binding.buttonMore.setOnClickListener { showPopupMenu(it) }
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.ingredient_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    onDeleteClicked(recipeId, ingredient)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    override fun getLayout(): Int {
        return R.layout.list_item_ingredient
    }

    override fun initializeViewBinding(view: View): ListItemIngredientBinding {
        return ListItemIngredientBinding.bind(view)
    }

}