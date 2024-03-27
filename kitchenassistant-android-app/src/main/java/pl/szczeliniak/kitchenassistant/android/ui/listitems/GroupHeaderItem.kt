package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemGroupHeaderBinding

class GroupHeaderItem(
    private val name: String
) : BindableItem<ListItemGroupHeaderBinding>() {

    override fun bind(binding: ListItemGroupHeaderBinding, position: Int) {
        binding.ingredientGroupName.text = name
    }

    override fun getLayout(): Int {
        return R.layout.list_item_group_header
    }

    override fun initializeViewBinding(view: View): ListItemGroupHeaderBinding {
        return ListItemGroupHeaderBinding.bind(view)
    }

}