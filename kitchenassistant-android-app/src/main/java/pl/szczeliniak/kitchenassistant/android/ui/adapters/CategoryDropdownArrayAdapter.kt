package pl.szczeliniak.kitchenassistant.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import pl.szczeliniak.kitchenassistant.android.databinding.DropdownCategoryBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.ui.components.forms.KaText

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

        getItem(position)?.name?.let {
            viewHolder.nameTextView.text = String.format(it)
        } ?: kotlin.run {
            viewHolder.nameTextView.text = String.format("---")
        }

        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    fun getPositionById(id: Int): Int? {
        for (i in 0..count) {
            val item = getItem(i)
            if (item != null && item.id == id) {
                return i
            }
        }
        return null
    }

    data class ViewHolder(
        val nameTextView: KaText
    )

}