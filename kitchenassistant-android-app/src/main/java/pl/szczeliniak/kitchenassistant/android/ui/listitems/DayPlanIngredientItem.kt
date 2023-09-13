package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemDayPlanIngredientBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlanResponse

class DayPlanIngredientItem(
    private val ingredient: DayPlanResponse.DayPlan.Recipe.IngredientGroup.Ingredient,
) : BindableItem<ListItemDayPlanIngredientBinding>() {

    override fun bind(binding: ListItemDayPlanIngredientBinding, position: Int) {
        binding.ingredientName.text = ingredient.name
        binding.ingredientQuantity.text = ingredient.quantity
    }

    override fun getLayout(): Int {
        return R.layout.list_item_day_plan_ingredient
    }

    override fun initializeViewBinding(view: View): ListItemDayPlanIngredientBinding {
        return ListItemDayPlanIngredientBinding.bind(view)
    }

}