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
import pl.szczeliniak.kitchenassistant.android.ui.listitems.StepItem

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ReceiptStepsFragment : ReceiptActivityFragment() {

    private lateinit var binding: FragmentReceiptStepsBinding

    private val stepsAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptStepsBinding.inflate(inflater)
        binding.fragmentReceiptStepsRecyclerView.adapter = stepsAdapter
        loadData()
        return binding.root
    }

    private fun loadData() {
        receipt?.let { r ->
            stepsAdapter.clear()
            r.steps.forEach {
                stepsAdapter.add(StepItem(it))
            }
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

}