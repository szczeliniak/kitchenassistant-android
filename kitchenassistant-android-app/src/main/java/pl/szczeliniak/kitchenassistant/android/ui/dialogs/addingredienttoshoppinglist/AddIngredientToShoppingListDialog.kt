package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addingredienttoshoppinglist

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
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddIngredientToShoppingListBinding
import pl.szczeliniak.kitchenassistant.android.events.ShoppingListSavedEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListItemRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListsResponse
import pl.szczeliniak.kitchenassistant.android.ui.adapters.ShoppingListDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddIngredientToShoppingListDialog : DialogFragment() {

    companion object {
        private const val INGREDIENT_NAME_EXTRA = "INGREDIENT_NAME_EXTRA"
        private const val INGREDIENT_QUANTITY_EXTRA = "INGREDIENT_QUANTITY_EXTRA"
        private const val RECIPE_ID_EXTRA = "RECIPE_ID_EXTRA"

        private const val TAG = "AddIngredientToShoppingListDialog"

        fun show(fragmentManager: FragmentManager, ingredientName: String, ingredientQuantity: String?, recipeId: Int) {
            val bundle = Bundle()
            bundle.putString(INGREDIENT_NAME_EXTRA, ingredientName)
            ingredientQuantity?.let {
                bundle.putString(INGREDIENT_QUANTITY_EXTRA, ingredientQuantity)
            }
            bundle.putInt(RECIPE_ID_EXTRA, recipeId)
            val dialog = AddIngredientToShoppingListDialog()
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private val viewModel: AddIngredientToShoppingListDialogViewModel by viewModels()

    private lateinit var binding: DialogAddIngredientToShoppingListBinding
    private lateinit var addIngredientToShoppingListStateHandler: LoadingStateHandler<Int>
    private lateinit var loadShoppingListsStateHandler: LoadingStateHandler<ShoppingListsResponse>
    private lateinit var shoppingListsDropdownAdapter: ShoppingListDropdownArrayAdapter
    private lateinit var positiveButton: Button

    @Inject
    lateinit var eventBus: EventBus

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddIngredientToShoppingListBinding.inflate(layoutInflater)

        addIngredientToShoppingListStateHandler = prepareAddIngredientToShoppingListStateHandler()
        loadShoppingListsStateHandler = prepareLoadShoppingListsStateHandler()

        viewModel.shoppingLists.observe(this) { loadShoppingListsStateHandler.handle(it) }

        shoppingListsDropdownAdapter = ShoppingListDropdownArrayAdapter(requireContext())
        binding.shoppingListName.adapter = shoppingListsDropdownAdapter

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
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ShoppingListSavedEvent())
                dismiss()
            }
        })
    }

    private fun prepareLoadShoppingListsStateHandler(): LoadingStateHandler<ShoppingListsResponse> {
        return LoadingStateHandler(
            requireActivity(),
            object : LoadingStateHandler.OnStateChanged<ShoppingListsResponse> {
                override fun onInProgress() {
                    binding.root.showProgressSpinner(requireActivity())
                }

                override fun onFinish() {
                    binding.root.hideProgressSpinner()
                }

                override fun onSuccess(data: ShoppingListsResponse) {
                    shoppingListsDropdownAdapter.addAll(data.shoppingLists)
                }
            })
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog
        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            viewModel.addItem(
                shoppingListsDropdownAdapter.getItem(binding.shoppingListName.selectedItemPosition)?.id!!,
                AddShoppingListItemRequest(ingredientName, ingredientQuantity, sequence, recipeId)
            ).observe(this) { addIngredientToShoppingListStateHandler.handle(it) }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private val sequence: Int?
        get() {
            val asString = binding.shoppingListItemSequence.text.toString()
            return if (asString.isEmpty()) null else asString.toInt()
        }

    private val ingredientName: String
        get() {
            return requireArguments().getString(INGREDIENT_NAME_EXTRA)
                ?: throw java.lang.IllegalStateException("Ingredient name cannot be null")
        }

    private val ingredientQuantity: String?
        get() {
            return requireArguments().getString(INGREDIENT_QUANTITY_EXTRA)
        }

    private val recipeId: Int
        get() {
            return requireArguments().getInt(RECIPE_ID_EXTRA)
        }

}