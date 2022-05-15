package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemCategoryBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.ui.components.buttons.KaIconButton

class CategoryItem constructor(
    private val context: Context,
    private val category: Category,
    private val onDeleteClick: OnClick,
    private val onEditClick: OnClick
) :
    BindableItem<ListItemCategoryBinding>() {

    override fun bind(binding: ListItemCategoryBinding, position: Int) {
        binding.categoryName.text = category.name
        binding.buttonMore.onClick = KaIconButton.OnClick { showPopupMenu(it) }
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.category_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.category_item_menu_item_delete -> {
                    onDeleteClick.onClick(category)
                    return@setOnMenuItemClickListener true
                }
                R.id.category_item_menu_item_edit -> {
                    onEditClick.onClick(category)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    override fun getLayout(): Int {
        return R.layout.list_item_category
    }

    override fun initializeViewBinding(view: View): ListItemCategoryBinding {
        return ListItemCategoryBinding.bind(view)
    }

    fun interface OnClick {
        fun onClick(category: Category)
    }

}