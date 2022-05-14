package pl.szczeliniak.kitchenassistant.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import pl.szczeliniak.kitchenassistant.android.databinding.DropdownShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.ui.components.TextComponent
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils

class ShoppingListDropdownArrayAdapter(context: Context) : ArrayAdapter<ShoppingList>(context, 0, ArrayList()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val binding: DropdownShoppingListBinding?

        if (convertView == null) {
            binding = DropdownShoppingListBinding.inflate(LayoutInflater.from(context))
            viewHolder = ViewHolder(binding.shoppingListName, binding.shoppingListDate)
            binding.root.tag = viewHolder
        } else {
            binding = DropdownShoppingListBinding.bind(convertView)
            viewHolder = binding.root.tag as ViewHolder
        }

        getItem(position)?.let {
            viewHolder.nameTextView.text = it.name
            it.date?.let { date ->
                viewHolder.dateTextView.visibility = View.VISIBLE
                viewHolder.dateTextView.text = LocalDateUtils.stringify(date)
            } ?: kotlin.run {
                viewHolder.dateTextView.visibility = View.GONE
            }
        }

        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    data class ViewHolder(
        val nameTextView: TextComponent,
        val dateTextView: TextComponent
    )

}