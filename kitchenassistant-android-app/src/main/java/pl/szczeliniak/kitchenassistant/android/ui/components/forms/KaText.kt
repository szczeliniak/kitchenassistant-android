package pl.szczeliniak.kitchenassistant.android.ui.components.forms

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

class KaText(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    val binding: ComponentTextBinding

    private val hasIcon: Boolean

    init {
        binding = ComponentTextBinding.inflate(LayoutInflater.from(context), this, true)

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.KaText, 0, 0).apply {
            binding.text.text = getString(R.styleable.KaText_text)
            if (getBoolean(R.styleable.KaText_url, false)) {
                binding.text.linksClickable = true
                Linkify.addLinks(binding.text, Linkify.WEB_URLS)
            }

            when (getInt(R.styleable.KaText_alignment, 0)) {
                0 -> binding.root.gravity = Gravity.START
                1 -> binding.root.gravity = Gravity.END
                2 -> binding.root.gravity = Gravity.CENTER
            }

            if (getBoolean(R.styleable.KaText_multiline, false)) {
                binding.text.isSingleLine = false
                binding.text.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }

            when (getInt(R.styleable.KaText_type, 0)) {
                0 -> {
                    applyFieldLabelStyle()
                }
                1 -> {
                    applyFieldValueStyle()
                }
                2 -> {
                    applyFieldDescriptionStyle()
                }
                3 -> {
                    applyMessageStyle()
                }
                4 -> {
                    applyDialogTitleStyle()
                }
                5 -> {
                    applyAppNameStyle()
                }
                6 -> {
                    applyAppDescriptionStyle()
                }
            }

            val drawable = getDrawable(R.styleable.KaText_iconAfterText)
            hasIcon = drawable != null
            if (hasIcon) {
                binding.icon.setImageDrawable(drawable)
                binding.icon.setColorFilter(
                    getColor(
                        R.styleable.KaText_iconAfterTextColor,
                        resources.getColor(R.color.black, null)
                    )
                )
                binding.icon.contentDescription = getString(R.styleable.KaText_iconAfterTextDescription) ?: ""
            } else {
                binding.icon.visibility = View.GONE
            }

            binding.text.setTextColor(
                getColor(
                    R.styleable.KaText_textColor,
                    binding.text.currentTextColor
                )
            )

        }

    }

    private fun applyFieldLabelStyle() {
        binding.text.isAllCaps = true
        binding.text.setTextColor(resources.getColor(R.color.secondary_dark, null))
        binding.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_tiny))
        binding.text.typeface = resources.getFont(R.font.roboto_bold)
    }

    private fun applyFieldValueStyle() {
        binding.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_medium))
    }

    private fun applyFieldDescriptionStyle() {
        binding.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_small))
        binding.text.typeface = resources.getFont(R.font.roboto_light)
    }

    private fun applyMessageStyle() {
        binding.text.isAllCaps = true
        binding.text.typeface = resources.getFont(R.font.roboto_light)
        binding.text.letterSpacing = 0.1F
        binding.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_big))
    }

    private fun applyDialogTitleStyle() {
        binding.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_medium))
        binding.text.isAllCaps = true
        binding.text.letterSpacing = 0.1F
        binding.text.setTextColor(resources.getColor(R.color.primary_dark, null))
    }

    private fun applyAppNameStyle() {
        binding.text.setTextColor(resources.getColor(R.color.white, null))
        binding.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_medium))
        binding.text.isAllCaps = true
        binding.text.letterSpacing = 0.1F
        binding.text.typeface = resources.getFont(R.font.roboto_light)
    }

    private fun applyAppDescriptionStyle() {
        binding.text.setTextColor(resources.getColor(R.color.black, null))
        binding.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_small))
        binding.text.letterSpacing = 0.1F
        binding.text.typeface = resources.getFont(R.font.roboto_thin)
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