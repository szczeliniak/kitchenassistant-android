package pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.dialogs.addshoppinglistitem

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddShoppingListItemBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListItemRequest
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.toast
import javax.inject.Inject

@AndroidEntryPoint
class AddShoppingListItemDialog private constructor() : DialogFragment() {

    companion object {
        private const val SHOPPING_LIST_ITEM_ID_EXTRA = "SHOPPING_LIST_ITEM_ID_EXTRA"

        const val TAG = "AddShoppingListItemDialog"

        fun newInstance(shoppingListId: Int): AddShoppingListItemDialog {
            val bundle = Bundle()
            bundle.putInt(SHOPPING_LIST_ITEM_ID_EXTRA, shoppingListId)
            val dialog = AddShoppingListItemDialog()
            dialog.arguments = bundle
            return dialog
        }
    }

    private lateinit var binding: DialogAddShoppingListItemBinding

    private lateinit var addShoppingListItemLoadingStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddShoppingListItemDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddShoppingListItemBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.button_label_add) { _, _ -> }
        builder.setNegativeButton(R.string.button_label_cancel) { _, _ -> }

        addShoppingListItemLoadingStateHandler = prepareAddShoppingListItemLoadingStateHandler()

        return builder.create()
    }

    private fun prepareAddShoppingListItemLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadShoppingListEvent())
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
            viewModel.addShoppingListItem(shoppingListId, AddShoppingListItemRequest(name, quantity, sequence))
                .observe(this) { addShoppingListItemLoadingStateHandler.handle(it) }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private fun validate(): Boolean {
        if (name.isEmpty()) {
            requireActivity().toast(R.string.toast_shopping_list_item_name_is_empty)
            return false
        } else if (quantity.isEmpty()) {
            requireActivity().toast(R.string.toast_shopping_list_item_quantity_is_empty)
            return false
        }
        return true
    }

    private val name: String
        get() {
            return binding.dialogAddShoppingListItemEdittextName.text.toString()
        }

    private val quantity: String
        get() {
            return binding.dialogAddShoppingListItemEdittextQuantity.text.toString()
        }

    private val sequence: Int?
        get() {
            val asString = binding.dialogAddShoppingListItemEdittextSequence.text.toString()
            if (asString.isEmpty()) {
                return null
            }
            return asString.toInt()
        }

    private val shoppingListId: Int
        get() {
            return requireArguments().getInt(SHOPPING_LIST_ITEM_ID_EXTRA)
        }

}