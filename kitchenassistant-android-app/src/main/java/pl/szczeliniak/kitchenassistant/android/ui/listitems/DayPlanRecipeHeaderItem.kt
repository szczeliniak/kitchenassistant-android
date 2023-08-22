package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemHeaderDayPlanRecipeBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlanResponse

class DayPlanRecipeHeaderItem constructor(
    private val recipe: DayPlanResponse.DayPlan.Recipe,
    private val context: Context,
    private val onDeleteClick: OnClick
) : BindableItem<ListItemHeaderDayPlanRecipeBinding>() {

    override fun bind(binding: ListItemHeaderDayPlanRecipeBinding, position: Int) {
        binding.recipeName.text = recipe.name
        binding.root.setOnLongClickListener { showPopupMenu(binding.root) }
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.day_plan_recipe_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    onDeleteClick.onClick(recipe.id)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    override fun getLayout(): Int {
        return R.layout.list_item_header_day_plan_recipe
    }

    override fun initializeViewBinding(view: View): ListItemHeaderDayPlanRecipeBinding {
        return ListItemHeaderDayPlanRecipeBinding.bind(view)
    }

    fun interface OnClick {
        fun onClick(recipeId: Int)
    }

}