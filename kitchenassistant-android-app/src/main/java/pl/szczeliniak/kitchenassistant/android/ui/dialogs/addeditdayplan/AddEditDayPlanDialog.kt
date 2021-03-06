package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditdayplan

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
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
import pl.szczeliniak.kitchenassistant.android.databinding.DialogAddEditDayPlanBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadDayPlansEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanDetails
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.utils.ButtonUtils.Companion.enable
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class AddEditDayPlanDialog : DialogFragment() {

    companion object {
        private const val DAY_PLAN_EXTRA = "DAY_PLAN_EXTRA"
        private const val TAG = "AddEditDayPlanDialog"

        fun show(fragmentManager: FragmentManager, dayPlanId: Int? = null) {
            val bundle = Bundle()
            dayPlanId?.let { bundle.putInt(DAY_PLAN_EXTRA, it) }
            val dialog = AddEditDayPlanDialog()
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogAddEditDayPlanBinding
    private lateinit var saveDayPlanLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var loadDayPlanLoadingStateHandler: LoadingStateHandler<DayPlanDetails>
    private lateinit var positiveButton: Button

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var localStorageService: LocalStorageService

    @Inject
    lateinit var addEditDayPlanDialogViewModel: AddEditDayPlanDialogViewModel.Factory

    private val viewModel: AddEditDayPlanDialogViewModel by viewModels {
        AddEditDayPlanDialogViewModel.provideFactory(addEditDayPlanDialogViewModel, dayPlanId)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddEditDayPlanBinding.inflate(layoutInflater)

        binding.dayPlanName.doOnTextChanged { _, _, _, _ -> checkButtonState() }

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

        saveDayPlanLoadingStateHandler = prepareSaveDayPlanLoadingStateHandler()
        loadDayPlanLoadingStateHandler = prepareLoadDayPlanLoadingStateHandler()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_add) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun checkButtonState() {
        positiveButton.enable(name != null)
    }

    private fun prepareSaveDayPlanLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadDayPlansEvent())
                dismiss()
            }
        })
    }

    private fun prepareLoadDayPlanLoadingStateHandler(): LoadingStateHandler<DayPlanDetails> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<DayPlanDetails> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: DayPlanDetails) {
                binding.title.text = getString(R.string.title_dialog_edit_day_plan)
                positiveButton.text = getString(R.string.title_button_edit)

                binding.dayPlanAutomaticArchiving.isChecked = data.automaticArchiving
                binding.dayPlanName.setText(data.name)
                binding.dayPlanDescription.setText(data.description)
                LocalDateUtils.stringify(data.date)?.let { date -> binding.dayPlanDate.text = date }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog

        viewModel.dayPlan.observe(this) { loadDayPlanLoadingStateHandler.handle(it) }

        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        checkButtonState()
        positiveButton.setOnClickListener { saveDayPlan() }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private fun saveDayPlan() {
        dayPlanId?.let {
            ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                viewModel.update(dayPlanId!!, UpdateDayPlanRequest(name!!, description, date, automaticArchiving))
                    .observe(this) { saveDayPlanLoadingStateHandler.handle(it) }
            }
        } ?: kotlin.run {
            viewModel.add(
                AddDayPlanRequest(name!!, description, localStorageService.getId(), date, automaticArchiving)
            ).observe(this) { saveDayPlanLoadingStateHandler.handle(it) }
        }
    }

    private val dayPlanId: Int?
        get() {
            val id = requireArguments().getInt(DAY_PLAN_EXTRA)
            return if (id <= 0) return null else id
        }

    private val date: LocalDate?
        get() {
            val asString = binding.dayPlanDate.text.toString()
            return if (LocalDateUtils.parsable(asString)) LocalDateUtils.parse(asString) else null
        }

    private val name: String?
        get() {
            return binding.dayPlanName.text.toString().ifEmpty { return null }
        }

    private val description: String?
        get() {
            return binding.dayPlanDescription.text.toString().ifEmpty { return null }
        }

    private val automaticArchiving: Boolean
        get() {
            return binding.dayPlanAutomaticArchiving.isChecked
        }

}