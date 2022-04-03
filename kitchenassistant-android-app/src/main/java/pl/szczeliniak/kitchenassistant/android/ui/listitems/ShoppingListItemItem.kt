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
    private val onDeleteClick: OnClick
) :
    BindableItem<ListItemShoppingListItemBinding>() {

    override fun bind(binding: ListItemShoppingListItemBinding, position: Int) {
        binding.listItemShoppingListItemTextviewName.text = shoppingListItem.name
        binding.listItemShoppingListItemTextviewQuantity.text = shoppingListItem.quantity
        binding.listItemShoppingListItemButtonMore.setOnClickListener { showPopupMenu(it) }
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.shopping_list_item_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.ingredient_item_menu_item_delete -> {
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

}