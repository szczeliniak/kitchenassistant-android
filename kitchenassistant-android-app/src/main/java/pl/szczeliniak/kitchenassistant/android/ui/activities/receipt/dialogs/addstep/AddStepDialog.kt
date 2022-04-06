package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.dialogs.addstep

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddStepBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddStepRequest
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.toast
import javax.inject.Inject

@AndroidEntryPoint
class AddStepDialog private constructor() : DialogFragment() {

    companion object {
        private const val RECEIPT_ID_EXTRA = "RECEIPT_ID_EXTRA"

        const val TAG = "AddStepDialog"

        fun newInstance(receiptId: Int): AddStepDialog {
            val bundle = Bundle()
            bundle.putInt(RECEIPT_ID_EXTRA, receiptId)
            val dialog = AddStepDialog()
            dialog.arguments = bundle
            return dialog
        }
    }

    private lateinit var binding: DialogAddStepBinding

    private lateinit var addStepLoadingStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddStepDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddStepBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }

        addStepLoadingStateHandler = prepareAddStepLoadingStateHandler()

        return builder.create()
    }

    private fun prepareAddStepLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadReceiptEvent())
                dismiss()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (!validate()) {
                return@setOnClickListener
            }
            viewModel.addStep(receiptId, AddStepRequest(name, description, sequence))
                .observe(this) { addStepLoadingStateHandler.handle(it) }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private fun validate(): Boolean {
        if (name.isEmpty()) {
            requireActivity().toast(R.string.message_step_name_is_empty)
            return false
        }
        return true
    }

    private val name: String
        get() {
            return binding.stepName.text.toString()
        }

    private val description: String
        get() {
            return binding.stepDescription.text.toString()
        }

    private val sequence: Int?
        get() {
            val asString = binding.stepSequence.text.toString()
            if (asString.isEmpty()) {
                return null
            }
            return asString.toInt()
        }

    private val receiptId: Int
        get() {
            return requireArguments().getInt(RECEIPT_ID_EXTRA)
        }

}