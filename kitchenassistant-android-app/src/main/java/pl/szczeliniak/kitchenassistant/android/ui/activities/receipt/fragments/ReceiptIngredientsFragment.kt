package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptIngredientsBinding
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.dialogs.addingredient.AddIngredientDialog
import pl.szczeliniak.kitchenassistant.android.ui.listitems.IngredientItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.showEmptyIcon

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ReceiptIngredientsFragment : ReceiptActivityFragment() {

    private lateinit var binding: FragmentReceiptIngredientsBinding

    private val ingredientsAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptIngredientsBinding.inflate(inflater)
        binding.fragmentReceiptIngredientsRecyclerView.adapter = ingredientsAdapter
        binding.fragmentReceiptIngredientsFabAddIngredient.setOnClickListener { showAddIngredientDialog() }
        loadData()
        return binding.root
    }

    private fun showAddIngredientDialog() {
        receipt?.let {
            AddIngredientDialog.newInstance(it.id)
                .show(requireActivity().supportFragmentManager, AddIngredientDialog.TAG)
        }
    }

    private fun loadData() {
        receipt?.let { r ->
            ingredientsAdapter.clear()
            if (r.ingredients.isEmpty()) {
                binding.root.showEmptyIcon(requireActivity())
            } else {
                r.ingredients.forEach {
                    ingredientsAdapter.add(IngredientItem(it))
                }
            }
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

}