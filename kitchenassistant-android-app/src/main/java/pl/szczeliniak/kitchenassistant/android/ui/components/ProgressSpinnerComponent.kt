package pl.szczeliniak.kitchenassistant.android.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentProgressSpinnerBinding

class ProgressSpinnerComponent(context: Context, attributeSet: AttributeSet?) : FrameLayout(context, attributeSet) {

    val binding: ComponentProgressSpinnerBinding

    init {
        binding = ComponentProgressSpinnerBinding.inflate(LayoutInflater.from(context), this, true)
        id = R.id.progress_spinner
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

}