package pl.szczeliniak.cookbook.android.ui.listitems

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.cookbook.android.R
import pl.szczeliniak.cookbook.android.databinding.ListItemDayPlanIngredientBinding
import pl.szczeliniak.cookbook.android.network.responses.DayPlanResponse
import java.time.LocalDate

class DayPlanIngredientItem(
    private val ingredient: DayPlanResponse.DayPlan.Recipe.IngredientGroup.Ingredient,
    private val date: LocalDate,
    private val recipeId: Int,
    private val ingredientGroupId: Int,
    private val onCheckboxClicked: ((date: LocalDate, recipeId: Int, ingredientGroupId: Int, ingredientId: Int, state: Boolean) -> Unit)
) : BindableItem<ListItemDayPlanIngredientBinding>() {

    override fun bind(binding: ListItemDayPlanIngredientBinding, position: Int) {
        binding.ingredientName.text = ingredient.name
        binding.ingredientQuantity.text = ingredient.quantity
        binding.ingredientIsChecked.isChecked = ingredient.checked
        binding.ingredientIsChecked.setOnClickListener { _ ->
            onCheckboxClicked(date, recipeId, ingredientGroupId, ingredient.id, binding.ingredientIsChecked.isChecked)
        }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_day_plan_ingredient
    }

    override fun initializeViewBinding(view: View): ListItemDayPlanIngredientBinding {
        return ListItemDayPlanIngredientBinding.bind(view)
    }

}