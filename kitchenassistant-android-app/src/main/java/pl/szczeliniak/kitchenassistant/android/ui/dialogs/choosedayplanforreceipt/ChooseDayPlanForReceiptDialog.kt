package pl.szczeliniak.kitchenassistant.android.ui.dialogs.choosedayplanforreceipt

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogChooseDayPlanForReceiptBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlansResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlan
import pl.szczeliniak.kitchenassistant.android.ui.adapters.DayPlanDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable

@AndroidEntryPoint
class ChooseDayPlanForReceiptDialog : DialogFragment() {

    companion object {
        private const val ON_DAY_PLAN_CHOSEN_CALLBACK_EXTRA: String = "ON_DAY_PLAN_CHOSEN_CALLBACK_EXTRA"
        private const val RECEIPT_ID_EXTRA: String = "RECEIPT_ID_EXTRA"
        private const val TAG = "ChooseDayPlanForReceiptDialog"

        fun show(fragmentManager: FragmentManager, receiptId: Int, onDayPlanChosen: OnDayPlanChosen) {
            val dialog = ChooseDayPlanForReceiptDialog()
            val bundle = Bundle()
            bundle.putInt(RECEIPT_ID_EXTRA, receiptId)
            bundle.putParcelable(ON_DAY_PLAN_CHOSEN_CALLBACK_EXTRA, onDayPlanChosen)
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogChooseDayPlanForReceiptBinding
    private lateinit var loadDayPlansLoadingStateHandler: LoadingStateHandler<DayPlansResponse>
    private lateinit var button: Button
    private lateinit var dayPlansDropdownArrayAdapter: DayPlanDropdownArrayAdapter

    private val viewModel: ChooseDayPlanForReceiptDialogViewModel by viewModels()

    private var selectedDayPlanId: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogChooseDayPlanForReceiptBinding.inflate(layoutInflater)
        loadDayPlansLoadingStateHandler = prepareLoadDayPlansLoadingStateHandler()
        dayPlansDropdownArrayAdapter = DayPlanDropdownArrayAdapter(requireContext())

        binding.dayPlan.adapter = dayPlansDropdownArrayAdapter

        binding.dayPlan.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedDayPlanId = dayPlansDropdownArrayAdapter.getItem(position)?.id
                refreshButtonState()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedDayPlanId = null
                refreshButtonState()
            }
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun refreshButtonState() {
        button.enable(selectedDayPlanId != null)
    }

    override fun onResume() {
        super.onResume()
        viewModel.dayPlans.observe(this) { loadDayPlansLoadingStateHandler.handle(it) }
        val dialog = dialog as AlertDialog
        button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        button.setOnClickListener {
            selectedDayPlanId?.let { onDayPlanChosen.onDayPlanChosen(it, receiptId) }
            dismiss()
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
        refreshButtonState()
    }

    private fun prepareLoadDayPlansLoadingStateHandler(): LoadingStateHandler<DayPlansResponse> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<DayPlansResponse> {
            override fun onSuccess(data: DayPlansResponse) {
                dayPlansDropdownArrayAdapter.clear()
                val list = ArrayList<DayPlan?>()
                list.add(null)
                list.addAll(data.dayPlans)
                dayPlansDropdownArrayAdapter.addAll(list)
            }
        })
    }

    @Parcelize
    class OnDayPlanChosen(private val action: (dayPlanId: Int, receiptId: Int) -> Unit) : Parcelable {
        fun onDayPlanChosen(dayPlanId: Int, receiptId: Int) = action(dayPlanId, receiptId)
    }

    private val onDayPlanChosen: OnDayPlanChosen
        get() {
            return requireArguments().getParcelable(ON_DAY_PLAN_CHOSEN_CALLBACK_EXTRA)!!
        }

    private val receiptId: Int
        get() {
            return requireArguments().getInt(RECEIPT_ID_EXTRA)
        }
}