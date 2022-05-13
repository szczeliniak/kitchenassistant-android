package pl.szczeliniak.kitchenassistant.android.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentEmptyMessageViewBinding

class EmptyMessageViewComponent(context: Context, attributeSet: AttributeSet?) : FrameLayout(context, attributeSet) {

    val binding: ComponentEmptyMessageViewBinding

    init {
        binding = ComponentEmptyMessageViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

}