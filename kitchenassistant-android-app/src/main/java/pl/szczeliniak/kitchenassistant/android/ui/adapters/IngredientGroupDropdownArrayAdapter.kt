package pl.szczeliniak.kitchenassistant.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.appcompat.widget.AppCompatTextView
import pl.szczeliniak.kitchenassistant.android.databinding.DropdownIngredientGroupBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.IngredientGroup

class IngredientGroupDropdownArrayAdapter(context: Context) : ArrayAdapter<IngredientGroup>(context, 0, ArrayList()) {

    private val allIngredientGroups = ArrayList<IngredientGroup>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val binding: DropdownIngredientGroupBinding?

        if (convertView == null) {
            binding = DropdownIngredientGroupBinding.inflate(LayoutInflater.from(context))
            viewHolder = ViewHolder(binding.ingredientGroup)
            binding.root.tag = viewHolder
        } else {
            binding = DropdownIngredientGroupBinding.bind(convertView)
            viewHolder = binding.root.tag as ViewHolder
        }

        getItem(position)?.name?.let {
            viewHolder.nameTextView.text = String.format(it)
        }

        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    fun getIngredientGroupById(id: Int): IngredientGroup? {
        return getItems().firstOrNull { it.id == id }
    }

    data class ViewHolder(
        val nameTextView: AppCompatTextView
    )

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                var items = getItems()
                if (constraint != null) {
                    items = allIngredientGroups.filter { it.name.contains(constraint) }
                }
                results.values = items
                results.count = items.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.values?.let {
                    val items = it as ArrayList<*>
                    if (items.isNotEmpty()) {
                        clear()
                        items.forEach { r -> add(r as IngredientGroup?) }
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun getItems(): List<IngredientGroup> {
        val items = ArrayList<IngredientGroup>()
        for (i in 0 until count) {
            items += getItem(i) as IngredientGroup
        }
        return items
    }

    fun getIngredientGroupByName(toString: String): IngredientGroup? {
        return getItems().firstOrNull { it.name.lowercase() == toString.lowercase() }
    }

    fun refresh(ingredientGroups: List<IngredientGroup>) {
        allIngredientGroups.clear()
        allIngredientGroups.addAll(ingredientGroups)
        clear()
        addAll(allIngredientGroups)
    }

}