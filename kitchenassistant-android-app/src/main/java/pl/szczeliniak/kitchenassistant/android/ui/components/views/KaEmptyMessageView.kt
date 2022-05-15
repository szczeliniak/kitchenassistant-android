package pl.szczeliniak.kitchenassistant.android.ui.components.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentEmptyMessageViewBinding

class KaEmptyMessageView(context: Context, attributeSet: AttributeSet?) : FrameLayout(context, attributeSet) {

    val binding: ComponentEmptyMessageViewBinding

    init {
        binding = ComponentEmptyMessageViewBinding.inflate(LayoutInflater.from(context), this, true)
        id = R.id.empty_message_view
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

}