package pl.szczeliniak.kitchenassistant.android.ui.activities.main.fragments.shoppinglists

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
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentShoppingListsBinding
import pl.szczeliniak.kitchenassistant.android.events.NewShoppingListEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.ui.activities.addshoppinglist.AddShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.ShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.listitems.ShoppingListItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ShoppingListsFragment : Fragment() {

    private val viewModel: ShoppingListsFragmentViewModel by viewModels()
    private val adapter = GroupAdapter<GroupieViewHolder>()

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: FragmentShoppingListsBinding
    private lateinit var shoppingListsLoadingStateHandler: LoadingStateHandler<List<ShoppingList>>
    private lateinit var deleteShoppingListLoadingStateHandler: LoadingStateHandler<Int>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentShoppingListsBinding.inflate(inflater)

        binding.root.setOnRefreshListener { viewModel.reloadShoppingLists() }
        binding.fragmentShoppingListsRecyclerView.adapter = adapter
        binding.fragmentShoppingListsRecyclerView.addItemDecoration(
            DividerItemDecoration(binding.fragmentShoppingListsRecyclerView.context, DividerItemDecoration.VERTICAL)
        )

        binding.fragmentShoppingListsFabAddReceipt.setOnClickListener { AddShoppingListActivity.start(requireContext()) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shoppingListsLoadingStateHandler = prepareShoppingListsLoadingStateHandler()
        deleteShoppingListLoadingStateHandler = prepareDeleteReceiptLoadingStateHandler()
        viewModel.shoppingLists.observe(viewLifecycleOwner) { shoppingListsLoadingStateHandler.handle(it) }
        viewModel.reloadShoppingLists()
    }

    private fun prepareDeleteReceiptLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.fragmentShoppingListsLayout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.fragmentShoppingListsLayout.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: Int) {
                adapter.clear()
                viewModel.reloadShoppingLists()
            }
        })
    }

    private fun prepareShoppingListsLoadingStateHandler(): LoadingStateHandler<List<ShoppingList>> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<List<ShoppingList>> {
            override fun onInProgress() {
                binding.root.isRefreshing = true
                binding.fragmentShoppingListsLayout.hideEmptyIcon()
                binding.fragmentShoppingListsLayout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.isRefreshing = false
                binding.fragmentShoppingListsLayout.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: List<ShoppingList>) {
                if (data.isEmpty()) {
                    binding.fragmentShoppingListsLayout.showEmptyIcon(requireActivity())
                } else {
                    adapter.clear()
                    data.forEach { shoppingList ->
                        adapter.add(ShoppingListItem(requireContext(), shoppingList, {
                            ShoppingListActivity.start(requireContext(), shoppingList.id)
                        }, {
                            viewModel.delete(it.id).observe(viewLifecycleOwner) { r ->
                                deleteShoppingListLoadingStateHandler.handle(r)
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
    fun newShoppingListEvent(event: NewShoppingListEvent) {
        viewModel.reloadShoppingLists()
    }

}