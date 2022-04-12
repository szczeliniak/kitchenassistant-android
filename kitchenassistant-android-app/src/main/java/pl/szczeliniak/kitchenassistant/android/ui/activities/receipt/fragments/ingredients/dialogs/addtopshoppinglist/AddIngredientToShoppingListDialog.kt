package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.ingredients.dialogs.addtopshoppinglist

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddIngredientToShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListsEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListItemRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Ingredient
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.ui.adapters.ShoppingListDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddIngredientToShoppingListDialog : DialogFragment() {

    companion object {
        private const val INGREDIENT_EXTRA = "INGREDIENT_EXTRA"

        const val TAG = "AddIngredientToShoppingListDialog"

        fun newInstance(ingredient: Ingredient): AddIngredientToShoppingListDialog {
            val bundle = Bundle()
            bundle.putParcelable(INGREDIENT_EXTRA, ingredient)
            val dialog = AddIngredientToShoppingListDialog()
            dialog.arguments = bundle
            return dialog
        }
    }

    private val viewModel: AddIngredientToShoppingListDialogViewModel by viewModels()

    private lateinit var binding: DialogAddIngredientToShoppingListBinding
    private lateinit var addIngredientToShoppingListStateHandler: LoadingStateHandler<Int>
    private lateinit var loadShoppingListsStateHandler: LoadingStateHandler<List<ShoppingList>>
    private lateinit var shoppingListsDropdownAdapter: ShoppingListDropdownArrayAdapter
    private lateinit var positiveButton: Button

    @Inject
    lateinit var eventBus: EventBus
    private var selectedShoppingList: ShoppingList? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddIngredientToShoppingListBinding.inflate(layoutInflater)

        addIngredientToShoppingListStateHandler = prepareAddIngredientToShoppingListStateHandler()
        loadShoppingListsStateHandler = prepareLoadShoppingListsStateHandler()

        viewModel.selectedShoppingList.observe(this) {
            selectedShoppingList = it
            binding.shoppingListName.setText(it?.name ?: "")
            if (it == null) {
                binding.shoppingListItemNameLayout.error = getString(R.string.message_shopping_list_item_name_is_empty)
            } else {
                binding.shoppingListItemNameLayout.error = null
            }
            positiveButton.enable(binding.shoppingListItemNameLayout.error == null)
        }

        viewModel.shoppingLists.observe(this) { loadShoppingListsStateHandler.handle(it) }

        shoppingListsDropdownAdapter = ShoppingListDropdownArrayAdapter(requireContext())
        binding.shoppingListName.setAdapter(shoppingListsDropdownAdapter)
        binding.shoppingListName.setOnItemClickListener { _, _, position, _ ->
            viewModel.setShoppingList(shoppingListsDropdownAdapter.getItem(position))
        }
        binding.shoppingListName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.shoppingListName.text.toString().isEmpty() || selectedShoppingList == null) {
                    viewModel.setShoppingList(null)
                } else {
                    viewModel.setShoppingList(selectedShoppingList)
                }
            }
        }
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun prepareAddIngredientToShoppingListStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadShoppingListsEvent())
                dismiss()
            }
        })
    }

    private fun prepareLoadShoppingListsStateHandler(): LoadingStateHandler<List<ShoppingList>> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<List<ShoppingList>> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: List<ShoppingList>) {
                shoppingListsDropdownAdapter.clear()
                shoppingListsDropdownAdapter.addAll(data)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog
        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            viewModel.addItem(
                selectedShoppingList?.id!!,
                AddShoppingListItemRequest(ingredient.name, ingredient.quantity, sequence)
            ).observe(this) { addIngredientToShoppingListStateHandler.handle(it) }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private val sequence: Int?
        get() {
            val asString = binding.shoppingListItemSequence.text.toString()
            return if (asString.isEmpty()) null else asString.toInt()
        }

    private val ingredient: Ingredient
        get() {
            return requireArguments().getParcelable(INGREDIENT_EXTRA)
                ?: throw java.lang.IllegalStateException("Ingredient cannot be null")
        }

}