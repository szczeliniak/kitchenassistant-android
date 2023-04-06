package pl.szczeliniak.kitchenassistant.android.ui.activities.dayplan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityDayPlanBinding
import pl.szczeliniak.kitchenassistant.android.events.DayPlanDeletedEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanDetails
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanIngredientGroupHeaderItem
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanIngredientItem
import pl.szczeliniak.kitchenassistant.android.ui.listitems.DayPlanRecipeHeaderItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import pl.szczeliniak.kitchenassistant.android.utils.LocalDateUtils
import javax.inject.Inject

@AndroidEntryPoint
class DayPlanActivity : AppCompatActivity() {

    companion object {
        private const val DAY_PLAN_ID_EXTRA = "DAY_PLAN_ID_EXTRA"

        fun start(context: Context, dayPlanId: Int) {
            val intent = Intent(context, DayPlanActivity::class.java)
            intent.putExtra(DAY_PLAN_ID_EXTRA, dayPlanId)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var factory: DayPlanActivityViewModel.Factory

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: ActivityDayPlanBinding
    private val recipesAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var recipesLoadingStateHandler: LoadingStateHandler<DayPlanDetails>
    private lateinit var deleteRecipeLoadingStateHandler: LoadingStateHandler<Int>

    private var numberOfRecipesForDayPlan = 0

    private val viewModel: DayPlanActivityViewModel by viewModels {
        DayPlanActivityViewModel.provideFactory(factory, dayPlanId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayPlanBinding.inflate(layoutInflater)
        binding.recyclerView.adapter = recipesAdapter
        recipesLoadingStateHandler = prepareRecipesLoadingStateHandler()
        deleteRecipeLoadingStateHandler = prepareDeleteRecipeLoadingStateHandler()
        viewModel.dayPlan.observe(this) { recipesLoadingStateHandler.handle(it) }
        setContentView(binding.root)
    }

    private fun prepareRecipesLoadingStateHandler(): LoadingStateHandler<DayPlanDetails> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<DayPlanDetails> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@DayPlanActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: DayPlanDetails) {
                binding.toolbarLayout.toolbar.init(this@DayPlanActivity, LocalDateUtils.stringify(data.date) ?: "")

                recipesAdapter.clear()
                if (data.recipes.isEmpty()) {
                    binding.root.showEmptyIcon(this@DayPlanActivity)
                } else {
                    binding.root.hideEmptyIcon()
                }
                numberOfRecipesForDayPlan = data.recipes.size
                data.recipes.forEach { recipe ->
                    recipesAdapter.add(DayPlanRecipeHeaderItem(recipe, this@DayPlanActivity) { recipeId ->
                        viewModel.deleteRecipe(data.id, recipeId).observe(this@DayPlanActivity) {
                            deleteRecipeLoadingStateHandler.handle(it)
                        }
                    })
                    recipe.ingredientGroups.forEach { ingredientGroup ->
                        if (ingredientGroup.ingredients.isNotEmpty()) {
                            recipesAdapter.add(DayPlanIngredientGroupHeaderItem(ingredientGroup))
                            ingredientGroup.ingredients.forEach { ingredient ->
                                recipesAdapter.add(DayPlanIngredientItem(ingredient))
                            }
                        }
                    }
                }
            }
        })
    }

    private fun prepareDeleteRecipeLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@DayPlanActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                if (numberOfRecipesForDayPlan > 1) {
                    viewModel.reload()
                } else {
                    eventBus.post(DayPlanDeletedEvent())
                    finish()
                }
            }
        })
    }

    private val dayPlanId: Int
        get() {
            return intent.getIntExtra(DAY_PLAN_ID_EXTRA, -1)
        }

}