package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ProgressSpinnerBinding
import pl.szczeliniak.kitchenassistant.android.ui.utils.ActivityUtils.Companion.lockOrientation
import pl.szczeliniak.kitchenassistant.android.ui.utils.ActivityUtils.Companion.unlockOrientation

fun ViewGroup.showProgressSpinner(activity: Activity?) {
    ProgressSpinnerBinding.inflate(LayoutInflater.from(activity), this, true)
    activity?.lockOrientation()
}

fun ViewGroup.hideProgressSpinner(activity: Activity?) {
    this.findViewById<ConstraintLayout>(R.id.progress_spinner_layout)?.let { this.removeView(it) }
    activity?.unlockOrientation()
}