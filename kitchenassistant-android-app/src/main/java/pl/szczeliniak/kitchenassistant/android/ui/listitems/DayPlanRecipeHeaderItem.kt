package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.view.View
import androidx.fragment.app.FragmentManager
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemHeaderDayPlanRecipeBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlanResponse
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog

class DayPlanRecipeHeaderItem(
    private val recipe: DayPlanResponse.DayPlan.Recipe,
    private val fragmentManager: FragmentManager,
    private val onClicked: ((recipeId: Int) -> Unit)? = null,
    private val onDeleteClicked: ((recipeId: Int) -> Unit)? = null
) : BindableItem<ListItemHeaderDayPlanRecipeBinding>() {

    override fun bind(binding: ListItemHeaderDayPlanRecipeBinding, position: Int) {
        binding.recipeName.text = recipe.name
        recipe.originalRecipeId?.let { recipeId ->
            onClicked?.let {
                binding.root.setOnClickListener { it(recipeId) }
            }
        }

        onDeleteClicked?.let {
            binding.openDeleteDialog.visibility = View.VISIBLE
            binding.openDeleteDialog.setOnClickListener {
                ConfirmationDialog.show(fragmentManager) {
                    it(recipe.id)
                }
            }
        } ?: run {
            binding.openDeleteDialog.visibility = View.GONE
        }

    }

    override fun getLayout(): Int {
        return R.layout.list_item_header_day_plan_recipe
    }

    override fun initializeViewBinding(view: View): ListItemHeaderDayPlanRecipeBinding {
        return ListItemHeaderDayPlanRecipeBinding.bind(view)
    }

}