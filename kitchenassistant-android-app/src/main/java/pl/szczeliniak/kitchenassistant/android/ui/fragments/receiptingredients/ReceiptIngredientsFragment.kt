package pl.szczeliniak.kitchenassistant.android.ui.fragments.receiptingredients

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
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditingredient.AddEditIngredientDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.addingredienttoshoppinglist.AddIngredientToShoppingListDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.fragments.ReceiptActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.listitems.IngredientGroupHeaderItem
import pl.szczeliniak.kitchenassistant.android.ui.listitems.IngredientItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class ReceiptIngredientsFragment : ReceiptActivityFragment() {

    companion object {
        fun create(): ReceiptIngredientsFragment {
            return ReceiptIngredientsFragment()
        }
    }

    private lateinit var binding: FragmentReceiptIngredientsBinding
    private lateinit var deleteIngredientStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    private val viewModel: ReceiptIngredientsFragmentViewModel by viewModels()
    private val ingredientsAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptIngredientsBinding.inflate(inflater)
        binding.recyclerView.adapter = ingredientsAdapter
        binding.buttonAddIngredient.setOnClickListener { showAddIngredientDialog() }
        return binding.root
    }

    private fun showAddIngredientDialog() {
        receipt?.let {
            val group = it.ingredientGroups.first()
            AddEditIngredientDialog.show(
                requireActivity().supportFragmentManager,
                it.id,
                group.id,
                group.name,
                ArrayList(it.ingredientGroups)
            )
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
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadReceiptEvent())
            }
        })
    }

    private fun loadData() {
        receipt?.let { r ->
            ingredientsAdapter.clear()
            if (r.ingredientGroups.flatMap { it.ingredients }.none()) {
                binding.root.showEmptyIcon(requireActivity())
            } else {
                binding.root.hideEmptyIcon()
                r.ingredientGroups.forEach { ingredientGroup ->
                    if (ingredientGroup.ingredients.isNotEmpty()) {
                        ingredientsAdapter.add(IngredientGroupHeaderItem(ingredientGroup))
                        ingredientGroup.ingredients.forEach { ingredient ->
                            ingredientsAdapter.add(IngredientItem(requireContext(), r.id, ingredient, { receiptId, i ->
                                ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                                    viewModel.delete(receiptId, ingredientGroup.id, i.id).observe(viewLifecycleOwner) {
                                        deleteIngredientStateHandler.handle(it)
                                    }
                                }
                            }, { receiptId, i ->
                                AddEditIngredientDialog.show(
                                    requireActivity().supportFragmentManager,
                                    receiptId,
                                    ingredientGroup.id,
                                    ingredientGroup.name,
                                    ArrayList(r.ingredientGroups),
                                    i
                                )
                            }, { _, i ->
                                AddIngredientToShoppingListDialog.show(
                                    requireActivity().supportFragmentManager,
                                    i,
                                    r.id
                                )
                            }))
                        }
                    }
                }
            }
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

}