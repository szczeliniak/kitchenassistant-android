package pl.szczeliniak.kitchenassistant.android.ui.fragments.dayplaningredients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentDayPlanIngredientsBinding
import pl.szczeliniak.kitchenassistant.android.events.DayPlanReloadedEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanReceipt
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanIngredientGroupHeaderItem
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanIngredientItem
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanReceiptHeaderItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class DayPlanIngredientsFragment : Fragment() {

    companion object {
        private const val DAY_PLAN_ID_EXTRA = "DAY_PLAN_ID_EXTRA"

        fun create(id: Int): DayPlanIngredientsFragment {
            val bundle = Bundle()
            id.let { bundle.putInt(DAY_PLAN_ID_EXTRA, it) }
            val fragment = DayPlanIngredientsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var factory: DayPlanIngredientsFragmentViewModel.Factory

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: DayPlanIngredientsFragmentViewModel by viewModels {
        DayPlanIngredientsFragmentViewModel.provideFactory(factory, dayPlanId)
    }
    private val receiptsAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var receiptsLoadingStateHandler: LoadingStateHandler<List<DayPlanReceipt>>
    private lateinit var binding: FragmentDayPlanIngredientsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDayPlanIngredientsBinding.inflate(inflater)
        binding.recyclerView.adapter = receiptsAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        receiptsLoadingStateHandler = prepareReceiptsLoadingStateHandler()
        viewModel.receipts.observe(requireActivity()) { receiptsLoadingStateHandler.handle(it) }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun prepareReceiptsLoadingStateHandler(): LoadingStateHandler<List<DayPlanReceipt>> {
        return LoadingStateHandler(requireContext(), object : LoadingStateHandler.OnStateChanged<List<DayPlanReceipt>> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: List<DayPlanReceipt>) {
                receiptsAdapter.clear()
                if (data.isEmpty()) {
                    binding.root.showEmptyIcon(requireActivity())
                } else {
                    binding.root.hideEmptyIcon()
                }
                data.forEach { receipt ->
                    receiptsAdapter.add(DayPlanReceiptHeaderItem(receipt))
                    receipt.ingredientGroups.forEach { ingredientGroup ->
                        if (ingredientGroup.ingredients.isNotEmpty()) {
                            receiptsAdapter.add(DayPlanIngredientGroupHeaderItem(ingredientGroup))
                            ingredientGroup.ingredients.forEach { ingredient ->
                                receiptsAdapter.add(DayPlanIngredientItem(ingredient))
                            }
                        }
                    }
                }
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
        viewModel.reload()
    }

}