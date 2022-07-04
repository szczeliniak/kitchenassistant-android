package pl.szczeliniak.kitchenassistant.android.ui.fragments.dayplans

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentDayPlansBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadDayPlansEvent
import pl.szczeliniak.kitchenassistant.android.listeners.EndlessScrollRecyclerViewListener
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlansResponse
import pl.szczeliniak.kitchenassistant.android.ui.activities.dayplan.DayPlanActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditdayplan.AddEditDayPlanDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.dayplanfilter.DayPlansFilterDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.DebounceExecutor
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class DayPlansFragment : Fragment() {

    companion object {
        private const val FILTER_SAVED_STATE_EXTRA = "FILTER_SAVED_STATE_EXTRA"
        fun create(): DayPlansFragment {
            return DayPlansFragment()
        }
    }

    private val viewModel: DayPlansFragmentViewModel by viewModels()
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val debounceExecutor = DebounceExecutor(500)

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: FragmentDayPlansBinding
    private lateinit var dayPlansLoadingStateHandler: LoadingStateHandler<DayPlansResponse>
    private lateinit var deleteDayPlanLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var endlessScrollRecyclerViewListener: EndlessScrollRecyclerViewListener
    private lateinit var searchView: SearchView

    private var filter: DayPlansFilterDialog.Filter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        savedInstanceState?.getParcelable<DayPlansFilterDialog.Filter?>(FILTER_SAVED_STATE_EXTRA)?.let {
            filter = it
        }
        binding = FragmentDayPlansBinding.inflate(inflater)

        binding.root.setOnRefreshListener { reset() }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        endlessScrollRecyclerViewListener = EndlessScrollRecyclerViewListener(
            binding.recyclerView.layoutManager as LinearLayoutManager
        ) { viewModel.reload(it, filter?.date) }
        binding.recyclerView.addOnScrollListener(endlessScrollRecyclerViewListener)

        binding.buttonAddDayPlan.setOnClickListener { AddEditDayPlanDialog.show(parentFragmentManager) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dayPlansLoadingStateHandler = prepareDayPlansLoadingStateHandler()
        deleteDayPlanLoadingStateHandler = prepareDeleteShoppingListLoadingStateHandler()
        viewModel.dayPlans.observe(viewLifecycleOwner) { dayPlansLoadingStateHandler.handle(it) }
    }

    private fun reset() {
        adapter.clear()
        endlessScrollRecyclerViewListener.reset()
    }

    private fun prepareDeleteShoppingListLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.layout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.layout.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                reset()
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
                    adapter.clear()
                    endlessScrollRecyclerViewListener.maxPage = data.pagination.numberOfPages
                    if (data.dayPlans.isEmpty()) {
                        binding.layout.showEmptyIcon(requireActivity())
                    } else {
                        binding.layout.hideEmptyIcon()
                        data.dayPlans.forEach { dayPlan ->
                            adapter.add(DayPlanItem(requireContext(), dayPlan, {
                                DayPlanActivity.start(requireContext(), dayPlan.id)
                            }, {
                                viewModel.delete(it.id).observe(viewLifecycleOwner) { r ->
                                    deleteDayPlanLoadingStateHandler.handle(r)
                                }
                            }, {
                                AddEditDayPlanDialog.show(parentFragmentManager, it)
                            }))
                        }
                    }
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        eventBus.register(this)
    }

    override fun onDestroy() {
        eventBus.unregister(this)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_day_plans, menu)
        searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                debounceExecutor.execute { reset() }
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter -> {
                DayPlansFilterDialog.show(
                    requireActivity().supportFragmentManager,
                    DayPlansFilterDialog.Filter(filter?.date),
                    DayPlansFilterDialog.OnFilterChanged {
                        filter = it
                        reset()
                    })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe
    fun reload(event: ReloadDayPlansEvent) {
        reset()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(FILTER_SAVED_STATE_EXTRA, filter)
    }

}