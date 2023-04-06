package pl.szczeliniak.kitchenassistant.android.ui.activities.recipe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityRecipeBinding
import pl.szczeliniak.kitchenassistant.android.events.RecipeSavedEvent
import pl.szczeliniak.kitchenassistant.android.events.RecipeDeletedEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.RecipeDetails
import pl.szczeliniak.kitchenassistant.android.ui.activities.addeditrecipe.AddEditRecipeActivity
import pl.szczeliniak.kitchenassistant.android.ui.adapters.FragmentPagerAdapter
import pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation.ConfirmationDialog
import pl.szczeliniak.kitchenassistant.android.ui.fragments.RecipeActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.fragments.recipeinfo.RecipeInfoFragment
import pl.szczeliniak.kitchenassistant.android.ui.fragments.recipeingredients.RecipeIngredientsFragment
import pl.szczeliniak.kitchenassistant.android.ui.fragments.recipesteps.RecipeStepsFragment
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import javax.inject.Inject

@AndroidEntryPoint
class RecipeActivity : AppCompatActivity() {

    companion object {
        private const val RECIPE_ID_EXTRA = "RECIPE_ID_EXTRA"

        fun start(context: Context, recipeId: Int) {
            val intent = Intent(context, RecipeActivity::class.java)
            intent.putExtra(RECIPE_ID_EXTRA, recipeId)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var recipeActivityViewModelFactory: RecipeActivityViewModel.Factory

    private lateinit var binding: ActivityRecipeBinding
    private val recipeLoadingStateHandler: LoadingStateHandler<RecipeDetails> = prepareRecipeLoadingStateHandler()
    private val deleteRecipeLoadingStateHandler: LoadingStateHandler<Int> = deleteRecipesLoadingStateHandler()
    private val observers = mutableListOf<RecipeActivityFragment>()

    private val viewModel: RecipeActivityViewModel by viewModels {
        RecipeActivityViewModel.provideFactory(
            recipeActivityViewModelFactory,
            recipeId
        )
    }

    var recipe: RecipeDetails? = null

    val recipeId get() = intent.getIntExtra(RECIPE_ID_EXTRA, -1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPager()
        viewModel.recipe.observe(this) { recipeLoadingStateHandler.handle(it) }
        eventBus.register(this)
    }

    private fun prepareRecipeLoadingStateHandler(): LoadingStateHandler<RecipeDetails> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<RecipeDetails> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@RecipeActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: RecipeDetails) {
                binding.toolbarLayout.toolbar.init(this@RecipeActivity, data.name)
                recipe = data
                observers.forEach { it.onRecipeChanged() }
            }
        })
    }

    private fun initPager() {
        binding.viewPager.adapter = FragmentPagerAdapter(
            arrayOf(
                RecipeInfoFragment.create(), RecipeIngredientsFragment.create(), RecipeStepsFragment.create()
            ), supportFragmentManager, lifecycle
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val nameId = when (position) {
                0 -> {
                    R.string.title_fragment_recipe_info
                }

                1 -> {
                    R.string.title_fragment_recipe_ingredients
                }

                2 -> {
                    R.string.title_fragment_recipe_steps
                }

                else -> {
                    throw UnsupportedOperationException()
                }
            }
            tab.text = getString(nameId)
        }.attach()
    }

    fun addChangesObserver(fragment: RecipeActivityFragment) {
        observers.add(fragment)
    }

    fun removeChangesObserver(fragment: RecipeActivityFragment) {
        observers.remove(fragment)
    }

    override fun onDestroy() {
        eventBus.unregister(this)
        super.onDestroy()
    }

    private fun deleteRecipesLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@RecipeActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(RecipeDeletedEvent())
                viewModel.reload()
                finish()
            }
        })
    }

    @Subscribe
    fun onRecipeSaved(event: RecipeSavedEvent) {
        viewModel.reload()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_recipe, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit -> {
                AddEditRecipeActivity.start(this, recipeId)
            }

            R.id.delete -> {
                ConfirmationDialog.show(supportFragmentManager) {
                    viewModel.delete(recipeId).observe(this) { deleteRecipeLoadingStateHandler.handle(it) }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

}