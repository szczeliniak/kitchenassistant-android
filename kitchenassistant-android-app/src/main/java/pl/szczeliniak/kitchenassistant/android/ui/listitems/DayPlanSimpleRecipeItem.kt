package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemDayPlanSimpleRecipeBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanRecipe
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide

class DayPlanSimpleRecipeItem constructor(
    private val context: Context,
    private val recipe: DayPlanRecipe,
    private val onClick: OnClick,
    private val onDeleteClick: OnClick
) : BindableItem<ListItemDayPlanSimpleRecipeBinding>() {

    override fun bind(binding: ListItemDayPlanSimpleRecipeBinding, position: Int) {
        binding.recipeName.text = recipe.name
        recipe.category?.let { binding.recipeCategory.fillOrHide(it, binding.recipeCategory) }
        binding.author.fillOrHide(recipe.author, binding.author)
        binding.root.setOnClickListener { onClick.onClick(recipe) }
        binding.buttonMore.setOnClickListener { showPopupMenu(it) }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_day_plan_simple_recipe
    }

    override fun initializeViewBinding(view: View): ListItemDayPlanSimpleRecipeBinding {
        return ListItemDayPlanSimpleRecipeBinding.bind(view)
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.day_plan_recipe_item)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    onDeleteClick.onClick(recipe)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    fun interface OnClick {
        fun onClick(recipe: DayPlanRecipe)
    }

}