package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemDayPlanBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlansResponse
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils

class DayPlanItem(
    private val context: Context,
    private val dayPlan: DayPlansResponse.DayPlan,
    private val onClicked: (dayPlan: DayPlansResponse.DayPlan) -> Unit,
    private val onDeleteClicked: ((dayPlan: DayPlansResponse.DayPlan) -> Unit)? = null,
    private val onEditClicked: ((dayPlan: DayPlansResponse.DayPlan) -> Unit)? = null
) : BindableItem<ListItemDayPlanBinding>() {

    override fun bind(binding: ListItemDayPlanBinding, position: Int) {
        binding.dayPlanDate.text = LocalDateUtils.stringify(dayPlan.date)
        binding.root.setOnClickListener { onClicked(dayPlan) }
        if (showMenu) {
            binding.buttonMore.visibility = View.VISIBLE
            binding.buttonMore.setOnClickListener { showPopupMenu(it) }
        } else {
            binding.buttonMore.visibility = View.GONE
        }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_day_plan
    }

    override fun initializeViewBinding(view: View): ListItemDayPlanBinding {
        return ListItemDayPlanBinding.bind(view)
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.dayplan_item)

        if (onEditClicked == null) {
            popupMenu.menu.removeItem(R.id.edit)
        }
        if (onDeleteClicked == null) {
            popupMenu.menu.removeItem(R.id.delete)
        }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    onDeleteClicked?.let { it(dayPlan) }
                    return@setOnMenuItemClickListener true
                }

                R.id.edit -> {
                    onEditClicked?.let { it(dayPlan) }
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    private val showMenu: Boolean
        get() = onDeleteClicked != null || onEditClicked != null

}