package pl.szczeliniak.kitchenassistant.android.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.setMargins
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ComponentFloatingActionButtonBinding

class FloatingActionButtonComponent(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    val binding: ComponentFloatingActionButtonBinding

    var onClick: OnClick? = null
        set(value) {
            field = value
            value?.let { onClick ->
                binding.fab.setOnClickListener { onClick.onClick() }
            } ?: kotlin.run {
                binding.fab.setOnClickListener(null)
            }
        }

    init {
        binding = ComponentFloatingActionButtonBinding.inflate(LayoutInflater.from(context), this, true)

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionButtonComponent, 0, 0).apply {
            binding.fab.setImageDrawable(getDrawable(R.styleable.FloatingActionButtonComponent_icon))
            binding.fab.contentDescription = getString(R.styleable.FloatingActionButtonComponent_description) ?: ""
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (layoutParams is MarginLayoutParams) {
            (layoutParams as MarginLayoutParams).setMargins(
                context.resources.getDimension(R.dimen.padding_margin_medium).toInt()
            )
        }
    }

    fun interface OnClick {
        fun onClick()
    }

}