package pl.szczeliniak.kitchenassistant.android.ui.dialogs.chooserecipetodayplan

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAssignRecipeToDayPlanBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipesResponse
import pl.szczeliniak.kitchenassistant.android.ui.adapters.RecipeDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.DebounceExecutor

@AndroidEntryPoint
class ChooseRecipeToDayPlanDialog : DialogFragment() {

    companion object {
        private const val ON_RECIPE_CHOSEN_CALLBACK_EXTRA: String = "ON_RECIPE_CHOSEN_CALLBACK_EXTRA"
        private const val TAG = "ChooseRecipeToDayPlanDialog"

        fun show(fragmentManager: FragmentManager, onRecipeChosen: OnRecipeChosen) {
            val dialog = ChooseRecipeToDayPlanDialog()
            val bundle = Bundle()
            bundle.putParcelable(ON_RECIPE_CHOSEN_CALLBACK_EXTRA, onRecipeChosen)
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogAssignRecipeToDayPlanBinding
    private lateinit var loadRecipesLoadingStateHandler: LoadingStateHandler<RecipesResponse>
    private lateinit var recipesDropdownArrayAdapter: RecipeDropdownArrayAdapter
    private lateinit var button: Button

    private val debounceExecutor = DebounceExecutor(500)
    private val viewModel: ChooseRecipeToDayPlanDialogViewModel by viewModels()

    private var selectedRecipeId: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAssignRecipeToDayPlanBinding.inflate(layoutInflater)
        recipesDropdownArrayAdapter = RecipeDropdownArrayAdapter(requireContext())
        binding.recipeName.setAdapter(recipesDropdownArrayAdapter)
        binding.recipeName.setOnItemClickListener { _, _, position, _ ->
            val recipe = recipesDropdownArrayAdapter.getItem(position)!!
            binding.recipeName.setText(recipe.name)
            selectedRecipeId = recipe.id
            refreshButtonState()
        }
        binding.recipeName.doOnTextChanged { text, _, _, _ ->
            debounceExecutor.execute { viewModel.reloadRecipes(text.toString()) }
        }

        loadRecipesLoadingStateHandler = prepareLoadRecipesLoadingStateHandler()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun refreshButtonState() {
        button.enable(selectedRecipeId != null)
    }

    override fun onResume() {
        super.onResume()
        viewModel.recipes.observe(this) { loadRecipesLoadingStateHandler.handle(it) }
        val dialog = dialog as AlertDialog
        button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        button.setOnClickListener {
            selectedRecipeId?.let { onRecipeChosen.onRecipeChosen(it) }
            dismiss()
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
        refreshButtonState()
    }

    private fun prepareLoadRecipesLoadingStateHandler(): LoadingStateHandler<RecipesResponse> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<RecipesResponse> {
            override fun onSuccess(data: RecipesResponse) {
                recipesDropdownArrayAdapter.refresh(data.recipes)
            }
        })
    }

    @Parcelize
    class OnRecipeChosen(private val action: (recipeId: Int) -> Unit) : Parcelable {
        fun onRecipeChosen(recipeId: Int) = action(recipeId)
    }

    private val onRecipeChosen: OnRecipeChosen
        get() {
            return requireArguments().getParcelable(ON_RECIPE_CHOSEN_CALLBACK_EXTRA)!!
        }
}