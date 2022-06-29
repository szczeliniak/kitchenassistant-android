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
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientGroupRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateIngredientRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Ingredient
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.IngredientGroup
import pl.szczeliniak.kitchenassistant.android.ui.adapters.IngredientGroupDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddEditIngredientDialog : DialogFragment() {

    companion object {
        private const val RECEIPT_ID_EXTRA = "RECEIPT_ID_EXTRA"
        private const val INGREDIENT_GROUP_ID_EXTRA = "INGREDIENT_GROUP_ID_EXTRA"
        private const val INGREDIENT_EXTRA = "INGREDIENT_EXTRA"
        private const val INGREDIENT_GROUPS_EXTRA = "INGREDIENT_GROUPS_EXTRA"

        private const val TAG = "AddEditIngredientDialog"

        fun show(
            fragmentManager: FragmentManager,
            receiptId: Int,
            ingredientGroupId: Int?,
            ingredientGroups: ArrayList<IngredientGroup>,
            ingredient: Ingredient? = null,
        ) {
            val bundle = Bundle()
            bundle.putInt(RECEIPT_ID_EXTRA, receiptId)
            ingredientGroupId?.let { bundle.putInt(INGREDIENT_GROUP_ID_EXTRA, it) }
            bundle.putParcelableArrayList(INGREDIENT_GROUPS_EXTRA, ingredientGroups)
            ingredient?.let { bundle.putParcelable(INGREDIENT_EXTRA, it) }
            val dialog = AddEditIngredientDialog()
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogAddEditIngredientBinding
    private lateinit var saveIngredientLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var saveIngredientGroupLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var positiveButton: Button
    private lateinit var ingredientGroupDropdownArrayAdapter: IngredientGroupDropdownArrayAdapter

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: AddEditIngredientDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddEditIngredientBinding.inflate(layoutInflater)

        ingredientGroupDropdownArrayAdapter = IngredientGroupDropdownArrayAdapter(requireContext())
        binding.ingredientGroupName.setAdapter(ingredientGroupDropdownArrayAdapter)
        binding.ingredientGroupName.setOnItemClickListener { _, _, position, _ ->
            binding.ingredientGroupName.setText(ingredientGroupDropdownArrayAdapter.getItem(position)!!.name)
        }

        ingredientGroupDropdownArrayAdapter.refresh(ingredientGroups)

        ingredientGroupId?.let {
            ingredientGroupDropdownArrayAdapter.getIngredientGroupById(it)?.let {
                binding.ingredientGroupName.setText(it.name)
            }
        } ?: kotlin.run {
            ingredientGroupDropdownArrayAdapter.getItem(0)?.let { binding.ingredientGroupName.setText(it.name) }
        }

        ingredient?.let {
            binding.ingredientName.setText(it.name)
            it.quantity?.let { quantity -> binding.ingredientQuantity.setText(quantity) }
            binding.title.text = getString(R.string.title_dialog_edit_ingredient)
        }

        saveIngredientLoadingStateHandler = prepareAddIngredientLoadingStateHandler()
        saveIngredientGroupLoadingStateHandler = prepareAddIngredientGroupLoadingStateHandler()
        binding.ingredientName.doOnTextChanged { _, _, _, _ ->
            if (!isNameValid()) {
                binding.ingredientNameLayout.error = getString(R.string.message_ingredient_name_is_empty)
            } else {
                binding.ingredientNameLayout.error = null
            }
            checkButtonState()
        }

        binding.ingredientGroupName.doOnTextChanged { _, _, _, _ ->
            if (!isIngredientGroupValid()) {
                binding.ingredientGroupNameLayout.error = getString(R.string.message_ingredient_group_name_is_empty)
            } else {
                binding.ingredientGroupNameLayout.error = null
            }
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

    private fun isIngredientGroupValid(): Boolean {
        return ingredientGroupName.isNotEmpty()
    }

    private fun checkButtonState() {
        positiveButton.enable(isNameValid() && isIngredientGroupValid())
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

    private fun prepareAddIngredientGroupLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                saveIngredient(data)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog
        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        checkButtonState()
        positiveButton.setOnClickListener {
            ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                ingredientGroupDropdownArrayAdapter.getIngredientGroupByName(ingredientGroupName)
                    ?.let { saveIngredient(it.id) } ?: kotlin.run {
                    viewModel.addIngredientGroup(receiptId, AddIngredientGroupRequest(ingredientGroupName))
                        .observe(this) {
                            saveIngredientGroupLoadingStateHandler.handle(it)
                        }
                }
            }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private fun saveIngredient(groupId: Int) {
        ingredient?.let { ingredient ->
            viewModel.updateIngredient(
                receiptId,
                ingredientGroupId ?: throw IllegalArgumentException("Ingredient group id not found"),
                ingredient.id,
                UpdateIngredientRequest(name, quantity, groupId)
            ).observe(this) { saveIngredientLoadingStateHandler.handle(it) }
        } ?: kotlin.run {
            viewModel.addIngredient(receiptId, groupId, AddIngredientRequest(name, quantity))
                .observe(this) { saveIngredientLoadingStateHandler.handle(it) }
        }
    }

    private val name: String
        get() {
            return binding.ingredientName.text.toString()
        }

    private val quantity: String?
        get() {
            val asString = binding.ingredientQuantity.text.toString()
            return asString.ifEmpty { null }
        }

    private val ingredientGroupName: String
        get() {
            return binding.ingredientGroupName.text.toString()
        }

    private val receiptId: Int
        get() {
            return requireArguments().getInt(RECEIPT_ID_EXTRA)
        }

    private val ingredientGroupId: Int?
        get() {
            val id = requireArguments().getInt(INGREDIENT_GROUP_ID_EXTRA, -1)
            if (id < 0) {
                return null
            }
            return id
        }

    private val ingredient: Ingredient?
        get() {
            return requireArguments().getParcelable(INGREDIENT_EXTRA)
        }

    private val ingredientGroups: ArrayList<IngredientGroup>
        get() {
            return requireArguments().getParcelableArrayList(INGREDIENT_GROUPS_EXTRA) ?: ArrayList()
        }

}