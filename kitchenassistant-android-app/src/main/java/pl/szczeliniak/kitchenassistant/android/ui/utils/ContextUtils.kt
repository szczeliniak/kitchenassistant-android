package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.content.Context
import android.widget.Toast

fun Context.toast(resId: Int) {
    showToast(getString(resId))
}

fun Context.toast(message: String) {
    showToast(message)
}

private fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}