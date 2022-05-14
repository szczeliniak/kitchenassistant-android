package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.ui.components.IconButtonComponent
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils

class ShoppingListItem constructor(
    private val context: Context,
    private val shoppingList: ShoppingList,
    private val onClick: OnClick,
    private val onDeleteClick: OnClick,
    private val onEditClick: OnClick
) : BindableItem<ListItemShoppingListBinding>() {

    override fun bind(binding: ListItemShoppingListBinding, position: Int) {
        binding.shoppingListName.text = shoppingList.name
        binding.shoppingListDescription.fillOrHide(shoppingList.description, binding.shoppingListDescription)
        binding.root.setOnClickListener { onClick.onClick(shoppingList) }
        binding.buttonMore.onClick = IconButtonComponent.OnClick { showPopupMenu(it) }
        shoppingList.date?.let {
            binding.shoppingListDate.text = LocalDateUtils.stringify(it)
        } ?: run {
            binding.shoppingListDate.visibility = View.GONE
        }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_shopping_list
    }

    override fun initializeViewBinding(view: View): ListItemShoppingListBinding {
        return ListItemShoppingListBinding.bind(view)
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.shopping_list_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.shopping_list_item_menu_item_delete -> {
                    onDeleteClick.onClick(shoppingList)
                    return@setOnMenuItemClickListener true
                }
                R.id.shopping_list_item_menu_item_edit -> {
                    onEditClick.onClick(shoppingList)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    fun interface OnClick {
        fun onClick(shoppingList: ShoppingList)
    }

}