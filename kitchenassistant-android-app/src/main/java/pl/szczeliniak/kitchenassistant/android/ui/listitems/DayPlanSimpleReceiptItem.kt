package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemDayPlanSimpleReceiptBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanSimpleReceipt
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide

class DayPlanSimpleReceiptItem constructor(
    private val context: Context,
    private val receipt: DayPlanSimpleReceipt,
    private val onClick: OnClick,
    private val onDeleteClick: OnClick
) : BindableItem<ListItemDayPlanSimpleReceiptBinding>() {

    override fun bind(binding: ListItemDayPlanSimpleReceiptBinding, position: Int) {
        binding.receiptName.text = receipt.name
        receipt.category?.let { binding.receiptCategory.fillOrHide(it, binding.receiptCategory) }
        binding.author.fillOrHide(receipt.author, binding.author)
        binding.root.setOnClickListener { onClick.onClick(receipt) }
        binding.buttonMore.setOnClickListener { showPopupMenu(it) }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_day_plan_simple_receipt
    }

    override fun initializeViewBinding(view: View): ListItemDayPlanSimpleReceiptBinding {
        return ListItemDayPlanSimpleReceiptBinding.bind(view)
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.day_plan_receipt_item)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    onDeleteClick.onClick(receipt)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    fun interface OnClick {
        fun onClick(receipt: DayPlanSimpleReceipt)
    }

}