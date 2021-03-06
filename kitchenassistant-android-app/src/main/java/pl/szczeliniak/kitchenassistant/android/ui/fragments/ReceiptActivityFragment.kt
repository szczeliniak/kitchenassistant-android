package pl.szczeliniak.kitchenassistant.android.ui.fragments

import androidx.fragment.app.Fragment
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ReceiptDetails
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.ReceiptActivity

abstract class ReceiptActivityFragment : Fragment() {

    val receipt: ReceiptDetails?
        get() {
            val activity = requireActivity()
            if (activity is ReceiptActivity) {
                return activity.receipt
            }
            return null
        }

    override fun onStart() {
        registerForReceiptsChanges()
        super.onStart()
    }

    private fun registerForReceiptsChanges() {
        val activity = requireActivity()
        if (activity is ReceiptActivity) {
            return activity.addChangesObserver(this)
        }
    }

    override fun onStop() {
        unregisterForReceiptsChanges()
        super.onStop()
    }

    private fun unregisterForReceiptsChanges() {
        val activity = requireActivity()
        if (activity is ReceiptActivity) {
            return activity.removeChangesObserver(this)
        }
    }

    abstract fun onReceiptChanged()

}