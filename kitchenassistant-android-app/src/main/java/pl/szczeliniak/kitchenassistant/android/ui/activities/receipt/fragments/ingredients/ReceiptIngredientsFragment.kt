package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.ingredients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptIngredientsBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.dialogs.addingredient.AddIngredientDialog
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.ReceiptActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.listitems.IngredientItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ReceiptIngredientsFragment : ReceiptActivityFragment() {

    private lateinit var binding: FragmentReceiptIngredientsBinding
    private lateinit var deleteIngredientStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: ReceiptIngredientsFragmentViewModel by viewModels()
    private val ingredientsAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptIngredientsBinding.inflate(inflater)
        binding.fragmentReceiptIngredientsRecyclerView.adapter = ingredientsAdapter
        binding.fragmentReceiptIngredientsFabAddIngredient.setOnClickListener { showAddIngredientDialog() }
        return binding.root
    }

    private fun showAddIngredientDialog() {
        receipt?.let {
            AddIngredientDialog.newInstance(it.id)
                .show(requireActivity().supportFragmentManager, AddIngredientDialog.TAG)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        deleteIngredientStateHandler = prepareDeleteIngredientLoadingStateHandler()
        loadData()
    }

    private fun prepareDeleteIngredientLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireContext(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner(requireActivity())
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadReceiptEvent())
            }
        })
    }

    private fun loadData() {
        receipt?.let { r ->
            ingredientsAdapter.clear()
            if (r.ingredients.isEmpty()) {
                binding.root.showEmptyIcon(requireActivity())
            } else {
                binding.root.hideEmptyIcon()
                r.ingredients.forEach { ingredient ->
                    ingredientsAdapter.add(IngredientItem(requireContext(), r.id, ingredient) { receiptId, i ->
                        viewModel.delete(receiptId, i.id).observe(viewLifecycleOwner) {
                            deleteIngredientStateHandler.handle(it)
                        }
                    })
                }
            }
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

}