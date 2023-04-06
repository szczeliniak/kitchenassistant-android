package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils

class ShoppingListItem constructor(
    private val context: Context,
    private val shoppingList: ShoppingList,
    private val onClick: OnClick,
    private val onDeleteClick: OnClick,
    private val onEditClick: OnClick,
    private val onArchiveClick: OnClick?
) : BindableItem<ListItemShoppingListBinding>() {

    override fun bind(binding: ListItemShoppingListBinding, position: Int) {
        binding.shoppingListName.text = shoppingList.name
        binding.shoppingListDescription.fillOrHide(shoppingList.description, binding.shoppingListDescription)
        binding.shoppingListDate.fillOrHide(
            LocalDateUtils.stringify(shoppingList.date),
            binding.shoppingListDate
        )
        binding.root.setOnClickListener { onClick.onClick(shoppingList) }
        binding.buttonMore.setOnClickListener { showPopupMenu(it) }
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

        if (onArchiveClick == null) {
            popupMenu.menu.removeItem(R.id.archive)
        }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    onDeleteClick.onClick(shoppingList)
                    return@setOnMenuItemClickListener true
                }

                R.id.edit -> {
                    onEditClick.onClick(shoppingList)
                    return@setOnMenuItemClickListener true
                }

                R.id.archive -> {
                    onArchiveClick?.onClick(shoppingList)
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