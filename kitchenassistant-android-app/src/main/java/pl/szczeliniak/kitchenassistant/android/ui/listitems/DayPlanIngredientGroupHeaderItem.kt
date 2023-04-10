package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemHeaderDayPlanIngredientGroupBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanIngredientGroup

class DayPlanIngredientGroupHeaderItem(
    private val ingredientGroup: DayPlanIngredientGroup
) : BindableItem<ListItemHeaderDayPlanIngredientGroupBinding>() {

    override fun bind(binding: ListItemHeaderDayPlanIngredientGroupBinding, position: Int) {
        binding.ingredientGroupName.text = ingredientGroup.name
    }

    override fun getLayout(): Int {
        return R.layout.list_item_header_day_plan_ingredient_group
    }

    override fun initializeViewBinding(view: View): ListItemHeaderDayPlanIngredientGroupBinding {
        return ListItemHeaderDayPlanIngredientGroupBinding.bind(view)
    }

}