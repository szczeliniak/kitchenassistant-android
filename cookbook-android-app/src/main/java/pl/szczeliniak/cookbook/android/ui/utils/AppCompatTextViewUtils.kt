package pl.szczeliniak.cookbook.android.ui.utils

import android.view.View
import androidx.appcompat.widget.AppCompatTextView

class AppCompatTextViewUtils {
    companion object {
        fun AppCompatTextView.fillOrHide(text: String?, layoutToHide: View) {
            if (text.isNullOrEmpty()) {
                layoutToHide.visibility = View.GONE
            } else {
                layoutToHide.visibility = View.VISIBLE
                this.text = text
            }
        }

        fun AppCompatTextView.setTextOrDefault(text: String?) {
            if (text.isNullOrEmpty()) {
                this.text = "---"
            } else {
                this.text = text
            }
        }

    }
}


