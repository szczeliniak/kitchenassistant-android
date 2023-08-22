package pl.szczeliniak.kitchenassistant.android.ui.fragments

import androidx.fragment.app.Fragment
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipeResponse
import pl.szczeliniak.kitchenassistant.android.ui.activities.recipe.RecipeActivity

abstract class RecipeActivityFragment : Fragment() {

    val recipe: RecipeResponse.Recipe?
        get() {
            val activity = requireActivity()
            if (activity is RecipeActivity) {
                return activity.recipe
            }
            return null
        }

    override fun onStart() {
        registerForRecipesChanges()
        super.onStart()
    }

    private fun registerForRecipesChanges() {
        val activity = requireActivity()
        if (activity is RecipeActivity) {
            return activity.addChangesObserver(this)
        }
    }

    override fun onStop() {
        unregisterForRecipesChanges()
        super.onStop()
    }

    private fun unregisterForRecipesChanges() {
        val activity = requireActivity()
        if (activity is RecipeActivity) {
            return activity.removeChangesObserver(this)
        }
    }

    abstract fun onRecipeChanged()

}