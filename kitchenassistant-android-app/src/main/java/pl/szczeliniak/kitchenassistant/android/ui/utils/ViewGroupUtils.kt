package pl.szczeliniak.kitchenassistant.android.ui.utils

import android.content.Context
import android.view.ViewGroup
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.ui.components.KaProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.components.views.KaEmptyMessageView

class ViewGroupUtils {

    companion object {
        fun ViewGroup.showProgressSpinner(context: Context) {
            val component = KaProgressSpinner(context, null)
            addView(component)
        }

        fun ViewGroup.hideProgressSpinner() {
            this.findViewById<KaProgressSpinner>(R.id.progress_spinner)?.let { this.removeView(it) }
        }

        fun ViewGroup.showEmptyIcon(context: Context) {
            val component = KaEmptyMessageView(context, null)
            addView(component)
        }

        fun ViewGroup.hideEmptyIcon() {
            this.findViewById<KaEmptyMessageView>(R.id.empty_message_view)?.let { this.removeView(it) }
        }
    }

}

