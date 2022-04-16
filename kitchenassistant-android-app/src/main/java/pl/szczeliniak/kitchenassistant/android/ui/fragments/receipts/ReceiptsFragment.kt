package pl.szczeliniak.kitchenassistant.android.ui.fragments.receipts

import android.os.Bundle
import android.view.*
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
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptsBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptsEvent
import pl.szczeliniak.kitchenassistant.android.listeners.EndlessScrollRecyclerViewListener
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.ReceiptsResponse
import pl.szczeliniak.kitchenassistant.android.ui.activities.addeditreceipt.AddEditReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.ReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.receiptsfilter.ReceiptsFilterDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.ReceiptItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ActivityUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ActivityUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ReceiptsFragment : Fragment() {

    companion object {
        private const val DEFAULT_PAGE = 1

        fun create(): ReceiptsFragment {
            return ReceiptsFragment()
        }
    }

    private val viewModel: ReceiptsFragmentViewModel by viewModels()
    private val adapter = GroupAdapter<GroupieViewHolder>()

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: FragmentReceiptsBinding
    private lateinit var receiptsLoadingStateHandler: LoadingStateHandler<ReceiptsResponse>
    private lateinit var deleteReceiptLoadingStateHandler: LoadingStateHandler<Int>

    private var filter: ReceiptsFilterDialog.Filter? = null
    private var page: Int = DEFAULT_PAGE
    private var maxPage: Int = DEFAULT_PAGE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptsBinding.inflate(inflater)
        binding.root.setOnRefreshListener { resetReceipts() }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        binding.recyclerView.addOnScrollListener(EndlessScrollRecyclerViewListener(
            binding.recyclerView.layoutManager as LinearLayoutManager,
            {
                page += 1
                viewModel.loadReceipts(page, filter?.categoryId, filter?.receiptName)
            }
        ) { !binding.root.isRefreshing && page < maxPage })

        binding.buttonAddReceipt.setOnClickListener { AddEditReceiptActivity.start(requireContext()) }
        return binding.root
    }

    private fun resetReceipts() {
        adapter.clear()
        page = DEFAULT_PAGE
        viewModel.loadReceipts(page, filter?.categoryId, filter?.receiptName)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        receiptsLoadingStateHandler = prepareReceiptsLoadingStateHandler()
        deleteReceiptLoadingStateHandler = prepareDeleteReceiptLoadingStateHandler()
        viewModel.receipts.observe(viewLifecycleOwner) { receiptsLoadingStateHandler.handle(it) }
        viewModel.filter.observe(viewLifecycleOwner) {
            this.filter = it
            resetReceipts()
        }
    }

    private fun prepareDeleteReceiptLoadingStateHandler(): LoadingStateHandler<Int> {
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
                maxPage = data.pagination.numberOfPages
                if (data.receipts.isEmpty()) {
                    binding.layout.showEmptyIcon(requireActivity())
                } else {
                    binding.layout.hideEmptyIcon()
                    data.receipts.forEach { receipt ->
                        adapter.add(ReceiptItem(requireContext(), receipt, {
                            ReceiptActivity.start(requireContext(), it.id)
                        }, {
                            viewModel.delete(it.id).observe(viewLifecycleOwner) { r ->
                                deleteReceiptLoadingStateHandler.handle(r)
                            }
                        }, {
                            AddEditReceiptActivity.start(requireContext(), it)
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
        inflater.inflate(R.menu.fragment_receipts, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fragment_receipts_menu_item_filter -> {
                ReceiptsFilterDialog.show(
                    requireActivity().supportFragmentManager,
                    ReceiptsFilterDialog.Filter(filter?.categoryId, filter?.receiptName),
                    ReceiptsFilterDialog.OnFilterChanged { viewModel.changeFilter(it) })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe
    fun reloadReceiptsEvent(event: ReloadReceiptsEvent) {
        resetReceipts()
    }

}