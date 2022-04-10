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
import pl.szczeliniak.kitchenassistant.android.events.ReloadShoppingListsEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList
import pl.szczeliniak.kitchenassistant.android.ui.activities.addshoppinglist.AddEditShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.activities.shoppinglist.ShoppingListActivity
import pl.szczeliniak.kitchenassistant.android.ui.listitems.ShoppingListItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ActivityUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ActivityUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ShoppingListsFragment : Fragment() {

    companion object {
        fun create(): ShoppingListsFragment {
            return ShoppingListsFragment()
        }
    }

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
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        )

        binding.buttonAddShoppingList.setOnClickListener { AddEditShoppingListActivity.start(requireContext()) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shoppingListsLoadingStateHandler = prepareShoppingListsLoadingStateHandler()
        deleteShoppingListLoadingStateHandler = prepareDeleteShoppingListLoadingStateHandler()
        viewModel.shoppingLists.observe(viewLifecycleOwner) { shoppingListsLoadingStateHandler.handle(it) }
    }

    private fun prepareDeleteShoppingListLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireActivity(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.layout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.layout.hideProgressSpinner(requireActivity())
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
                binding.layout.hideEmptyIcon()
                binding.layout.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.isRefreshing = false
                binding.layout.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: List<ShoppingList>) {
                adapter.clear()
                if (data.isEmpty()) {
                    binding.layout.showEmptyIcon(requireActivity())
                } else {
                    binding.layout.hideEmptyIcon()
                    data.forEach { shoppingList ->
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
        eventBus.register(this)
    }

    override fun onDestroy() {
        eventBus.unregister(this)
        super.onDestroy()
    }

    @Subscribe
    fun reloadShoppingListsEvent(event: ReloadShoppingListsEvent) {
        viewModel.reloadShoppingLists()
    }

}