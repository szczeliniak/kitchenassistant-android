package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemDayPlanIngredientBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlanResponse

class DayPlanIngredientItem(
    private val context: Context,
    private val ingredient: DayPlanResponse.DayPlan.Recipe.IngredientGroup.Ingredient,
    private val recipeId: Int,
    private val onAddToShoppingListItem: OnClick
) : BindableItem<ListItemDayPlanIngredientBinding>() {

    override fun bind(binding: ListItemDayPlanIngredientBinding, position: Int) {
        binding.ingredientName.text = ingredient.name
        binding.ingredientQuantity.text = ingredient.quantity
        binding.buttonMore.setOnClickListener { showPopupMenu(it) }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_day_plan_ingredient
    }

    override fun initializeViewBinding(view: View): ListItemDayPlanIngredientBinding {
        return ListItemDayPlanIngredientBinding.bind(view)
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.day_plan_ingredient_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add_to_shopping_list -> {
                    onAddToShoppingListItem.onClick(ingredient, recipeId)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    fun interface OnClick {
        fun onClick(ingredient: DayPlanResponse.DayPlan.Recipe.IngredientGroup.Ingredient, recipeId: Int)
    }

}