package pl.szczeliniak.kitchenassistant.android.ui.dialogs.choosedayplanforrecipe

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogChooseDayForRecipeBinding
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import java.time.LocalDate

@AndroidEntryPoint
class ChooseDayForRecipeDialog : DialogFragment() {

    companion object {
        private const val ON_DAY_CHOSEN_CALLBACK_EXTRA: String = "ON_DAY_CHOSEN_CALLBACK_EXTRA"
        private const val TAG = "ChooseDayForRecipeDialog"

        fun show(fragmentManager: FragmentManager, onDayChosen: OnDayChosen) {
            val dialog = ChooseDayForRecipeDialog()
            val bundle = Bundle()
            bundle.putParcelable(ON_DAY_CHOSEN_CALLBACK_EXTRA, onDayChosen)
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogChooseDayForRecipeBinding
    private lateinit var button: Button

    private var selectedDay: LocalDate? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogChooseDayForRecipeBinding.inflate(layoutInflater)
        binding.calendar.minDate = System.currentTimeMillis() - 1000
        binding.calendar.setOnDateChangeListener { _, year, month, day ->
            selectedDay = LocalDate.of(year, month + 1, day)
            refreshButtonState()
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_select_date) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun refreshButtonState() {
        button.enable(selectedDay != null)
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog
        button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        button.setOnClickListener {
            selectedDay?.let { onDayChosen.onDayChosen(it) }
            dismiss()
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
        refreshButtonState()
    }

    @Parcelize
    class OnDayChosen(private val action: (date: LocalDate) -> Unit) : Parcelable {
        fun onDayChosen(date: LocalDate) = action(date)
    }

    private val onDayChosen: OnDayChosen
        get() {
            return requireArguments().getParcelable(ON_DAY_CHOSEN_CALLBACK_EXTRA, OnDayChosen::class.java)!!
        }

}