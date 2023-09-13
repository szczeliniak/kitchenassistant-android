package pl.szczeliniak.kitchenassistant.android.ui.fragments.dayplans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentDayPlansBinding
import pl.szczeliniak.kitchenassistant.android.events.DayPlanDeletedEvent
import pl.szczeliniak.kitchenassistant.android.events.DayPlanEditedEvent
import pl.szczeliniak.kitchenassistant.android.listeners.EndlessScrollRecyclerViewListener
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlansResponse
import pl.szczeliniak.kitchenassistant.android.ui.activities.dayplan.DayPlanActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.updatedayplan.UpdateDayPlanDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class DayPlansFragment : Fragment() {

    companion object {
        fun create(): DayPlansFragment {
            return DayPlansFragment()
        }
    }

    private val viewModel: DayPlansFragmentViewModel by viewModels()
    private val adapter = GroupAdapter<GroupieViewHolder>()

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: FragmentDayPlansBinding
    private lateinit var dayPlansLoadingStateHandler: LoadingStateHandler<DayPlansResponse>
    private lateinit var deleteDayPlanLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var endlessScrollRecyclerViewListener: EndlessScrollRecyclerViewListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDayPlansBinding.inflate(inflater)

        binding.root.setOnRefreshListener { endlessScrollRecyclerViewListener.reset() }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        endlessScrollRecyclerViewListener = EndlessScrollRecyclerViewListener(
            binding.recyclerView.layoutManager as LinearLayoutManager,
            { viewModel.reload(it) },
            { adapter.clear() }
        )
        binding.recyclerView.addOnScrollListener(endlessScrollRecyclerViewListener)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dayPlansLoadingStateHandler = prepareDayPlansLoadingStateHandler()
        deleteDayPlanLoadingStateHandler = prepareDeleteDayPlanLoadingStateHandler()
        viewModel.dayPlans.observe(viewLifecycleOwner) { dayPlansLoadingStateHandler.handle(it) }
    }

    private fun prepareDeleteDayPlanLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.layout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.layout.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                endlessScrollRecyclerViewListener.reset()
            }
        })
    }

    private fun prepareDayPlansLoadingStateHandler(): LoadingStateHandler<DayPlansResponse> {
        return LoadingStateHandler(
            requireActivity(),
            object : LoadingStateHandler.OnStateChanged<DayPlansResponse> {
                override fun onInProgress() {
                    binding.root.isRefreshing = true
                }

                override fun onFinish() {
                    binding.root.isRefreshing = false
                }

                override fun onSuccess(data: DayPlansResponse) {
                    endlessScrollRecyclerViewListener.maxPage = data.dayPlans.totalNumberOfPages
                    if (data.dayPlans.items.isEmpty()) {
                        binding.layout.showEmptyIcon(requireActivity())
                    } else {
                        binding.layout.hideEmptyIcon()
                        data.dayPlans.items.forEach { dayPlan ->
                            adapter.add(DayPlanItem(requireContext(), dayPlan, {
                                DayPlanActivity.start(requireContext(), dayPlan.id)
                            }, {
                                viewModel.delete(it.id).observe(viewLifecycleOwner) { r ->
                                    deleteDayPlanLoadingStateHandler.handle(r)
                                }
                            }, {
                                UpdateDayPlanDialog.show(parentFragmentManager, dayPlan.id)
                            }))
                        }
                    }
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventBus.register(this)
    }

    override fun onDestroy() {
        eventBus.unregister(this)
        super.onDestroy()
    }

    @Subscribe
    fun onDayPlanDeleted(event: DayPlanDeletedEvent) {
        endlessScrollRecyclerViewListener.reset()
    }

    @Subscribe
    fun onDayPlanEdited(event: DayPlanEditedEvent) {
        endlessScrollRecyclerViewListener.reset()
    }

}