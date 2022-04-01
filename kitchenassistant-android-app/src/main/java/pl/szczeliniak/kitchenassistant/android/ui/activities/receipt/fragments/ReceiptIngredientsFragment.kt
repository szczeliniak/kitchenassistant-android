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
import pl.szczeliniak.kitchenassistant.android.ui.listitems.IngredientItem

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ReceiptIngredientsFragment : ReceiptActivityFragment() {

    private lateinit var binding: FragmentReceiptIngredientsBinding

    private val ingredientsAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptIngredientsBinding.inflate(inflater)
        binding.fragmentReceiptIngredientsRecyclerView.adapter = ingredientsAdapter
        loadData()
        return binding.root
    }

    private fun loadData() {
        receipt?.let { r ->
            ingredientsAdapter.clear()
            r.ingredients.forEach {
                ingredientsAdapter.add(IngredientItem(it))
            }
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

}