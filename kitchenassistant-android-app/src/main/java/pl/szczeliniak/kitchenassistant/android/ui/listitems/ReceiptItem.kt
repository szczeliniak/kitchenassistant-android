package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemReceiptBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide

class ReceiptItem constructor(
    private val context: Context,
    private val receipt: Receipt,
    private val onClick: OnClick,
    private val onDeleteClick: OnClick,
    private val onEditClick: OnClick,
    private val onAddRemoveFromFavourites: OnClick,
) : BindableItem<ListItemReceiptBinding>() {

    override fun bind(binding: ListItemReceiptBinding, position: Int) {
        binding.receiptName.text = receipt.name
        binding.receiptAuthor.fillOrHide(receipt.author, binding.receiptAuthor)
        binding.receiptDescription.fillOrHide(receipt.description, binding.receiptDescription)
        binding.root.setOnClickListener { onClick.onClick(receipt) }
        binding.buttonMore.setOnClickListener { showPopupMenu(it) }
        binding.receiptIsFavorite.visibility = if (receipt.favorite) View.VISIBLE else View.GONE
    }

    override fun getLayout(): Int {
        return R.layout.list_item_receipt
    }

    override fun initializeViewBinding(view: View): ListItemReceiptBinding {
        return ListItemReceiptBinding.bind(view)
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.receipt_item)

        popupMenu.menu.findItem(R.id.receipt_item_menu_item_add_remove_from_favorites).title =
            context.getString(if (receipt.favorite) R.string.label_button_remove_from_favorites else R.string.label_button_add_to_favorites)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.receipt_item_menu_item_delete -> {
                    onDeleteClick.onClick(receipt)
                    return@setOnMenuItemClickListener true
                }
                R.id.receipt_item_menu_item_edit -> {
                    onEditClick.onClick(receipt)
                    return@setOnMenuItemClickListener true
                }
                R.id.receipt_item_menu_item_add_remove_from_favorites -> {
                    onAddRemoveFromFavourites.onClick(receipt)
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