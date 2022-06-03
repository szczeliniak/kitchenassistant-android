package pl.szczeliniak.kitchenassistant.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.appcompat.widget.AppCompatTextView
import pl.szczeliniak.kitchenassistant.android.databinding.DropdownAuthorBinding

class AuthorDropdownArrayAdapter(context: Context) : ArrayAdapter<String>(context, 0, ArrayList()) {

    private val allAuthors = ArrayList<String>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val binding: DropdownAuthorBinding?

        if (convertView == null) {
            binding = DropdownAuthorBinding.inflate(LayoutInflater.from(context))
            viewHolder = ViewHolder(binding.author)
            binding.root.tag = viewHolder
        } else {
            binding = DropdownAuthorBinding.bind(convertView)
            viewHolder = binding.root.tag as ViewHolder
        }

        getItem(position)?.let {
            viewHolder.nameTextView.text = String.format(it)
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

            private fun filterCategories(constraint: CharSequence): ArrayList<String> {
                return ArrayList(allAuthors.filter { c ->
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
        allAuthors.clear()
        allAuthors.addAll(tags)
    }

}