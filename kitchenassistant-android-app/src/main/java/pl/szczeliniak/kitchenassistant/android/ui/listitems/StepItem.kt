package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemStepBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipeResponse

class StepItem(
    private val context: Context,
    private val recipeId: Int,
    private val step: RecipeResponse.Recipe.Step,
    private val onDeleteClicked: (recipeId: Int, step: RecipeResponse.Recipe.Step) -> Unit,
    private val onEditClicked: (recipeId: Int, step: RecipeResponse.Recipe.Step) -> Unit
) :
    BindableItem<ListItemStepBinding>() {

    override fun bind(binding: ListItemStepBinding, position: Int) {
        binding.stepDescription.text = String.format("%s. %s", position + 1, step.description)
        binding.buttonMore.setOnClickListener { showPopupMenu(it) }
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.step_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    onDeleteClicked(recipeId, step)
                    return@setOnMenuItemClickListener true
                }

                R.id.edit -> {
                    onEditClicked(recipeId, step)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    override fun getLayout(): Int {
        return R.layout.list_item_step
    }

    override fun initializeViewBinding(view: View): ListItemStepBinding {
        return ListItemStepBinding.bind(view)
    }

}