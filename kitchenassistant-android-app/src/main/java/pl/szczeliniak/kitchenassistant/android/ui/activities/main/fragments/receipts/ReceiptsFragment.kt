package pl.szczeliniak.kitchenassistant.android.ui.activities.main.fragments.receipts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptsBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptsEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.ui.activities.addreceipt.AddReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.ReceiptActivity
import pl.szczeliniak.kitchenassistant.android.ui.listitems.ReceiptItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ReceiptsFragment : Fragment() {

    private val viewModel: ReceiptsFragmentViewModel by viewModels()
    private val adapter = GroupAdapter<GroupieViewHolder>()

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: FragmentReceiptsBinding
    private lateinit var receiptsLoadingStateHandler: LoadingStateHandler<List<Receipt>>
    private lateinit var deleteReceiptLoadingStateHandler: LoadingStateHandler<Int>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptsBinding.inflate(inflater)

        binding.root.setOnRefreshListener { viewModel.reloadReceipts() }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        binding.buttonAddReceipt.setOnClickListener { AddReceiptActivity.start(requireContext()) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        receiptsLoadingStateHandler = prepareReceiptsLoadingStateHandler()
        deleteReceiptLoadingStateHandler = prepareDeleteReceiptLoadingStateHandler()
        viewModel.receipts.observe(viewLifecycleOwner) { receiptsLoadingStateHandler.handle(it) }
        viewModel.reloadReceipts()
    }

    private fun prepareDeleteReceiptLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.layout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.layout.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: Int) {
                adapter.clear()
                viewModel.reloadReceipts()
            }
        })
    }

    private fun prepareReceiptsLoadingStateHandler(): LoadingStateHandler<List<Receipt>> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<List<Receipt>> {
            override fun onInProgress() {
                binding.root.isRefreshing = true
                binding.layout.hideEmptyIcon()
                binding.layout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.isRefreshing = false
                binding.layout.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: List<Receipt>) {
                adapter.clear()
                if (data.isEmpty()) {
                    binding.layout.showEmptyIcon(requireActivity())
                } else {
                    binding.layout.hideEmptyIcon()
                    data.forEach { receipt ->
                        adapter.add(ReceiptItem(requireContext(), receipt, {
                            ReceiptActivity.start(requireContext(), it.id)
                        }, {
                            viewModel.delete(it.id).observe(viewLifecycleOwner) { r ->
                                deleteReceiptLoadingStateHandler.handle(r)
                            }
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
    fun reloadReceiptsEvent(event: ReloadReceiptsEvent) {
        viewModel.reloadReceipts()
    }

}