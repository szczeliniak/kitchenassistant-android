package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.EmptyMessageViewBinding
import pl.szczeliniak.kitchenassistant.android.databinding.ProgressSpinnerBinding

class ViewGroupUtils {

    companion object {
        fun ViewGroup.showProgressSpinner(activity: Activity?) {
            ProgressSpinnerBinding.inflate(LayoutInflater.from(activity), this, true)
        }

        fun ViewGroup.hideProgressSpinner() {
            this.findViewById<ConstraintLayout>(R.id.progress_spinner_layout)?.let { this.removeView(it) }
        }

        fun ViewGroup.showEmptyIcon(activity: Activity) {
            EmptyMessageViewBinding.inflate(LayoutInflater.from(activity), this, true)
        }

        fun ViewGroup.hideEmptyIcon() {
            this.findViewById<ConstraintLayout>(R.id.message_layout)?.let { this.removeView(it) }
        }
    }

}

