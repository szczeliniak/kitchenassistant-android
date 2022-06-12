package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemHeaderIngredientGroupBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.IngredientGroup

class IngredientGroupHeaderItem constructor(private val ingredientGroup: IngredientGroup) :
    BindableItem<ListItemHeaderIngredientGroupBinding>() {

    override fun bind(binding: ListItemHeaderIngredientGroupBinding, position: Int) {
        binding.ingredientGroupName.text = ingredientGroup.name
    }

    override fun getLayout(): Int {
        return R.layout.list_item_header_ingredient_group
    }

    override fun initializeViewBinding(view: View): ListItemHeaderIngredientGroupBinding {
        return ListItemHeaderIngredientGroupBinding.bind(view)
    }

}