package pl.szczeliniak.kitchenassistant.android.ui.fragments.recipesteps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentRecipeStepsBinding
import pl.szczeliniak.kitchenassistant.android.events.RecipeChanged
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditstep.AddEditStepDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.fragments.RecipeActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.listitems.StepItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class RecipeStepsFragment : RecipeActivityFragment() {

    companion object {
        fun create(): RecipeStepsFragment {
            return RecipeStepsFragment()
        }
    }

    private lateinit var binding: FragmentRecipeStepsBinding
    private lateinit var deleteStepStateHandler: LoadingStateHandler<Int>

    @Inject
    lateinit var eventBus: EventBus

    private val stepsAdapter = GroupAdapter<GroupieViewHolder>()
    private val viewModel: RecipeStepsFragmentViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRecipeStepsBinding.inflate(inflater)
        binding.recyclerView.adapter = stepsAdapter
        binding.buttonAddStep.setOnClickListener {
            recipe?.let {
                AddEditStepDialog.show(
                    requireActivity().supportFragmentManager,
                    it.id
                )
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        deleteStepStateHandler = prepareDeleteStepLoadingStateHandler()
        loadData()
    }

    private fun prepareDeleteStepLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(requireContext(), object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(requireActivity())
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(RecipeChanged())
            }
        })
    }

    private fun loadData() {
        recipe?.let { r ->
            stepsAdapter.clear()
            if (r.steps.isEmpty()) {
                binding.root.showEmptyIcon(requireActivity())
            } else {
                binding.root.hideEmptyIcon()
                r.steps.forEach { step ->
                    stepsAdapter.add(StepItem(requireContext(), r.id, step, { recipeId, s ->
                        ConfirmationDialog.show(requireActivity().supportFragmentManager) {
                            viewModel.delete(recipeId, s.id).observe(viewLifecycleOwner) {
                                deleteStepStateHandler.handle(it)
                            }
                        }
                    }, { recipeId, s ->
                        AddEditStepDialog.show(requireActivity().supportFragmentManager, recipeId, s)
                    }))
                }
            }
        }
    }

    override fun onRecipeChanged() {
        loadData()
    }

}