package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.EmptyMessageViewBinding

fun ViewGroup.showEmptyIcon(activity: Activity) {
    EmptyMessageViewBinding.inflate(LayoutInflater.from(activity), this, true)
}

fun ViewGroup.hideEmptyIcon() {
    this.findViewById<ConstraintLayout>(R.id.message_view_layout)?.let { this.removeView(it) }
}

fun Activity.lockOrientation() {
    val currentOrientation = resources.configuration.orientation
    requestedOrientation = if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    } else {
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}

fun Activity.unlockOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}