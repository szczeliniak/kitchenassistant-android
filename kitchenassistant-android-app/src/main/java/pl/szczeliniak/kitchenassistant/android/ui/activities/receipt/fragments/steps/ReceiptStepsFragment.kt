package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.steps

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
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.dialogs.addingredient.AddIngredientDialog
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.dialogs.addstep.AddStepDialog
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.ReceiptActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.listitems.StepItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ReceiptStepsFragment : ReceiptActivityFragment() {

    private lateinit var binding: FragmentReceiptStepsBinding
    private lateinit var deleteStepStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    private val stepsAdapter = GroupAdapter<GroupieViewHolder>()
    private val viewModel: ReceiptStepsFragmentViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptStepsBinding.inflate(inflater)
        binding.recyclerView.adapter = stepsAdapter
        binding.buttonAddStep.setOnClickListener { showAddStepDialog() }
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
                binding.root.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadReceiptEvent())
            }
        })
    }

    private fun showAddStepDialog() {
        receipt?.let {
            AddStepDialog.newInstance(it.id)
                .show(requireActivity().supportFragmentManager, AddIngredientDialog.TAG)
        }
    }

    private fun loadData() {
        receipt?.let { r ->
            stepsAdapter.clear()
            if (r.steps.isEmpty()) {
                binding.root.showEmptyIcon(requireActivity())
            } else {
                binding.root.hideEmptyIcon()
                r.steps.forEach { step ->
                    stepsAdapter.add(StepItem(requireContext(), r.id, step) { receiptId, s ->
                        viewModel.delete(receiptId, s.id).observe(viewLifecycleOwner) {
                            deleteStepStateHandler.handle(it)
                        }
                    })
                }
            }
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

}