package pl.szczeliniak.kitchenassistant.android.ui.components.buttons

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentIconButtonBinding

class KaIconButton(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    val binding: ComponentIconButtonBinding

    var onClick: OnClick? = null
        set(value) {
            field = value
            value?.let { onClick ->
                binding.button.setOnClickListener { onClick.onClick(it) }
            } ?: kotlin.run {
                binding.button.setOnClickListener(null)
            }
        }

    init {
        binding = ComponentIconButtonBinding.inflate(LayoutInflater.from(context), this, true)

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.KaIconButton, 0, 0).apply {
            binding.button.setImageDrawable(getDrawable(R.styleable.KaIconButton_icon))
        }

    }

    fun interface OnClick {
        fun onClick(view: View)
    }

}