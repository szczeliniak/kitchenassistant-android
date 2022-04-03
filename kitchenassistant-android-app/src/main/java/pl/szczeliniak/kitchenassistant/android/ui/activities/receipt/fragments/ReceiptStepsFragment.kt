package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptStepsBinding
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.dialogs.addingredient.AddIngredientDialog
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.dialogs.addstep.AddStepDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.StepItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.showEmptyIcon

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ReceiptStepsFragment : ReceiptActivityFragment() {

    private lateinit var binding: FragmentReceiptStepsBinding

    private val stepsAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptStepsBinding.inflate(inflater)
        binding.fragmentReceiptStepsRecyclerView.adapter = stepsAdapter
        binding.fragmentReceiptStepsFabAddStep.setOnClickListener { showAddStepDialog() }
        loadData()
        return binding.root
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
                r.steps.forEach {
                    stepsAdapter.add(StepItem(it))
                }
            }
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

}