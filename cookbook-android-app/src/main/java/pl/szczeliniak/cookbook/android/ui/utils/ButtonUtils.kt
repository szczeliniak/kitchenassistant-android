package pl.szczeliniak.cookbook.android.ui.utils

import android.widget.Button

class ButtonUtils {

    companion object {

        fun Button.enable(enable: Boolean) {
            if (enable) {
                isEnabled = true
                alpha = 1F
            } else {
                isEnabled = false
                alpha = 0.6F
            }
        }

    }

}