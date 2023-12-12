package pl.szczeliniak.kitchenassistant.android.ui.activities.dayplan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityDayPlanBinding
import pl.szczeliniak.kitchenassistant.android.events.DayPlanDeletedEvent
import pl.szczeliniak.kitchenassistant.android.events.DayPlanEditedEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlanResponse
import pl.szczeliniak.kitchenassistant.android.ui.activities.recipe.RecipeActivity
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.updatedayplan.UpdateDayPlanDialog
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
        private const val DAY_PLAN_ALLOW_TO_EDIT = "DAY_PLAN_ALLOW_TO_EDIT_EXTRA"

        fun start(context: Context, id: Int, allowToEdit: Boolean = true) {
            val intent = Intent(context, DayPlanActivity::class.java)
            intent.putExtra(DAY_PLAN_ID_EXTRA, id)
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
    private val deleteRecipeLoadingStateHandler = deleteRecipeLoadingStateHandler()
    private val deleteDayPlanLoadingStateHandler = deleteDayPlanLoadingStateHandler()
    private val changeIngredientStateLoadingStateHandler = changeIngredientStateLoadingStateHandler()

    private var numberOfRecipesForDayPlan = 0

    private val viewModel: DayPlanActivityViewModel by viewModels {
        DayPlanActivityViewModel.provideFactory(factory, dayPlanId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayPlanBinding.inflate(layoutInflater)
        binding.recyclerView.adapter = recipesAdapter
        viewModel.dayPlan.observe(this) { recipesLoadingStateHandler.handle(it) }
        setContentView(binding.root)
        eventBus.register(this)
    }

    override fun onDestroy() {
        eventBus.unregister(this)
        super.onDestroy()
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
                    recipesAdapter.add(DayPlanRecipeHeaderItem(recipe,
                        supportFragmentManager, {
                            RecipeActivity.start(this@DayPlanActivity, it)
                        },
                        if (isEditAllowed) {
                            { recipeId ->
                                viewModel.deleteRecipe(data.id, recipeId).observe(this@DayPlanActivity) {
                                    deleteRecipeLoadingStateHandler.handle(it)
                                }
                            }
                        } else {
                            null
                        }))
                    recipe.ingredientGroups.forEach { ingredientGroup ->
                        if (ingredientGroup.ingredients.isNotEmpty()) {
                            recipesAdapter.add(DayPlanIngredientGroupHeaderItem(ingredientGroup))
                            ingredientGroup.ingredients.forEach { ingredient ->
                                recipesAdapter.add(
                                    DayPlanIngredientItem(
                                        ingredient,
                                        data.id,
                                        recipe.id,
                                        ingredientGroup.id, if (isEditAllowed) {
                                            { dayPlanId, recipeId, ingredientGroupId, ingredientId, state ->
                                                viewModel.changeIngredientState(
                                                    dayPlanId,
                                                    recipeId,
                                                    ingredientGroupId,
                                                    ingredientId,
                                                    state
                                                ).observe(this@DayPlanActivity) {
                                                    changeIngredientStateLoadingStateHandler.handle(it)
                                                }
                                            }
                                        } else {
                                            null
                                        }
                                    ))
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

    private fun deleteDayPlanLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@DayPlanActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(DayPlanDeletedEvent())
                finish()
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

            override fun onSuccess(data: Int) {
            }
        })
    }

    private val dayPlanId: Int
        get() {
            val id = intent.getIntExtra(DAY_PLAN_ID_EXTRA, -1)
            if (id < 0) {
                throw IllegalArgumentException("Dayplan id cannot be null")
            }
            return id
        }

    private val isEditAllowed: Boolean
        get() {
            return intent.getBooleanExtra(DAY_PLAN_ALLOW_TO_EDIT, true)
        }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (isEditAllowed) {
            menuInflater.inflate(R.menu.activity_day_plan, menu)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                ConfirmationDialog.show(supportFragmentManager) {
                    viewModel.delete(dayPlanId).observe(this) {
                        deleteDayPlanLoadingStateHandler.handle(it)
                    }
                }
            }

            R.id.edit -> {
                UpdateDayPlanDialog.show(supportFragmentManager, dayPlanId)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe
    fun onDayPlanEdited(event: DayPlanEditedEvent) {
        viewModel.reload()
    }

}