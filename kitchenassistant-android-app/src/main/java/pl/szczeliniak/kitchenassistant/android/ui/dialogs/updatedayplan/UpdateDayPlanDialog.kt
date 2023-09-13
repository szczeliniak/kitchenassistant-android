package pl.szczeliniak.kitchenassistant.android.ui.dialogs.updatedayplan

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogUpdateDayplanBinding
import pl.szczeliniak.kitchenassistant.android.events.DayPlanEditedEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlanResponse
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class UpdateDayPlanDialog : DialogFragment() {

    companion object {
        private const val DAY_PLAN_ID_EXTRA = "DAY_PLAN_ID_EXTRA"
        private const val TAG = "UpdateDayPlanDialog"

        fun show(fragmentManager: FragmentManager, dayPlanId: Int) {
            val bundle = Bundle()
            bundle.putInt(DAY_PLAN_ID_EXTRA, dayPlanId)
            val dialog = UpdateDayPlanDialog()
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogUpdateDayplanBinding
    private lateinit var positiveButton: Button
    private lateinit var updateDayPlanLoadingStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var factory: UpdateDayPlanDialogViewModel.Factory

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: UpdateDayPlanDialogViewModel by viewModels {
        UpdateDayPlanDialogViewModel.provideFactory(factory, dayPlanId)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogUpdateDayplanBinding.inflate(layoutInflater)

        viewModel.dayPlan.observe(requireActivity()) {
            loadDayPlanLoadingStateHandler().handle(it)
        }

        updateDayPlanLoadingStateHandler = prepareUpdateDayPlanLoadingStateHandler()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_edit) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    private fun loadDayPlanLoadingStateHandler(): LoadingStateHandler<DayPlanResponse.DayPlan> {
        return LoadingStateHandler(
            requireActivity(),
            object : LoadingStateHandler.OnStateChanged<DayPlanResponse.DayPlan> {
                override fun onInProgress() {
                    binding.root.showProgressSpinner(requireActivity())
                }

                override fun onFinish() {
                    binding.root.hideProgressSpinner()
                }

                override fun onSuccess(data: DayPlanResponse.DayPlan) {
                    binding.calendar.date = LocalDateUtils.toMillis(data.date)

                    binding.calendar.setOnDateChangeListener { calendarView, year, month, day ->
                        calendarView.date = LocalDateUtils.toMillis(year, month, day)
                    }
                }
            })
    }

    private fun prepareUpdateDayPlanLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(DayPlanEditedEvent())
                dismiss()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog
        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                viewModel.update(
                    dayPlanId,
                    UpdateDayPlanRequest(LocalDateUtils.toLocalDate(binding.calendar.date))
                ).observe(this) { updateDayPlanLoadingStateHandler.handle(it) }
            }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    private val dayPlanId: Int
        get() {
            val id = requireArguments().getInt(DAY_PLAN_ID_EXTRA, -1)
            if(id < 0) {
                throw IllegalArgumentException("Dayplan id cannot be null")
            }
            return id
        }

}