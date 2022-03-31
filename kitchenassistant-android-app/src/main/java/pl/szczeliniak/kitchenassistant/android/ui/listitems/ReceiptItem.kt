package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemReceiptBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.ui.utils.fillOrHide

class ReceiptItem constructor(
    private val context: Context,
    private val receipt: Receipt,
    private val onClick: OnClick?,
    private val onDeleteClick: OnClick?,
) : BindableItem<ListItemReceiptBinding>() {

    override fun bind(binding: ListItemReceiptBinding, position: Int) {
        binding.receiptItemTextviewName.text = receipt.name
        binding.receiptItemTextviewAuthor.fillOrHide(receipt.author, binding.receiptItemTextviewAuthor)
        binding.receiptItemTextviewDescription.fillOrHide(receipt.description, binding.receiptItemTextviewDescription)
        onClick.let { onClick -> binding.root.setOnClickListener { onClick?.onClick(receipt) } }
        binding.receiptItemButtonMore.setOnClickListener { showPopupMenu(it) }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_receipt
    }

    override fun initializeViewBinding(view: View): ListItemReceiptBinding {
        return ListItemReceiptBinding.bind(view)
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.fragment_receipt_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.fragment_receipts_menu_item_delete -> {
                    onDeleteClick?.onClick(receipt)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    fun interface OnClick {
        fun onClick(receipt: Receipt)
    }

}