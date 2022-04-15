package pl.szczeliniak.kitchenassistant.android.ui.dialogs.receiptsfilter

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogReceiptsFilterBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.ui.adapters.CategoryDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner

@AndroidEntryPoint
class ReceiptsFilterDialog : DialogFragment() {

    companion object {
        private const val CALLBACK_EXTRA: String = "CALLBACK_EXTRA"
        private const val FILTER_EXTRA: String = "FILTER_EXTRA"
        private const val TAG = "ReceiptsFilterDialog"

        fun show(fragmentManager: FragmentManager, receiptsFilter: ReceiptsFilter?, onFilterChanged: OnFilterChanged) {
            val dialog = ReceiptsFilterDialog()
            val bundle = Bundle()
            bundle.putParcelable(CALLBACK_EXTRA, onFilterChanged)
            bundle.putParcelable(FILTER_EXTRA, receiptsFilter)
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogReceiptsFilterBinding
    private lateinit var loadCategoriesLoadingStateHandler: LoadingStateHandler<List<Category>>
    private lateinit var categoryDropdownArrayAdapter: CategoryDropdownArrayAdapter
    private var selectedCategory: Category? = null

    private val viewModel: ReceiptsFilterDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogReceiptsFilterBinding.inflate(layoutInflater)
        categoryDropdownArrayAdapter = CategoryDropdownArrayAdapter(requireContext())
        binding.categoryName.setAdapter(categoryDropdownArrayAdapter)
        binding.categoryName.setOnItemClickListener { _, _, position, _ ->
            viewModel.setCategory(categoryDropdownArrayAdapter.getItem(position))
        }
        receiptsFilter?.let {
            it.receiptName?.let { name -> binding.receiptName.setText(name) }
        }

        loadCategoriesLoadingStateHandler = prepareLoadCategoriesLoadingStateHandler()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_filter) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        viewModel.categories.observe(this) { loadCategoriesLoadingStateHandler.handle(it) }
        viewModel.selectedCategory.observe(this) {
            selectedCategory = it
            binding.categoryName.setText(it?.name ?: "")
        }
        binding.categoryName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.categoryName.text.toString().isEmpty() || selectedCategory == null) {
                    viewModel.setCategory(null)
                } else {
                    viewModel.setCategory(selectedCategory)
                }
            }
        }

        val dialog = dialog as AlertDialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            binding.categoryName.clearFocus()
            val filter = ReceiptsFilter(selectedCategory?.id, name)
            onFilterChanged.onFilterChanged(filter)
            dismiss()
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private fun prepareLoadCategoriesLoadingStateHandler(): LoadingStateHandler<List<Category>> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<List<Category>> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: List<Category>) {
                categoryDropdownArrayAdapter.refresh(data)
                receiptsFilter?.categoryId?.let { categoryId ->
                    data.firstOrNull { category -> category.id == categoryId }?.let {
                        viewModel.setCategory(it)
                    }
                }
            }
        })
    }

    @Parcelize
    class OnFilterChanged(private val action: (receiptsFilter: ReceiptsFilter) -> Unit) : Parcelable {
        fun onFilterChanged(receiptsFilter: ReceiptsFilter) = action(receiptsFilter)
    }

    @Parcelize
    data class ReceiptsFilter(val categoryId: Int?, val receiptName: String?) : Parcelable {}

    private val onFilterChanged: OnFilterChanged
        get() {
            return requireArguments().getParcelable(CALLBACK_EXTRA)!!
        }

    private val name: String?
        get() {
            return binding.receiptName.text.toString().ifEmpty { null }
        }

    private val receiptsFilter: ReceiptsFilter?
        get() {
            return requireArguments().getParcelable(FILTER_EXTRA)
        }

}