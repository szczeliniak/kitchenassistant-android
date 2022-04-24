package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.view.LayoutInflater
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import pl.szczeliniak.kitchenassistant.android.R

class ChipGroupUtils {

    companion object {
        fun ChipGroup.add(layoutInflater: LayoutInflater, text: String, deletable: Boolean) {
            val chip = layoutInflater.inflate(R.layout.chip, this, false) as Chip
            chip.text = text
            addView(chip)
            chip.isCloseIconVisible = deletable
            if (deletable) {
                chip.setOnCloseIconClickListener { (it.parent as ChipGroup).removeView(it) }
            }
        }

        fun ChipGroup.getTextInChips(): List<String> {
            val names = ArrayList<String>()
            children.iterator().forEach { names.add((it as Chip).text.toString()) }
            return names
        }
    }

}