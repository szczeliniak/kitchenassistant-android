package pl.szczeliniak.cookbook.android.ui.activities.dayplan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.szczeliniak.cookbook.android.databinding.ActivityDayPlanBinding
import pl.szczeliniak.cookbook.android.events.DayPlanEditedEvent
import pl.szczeliniak.cookbook.android.network.LoadingStateHandler
import pl.szczeliniak.cookbook.android.network.responses.DayPlanResponse
import pl.szczeliniak.cookbook.android.ui.activities.recipe.RecipeActivity
import pl.szczeliniak.cookbook.android.ui.listitems.DayPlanIngredientItem
import pl.szczeliniak.cookbook.android.ui.listitems.DayPlanRecipeHeaderItem
import pl.szczeliniak.cookbook.android.ui.listitems.GroupHeaderItem
import pl.szczeliniak.cookbook.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.hideEmptyIcon
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.showEmptyIcon
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import pl.szczeliniak.cookbook.android.utils.LocalDateUtils
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class DayPlanActivity : AppCompatActivity() {

    companion object {
        private const val DAY_PLAN_DATE_EXTRA = "DAY_PLAN_DATE_EXTRA"
        private const val DAY_PLAN_ALLOW_TO_EDIT = "DAY_PLAN_ALLOW_TO_EDIT_EXTRA"

        fun start(context: Context, date: LocalDate, allowToEdit: Boolean = true) {
            val intent = Intent(context, DayPlanActivity::class.java)
            intent.putExtra(DAY_PLAN_DATE_EXTRA, date)
            intent.putExtra(DAY_PLAN_ALLOW_TO_EDIT, allowToEdit)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var factory: DayPlanActivityViewModel.Factory

    @Inject
    lateinit var eventBus: EventBus

    private lateinit var binding: ActivityDayPlanBinding
    private val recipesAdapter = GroupAdapter<GroupieViewHolder>()
    private val recipesLoadingStateHandler = recipesLoadingStateHandler()
    private val changeIngredientStateLoadingStateHandler = changeIngredientStateLoadingStateHandler()
    private val deleteRecipeLoadingStateHandler = deleteRecipeLoadingStateHandler()

    private var numberOfRecipesForDayPlan = 0

    private val viewModel: DayPlanActivityViewModel by viewModels {
        DayPlanActivityViewModel.provideFactory(factory, date)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayPlanBinding.inflate(layoutInflater)
        binding.recyclerView.adapter = recipesAdapter
        viewModel.dayPlan.observe(this) { recipesLoadingStateHandler.handle(it) }
        setContentView(binding.root)
    }

    private fun recipesLoadingStateHandler(): LoadingStateHandler<DayPlanResponse.DayPlan> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<DayPlanResponse.DayPlan> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@DayPlanActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: DayPlanResponse.DayPlan) {
                binding.toolbarLayout.toolbar.init(this@DayPlanActivity, LocalDateUtils.stringify(data.date) ?: "")

                recipesAdapter.clear()
                if (data.recipes.isEmpty()) {
                    binding.root.showEmptyIcon(this@DayPlanActivity)
                } else {
                    binding.root.hideEmptyIcon()
                }
                numberOfRecipesForDayPlan = data.recipes.size
                data.recipes.forEach { recipe ->
                    recipesAdapter.add(
                        DayPlanRecipeHeaderItem(
                            recipe,
                            supportFragmentManager,
                            {
                                RecipeActivity.start(this@DayPlanActivity, it)
                            },
                            { recipeId ->
                                viewModel.deleteRecipe(data.date, recipeId).observe(this@DayPlanActivity) {
                                    deleteRecipeLoadingStateHandler.handle(it)
                                }
                            }
                        )
                    )
                    recipe.ingredientGroups.forEach { ingredientGroup ->
                        if (ingredientGroup.ingredients.isNotEmpty()) {
                            ingredientGroup.name?.let { recipesAdapter.add(GroupHeaderItem(it)) }
                            ingredientGroup.ingredients.forEach { ingredient ->
                                recipesAdapter.add(
                                    DayPlanIngredientItem(
                                        ingredient,
                                        data.date,
                                        recipe.id,
                                        ingredientGroup.id
                                    ) { date, recipeId, ingredientGroupId, ingredientId, state ->
                                        viewModel.changeIngredientState(
                                            date,
                                            recipeId,
                                            ingredientGroupId,
                                            ingredientId,
                                            state
                                        ).observe(this@DayPlanActivity) {
                                            changeIngredientStateLoadingStateHandler.handle(it)
                                        }
                                    })
                            }
                        }
                    }
                }
            }
        })
    }

    private fun deleteRecipeLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@DayPlanActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(DayPlanEditedEvent())
                if (numberOfRecipesForDayPlan > 1) {
                    viewModel.reload()
                } else {
                    finish()
                }
            }
        })
    }

    private fun changeIngredientStateLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@DayPlanActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

        })
    }

    private val date: LocalDate
        get() {
            return intent.getSerializableExtra(DAY_PLAN_DATE_EXTRA, LocalDate::class.java)
                ?: throw IllegalArgumentException("Dayplan id cannot be null")
        }

}