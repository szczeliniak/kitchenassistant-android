package pl.szczeliniak.kitchenassistant.android.ui.activities.categories.dialogs.addeditcategory

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
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddEditCategoryBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadCategoriesEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddCategoryRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateCategoryRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddEditCategoryDialog : DialogFragment() {

    companion object {
        private const val CATEGORY_EXTRA = "CATEGORY_EXTRA"
        private const val TAG = "AddEditCategoryDialog"

        fun show(fragmentManager: FragmentManager, category: Category? = null) {
            val bundle = Bundle()
            category?.let { bundle.putParcelable(CATEGORY_EXTRA, it) }
            val dialog = AddEditCategoryDialog()
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogAddEditCategoryBinding
    private lateinit var positiveButton: Button
    private lateinit var addStepLoadingStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var localStorageService: LocalStorageService

    private val viewModel: AddEditCategoryDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddEditCategoryBinding.inflate(layoutInflater)
        category?.let { step ->
            binding.categoryName.setText(step.name)
            binding.title.text = getString(R.string.title_dialog_edit_category)
        }

        binding.categoryName.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                binding.categoryNameLayout.error = getString(R.string.message_category_name_is_empty)
            } else {
                binding.categoryNameLayout.error = null
            }
            positiveButton.enable(binding.categoryNameLayout.error == null)
        }

        addStepLoadingStateHandler = prepareSaveStepLoadingStateHandler()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun prepareSaveStepLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadCategoriesEvent())
                dismiss()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog
        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            category?.let { c ->
                viewModel.updateCategory(c.id, UpdateCategoryRequest(name))
                    .observe(this) { addStepLoadingStateHandler.handle(it) }
            } ?: kotlin.run {
                viewModel.addCategory(AddCategoryRequest(name, localStorageService.getId()))
                    .observe(this) { addStepLoadingStateHandler.handle(it) }
            }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private val name: String
        get() {
            return binding.categoryName.text.toString()
        }

    private val category: Category?
        get() {
            return requireArguments().getParcelable(CATEGORY_EXTRA)
        }

}