package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditstep

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
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddEditStepBinding
import pl.szczeliniak.kitchenassistant.android.events.RecipeChanged
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddStepRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateStepRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipeResponse
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddEditStepDialog : DialogFragment() {

    companion object {
        private const val RECIPE_ID_EXTRA = "RECIPE_ID_EXTRA"
        private const val STEP_EXTRA = "STEP_EXTRA"
        private const val TAG = "AddEditStepDialog"

        fun show(fragmentManager: FragmentManager, recipeId: Int, step: RecipeResponse.Recipe.Step? = null) {
            val bundle = Bundle()
            bundle.putInt(RECIPE_ID_EXTRA, recipeId)
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
            binding.stepDescription.setText(step.description)
            step.sequence?.let {
                binding.stepSequence.setText(it.toString())
            }
            binding.title.text = getString(R.string.title_dialog_edit_step)
        }

        binding.stepDescription.doOnTextChanged { _, _, _, _ ->
            checkButtonState()
        }

        addStepLoadingStateHandler = prepareAddStepLoadingStateHandler()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun checkButtonState() {
        positiveButton.enable(isDescriptionValid())
    }

    private fun isDescriptionValid(): Boolean {
        return description.isNotEmpty()
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
                eventBus.post(RecipeChanged())
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
                ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                    viewModel.updateStep(recipeId, step.id, UpdateStepRequest(description, null, sequence))
                        .observe(this) { addStepLoadingStateHandler.handle(it) }
                }
            } ?: kotlin.run {
                viewModel.addStep(recipeId, AddStepRequest(description, null, sequence))
                    .observe(this) { addStepLoadingStateHandler.handle(it) }
            }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private val description: String
        get() {
            return binding.stepDescription.text.toString()
        }

    private val sequence: Int?
        get() {
            val asString = binding.stepSequence.text.toString()
            if (asString.isEmpty()) {
                return null
            }
            return asString.toInt()
        }

    private val recipeId: Int
        get() {
            return requireArguments().getInt(RECIPE_ID_EXTRA)
        }

    private val step: RecipeResponse.Recipe.Step?
        get() {
            return requireArguments().getParcelable(STEP_EXTRA, RecipeResponse.Recipe.Step::class.java)
        }

}