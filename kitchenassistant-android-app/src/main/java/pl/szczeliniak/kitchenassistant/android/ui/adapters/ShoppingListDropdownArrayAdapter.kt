package pl.szczeliniak.kitchenassistant.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.appcompat.widget.AppCompatTextView
import pl.szczeliniak.kitchenassistant.android.databinding.DropdownShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.ui.utils.ArrayAdapterUtils.Companion.getItems
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

    data class ViewHolder(
        val nameTextView: AppCompatTextView,
        val dateTextView: AppCompatTextView
    )

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint != null) {
                    val list = filterCategories(constraint)
                    results.values = list
                    results.count = list.size
                }
                return results
            }

            private fun filterCategories(constraint: CharSequence): ArrayList<ShoppingList> {
                val shoppingLists = ArrayList<ShoppingList>()
                for (shoppingList in getItems()) {
                    if (shoppingList.name.lowercase().contains(constraint.toString().lowercase())) {
                        shoppingLists.add(shoppingList)
                    }
                }
                return shoppingLists
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.values?.let {
                    val items = it as ArrayList<*>
                    if (items.isNotEmpty()) {
                        clear()
                        items.forEach { sl -> add(sl as ShoppingList) }
                        notifyDataSetChanged()
                    }
                }
            }

        }
    }

}