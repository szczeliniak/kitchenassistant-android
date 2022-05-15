package pl.szczeliniak.kitchenassistant.android.ui.components.forms

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.widget.doOnTextChanged
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentInputBinding

class KaInput(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    val binding: ComponentInputBinding

    var onTextChangedValidator: OnTextChangedValidator? = null
        set(value) {
            field = value
            binding.inputLayout.isErrorEnabled = value != null
            binding.inputEditText.doOnTextChanged { _, _, _, _ ->
                field?.validate()?.let {
                    binding.inputLayout.error = context.getString(it)
                } ?: kotlin.run {
                    binding.inputLayout.error = null
                }
            }
        }

    init {
        binding = ComponentInputBinding.inflate(LayoutInflater.from(context), this, true)

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.KaInput, 0, 0).apply {
            binding.inputEditText.hint = getString(R.styleable.KaInput_hint) ?: ""
            binding.inputEditText.inputType = getInputType(getInt(R.styleable.KaInput_inputType, 0))

            val maxLength = getInt(R.styleable.KaInput_maxLength, 0)
            if (maxLength > 0) {
                binding.inputEditText.filters = arrayOf(InputFilter.LengthFilter(maxLength))
            }

            if (getBoolean(R.styleable.KaInput_counterEnabled, false)) {
                binding.inputLayout.isCounterEnabled = true
                binding.inputLayout.counterMaxLength = maxLength
            } else {
                binding.inputLayout.isCounterEnabled = false
            }

            binding.inputEditText.minLines = getInt(R.styleable.KaInput_minLines, 1)

        }
    }

    private fun getInputType(int: Int): Int {
        var inputType: Int = InputType.TYPE_TEXT_VARIATION_NORMAL
        when (int) {
            0 -> inputType = InputType.TYPE_TEXT_VARIATION_NORMAL
            1 -> inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            2 -> inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            3 -> inputType = InputType.TYPE_TEXT_VARIATION_URI
            4 -> inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            5 -> inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        return inputType + 1
    }

    var text: String
        get() {
            return binding.inputEditText.text.toString()
        }
        set(value) {
            binding.inputEditText.setText(value)
        }

    val textOrNull: String?
        get() {
            val text = text
            if (text.isEmpty()) {
                return null
            }
            return text
        }

    fun interface OnTextChangedValidator {
        fun validate(): Int?
    }

}