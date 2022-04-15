package pl.szczeliniak.kitchenassistant.android.ui.dialogs.shoppinglistsfilter

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogShoppingListsFilterBinding

@AndroidEntryPoint
class ShoppingListsFilterDialog : DialogFragment() {

    companion object {
        private const val CALLBACK_EXTRA: String = "CALLBACK_EXTRA"
        private const val FILTER_EXTRA: String = "FILTER_EXTRA"
        private const val TAG = "ShoppingListsFilterDialog"

        fun show(
            fragmentManager: FragmentManager,
            filter: Filter?,
            onFilterChanged: OnFilterChanged
        ) {
            val dialog = ShoppingListsFilterDialog()
            val bundle = Bundle()
            bundle.putParcelable(CALLBACK_EXTRA, onFilterChanged)
            bundle.putParcelable(FILTER_EXTRA, filter)
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogShoppingListsFilterBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogShoppingListsFilterBinding.inflate(layoutInflater)
        filter?.let {
            it.name?.let { name -> binding.shoppingListName.setText(name) }
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_filter) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val filter = Filter(name)
            onFilterChanged.onFilterChanged(filter)
            dismiss()
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    @Parcelize
    class OnFilterChanged(private val action: (filter: Filter) -> Unit) : Parcelable {
        fun onFilterChanged(filter: Filter) = action(filter)
    }

    @Parcelize
    data class Filter(val name: String?) : Parcelable {}

    private val onFilterChanged: OnFilterChanged
        get() {
            return requireArguments().getParcelable(CALLBACK_EXTRA)!!
        }

    private val name: String?
        get() {
            return binding.shoppingListName.text.toString().ifEmpty { null }
        }

    private val filter: Filter?
        get() {
            return requireArguments().getParcelable(FILTER_EXTRA)
        }

}