package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemStepBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Step
import pl.szczeliniak.kitchenassistant.android.ui.utils.fillOrHide

class StepItem constructor(
    private val context: Context,
    private val receiptId: Int,
    private val step: Step,
    private val onDeleteClick: OnClick
) :
    BindableItem<ListItemStepBinding>() {

    override fun bind(binding: ListItemStepBinding, position: Int) {
        binding.listItemStepTextviewName.text = String.format("%s. %s", position + 1, step.title)
        binding.listItemStepTextviewDescription.fillOrHide(step.description, binding.listItemStepTextviewDescription)
        binding.stepItemButtonMore.setOnClickListener { showPopupMenu(it) }
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.step_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.step_item_menu_item_delete -> {
                    onDeleteClick.onClick(receiptId, step)
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

    fun interface OnClick {
        fun onClick(receiptId: Int, step: Step)
    }

}