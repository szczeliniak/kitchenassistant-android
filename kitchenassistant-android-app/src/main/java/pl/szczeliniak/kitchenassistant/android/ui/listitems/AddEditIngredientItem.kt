package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.core.widget.doOnTextChanged
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemAddEditIngredientBinding

class AddEditIngredientItem(
    private val context: Context,
    private val id: Int?,
    private var name: String = "",
    private var quantity: String?,
    private val onDeleteClicked: OnDeleteClicked,
    private val onFormChanged: OnFormChanged
) : BindableItem<ListItemAddEditIngredientBinding>() {

    override fun bind(binding: ListItemAddEditIngredientBinding, position: Int) {
        binding.remove.setOnClickListener {
            onDeleteClicked.onDeleteClicked(this@AddEditIngredientItem)
            onFormChanged.onFormChanged()
        }

        binding.ingredientName.setText(name)
        binding.ingredientName.doOnTextChanged { text, _, _, _ ->
            name = text.toString()
            if (!isNameValid()) {
                binding.ingredientNameLayout.error = context.getString(R.string.message_ingredient_name_is_empty)
            } else {
                binding.ingredientNameLayout.error = null
            }
            onFormChanged.onFormChanged()
        }

        quantity?.let { quantity ->
            binding.ingredientQuantity.setText(quantity)
        }
        binding.ingredientQuantity.doOnTextChanged { text, _, _, _ ->
            quantity = text.toString()
            onFormChanged.onFormChanged()
        }

        onFormChanged.onFormChanged()
    }

    override fun getLayout(): Int {
        return R.layout.list_item_add_edit_ingredient
    }

    override fun initializeViewBinding(view: View): ListItemAddEditIngredientBinding {
        return ListItemAddEditIngredientBinding.bind(view)
    }

    fun interface OnDeleteClicked {
        fun onDeleteClicked(addEditIngredientItem: AddEditIngredientItem)
    }

    fun interface OnFormChanged {
        fun onFormChanged()
    }

    private fun isNameValid(): Boolean {
        return name.isNotEmpty()
    }

    fun isValid(): Boolean {
        return isNameValid()
    }

    val ingredientId: Int?
        get() {
            return id
        }

    val ingredientName: String
        get() {
            return name
        }

    val ingredientQuantity: String?
        get() {
            return if (quantity.isNullOrEmpty()) null else quantity
        }

}