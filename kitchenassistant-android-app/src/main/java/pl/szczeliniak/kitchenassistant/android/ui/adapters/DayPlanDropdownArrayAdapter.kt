package pl.szczeliniak.kitchenassistant.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatTextView
import pl.szczeliniak.kitchenassistant.android.databinding.DropdownDayPlanBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlan
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils

class DayPlanDropdownArrayAdapter(context: Context) : ArrayAdapter<DayPlan?>(context, 0, ArrayList()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val binding: DropdownDayPlanBinding?

        if (convertView == null) {
            binding = DropdownDayPlanBinding.inflate(LayoutInflater.from(context))
            viewHolder = ViewHolder(binding.dayPlanDate)
            binding.root.tag = viewHolder
        } else {
            binding = DropdownDayPlanBinding.bind(convertView)
            viewHolder = binding.root.tag as ViewHolder
        }

        getItem(position)?.let {
            viewHolder.dateTextView.text = LocalDateUtils.stringify(it.date)
        } ?: kotlin.run {
            viewHolder.dateTextView.text = "---"
        }

        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    data class ViewHolder(
        val dateTextView: AppCompatTextView
    )

}