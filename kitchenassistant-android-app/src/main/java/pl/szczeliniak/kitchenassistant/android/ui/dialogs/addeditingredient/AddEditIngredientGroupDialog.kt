package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditingredient

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddEditIngredientGroupBinding
import pl.szczeliniak.kitchenassistant.android.events.RecipeChanged
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientGroupRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.EditIngredientGroupRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.IngredientGroupResponse
import pl.szczeliniak.kitchenassistant.android.ui.listitems.AddEditIngredientItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.GroupAdapterUtils.Companion.getItems
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddEditIngredientGroupDialog : DialogFragment() {

    companion object {
        private const val RECIPE_ID_EXTRA = "RECIPE_ID_EXTRA"
        private const val INGREDIENT_GROUP_ID_EXTRA = "INGREDIENT_GROUP_ID_EXTRA"
        private const val TAG = "AddEditIngredientGroupDialog"

        fun show(
            fragmentManager: FragmentManager,
            recipeId: Int,
            ingredientGroupId: Int?
        ) {
            val bundle = Bundle()
            bundle.putInt(RECIPE_ID_EXTRA, recipeId)
            ingredientGroupId?.let { bundle.putInt(INGREDIENT_GROUP_ID_EXTRA, it) }
            val dialog = AddEditIngredientGroupDialog()
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogAddEditIngredientGroupBinding
    private lateinit var saveIngredientGroupLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var loadIngredientGroupLoadingStateHandler: LoadingStateHandler<IngredientGroupResponse.IngredientGroup>
    private lateinit var positiveButton: Button
    private var ingredientsAdapter = GroupieAdapter()

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var factory: AddEditIngredientGroupDialogViewModel.Factory

    private val viewModel: AddEditIngredientGroupDialogViewModel by viewModels {
        AddEditIngredientGroupDialogViewModel.provideFactory(factory, recipeId, ingredientGroupId)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddEditIngredientGroupBinding.inflate(layoutInflater)

        saveIngredientGroupLoadingStateHandler = saveIngredientGroupLoadingStateHandler()
        loadIngredientGroupLoadingStateHandler = loadIngredientGroupLoadingStateHandler()

        viewModel.ingredientGroup.observe(requireActivity()) {
            loadIngredientGroupLoadingStateHandler.handle(it)
        }

        binding.ingredientGroupName.doOnTextChanged { _, _, _, _ ->
            if (!isIngredientGroupNameValid()) {
                binding.ingredientGroupNameLayout.error = getString(R.string.message_ingredient_group_name_is_empty)
            } else {
                binding.ingredientGroupNameLayout.error = null
            }
            checkButtonState()
        }

        binding.recyclerView.adapter = ingredientsAdapter
        binding.addIngredient.setOnClickListener {
            ingredientsAdapter.add(AddEditIngredientItem(requireContext(), null, "", null, {
                ingredientsAdapter.remove(it)
            }, {
                checkButtonState()
            }))
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun isIngredientGroupNameValid(): Boolean {
        return ingredientGroupName.isNotEmpty()
    }

    private fun checkButtonState() {
        positiveButton.enable(
            isIngredientGroupNameValid() && ingredientsAdapter.itemCount > 0 && ingredientsAdapter.getItems<AddEditIngredientItem>()
                .all { it.isValid() })
    }

    private fun saveIngredientGroupLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(RecipeChanged())
                dismiss()
            }
        })
    }

    private fun loadIngredientGroupLoadingStateHandler(): LoadingStateHandler<IngredientGroupResponse.IngredientGroup> {
        return LoadingStateHandler(
            requireActivity(),
            object : LoadingStateHandler.OnStateChanged<IngredientGroupResponse.IngredientGroup> {
                override fun onInProgress() {
                    binding.root.showProgressSpinner(requireActivity())
                }

                override fun onFinish() {
                    binding.root.hideProgressSpinner()
                }

                override fun onSuccess(data: IngredientGroupResponse.IngredientGroup) {
                    binding.title.text = getString(R.string.title_dialog_edit_ingredient_group)
                    binding.ingredientGroupName.setText(data.name)
                    data.ingredients.forEach {
                        ingredientsAdapter.add(
                            AddEditIngredientItem(
                                requireContext(),
                                it.id,
                                it.name,
                                it.quantity, { item ->
                                    ingredientsAdapter.remove(item)
                                }, {
                                    checkButtonState()
                                })
                        )
                    }
                    checkButtonState()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog
        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        ingredientGroupId?.let {
            positiveButton.setText(R.string.title_button_edit)
        }

        checkButtonState()
        positiveButton.setOnClickListener {
            checkButtonState()
            if (positiveButton.isEnabled) {
                ingredientGroupId?.let { groupId ->
                    viewModel.editIngredientGroup(
                        recipeId, groupId, EditIngredientGroupRequest(ingredientGroupName, prepareIngredientToEdit())
                    ).observe(this) { saveIngredientGroupLoadingStateHandler.handle(it) }
                } ?: kotlin.run {
                    viewModel.addIngredientGroup(
                        recipeId,
                        AddIngredientGroupRequest(ingredientGroupName, prepareIngredientToAdd())
                    ).observe(this) { saveIngredientGroupLoadingStateHandler.handle(it) }
                }
            }
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private fun prepareIngredientToAdd(): List<AddIngredientGroupRequest.AddIngredientDto> {
        return ingredientsAdapter.getItems<AddEditIngredientItem>().map {
            AddIngredientGroupRequest.AddIngredientDto(it.ingredientName, it.ingredientQuantity)
        }
    }

    private fun prepareIngredientToEdit(): List<EditIngredientGroupRequest.EditIngredientDto> {
        return ingredientsAdapter.getItems<AddEditIngredientItem>().map {
            EditIngredientGroupRequest.EditIngredientDto(it.ingredientId, it.ingredientName, it.ingredientQuantity)
        }
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

}