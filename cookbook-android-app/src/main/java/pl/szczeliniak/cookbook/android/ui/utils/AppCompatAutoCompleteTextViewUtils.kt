package pl.szczeliniak.cookbook.android.ui.utils

import androidx.appcompat.widget.AppCompatAutoCompleteTextView

class AppCompatAutoCompleteTextViewUtils {

    companion object {
        fun AppCompatAutoCompleteTextView.getTextOrNull(): String? {
            return text.toString().ifEmpty { null }
        }
    }

}

