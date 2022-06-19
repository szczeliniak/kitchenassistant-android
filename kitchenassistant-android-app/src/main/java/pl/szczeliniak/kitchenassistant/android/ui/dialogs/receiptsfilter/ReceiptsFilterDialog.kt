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
import pl.szczeliniak.kitchenassistant.android.ui.adapters.TagDropdownArrayAdapter

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
    private lateinit var loadTagsLoadingStateHandler: LoadingStateHandler<List<String>>
    private lateinit var tagDropdownArrayAdapter: TagDropdownArrayAdapter

    private val viewModel: ReceiptsFilterDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogReceiptsFilterBinding.inflate(layoutInflater)
        tagDropdownArrayAdapter = TagDropdownArrayAdapter(requireContext())
        binding.receiptTagName.setAdapter(tagDropdownArrayAdapter)
        binding.receiptTagName.setOnItemClickListener { _, _, position, _ ->
            binding.receiptTagName.setText(tagDropdownArrayAdapter.getItem(position)!!)
        }
        filter?.let {
            it.receiptTag?.let { name -> binding.receiptTagName.setText(name) }
        }

        loadTagsLoadingStateHandler = prepareLoadTagsLoadingStateHandler()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_filter) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        viewModel.tags.observe(this) { loadTagsLoadingStateHandler.handle(it) }

        val dialog = dialog as AlertDialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val filter = Filter(receiptTag)
            onFilterChanged.onFilterChanged(filter)
            dismiss()
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private fun prepareLoadTagsLoadingStateHandler(): LoadingStateHandler<List<String>> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<List<String>> {
            override fun onSuccess(data: List<String>) {
                tagDropdownArrayAdapter.refresh(data)
            }
        })
    }

    @Parcelize
    class OnFilterChanged(private val action: (filter: Filter) -> Unit) : Parcelable {
        fun onFilterChanged(filter: Filter) = action(filter)
    }

    @Parcelize
    data class Filter(val receiptTag: String?) : Parcelable

    private val onFilterChanged: OnFilterChanged
        get() {
            return requireArguments().getParcelable(CALLBACK_EXTRA)!!
        }

    private val receiptTag: String?
        get() {
            return binding.receiptTagName.text.toString().ifEmpty { null }
        }

    private val filter: Filter?
        get() {
            return requireArguments().getParcelable(FILTER_EXTRA)
        }

}