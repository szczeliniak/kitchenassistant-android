package pl.szczeliniak.kitchenassistant.android.ui.components.layouts

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.setPadding
import pl.szczeliniak.kitchenassistant.android.R

class KaLinearLayout(context: Context, attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {

    init {

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.KaLinearLayout, 0, 0).apply {
            if (getBoolean(R.styleable.KaLinearLayout_withPadding, false)) {
                setPadding(resources.getDimension(R.dimen.padding_margin_medium).toInt())
            }
        }

    }

}