package pl.szczeliniak.kitchenassistant.android.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentProgressSpinnerBinding

class ProgressSpinnerComponent(context: Context, attributeSet: AttributeSet?) : FrameLayout(context, attributeSet) {

    val binding: ComponentProgressSpinnerBinding

    init {
        binding = ComponentProgressSpinnerBinding.inflate(LayoutInflater.from(context), this, true)
    }

}