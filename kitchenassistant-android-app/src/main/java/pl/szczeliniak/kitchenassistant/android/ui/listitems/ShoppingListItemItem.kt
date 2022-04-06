package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemShoppingListItemBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingListItem

class ShoppingListItemItem constructor(
    private val context: Context,
    private val shoppingListId: Int,
    private val shoppingListItem: ShoppingListItem,
    private val onDeleteClick: OnClick,
    private val onCheckClick: OnCheckboxClick
) :
    BindableItem<ListItemShoppingListItemBinding>() {

    override fun bind(binding: ListItemShoppingListItemBinding, position: Int) {
        binding.shoppingListItemName.text = shoppingListItem.name
        binding.shoppingListItemQuantity.text = shoppingListItem.quantity
        binding.shoppingListItemIsDone.isChecked = shoppingListItem.done

        binding.shoppingListItemIsDone.setOnCheckedChangeListener { _, isChecked ->
            onCheckClick.onClick(shoppingListId, shoppingListItem, isChecked)
        }

        binding.buttonMore.setOnClickListener { showPopupMenu(it) }
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.shopping_list_item_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.shopping_list_item_item_menu_item_delete -> {
                    onDeleteClick.onClick(shoppingListId, shoppingListItem)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    override fun getLayout(): Int {
        return R.layout.list_item_shopping_list_item
    }

    override fun initializeViewBinding(view: View): ListItemShoppingListItemBinding {
        return ListItemShoppingListItemBinding.bind(view)
    }

    fun interface OnClick {
        fun onClick(shoppingListId: Int, shoppingListItem: ShoppingListItem)
    }

    fun interface OnCheckboxClick {
        fun onClick(shoppingListId: Int, shoppingListItem: ShoppingListItem, state: Boolean)
    }

}