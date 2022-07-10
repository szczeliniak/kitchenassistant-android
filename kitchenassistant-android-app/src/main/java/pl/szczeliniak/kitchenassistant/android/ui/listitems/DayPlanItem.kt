package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemDayPlanBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlan
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils

class DayPlanItem constructor(
    private val context: Context,
    private val dayPlan: DayPlan,
    private val onClick: OnClick,
    private val onDeleteClick: OnClick,
    private val onEditClick: OnClick
) : BindableItem<ListItemDayPlanBinding>() {

    override fun bind(binding: ListItemDayPlanBinding, position: Int) {
        binding.dayPlanDate.fillOrHide(LocalDateUtils.stringify(dayPlan.date), binding.dayPlanDate)
        binding.dayPlanName.text = dayPlan.name
        binding.root.setOnClickListener { onClick.onClick(dayPlan) }
        binding.buttonMore.setOnClickListener { showPopupMenu(it) }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_day_plan
    }

    override fun initializeViewBinding(view: View): ListItemDayPlanBinding {
        return ListItemDayPlanBinding.bind(view)
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.shopping_list_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    onDeleteClick.onClick(dayPlan)
                    return@setOnMenuItemClickListener true
                }
                R.id.edit -> {
                    onEditClick.onClick(dayPlan)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    fun interface OnClick {
        fun onClick(dayPlan: DayPlan)
    }

}