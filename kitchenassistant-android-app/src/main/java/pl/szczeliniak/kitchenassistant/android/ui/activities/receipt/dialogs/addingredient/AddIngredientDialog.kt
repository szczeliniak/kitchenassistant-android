package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.dialogs.addingredient

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddIngredientBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientRequest
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.toast
import javax.inject.Inject

@AndroidEntryPoint
class AddIngredientDialog private constructor() : DialogFragment() {

    companion object {
        private const val RECEIPT_ID_EXTRA = "RECEIPT_ID_EXTRA"

        const val TAG = "AddIngredientDialog"

        fun newInstance(receiptId: Int): AddIngredientDialog {
            val bundle = Bundle()
            bundle.putInt(RECEIPT_ID_EXTRA, receiptId)
            val dialog = AddIngredientDialog()
            dialog.arguments = bundle
            return dialog
        }
    }

    private lateinit var binding: DialogAddIngredientBinding

    private lateinit var addIngredientLoadingStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddIngredientDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddIngredientBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }

        addIngredientLoadingStateHandler = prepareAddIngredientLoadingStateHandler()

        return builder.create()
    }

    private fun prepareAddIngredientLoadingStateHandler(): LoadingStateHandler<Int> {
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
            viewModel.addIngredient(receiptId, AddIngredientRequest(name, quantity))
                .observe(this) { addIngredientLoadingStateHandler.handle(it) }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private fun validate(): Boolean {
        if (name.isEmpty()) {
            requireActivity().toast(R.string.message_ingredient_name_is_empty)
            return false
        } else if (quantity.isEmpty()) {
            requireActivity().toast(R.string.message_ingredient_quantity_is_empty)
            return false
        }
        return true
    }

    private val name: String
        get() {
            return binding.dialogAddIngredientEdittextName.text.toString()
        }

    private val quantity: String
        get() {
            return binding.dialogAddIngredientEdittextQuantity.text.toString()
        }

    private val receiptId: Int
        get() {
            return requireArguments().getInt(RECEIPT_ID_EXTRA)
        }

}