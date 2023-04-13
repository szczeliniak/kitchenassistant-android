package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.view.View
import androidx.fragment.app.FragmentManager
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemHeaderIngredientGroupBinding
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditingredient.AddEditIngredientGroupDialog

class IngredientGroupHeaderItem(
    private val id: Int,
    private val name: String,
    private val recipeId: Int,
    private val fragmentManager: FragmentManager
) :
    BindableItem<ListItemHeaderIngredientGroupBinding>() {

    override fun bind(binding: ListItemHeaderIngredientGroupBinding, position: Int) {
        binding.ingredientGroupName.text = name
        binding.openEditDialog.setOnClickListener {
            AddEditIngredientGroupDialog.show(fragmentManager, recipeId, id)
        }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_header_ingredient_group
    }

    override fun initializeViewBinding(view: View): ListItemHeaderIngredientGroupBinding {
        return ListItemHeaderIngredientGroupBinding.bind(view)
    }

}