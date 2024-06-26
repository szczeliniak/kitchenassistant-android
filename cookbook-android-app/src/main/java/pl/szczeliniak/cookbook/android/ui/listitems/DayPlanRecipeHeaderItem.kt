package pl.szczeliniak.cookbook.android.ui.listitems

import android.view.View
import androidx.fragment.app.FragmentManager
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.cookbook.android.R
import pl.szczeliniak.cookbook.android.databinding.ListItemHeaderDayPlanRecipeBinding
import pl.szczeliniak.cookbook.android.network.responses.DayPlanResponse
import pl.szczeliniak.cookbook.android.ui.dialogs.confirmation.ConfirmationDialog

class DayPlanRecipeHeaderItem(
    private val recipe: DayPlanResponse.DayPlan.Recipe,
    private val fragmentManager: FragmentManager,
    private val onClicked: ((recipeId: Int) -> Unit)? = null,
    private val onDeleteClicked: ((recipeId: Int) -> Unit)? = null
) : BindableItem<ListItemHeaderDayPlanRecipeBinding>() {

    override fun bind(binding: ListItemHeaderDayPlanRecipeBinding, position: Int) {
        binding.recipeName.text = recipe.name
        onClicked?.let {
            binding.root.setOnClickListener { it(recipe.originalRecipeId) }
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