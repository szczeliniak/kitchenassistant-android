package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemDayPlanBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlansResponse
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils

class DayPlanItem(
    private val dayPlan: DayPlansResponse.DayPlan,
    private val onClicked: (dayPlan: DayPlansResponse.DayPlan) -> Unit,
) : BindableItem<ListItemDayPlanBinding>() {

    override fun bind(binding: ListItemDayPlanBinding, position: Int) {
        binding.dayPlanDate.text = LocalDateUtils.stringify(dayPlan.date)
        binding.root.setOnClickListener { onClicked(dayPlan) }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_day_plan
    }

    override fun initializeViewBinding(view: View): ListItemDayPlanBinding {
        return ListItemDayPlanBinding.bind(view)
    }

}