package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addreceipttodayplan

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAssignReceiptToDayPlanBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.ReceiptsResponse
import pl.szczeliniak.kitchenassistant.android.ui.adapters.ReceiptDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.DebounceExecutor

@AndroidEntryPoint
class AssignReceiptToDayPlanDialog : DialogFragment() {

    companion object {
        private const val ON_RECEIPT_CHOSEN_CALLBACK_EXTRA: String = "ON_RECEIPT_CHOSEN_CALLBACK_EXTRA"
        private const val TAG = "AddReceiptToDayPlanDialog"

        fun show(fragmentManager: FragmentManager, onReceiptChosen: OnReceiptChosen) {
            val dialog = AssignReceiptToDayPlanDialog()
            val bundle = Bundle()
            bundle.putParcelable(ON_RECEIPT_CHOSEN_CALLBACK_EXTRA, onReceiptChosen)
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogAssignReceiptToDayPlanBinding
    private lateinit var loadReceiptsLoadingStateHandler: LoadingStateHandler<ReceiptsResponse>
    private lateinit var receiptsDropdownArrayAdapter: ReceiptDropdownArrayAdapter
    private lateinit var button: Button

    private val debounceExecutor = DebounceExecutor(500)
    private val viewModel: AssignReceiptToDayPlanDialogViewModel by viewModels()

    private var selectedReceiptId: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAssignReceiptToDayPlanBinding.inflate(layoutInflater)
        receiptsDropdownArrayAdapter = ReceiptDropdownArrayAdapter(requireContext())
        binding.receiptName.setAdapter(receiptsDropdownArrayAdapter)
        binding.receiptName.setOnItemClickListener { _, _, position, _ ->
            val receipt = receiptsDropdownArrayAdapter.getItem(position)!!
            binding.receiptName.setText(receipt.name)
            selectedReceiptId = receipt.id
            refreshButtonState()
        }
        binding.receiptName.doOnTextChanged { text, _, _, _ ->
            debounceExecutor.execute { viewModel.reloadReceipts(text.toString()) }
        }

        loadReceiptsLoadingStateHandler = prepareLoadReceiptsLoadingStateHandler()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun refreshButtonState() {
        button.enable(selectedReceiptId != null)
    }

    override fun onResume() {
        super.onResume()
        viewModel.receipts.observe(this) { loadReceiptsLoadingStateHandler.handle(it) }
        val dialog = dialog as AlertDialog
        button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        button.setOnClickListener {
            selectedReceiptId?.let { onReceiptChosen.onReceiptChosen(it) }
            dismiss()
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
        refreshButtonState()
    }

    private fun prepareLoadReceiptsLoadingStateHandler(): LoadingStateHandler<ReceiptsResponse> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<ReceiptsResponse> {
            override fun onSuccess(data: ReceiptsResponse) {
                receiptsDropdownArrayAdapter.refresh(data.receipts)
            }
        })
    }

    @Parcelize
    class OnReceiptChosen(private val action: (receiptId: Int) -> Unit) : Parcelable {
        fun onReceiptChosen(receiptId: Int) = action(receiptId)
    }

    private val onReceiptChosen: OnReceiptChosen
        get() {
            return requireArguments().getParcelable(ON_RECEIPT_CHOSEN_CALLBACK_EXTRA)!!
        }
}