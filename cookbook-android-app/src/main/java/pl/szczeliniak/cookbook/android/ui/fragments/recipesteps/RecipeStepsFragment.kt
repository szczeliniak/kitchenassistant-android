package pl.szczeliniak.cookbook.android.ui.fragments.recipesteps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.cookbook.android.databinding.FragmentRecipeStepsBinding
import pl.szczeliniak.cookbook.android.ui.fragments.RecipeActivityFragment
import pl.szczeliniak.cookbook.android.ui.listitems.GroupHeaderItem
import pl.szczeliniak.cookbook.android.ui.listitems.StepItem
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon

@AndroidEntryPoint
class RecipeStepsFragment : RecipeActivityFragment() {

    companion object {
        fun create(): RecipeStepsFragment {
            return RecipeStepsFragment()
        }
    }

    private lateinit var binding: FragmentRecipeStepsBinding

    private val stepsAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRecipeStepsBinding.inflate(inflater)
        binding.recyclerView.adapter = stepsAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadData()
    }

    private fun loadData() {
        recipe?.let { r ->
            stepsAdapter.clear()
            if (r.stepGroups.flatMap { it.steps }.none()) {
                binding.root.showEmptyIcon(requireActivity())
            } else {
                binding.root.hideEmptyIcon()
                r.stepGroups.forEach { stepGroup ->
                    if (stepGroup.steps.isNotEmpty()) {
                        stepGroup.name?.let { stepsAdapter.add(GroupHeaderItem(it)) }
                        stepGroup.steps.forEachIndexed { index, step -> stepsAdapter.add(StepItem(index, step)) }
                    }
                }
            }
        }
    }

    override fun onRecipeChanged() {
        loadData()
    }

}