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

        fun show(fragmentManager: FragmentManager, filter: Filter?, onFilterChanged: OnFilterChanged) {
            val dialog = ReceiptsFilterDialog()
            val bundle = Bundle()
            bundle.putParcelable(CALLBACK_EXTRA, onFilterChanged)
            bundle.putParcelable(FILTER_EXTRA, filter)
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogReceiptsFilterBinding
    private lateinit var loadCategoriesLoadingStateHandler: LoadingStateHandler<List<Category>>
    private lateinit var categoryDropdownArrayAdapter: CategoryDropdownArrayAdapter

    private val viewModel: ReceiptsFilterDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogReceiptsFilterBinding.inflate(layoutInflater)
        categoryDropdownArrayAdapter = CategoryDropdownArrayAdapter(requireContext())
        binding.receiptCategory.adapter = categoryDropdownArrayAdapter
        filter?.let {
            it.receiptTag?.let { name -> binding.receiptTagName.text = name }
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

        val dialog = dialog as AlertDialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val filter = Filter(categoryId, receiptTag)
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
                categoryDropdownArrayAdapter.clear()
                categoryDropdownArrayAdapter.add(null)
                categoryDropdownArrayAdapter.addAll(data)

                filter?.categoryId?.let { categoryId ->
                    categoryDropdownArrayAdapter.getPositionById(categoryId)?.let { position ->
                        binding.receiptCategory.setSelection(position)
                    }
                }
            }
        })
    }

    @Parcelize
    class OnFilterChanged(private val action: (filter: Filter) -> Unit) : Parcelable {
        fun onFilterChanged(filter: Filter) = action(filter)
    }

    @Parcelize
    data class Filter(val categoryId: Int?, val receiptTag: String?) : Parcelable

    private val onFilterChanged: OnFilterChanged
        get() {
            return requireArguments().getParcelable(CALLBACK_EXTRA)!!
        }

    private val receiptTag: String?
        get() {
            return binding.receiptTagName.text.ifEmpty { null }
        }

    private val filter: Filter?
        get() {
            return requireArguments().getParcelable(FILTER_EXTRA)
        }

    private val categoryId: Int?
        get() {
            val position = binding.receiptCategory.selectedItemPosition
            return if (position == 0) null else categoryDropdownArrayAdapter.getItem(position)?.id
        }

}