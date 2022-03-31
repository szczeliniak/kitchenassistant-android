package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.view.View
import androidx.appcompat.widget.AppCompatTextView

fun AppCompatTextView.fillOrHide(text: String?, layoutToHide: View) {
    if (text.isNullOrEmpty()) {
        layoutToHide.visibility = View.GONE
    } else {
        layoutToHide.visibility = View.VISIBLE
        this.text = text
    }
}