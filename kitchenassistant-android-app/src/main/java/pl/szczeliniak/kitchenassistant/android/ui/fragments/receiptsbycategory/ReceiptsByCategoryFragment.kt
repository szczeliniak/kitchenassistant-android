package pl.szczeliniak.kitchenassistant.android.ui.fragments.receiptsbycategory

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
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptsByCategoryBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptsEvent
import pl.szczeliniak.kitchenassistant.android.listeners.EndlessScrollRecyclerViewListener
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.ReceiptsResponse
import pl.szczeliniak.kitchenassistant.android.ui.activities.addeditreceipt.AddEditReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.ReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.choosedayplanforreceipt.ChooseDayPlanForReceiptDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.receiptsfilter.ReceiptsFilterDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.ReceiptItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.DebounceExecutor
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ReceiptsByCategoryFragment : Fragment() {

    companion object {
        private const val FILTER_SAVED_STATE_EXTRA = "FILTER_SAVED_STATE_EXTRA"
        private const val CATEGORY_ID_EXTRA = "CATEGORY_ID_EXTRA"

        fun create(id: Int?): ReceiptsByCategoryFragment {
            val bundle = Bundle()
            id?.let { bundle.putInt(CATEGORY_ID_EXTRA, it) }
            val fragment = ReceiptsByCategoryFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var viewModel: ReceiptsByCategoryFragmentViewModel
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val debounceExecutor = DebounceExecutor(500)

    @Inject
    lateinit var receiptsByCategoryFragmentViewModel: ReceiptsByCategoryFragmentViewModel.Factory

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: FragmentReceiptsByCategoryBinding
    private lateinit var receiptsLoadingStateHandler: LoadingStateHandler<ReceiptsResponse>
    private lateinit var doActionAndResetReceiptsLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var doActionLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var endlessScrollRecyclerViewListener: EndlessScrollRecyclerViewListener
    private lateinit var searchView: SearchView

    private var filter: ReceiptsFilterDialog.Filter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        savedInstanceState?.getParcelable<ReceiptsFilterDialog.Filter?>(FILTER_SAVED_STATE_EXTRA)?.let {
            filter = it
        }

        val viewModel: ReceiptsByCategoryFragmentViewModel by viewModels {
            ReceiptsByCategoryFragmentViewModel.provideFactory(receiptsByCategoryFragmentViewModel, categoryId)
        }
        this.viewModel = viewModel

        binding = FragmentReceiptsByCategoryBinding.inflate(inflater)
        binding.root.setOnRefreshListener { resetReceipts() }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        endlessScrollRecyclerViewListener = EndlessScrollRecyclerViewListener(
            binding.recyclerView.layoutManager as LinearLayoutManager
        ) { viewModel.loadReceipts(it, searchView.query.toString(), filter?.receiptTag) }
        binding.recyclerView.addOnScrollListener(endlessScrollRecyclerViewListener)

        return binding.root
    }

    private fun resetReceipts() {
        adapter.clear()
        endlessScrollRecyclerViewListener.reset()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiptsLoadingStateHandler = prepareReceiptsLoadingStateHandler()
        doActionAndResetReceiptsLoadingStateHandler = prepareDoActionAndResetReceiptsLoadingStateHandler()
        doActionLoadingStateHandler = prepareDoActionLoadingStateHandler()
        viewModel.receipts.observe(viewLifecycleOwner) { receiptsLoadingStateHandler.handle(it) }
    }

    private fun prepareDoActionAndResetReceiptsLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.layout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.layout.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                resetReceipts()
            }
        })
    }

    private fun prepareDoActionLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.layout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.layout.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
            }
        })
    }

    private fun prepareReceiptsLoadingStateHandler(): LoadingStateHandler<ReceiptsResponse> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<ReceiptsResponse> {
            override fun onInProgress() {
                binding.root.isRefreshing = true
                binding.layout.hideEmptyIcon()
            }

            override fun onFinish() {
                binding.root.isRefreshing = false
            }

            override fun onSuccess(data: ReceiptsResponse) {
                adapter.clear()
                endlessScrollRecyclerViewListener.maxPage = data.pagination.numberOfPages
                if (data.receipts.isEmpty()) {
                    binding.layout.showEmptyIcon(requireActivity())
                } else {
                    binding.layout.hideEmptyIcon()
                    data.receipts.forEach { receipt ->
                        adapter.add(ReceiptItem(requireContext(), receipt, categoryId == null, {
                            ReceiptActivity.start(requireContext(), it.id)
                        }, {
                            ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                                viewModel.delete(it.id).observe(viewLifecycleOwner) { r ->
                                    doActionAndResetReceiptsLoadingStateHandler.handle(r)
                                }
                            }
                        }, {
                            AddEditReceiptActivity.start(requireContext(), it.id)
                        }, {
                            ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                                viewModel.setFavorite(it.id, !it.favorite).observe(viewLifecycleOwner) { r ->
                                    doActionAndResetReceiptsLoadingStateHandler.handle(r)
                                }
                            }
                        }, {
                            ChooseDayPlanForReceiptDialog.show(
                                requireActivity().supportFragmentManager,
                                it.id,
                                ChooseDayPlanForReceiptDialog.OnDayPlanChosen { dayPlanId, receiptId ->
                                    viewModel.assignReceiptToDayPlan(receiptId, dayPlanId)
                                        .observe(viewLifecycleOwner) { r ->
                                            doActionLoadingStateHandler.handle(r)
                                        }
                                })
                        }))
                    }
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventBus.register(this)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_receipts, menu)
        searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                debounceExecutor.execute { resetReceipts() }
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter -> {
                ReceiptsFilterDialog.show(
                    requireActivity().supportFragmentManager,
                    ReceiptsFilterDialog.Filter(filter?.receiptTag),
                    ReceiptsFilterDialog.OnFilterChanged {
                        filter = it
                        resetReceipts()
                    }
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(FILTER_SAVED_STATE_EXTRA, filter)
    }

    private val categoryId: Int?
        get() {
            return requireArguments().get(CATEGORY_ID_EXTRA) as Int?
        }

    override fun onDestroy() {
        eventBus.unregister(this)
        super.onDestroy()
    }

    @Subscribe
    fun reloadReceipts(event: ReloadReceiptsEvent) {
        endlessScrollRecyclerViewListener.reset()
    }

}