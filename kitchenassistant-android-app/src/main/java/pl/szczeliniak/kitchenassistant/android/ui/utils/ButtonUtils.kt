package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.widget.Button

fun Button.enable(enable: Boolean) {
    if (enable) {
        isEnabled = true
        alpha = 1F
    } else {
        isEnabled = false
        alpha = 0.6F
    }
}