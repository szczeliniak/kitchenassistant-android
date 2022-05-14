package pl.szczeliniak.kitchenassistant.android.ui.fragments.shoppinglists

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
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentShoppingListsBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListsEvent
import pl.szczeliniak.kitchenassistant.android.listeners.EndlessScrollRecyclerViewListener
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListsResponse
import pl.szczeliniak.kitchenassistant.android.ui.activities.addshoppinglist.AddEditShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.ShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.components.FloatingActionButtonComponent
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.shoppinglistsfilter.ShoppingListsFilterDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.ShoppingListItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.DebounceExecutor
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ShoppingListsFragment : Fragment() {

    companion object {
        private const val FILTER_SAVED_STATE_EXTRA = "FILTER_SAVED_STATE_EXTRA"
        fun create(): ShoppingListsFragment {
            return ShoppingListsFragment()
        }
    }

    private val viewModel: ShoppingListsFragmentViewModel by viewModels()
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val debounceExecutor = DebounceExecutor(500)

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: FragmentShoppingListsBinding
    private lateinit var shoppingListsLoadingStateHandler: LoadingStateHandler<ShoppingListsResponse>
    private lateinit var deleteShoppingListLoadingStateHandler: LoadingStateHandler<Int>
    private lateinit var endlessScrollRecyclerViewListener: EndlessScrollRecyclerViewListener
    private lateinit var searchView: SearchView

    private var filter: ShoppingListsFilterDialog.Filter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        savedInstanceState?.getParcelable<ShoppingListsFilterDialog.Filter?>(FILTER_SAVED_STATE_EXTRA)?.let {
            filter = it
        }
        binding = FragmentShoppingListsBinding.inflate(inflater)

        binding.root.setOnRefreshListener { resetShoppingLists() }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        endlessScrollRecyclerViewListener = EndlessScrollRecyclerViewListener(
            binding.recyclerView.layoutManager as LinearLayoutManager
        ) { viewModel.reloadShoppingLists(it, searchView.query.toString(), filter?.date) }
        binding.recyclerView.addOnScrollListener(endlessScrollRecyclerViewListener)

        binding.buttonAddShoppingList.onClick =
            FloatingActionButtonComponent.OnClick { AddEditShoppingListActivity.start(requireContext()) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shoppingListsLoadingStateHandler = prepareShoppingListsLoadingStateHandler()
        deleteShoppingListLoadingStateHandler = prepareDeleteShoppingListLoadingStateHandler()
        viewModel.shoppingLists.observe(viewLifecycleOwner) { shoppingListsLoadingStateHandler.handle(it) }
    }

    private fun resetShoppingLists() {
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
                resetShoppingLists()
            }
        })
    }

    private fun prepareShoppingListsLoadingStateHandler(): LoadingStateHandler<ShoppingListsResponse> {
        return LoadingStateHandler(
            requireActivity(),
            object : LoadingStateHandler.OnStateChanged<ShoppingListsResponse> {
                override fun onInProgress() {
                    binding.root.isRefreshing = true
                }

                override fun onFinish() {
                    binding.root.isRefreshing = false
                }

                override fun onSuccess(data: ShoppingListsResponse) {
                    adapter.clear()
                    endlessScrollRecyclerViewListener.maxPage = data.pagination.numberOfPages
                    if (data.shoppingLists.isEmpty()) {
                        binding.layout.showEmptyIcon(requireActivity())
                    } else {
                        binding.layout.hideEmptyIcon()
                        data.shoppingLists.forEach { shoppingList ->
                            adapter.add(ShoppingListItem(requireContext(), shoppingList, {
                                ShoppingListActivity.start(requireContext(), shoppingList.id, false)
                            }, {
                                viewModel.delete(it.id).observe(viewLifecycleOwner) { r ->
                                    deleteShoppingListLoadingStateHandler.handle(r)
                                }
                            }, {
                                AddEditShoppingListActivity.start(requireActivity(), it)
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
        inflater.inflate(R.menu.fragment_shopping_lists, menu)
        searchView = menu.findItem(R.id.fragment_shopping_lists_menu_item_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                debounceExecutor.execute { resetShoppingLists() }
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fragment_shopping_lists_menu_item_filter -> {
                ShoppingListsFilterDialog.show(
                    requireActivity().supportFragmentManager,
                    ShoppingListsFilterDialog.Filter(filter?.date),
                    ShoppingListsFilterDialog.OnFilterChanged {
                        filter = it
                        resetShoppingLists()
                    })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe
    fun reloadShoppingListsEvent(event: ReloadShoppingListsEvent) {
        resetShoppingLists()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(FILTER_SAVED_STATE_EXTRA, filter)
    }

}