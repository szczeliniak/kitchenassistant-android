package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditingredient

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddEditIngredientBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateIngredientRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Ingredient
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddEditIngredientDialog : DialogFragment() {

    companion object {
        private const val RECEIPT_ID_EXTRA = "RECEIPT_ID_EXTRA"
        private const val INGREDIENT_EXTRA = "INGREDIENT_EXTRA"

        private const val TAG = "AddEditIngredientDialog"

        fun show(fragmentManager: FragmentManager, receiptId: Int, ingredient: Ingredient? = null) {
            val bundle = Bundle()
            bundle.putInt(RECEIPT_ID_EXTRA, receiptId)
            ingredient?.let { bundle.putParcelable(INGREDIENT_EXTRA, it) }
            val dialog = AddEditIngredientDialog()
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogAddEditIngredientBinding
    private lateinit var saveIngredientLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var positiveButton: Button

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddEditIngredientDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddEditIngredientBinding.inflate(layoutInflater)
        ingredient?.let {
            binding.ingredientName.setText(it.name)
            binding.ingredientQuantity.setText(it.quantity)
            binding.title.text = getString(R.string.title_dialog_edit_ingredient)
        }

        saveIngredientLoadingStateHandler = prepareAddIngredientLoadingStateHandler()
        binding.ingredientName.doOnTextChanged { _, _, _, _ ->
            if (!isNameValid()) {
                binding.ingredientNameLayout.error = getString(R.string.message_ingredient_name_is_empty)
            } else {
                binding.ingredientNameLayout.error = null
            }
            checkButtonState()
        }

        binding.ingredientQuantity.doOnTextChanged { _, _, _, _ ->
            if (!isQuantityValid()) {
                binding.ingredientQuantityLayout.error = getString(R.string.message_ingredient_quantity_is_empty)
            } else {
                binding.ingredientQuantityLayout.error = null
            }
            checkButtonState()
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun isNameValid(): Boolean {
        return name.isNotEmpty()
    }

    private fun isQuantityValid(): Boolean {
        return quantity.isNotEmpty()
    }

    private fun checkButtonState() {
        positiveButton.enable(isNameValid() && isQuantityValid())
    }

    private fun prepareAddIngredientLoadingStateHandler(): LoadingStateHandler<Int> {
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
            ingredient?.let { ingredient ->
                ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                    viewModel.updateIngredient(receiptId, ingredient.id, UpdateIngredientRequest(name, quantity))
                        .observe(this) { saveIngredientLoadingStateHandler.handle(it) }
                }
            } ?: kotlin.run {
                viewModel.addIngredient(receiptId, AddIngredientRequest(name, quantity))
                    .observe(this) { saveIngredientLoadingStateHandler.handle(it) }
            }

        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private val name: String
        get() {
            return binding.ingredientName.text.toString()
        }

    private val quantity: String
        get() {
            return binding.ingredientQuantity.text.toString()
        }

    private val receiptId: Int
        get() {
            return requireArguments().getInt(RECEIPT_ID_EXTRA)
        }

    private val ingredient: Ingredient?
        get() {
            return requireArguments().getParcelable(INGREDIENT_EXTRA)
        }

}