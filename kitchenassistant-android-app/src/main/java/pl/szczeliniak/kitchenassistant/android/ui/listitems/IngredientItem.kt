package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemIngredientBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipeResponse

class IngredientItem(
    private val ingredient: RecipeResponse.Recipe.IngredientGroup.Ingredient
) :
    BindableItem<ListItemIngredientBinding>() {

    override fun bind(binding: ListItemIngredientBinding, position: Int) {
        binding.ingredientName.text = ingredient.name
        binding.ingredientQuantity.text = ingredient.quantity ?: "---"
    }

    override fun getLayout(): Int {
        return R.layout.list_item_ingredient
    }

    override fun initializeViewBinding(view: View): ListItemIngredientBinding {
        return ListItemIngredientBinding.bind(view)
    }

}