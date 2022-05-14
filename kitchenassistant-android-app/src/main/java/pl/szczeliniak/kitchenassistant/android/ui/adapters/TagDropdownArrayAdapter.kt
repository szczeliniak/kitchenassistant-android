package pl.szczeliniak.kitchenassistant.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import pl.szczeliniak.kitchenassistant.android.databinding.DropdownTagBinding
import pl.szczeliniak.kitchenassistant.android.ui.components.TextComponent

class TagDropdownArrayAdapter(context: Context) : ArrayAdapter<String>(context, 0, ArrayList()) {

    private val allTags = ArrayList<String>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val binding: DropdownTagBinding?

        if (convertView == null) {
            binding = DropdownTagBinding.inflate(LayoutInflater.from(context))
            viewHolder = ViewHolder(binding.root)
            binding.root.tag = viewHolder
        } else {
            binding = DropdownTagBinding.bind(convertView)
            viewHolder = binding.root.tag as ViewHolder
        }

        getItem(position)?.let {
            viewHolder.nameTextView.text = String.format(it)
        }

        return binding.root
    }

    data class ViewHolder(
        val nameTextView: TextComponent
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

            private fun filterCategories(constraint: CharSequence): ArrayList<String> {
                return ArrayList(allTags.filter { c ->
                    c.lowercase().contains(constraint.toString().lowercase())
                })
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.values?.let {
                    val items = it as ArrayList<*>
                    if (items.isNotEmpty()) {
                        clear()
                        items.forEach { r -> add(r as String) }
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    fun refresh(tags: List<String>) {
        allTags.clear()
        allTags.addAll(tags)
    }

}