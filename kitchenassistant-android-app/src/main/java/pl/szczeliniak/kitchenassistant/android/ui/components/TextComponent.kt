package pl.szczeliniak.kitchenassistant.android.ui.components

import android.content.Context
import android.text.InputType
import android.text.util.Linkify
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentTextBinding
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.setTextOrDefault

class TextComponent(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    val binding: ComponentTextBinding

    private val hasIcon: Boolean

    init {
        binding = ComponentTextBinding.inflate(LayoutInflater.from(context), this, true)

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.TextComponent, 0, 0).apply {
            binding.text.text = getString(R.styleable.TextComponent_text)
            if (getBoolean(R.styleable.TextComponent_url, false)) {
                binding.text.linksClickable = true
                Linkify.addLinks(binding.text, Linkify.WEB_URLS)
            }

            when (getInt(R.styleable.TextComponent_alignment, 0)) {
                0 -> binding.root.gravity = Gravity.START
                1 -> binding.root.gravity = Gravity.END
                2 -> binding.root.gravity = Gravity.CENTER
            }

            if (getBoolean(R.styleable.TextComponent_multiline, false)) {
                binding.text.isSingleLine = false
                binding.text.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }

            when (getInt(R.styleable.TextComponent_type, 0)) {
                0 -> {
                    applyFieldLabel()
                }
                1 -> {
                    applyFieldValue()
                }
            }


            val drawable = getDrawable(R.styleable.TextComponent_iconAfterText)
            hasIcon = drawable != null
            if (hasIcon) {
                binding.icon.setImageDrawable(drawable)
                binding.icon.setColorFilter(
                    getColor(
                        R.styleable.TextComponent_iconAfterTextColor,
                        resources.getColor(R.color.black, null)
                    )
                )
                binding.icon.contentDescription = getString(R.styleable.TextComponent_iconAfterTextDescription) ?: ""
            } else {
                binding.icon.visibility = View.GONE
            }

        }

    }

    private fun applyFieldLabel() {
        binding.text.isAllCaps = true
        binding.text.setTextColor(resources.getColor(R.color.secondary_dark, null))
        binding.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_tiny))
        binding.text.typeface = resources.getFont(R.font.roboto_bold)
    }

    private fun applyFieldValue() {
        binding.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_medium))
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

    fun setTextOrDefault(text: String?) {
        binding.text.setTextOrDefault(text)
    }

    fun showIcon(show: Boolean) {
        if (hasIcon) {
            binding.icon.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

}