package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditstep

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddEditStepBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddStepRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateStepRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Step
import pl.szczeliniak.kitchenassistant.android.ui.components.InputComponent
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddEditStepDialog : DialogFragment() {

    companion object {
        private const val RECEIPT_ID_EXTRA = "RECEIPT_ID_EXTRA"
        private const val STEP_EXTRA = "STEP_EXTRA"
        private const val TAG = "AddEditStepDialog"

        fun show(fragmentManager: FragmentManager, receiptId: Int, step: Step? = null) {
            val bundle = Bundle()
            bundle.putInt(RECEIPT_ID_EXTRA, receiptId)
            step?.let { bundle.putParcelable(STEP_EXTRA, it) }
            val dialog = AddEditStepDialog()
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogAddEditStepBinding
    private lateinit var addStepLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var positiveButton: Button

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddEditStepDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddEditStepBinding.inflate(layoutInflater)
        step?.let { step ->
            binding.stepName.text = step.name
            step.description?.let {
                binding.stepDescription.text = it
            }
            step.sequence?.let {
                binding.stepSequence.text = it.toString()
            }
            binding.title.text = getString(R.string.title_dialog_edit_step)
        }

        addStepLoadingStateHandler = prepareAddStepLoadingStateHandler()

        binding.stepName.onTextChangedValidator = InputComponent.OnTextChangedValidator {
            var id: Int? = null
            if (!isNameValid()) {
                id = R.string.message_step_name_is_empty
            }
            checkButtonState()
            return@OnTextChangedValidator id
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun checkButtonState() {
        positiveButton.enable(isNameValid())
    }

    private fun isNameValid(): Boolean {
        return name.isNotEmpty()
    }

    private fun prepareAddStepLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
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
        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        checkButtonState()
        positiveButton.setOnClickListener {
            step?.let { step ->
                viewModel.updateStep(receiptId, step.id, UpdateStepRequest(name, description, sequence))
                    .observe(this) { addStepLoadingStateHandler.handle(it) }
            } ?: kotlin.run {
                viewModel.addStep(receiptId, AddStepRequest(name, description, sequence))
                    .observe(this) { addStepLoadingStateHandler.handle(it) }
            }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private val name: String
        get() {
            return binding.stepName.text
        }

    private val description: String
        get() {
            return binding.stepDescription.text
        }

    private val sequence: Int?
        get() {
            return binding.stepSequence.textOrNull?.toInt()
        }

    private val receiptId: Int
        get() {
            return requireArguments().getInt(RECEIPT_ID_EXTRA)
        }

    private val step: Step?
        get() {
            return requireArguments().getParcelable(STEP_EXTRA)
        }

}