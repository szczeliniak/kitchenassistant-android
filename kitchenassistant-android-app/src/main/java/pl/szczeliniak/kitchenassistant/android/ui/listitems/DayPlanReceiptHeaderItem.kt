package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemHeaderDayPlanReceiptBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanReceipt

class DayPlanReceiptHeaderItem constructor(
    private val receipt: DayPlanReceipt
) : BindableItem<ListItemHeaderDayPlanReceiptBinding>() {

    override fun bind(binding: ListItemHeaderDayPlanReceiptBinding, position: Int) {
        binding.receiptName.text = receipt.name
    }

    override fun getLayout(): Int {
        return R.layout.list_item_header_day_plan_receipt
    }

    override fun initializeViewBinding(view: View): ListItemHeaderDayPlanReceiptBinding {
        return ListItemHeaderDayPlanReceiptBinding.bind(view)
    }

}