package pl.szczeliniak.cookbook.android.ui.utils

import android.content.Context
import android.widget.Toast

class ContextUtils {

    companion object {
        fun Context.toast(resId: Int) {
            Toast.makeText(this, getString(resId), Toast.LENGTH_LONG).show()
        }
    }

}