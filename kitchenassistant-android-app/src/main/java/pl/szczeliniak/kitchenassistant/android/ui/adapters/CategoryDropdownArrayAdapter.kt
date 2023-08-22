package pl.szczeliniak.kitchenassistant.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatTextView
import pl.szczeliniak.kitchenassistant.android.databinding.DropdownCategoryBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.CategoriesResponse

class CategoryDropdownArrayAdapter(context: Context) : ArrayAdapter<CategoriesResponse.Category>(context, 0, ArrayList()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val binding: DropdownCategoryBinding?

        if (convertView == null) {
            binding = DropdownCategoryBinding.inflate(LayoutInflater.from(context))
            viewHolder = ViewHolder(binding.category)
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
        if (count > 0) {
            for (i in 0..count) {
                val item = getItem(i)
                if (item != null && item.id == id) {
                    return i
                }
            }
        }
        return null
    }

    data class ViewHolder(
        val nameTextView: AppCompatTextView
    )

}