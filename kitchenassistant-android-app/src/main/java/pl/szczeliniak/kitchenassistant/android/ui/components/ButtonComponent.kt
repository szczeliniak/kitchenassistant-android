package pl.szczeliniak.kitchenassistant.android.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentButtonBinding
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable

class ButtonComponent(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    val binding: ComponentButtonBinding

    var onClick: OnClick? = null
        set(value) {
            field = value
            value?.let { onClick ->
                binding.button.setOnClickListener { onClick.onClick() }
            } ?: kotlin.run {
                binding.button.setOnClickListener(null)
            }
        }

    var text: String? = null
        set(value) {
            field = value
            binding.button.text = value
        }

    init {
        binding = ComponentButtonBinding.inflate(LayoutInflater.from(context), this, true)

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.ButtonComponent, 0, 0).apply {
            binding.button.text = getString(R.styleable.ButtonComponent_text) ?: ""

            when (getInt(R.styleable.ButtonComponent_style, 0)) {
                0 -> applyPrimary()
                1 -> applySecondary()
            }

        }
    }

    private fun applySecondary() {
        binding.button.setBackgroundColor(context.getColor(R.color.secondary_dark))

    }

    private fun applyPrimary() {
        binding.button.setBackgroundColor(context.getColor(R.color.primary_dark))
    }

    fun enable(enable: Boolean) {
        binding.button.enable(enable)
    }

    fun interface OnClick {
        fun onClick()
    }

}