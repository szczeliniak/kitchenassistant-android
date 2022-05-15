package pl.szczeliniak.kitchenassistant.android.ui.components.layouts

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import pl.szczeliniak.kitchenassistant.android.R

class KaConstraintLayout(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {

    init {
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.KaConstraintLayout, 0, 0).apply {
            if (getBoolean(R.styleable.KaConstraintLayout_withPadding, false)) {
                setPadding(resources.getDimension(R.dimen.padding_margin_medium).toInt())
            }
        }
    }

}