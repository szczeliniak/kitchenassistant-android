package pl.szczeliniak.kitchenassistant.android.ui.fragments.shoppinglists

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
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentShoppingListsBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListsEvent
import pl.szczeliniak.kitchenassistant.android.listeners.EndlessScrollRecyclerViewListener
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListsResponse
import pl.szczeliniak.kitchenassistant.android.ui.activities.addshoppinglist.AddEditShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.ShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.shoppinglistsfilter.ShoppingListsFilterDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.ShoppingListItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ActivityUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ActivityUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ShoppingListsFragment : Fragment() {

    companion object {
        private const val DEFAULT_PAGE = 1

        fun create(): ShoppingListsFragment {
            return ShoppingListsFragment()
        }
    }

    private val viewModel: ShoppingListsFragmentViewModel by viewModels()
    private val adapter = GroupAdapter<GroupieViewHolder>()

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: FragmentShoppingListsBinding
    private lateinit var shoppingListsLoadingStateHandler: LoadingStateHandler<ShoppingListsResponse>
    private lateinit var deleteShoppingListLoadingStateHandler: LoadingStateHandler<Int>

    private var filter: ShoppingListsFilterDialog.Filter? = null
    private var page: Int = DEFAULT_PAGE
    private var maxPage: Int = DEFAULT_PAGE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentShoppingListsBinding.inflate(inflater)

        binding.root.setOnRefreshListener { resetShoppingLists() }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )
        binding.recyclerView.addOnScrollListener(EndlessScrollRecyclerViewListener(
            binding.recyclerView.layoutManager as LinearLayoutManager,
            {
                page += 1
                viewModel.reloadShoppingLists(page, filter?.name, filter?.date)
            }
        ) { !binding.root.isRefreshing && page < maxPage })

        binding.buttonAddShoppingList.setOnClickListener { AddEditShoppingListActivity.start(requireContext()) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shoppingListsLoadingStateHandler = prepareShoppingListsLoadingStateHandler()
        deleteShoppingListLoadingStateHandler = prepareDeleteShoppingListLoadingStateHandler()
        viewModel.shoppingLists.observe(viewLifecycleOwner) { shoppingListsLoadingStateHandler.handle(it) }
        viewModel.filter.observe(viewLifecycleOwner) {
            this.filter = it
            resetShoppingLists()
        }
    }

    private fun resetShoppingLists() {
        adapter.clear()
        page = DEFAULT_PAGE
        viewModel.reloadShoppingLists(page, filter?.name, filter?.date)
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
                    maxPage = data.pagination.numberOfPages
                    if (data.shoppingLists.isEmpty()) {
                        binding.layout.showEmptyIcon(requireActivity())
                    } else {
                        binding.layout.hideEmptyIcon()
                        data.shoppingLists.forEach { shoppingList ->
                            adapter.add(ShoppingListItem(requireContext(), shoppingList, {
                                ShoppingListActivity.start(requireContext(), shoppingList.id)
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
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fragment_shopping_lists_menu_item_filter -> {
                ShoppingListsFilterDialog.show(
                    requireActivity().supportFragmentManager,
                    ShoppingListsFilterDialog.Filter(filter?.name, filter?.date),
                    ShoppingListsFilterDialog.OnFilterChanged { viewModel.changeFilter(it) })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe
    fun reloadShoppingListsEvent(event: ReloadShoppingListsEvent) {
        resetShoppingLists()
    }

}