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
import pl.szczeliniak.kitchenassistant.android.events.ReloadRecipeEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientGroupRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateIngredientRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Ingredient
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.IngredientGroup
import pl.szczeliniak.kitchenassistant.android.ui.adapters.IngredientGroupDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddEditIngredientDialog : DialogFragment() {

    companion object {
        private const val RECIPE_ID_EXTRA = "RECIPE_ID_EXTRA"
        private const val INGREDIENT_GROUP_ID_EXTRA = "INGREDIENT_GROUP_ID_EXTRA"
        private const val INGREDIENT_GROUP_NAME_EXTRA = "INGREDIENT_GROUP_NAME_EXTRA"
        private const val INGREDIENT_EXTRA = "INGREDIENT_EXTRA"
        private const val INGREDIENT_GROUPS_EXTRA = "INGREDIENT_GROUPS_EXTRA"

        private const val TAG = "AddEditIngredientDialog"

        fun show(
            fragmentManager: FragmentManager,
            recipeId: Int,
            ingredientGroupId: Int?,
            ingredientGroupName: String?,
            ingredientGroups: ArrayList<IngredientGroup>,
            ingredient: Ingredient? = null,
        ) {
            val bundle = Bundle()
            bundle.putInt(RECIPE_ID_EXTRA, recipeId)
            ingredientGroupId?.let { bundle.putInt(INGREDIENT_GROUP_ID_EXTRA, it) }
            ingredientGroupName?.let { bundle.putString(INGREDIENT_GROUP_NAME_EXTRA, it) }
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

        if (ingredientGroupDropdownArrayAdapter.count <= 1) {
            ingredientGroupId?.let { id ->
                ingredientGroupDropdownArrayAdapter.getIngredientGroupById(id)?.let {
                    binding.ingredientGroupName.setText(it.name)
                }
            } ?: kotlin.run {
                ingredientGroupDropdownArrayAdapter.getItem(0)?.let { binding.ingredientGroupName.setText(it.name) }
            }
        }

        ingredient?.let {
            binding.ingredientGroupName.setText(groupName)
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
                eventBus.post(ReloadRecipeEvent())
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

        ingredient?.let {
            positiveButton.setText(R.string.title_button_edit)
        }

        checkButtonState()
        positiveButton.setOnClickListener {
            ingredientGroupDropdownArrayAdapter.getIngredientGroupByName(ingredientGroupName)
                ?.let { saveIngredient(it.id) } ?: kotlin.run {
                viewModel.addIngredientGroup(recipeId, AddIngredientGroupRequest(ingredientGroupName))
                    .observe(this) {
                        saveIngredientGroupLoadingStateHandler.handle(it)
                    }
            }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private fun saveIngredient(groupId: Int) {
        ingredient?.let { ingredient ->
            viewModel.updateIngredient(
                recipeId,
                ingredientGroupId ?: throw IllegalArgumentException("Ingredient group id not found"),
                ingredient.id,
                UpdateIngredientRequest(name, quantity, groupId)
            ).observe(this) { saveIngredientLoadingStateHandler.handle(it) }
        } ?: kotlin.run {
            viewModel.addIngredient(recipeId, groupId, AddIngredientRequest(name, quantity))
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

    private val recipeId: Int
        get() {
            return requireArguments().getInt(RECIPE_ID_EXTRA)
        }

    private val ingredientGroupId: Int?
        get() {
            val id = requireArguments().getInt(INGREDIENT_GROUP_ID_EXTRA, -1)
            if (id < 0) {
                return null
            }
            return id
        }

    private val groupName: String?
        get() {
            return requireArguments().getString(INGREDIENT_GROUP_NAME_EXTRA, null)
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