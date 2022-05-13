package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.ui.components.EmptyMessageViewComponent
import pl.szczeliniak.kitchenassistant.android.ui.components.ProgressSpinnerComponent

class ViewGroupUtils {

    companion object {
        fun ViewGroup.showProgressSpinner(context: Context) {
            val component = ProgressSpinnerComponent(context, null)
            component.id = R.id.progress_spinner
            component.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addView(component)
        }

        fun ViewGroup.hideProgressSpinner() {
            this.findViewById<ProgressSpinnerComponent>(R.id.progress_spinner)?.let { this.removeView(it) }
        }

        fun ViewGroup.showEmptyIcon(context: Context) {
            val component = EmptyMessageViewComponent(context, null)
            component.id = R.id.empty_message_view
            component.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addView(component)
        }

        fun ViewGroup.hideEmptyIcon() {
            this.findViewById<EmptyMessageViewComponent>(R.id.empty_message_view)?.let { this.removeView(it) }
        }
    }

}

