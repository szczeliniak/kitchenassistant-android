package pl.szczeliniak.kitchenassistant.android.ui.dialogs.dayplanfilter

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogDayPlansFilterBinding
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils
import java.time.LocalDate

@AndroidEntryPoint
class DayPlansFilterDialog : DialogFragment() {

    companion object {
        private const val CALLBACK_EXTRA: String = "CALLBACK_EXTRA"
        private const val FILTER_EXTRA: String = "FILTER_EXTRA"
        private const val TAG = "DayPlansFilterDialog"

        fun show(
            fragmentManager: FragmentManager,
            filter: Filter?,
            onFilterChanged: OnFilterChanged
        ) {
            val dialog = DayPlansFilterDialog()
            val bundle = Bundle()
            bundle.putParcelable(CALLBACK_EXTRA, onFilterChanged)
            bundle.putParcelable(FILTER_EXTRA, filter)
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogDayPlansFilterBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDayPlansFilterBinding.inflate(layoutInflater)
        binding.dayPlanDate.setOnClickListener {
            val date = this.date ?: LocalDate.now()
            val dialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                binding.dayPlanDate.text = LocalDateUtils.stringify(LocalDate.of(year, month + 1, dayOfMonth))
            }, date.year, date.monthValue - 1, date.dayOfMonth)
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.label_button_cancel)) { _, _ ->
                binding.dayPlanDate.text = getString(R.string.label_button_select_date)
            }
            dialog.show()
        }
        filter?.let {
            it.date?.let { date -> binding.dayPlanDate.text = LocalDateUtils.stringify(date) }
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
            val filter = Filter(date)
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
    data class Filter(val date: LocalDate?) : Parcelable

    private val onFilterChanged: OnFilterChanged
        get() {
            return requireArguments().getParcelable(CALLBACK_EXTRA)!!
        }

    private val filter: Filter?
        get() {
            return requireArguments().getParcelable(FILTER_EXTRA)
        }

    private val date: LocalDate?
        get() {
            val asString = binding.dayPlanDate.text.toString()
            return if (LocalDateUtils.parsable(asString)) LocalDateUtils.parse(asString) else null
        }

}