package pl.szczeliniak.kitchenassistant.android.ui.fragments.dayplaninfo

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentDayPlanInfoBinding
import pl.szczeliniak.kitchenassistant.android.events.DayPlanReloadedEvent
import pl.szczeliniak.kitchenassistant.android.events.ReloadDayPlansEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanDetails
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.ReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.choosereceipttodayplan.ChooseReceiptToDayPlanDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanSimpleReceiptItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils
import javax.inject.Inject

@AndroidEntryPoint
class DayPlanInfoFragment : Fragment() {

    companion object {
        private const val DAY_PLAN_ID_EXTRA = "DAY_PLAN_ID_EXTRA"

        fun create(id: Int): DayPlanInfoFragment {
            val bundle = Bundle()
            id.let { bundle.putInt(DAY_PLAN_ID_EXTRA, it) }
            val fragment = DayPlanInfoFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var factory: DayPlanInfoFragmentViewModel.Factory

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: DayPlanInfoFragmentViewModel by viewModels {
        DayPlanInfoFragmentViewModel.provideFactory(factory, dayPlanId)
    }
    private val receiptsAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var archiveDayPlanStateHandler: LoadingStateHandler<Int>
    private lateinit var assignDeassignReceiptFromDayPlanStateHandler: LoadingStateHandler<Int>
    private lateinit var dayPlanLoadingStateHandler: LoadingStateHandler<DayPlanDetails>
    private lateinit var binding: FragmentDayPlanInfoBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDayPlanInfoBinding.inflate(inflater)
        binding.recyclerView.adapter = receiptsAdapter
        binding.buttonAddReceiptToDayPlan.setOnClickListener {
            ChooseReceiptToDayPlanDialog.show(
                childFragmentManager,
                ChooseReceiptToDayPlanDialog.OnReceiptChosen { id ->
                    viewModel.assignReceipt(id).observe(requireActivity()) {
                        assignDeassignReceiptFromDayPlanStateHandler.handle(it)
                    }
                })
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        archiveDayPlanStateHandler = prepareArchiveDayPlanStateHandler()
        assignDeassignReceiptFromDayPlanStateHandler = assignDeassignReceiptFromDayPlanStateHandler()
        dayPlanLoadingStateHandler = prepareDayPlanLoadingStateHandler()
        viewModel.dayPlan.observe(requireActivity()) { dayPlanLoadingStateHandler.handle(it) }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun assignDeassignReceiptFromDayPlanStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireContext(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                viewModel.reload()
            }
        })
    }

    private fun prepareArchiveDayPlanStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireContext(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadDayPlansEvent())
                requireActivity().finish()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_day_plan, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.archive) {
            ConfirmationDialog.show(childFragmentManager) {
                viewModel.archive(dayPlanId).observe(this) { archiveDayPlanStateHandler.handle(it) }
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun prepareDayPlanLoadingStateHandler(): LoadingStateHandler<DayPlanDetails> {
        return LoadingStateHandler(requireContext(), object : LoadingStateHandler.OnStateChanged<DayPlanDetails> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: DayPlanDetails) {
                eventBus.post(DayPlanReloadedEvent(data))
            }
        })
    }

    private val dayPlanId: Int
        get() {
            return requireArguments().getInt(DAY_PLAN_ID_EXTRA)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        eventBus.register(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        eventBus.unregister(this)
        super.onDestroy()
    }

    @Subscribe
    fun dayPlanReloadedEvent(event: DayPlanReloadedEvent) {
        event.data.let {
            binding.dayPlanDate.fillOrHide(LocalDateUtils.stringify(it.date), binding.dayPlanDate)
            binding.dayPlanName.text = it.name
            binding.dayPlanDescription.fillOrHide(it.description, binding.dayPlanDescriptionLayout)

            receiptsAdapter.clear()
            if (it.receipts.isEmpty()) {
                binding.dayPlanReceiptsLayout.showEmptyIcon(requireActivity())
            } else {
                binding.dayPlanReceiptsLayout.hideEmptyIcon()
                it.receipts.forEach { item ->
                    receiptsAdapter.add(
                        DayPlanSimpleReceiptItem(
                            requireContext(), item, { receipt ->
                                ReceiptActivity.start(requireContext(), receipt.id)
                            }, { receipt ->
                                ConfirmationDialog.show(childFragmentManager) {
                                    viewModel.deassignReceipt(receipt.id)
                                        .observe(requireActivity()) {
                                            assignDeassignReceiptFromDayPlanStateHandler.handle(it)
                                        }
                                }
                            }
                        ))
                }
            }
        }
    }

}