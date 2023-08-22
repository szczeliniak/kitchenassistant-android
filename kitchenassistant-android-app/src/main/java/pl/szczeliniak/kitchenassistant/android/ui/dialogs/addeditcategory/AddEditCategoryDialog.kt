package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditcategory

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
import pl.szczeliniak.kitchenassistant.android.events.CategoriesChangedEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddCategoryRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateCategoryRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.CategoriesResponse
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class AddEditCategoryDialog : DialogFragment() {

    companion object {
        private const val CATEGORY_EXTRA = "CATEGORY_EXTRA"
        private const val TAG = "AddEditCategoryDialog"

        fun show(fragmentManager: FragmentManager, category: CategoriesResponse.Category? = null) {
            val bundle = Bundle()
            category?.let { bundle.putParcelable(CATEGORY_EXTRA, it) }
            val dialog = AddEditCategoryDialog()
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogAddEditCategoryBinding
    private lateinit var positiveButton: Button
    private lateinit var addEditCategoryLoadingStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var localStorageService: LocalStorageService

    private val viewModel: AddEditCategoryDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddEditCategoryBinding.inflate(layoutInflater)
        category?.let { category ->
            binding.categoryName.setText(category.name)
            binding.categorySequence.setText(category.sequence?.toString())
            binding.title.text = getString(R.string.title_dialog_edit_category)
        }

        binding.categoryName.doOnTextChanged { _, _, _, _ ->
            if (!isNameValid()) {
                binding.categoryNameLayout.error = getString(R.string.message_category_name_is_empty)
            } else {
                binding.categoryNameLayout.error = null
            }
            checkButtonState()
        }

        addEditCategoryLoadingStateHandler = prepareAddEditCategoryLoadingStateHandler()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun checkButtonState() {
        positiveButton.enable(isNameValid())
    }

    private fun isNameValid(): Boolean {
        return name.isNotEmpty()
    }

    private fun prepareAddEditCategoryLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(CategoriesChangedEvent())
                dismiss()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog
        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        category?.let {
            positiveButton.setText(R.string.title_button_edit)
        }
        checkButtonState()
        positiveButton.setOnClickListener {
            category?.let { c ->
                ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                    viewModel.updateCategory(c.id, UpdateCategoryRequest(name, sequence))
                        .observe(this) { addEditCategoryLoadingStateHandler.handle(it) }
                }
            } ?: kotlin.run {
                viewModel.addCategory(AddCategoryRequest(name, sequence))
                    .observe(this) { addEditCategoryLoadingStateHandler.handle(it) }
            }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private val name: String
        get() {
            return binding.categoryName.text.toString()
        }

    private val sequence: Int?
        get() {
            val asString = binding.categorySequence.text.toString()
            return if (asString.isEmpty()) null else asString.toInt()
        }

    private val category: CategoriesResponse.Category?
        get() {
            return requireArguments().getParcelable(CATEGORY_EXTRA, CategoriesResponse.Category::class.java)
        }

}