package pl.szczeliniak.kitchenassistant.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.appcompat.widget.AppCompatTextView
import pl.szczeliniak.kitchenassistant.android.databinding.DropdownReceiptBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide

class ReceiptDropdownArrayAdapter(context: Context) : ArrayAdapter<Receipt>(context, 0, ArrayList()) {

    private val allReceipts = ArrayList<Receipt>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val binding: DropdownReceiptBinding?

        if (convertView == null) {
            binding = DropdownReceiptBinding.inflate(LayoutInflater.from(context))
            viewHolder = ViewHolder(binding.receiptName, binding.receiptCategory, binding.receiptAuthor)
            binding.root.tag = viewHolder
        } else {
            binding = DropdownReceiptBinding.bind(convertView)
            viewHolder = binding.root.tag as ViewHolder
        }

        getItem(position)?.let {
            viewHolder.nameTextView.text = it.name
            viewHolder.categoryTextView.fillOrHide(it.category?.name, viewHolder.categoryTextView)
            viewHolder.authorTextView.fillOrHide(it.author, viewHolder.authorTextView)
        }

        return binding.root
    }

    data class ViewHolder(
        val nameTextView: AppCompatTextView,
        val categoryTextView: AppCompatTextView,
        val authorTextView: AppCompatTextView
    )

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint != null) {
                    val list = filterReceipts(constraint)
                    results.values = list
                    results.count = list.size
                }
                return results
            }

            private fun filterReceipts(constraint: CharSequence): ArrayList<Receipt> {
                return ArrayList(allReceipts.filter { r ->
                    r.name.lowercase().contains(constraint.toString().lowercase())
                })
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.values?.let {
                    val items = it as ArrayList<*>
                    if (items.isNotEmpty()) {
                        clear()
                        items.forEach { r -> add(r as Receipt) }
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    fun refresh(receipts: List<Receipt>) {
        allReceipts.clear()
        allReceipts.addAll(receipts)
    }

}