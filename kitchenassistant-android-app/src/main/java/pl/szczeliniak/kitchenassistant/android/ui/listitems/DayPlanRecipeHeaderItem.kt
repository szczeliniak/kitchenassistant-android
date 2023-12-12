package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemHeaderDayPlanRecipeBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlanResponse
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog

class DayPlanRecipeHeaderItem(
    private val recipe: DayPlanResponse.DayPlan.Recipe,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val onDeleteClicked: (recipeId: Int) -> Unit
) : BindableItem<ListItemHeaderDayPlanRecipeBinding>() {

    override fun bind(binding: ListItemHeaderDayPlanRecipeBinding, position: Int) {
        binding.recipeName.text = recipe.name
        binding.openDeleteDialog.setOnClickListener {
            ConfirmationDialog.show(fragmentManager) {
                onDeleteClicked(recipe.id)
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_header_day_plan_recipe
    }

    override fun initializeViewBinding(view: View): ListItemHeaderDayPlanRecipeBinding {
        return ListItemHeaderDayPlanRecipeBinding.bind(view)
    }

}