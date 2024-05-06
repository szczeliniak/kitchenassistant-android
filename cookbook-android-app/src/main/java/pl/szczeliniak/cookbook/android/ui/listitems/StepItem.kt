package pl.szczeliniak.cookbook.android.ui.listitems

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.cookbook.android.R
import pl.szczeliniak.cookbook.android.databinding.ListItemStepBinding
import pl.szczeliniak.cookbook.android.network.responses.RecipeResponse

class StepItem(
    private val index: Int,
    private val step: RecipeResponse.Recipe.StepGroup.Step,
) :
    BindableItem<ListItemStepBinding>() {

    override fun bind(binding: ListItemStepBinding, position: Int) {
        binding.stepDescription.text = String.format("%s. %s", index + 1, step.description)
    }

    override fun getLayout(): Int {
        return R.layout.list_item_step
    }

    override fun initializeViewBinding(view: View): ListItemStepBinding {
        return ListItemStepBinding.bind(view)
    }

}