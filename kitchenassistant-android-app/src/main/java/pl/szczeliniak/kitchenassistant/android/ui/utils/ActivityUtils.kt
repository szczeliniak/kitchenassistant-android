package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration

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