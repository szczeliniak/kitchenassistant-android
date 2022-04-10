package pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.dialogs.addeditshoppinglistitem

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddEditShoppingListItemBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListItemRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateShoppingListItemRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingListItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ContextUtils.Companion.toast
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddEditShoppingListItemDialog private constructor() : DialogFragment() {

    companion object {
        private const val SHOPPING_LIST_ID_EXTRA = "SHOPPING_LIST_ID_EXTRA"
        private const val SHOPPING_LIST_ITEM_EXTRA = "SHOPPING_LIST_ITEM_EXTRA"

        const val TAG = "AddShoppingListItemDialog"

        fun newInstance(
            shoppingListId: Int,
            shoppingListItem: ShoppingListItem? = null
        ): AddEditShoppingListItemDialog {
            val bundle = Bundle()
            bundle.putInt(SHOPPING_LIST_ID_EXTRA, shoppingListId)
            shoppingListItem?.let { bundle.putParcelable(SHOPPING_LIST_ITEM_EXTRA, it) }
            val dialog = AddEditShoppingListItemDialog()
            dialog.arguments = bundle
            return dialog
        }
    }

    private lateinit var binding: DialogAddEditShoppingListItemBinding

    private lateinit var addShoppingListItemLoadingStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddEditShoppingListItemDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddEditShoppingListItemBinding.inflate(layoutInflater)

        shoppingListItem?.let {
            binding.shoppingListName.setText(it.name)
            binding.shoppingListItemQuantity.setText(it.quantity)
            binding.shoppingListItemSequence.setText(it.sequence)
            binding.title.text = getString(R.string.title_dialog_edit_shopping_list_item)
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }

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
            shoppingListItem?.let { item ->
                viewModel.updateShoppingListItem(
                    shoppingListId,
                    item.id,
                    UpdateShoppingListItemRequest(name, quantity, sequence)
                ).observe(this) { addShoppingListItemLoadingStateHandler.handle(it) }
            } ?: kotlin.run {
                viewModel.addShoppingListItem(shoppingListId, AddShoppingListItemRequest(name, quantity, sequence))
                    .observe(this) { addShoppingListItemLoadingStateHandler.handle(it) }
            }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private fun validate(): Boolean {
        if (name.isEmpty()) {
            requireActivity().toast(R.string.message_shopping_list_item_name_is_empty)
            return false
        } else if (quantity.isEmpty()) {
            requireActivity().toast(R.string.message_shopping_list_item_quantity_is_empty)
            return false
        }
        return true
    }

    private val name: String
        get() {
            return binding.shoppingListName.text.toString()
        }

    private val quantity: String
        get() {
            return binding.shoppingListItemQuantity.text.toString()
        }

    private val sequence: Int?
        get() {
            val asString = binding.shoppingListItemSequence.text.toString()
            if (asString.isEmpty()) {
                return null
            }
            return asString.toInt()
        }

    private val shoppingListId: Int
        get() {
            return requireArguments().getInt(SHOPPING_LIST_ID_EXTRA)
        }

    private val shoppingListItem: ShoppingListItem?
        get() {
            return requireArguments().getParcelable(SHOPPING_LIST_ITEM_EXTRA)
        }

}