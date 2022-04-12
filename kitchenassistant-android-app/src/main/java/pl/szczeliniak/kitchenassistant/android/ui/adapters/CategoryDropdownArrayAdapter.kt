package pl.szczeliniak.kitchenassistant.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.appcompat.widget.AppCompatTextView
import pl.szczeliniak.kitchenassistant.android.databinding.DropdownCategoryBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.ui.utils.ArrayAdapterUtils.Companion.getItems

class CategoryDropdownArrayAdapter(context: Context) : ArrayAdapter<Category>(context, 0, ArrayList()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val binding: DropdownCategoryBinding?

        if (convertView == null) {
            binding = DropdownCategoryBinding.inflate(LayoutInflater.from(context))
            viewHolder = ViewHolder(binding.root)
            binding.root.tag = viewHolder
        } else {
            binding = DropdownCategoryBinding.bind(convertView)
            viewHolder = binding.root.tag as ViewHolder
        }

        getItem(position)?.let {
            viewHolder.nameTextView.text = String.format(it.name)
        }

        return binding.root
    }

    data class ViewHolder(
        val nameTextView: AppCompatTextView
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

            private fun filterCategories(constraint: CharSequence): ArrayList<Category> {
                val categories = ArrayList<Category>()
                for (category in getItems()) {
                    if (category.name.lowercase().contains(constraint.toString().lowercase())) {
                        categories.add(category)
                    }
                }
                return categories
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.values?.let {
                    val items = it as ArrayList<*>
                    if (items.isNotEmpty()) {
                        clear()
                        items.forEach { r -> add(r as Category) }
                        notifyDataSetChanged()
                    }
                }
            }

        }
    }

}