package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.MessageViewLayoutBinding
import pl.szczeliniak.kitchenassistant.android.databinding.ProgressSpinnerBinding

fun ViewGroup.showMessage(activity: Activity, stringId: Int) {
    val binding = MessageViewLayoutBinding.inflate(LayoutInflater.from(activity), this, true)
    binding.messageView.text = activity.getString(stringId)
}

fun ViewGroup.hideMessage() {
    this.findViewById<ConstraintLayout>(R.id.message_view_layout)?.let { this.removeView(it) }
}

fun ViewGroup.showProgressSpinner(activity: Activity?) {
    ProgressSpinnerBinding.inflate(LayoutInflater.from(activity), this, true)
    activity?.lockOrientation()
}

fun ViewGroup.hideProgressSpinner(activity: Activity?) {
    this.findViewById<ConstraintLayout>(R.id.progress_spinner_layout)?.let { this.removeView(it) }
    activity?.unlockOrientation()
}