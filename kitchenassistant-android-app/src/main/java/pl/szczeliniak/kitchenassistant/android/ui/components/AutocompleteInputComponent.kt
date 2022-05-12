package pl.szczeliniak.kitchenassistant.android.ui.components

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.Filterable
import android.widget.FrameLayout
import android.widget.ListAdapter
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentAutocompleteInputBinding

class AutocompleteInputComponent(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    val binding: ComponentAutocompleteInputBinding

    var onItemClicked: OnItemClicked? = null
        set(value) {
            field = value

            value?.let {
                binding.inputEditText.setOnItemClickListener { _, _, position, _ -> it.onItemClicked(position) }
            } ?: kotlin.run {
                binding.inputEditText.onItemClickListener = null
            }
        }

    var onKeyListener: OnKeyListener? = null
        set(value) {
            field = value
            value?.let {
                binding.inputEditText.setOnKeyListener { _, keyCode, keyEvent ->
                    it.onKeyListener(keyCode, keyEvent)
                }
            } ?: kotlin.run {
                binding.inputEditText.setOnKeyListener(null)
            }
        }

    fun <T> setAdapter(adapter: T) where T : ListAdapter, T : Filterable {
        binding.inputEditText.setAdapter(adapter)
    }

    init {
        binding = ComponentAutocompleteInputBinding.inflate(LayoutInflater.from(context), this, true)

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.AutocompleteInputComponent, 0, 0).apply {
            binding.inputEditText.hint = getString(R.styleable.AutocompleteInputComponent_hint) ?: ""

            val maxLength = getInt(R.styleable.AutocompleteInputComponent_maxLength, 0)
            if (maxLength > 0) {
                binding.inputEditText.filters = arrayOf(InputFilter.LengthFilter(maxLength))
            }

            if (getBoolean(R.styleable.AutocompleteInputComponent_counterEnabled, false)) {
                binding.inputLayout.isCounterEnabled = true
                binding.inputLayout.counterMaxLength = maxLength
            } else {
                binding.inputLayout.isCounterEnabled = false
            }

        }
    }

    var text: String
        get() {
            return binding.inputEditText.text.toString()
        }
        set(value) {
            binding.inputEditText.setText(value)
        }

    fun interface OnItemClicked {
        fun onItemClicked(position: Int)
    }

    fun interface OnKeyListener {
        fun onKeyListener(keyCode: Int, event: KeyEvent): Boolean
    }

}