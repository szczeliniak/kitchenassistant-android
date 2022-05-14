package pl.szczeliniak.kitchenassistant.android.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentTextBinding
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide

class TextComponent(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    val binding: ComponentTextBinding

    init {
        binding = ComponentTextBinding.inflate(LayoutInflater.from(context), this, true)

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.TextComponent, 0, 0).apply {
            binding.text.text = getString(R.styleable.TextComponent_text)
        }

    }

    var text: String? = null
        get() {
            return binding.text.text.toString()
        }
        set(value) {
            field = value
            binding.text.text = value
        }

    fun fillOrHide(text: String?, layoutToHide: View) {
        binding.text.fillOrHide(text, layoutToHide)
    }

}