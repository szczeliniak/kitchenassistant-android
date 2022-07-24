package pl.szczeliniak.kitchenassistant.android.ui.fragments.receiptsteps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptStepsBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditstep.AddEditStepDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.fragments.ReceiptActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.listitems.StepItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ReceiptStepsFragment : ReceiptActivityFragment() {

    companion object {
        fun create(): ReceiptStepsFragment {
            return ReceiptStepsFragment()
        }
    }

    private lateinit var binding: FragmentReceiptStepsBinding
    private lateinit var deleteStepStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    private val stepsAdapter = GroupAdapter<GroupieViewHolder>()
    private val viewModel: ReceiptStepsFragmentViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptStepsBinding.inflate(inflater)
        binding.recyclerView.adapter = stepsAdapter
        binding.buttonAddStep.setOnClickListener {
            receipt?.let {
                AddEditStepDialog.show(
                    requireActivity().supportFragmentManager,
                    it.id
                )
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        deleteStepStateHandler = prepareDeleteStepLoadingStateHandler()
        loadData()
    }

    private fun prepareDeleteStepLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireContext(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadReceiptEvent())
            }
        })
    }

    private fun loadData() {
        receipt?.let { r ->
            stepsAdapter.clear()
            if (r.steps.isEmpty()) {
                binding.root.showEmptyIcon(requireActivity())
            } else {
                binding.root.hideEmptyIcon()
                r.steps.forEach { step ->
                    stepsAdapter.add(StepItem(requireContext(), r.id, step, { receiptId, s ->
                        ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                            viewModel.delete(receiptId, s.id).observe(viewLifecycleOwner) {
                                deleteStepStateHandler.handle(it)
                            }
                        }
                    }, { receiptId, s ->
                        AddEditStepDialog.show(requireActivity().supportFragmentManager, receiptId, s)
                    }))
                }
            }
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

}