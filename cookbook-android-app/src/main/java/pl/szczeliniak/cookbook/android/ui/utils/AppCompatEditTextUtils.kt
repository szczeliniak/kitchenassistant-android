package pl.szczeliniak.cookbook.android.ui.utils

import androidx.appcompat.widget.AppCompatEditText

class AppCompatEditTextUtils {

    companion object {
        fun AppCompatEditText.getTextOrNull(): String? {
            return text.toString().ifEmpty { null }
        }
    }

}

