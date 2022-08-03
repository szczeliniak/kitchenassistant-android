package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemHeaderDayPlanRecipeBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanRecipe

class DayPlanRecipeHeaderItem constructor(
    private val recipe: DayPlanRecipe
) : BindableItem<ListItemHeaderDayPlanRecipeBinding>() {

    override fun bind(binding: ListItemHeaderDayPlanRecipeBinding, position: Int) {
        binding.recipeName.text = recipe.name
    }

    override fun getLayout(): Int {
        return R.layout.list_item_header_day_plan_recipe
    }

    override fun initializeViewBinding(view: View): ListItemHeaderDayPlanRecipeBinding {
        return ListItemHeaderDayPlanRecipeBinding.bind(view)
    }

}