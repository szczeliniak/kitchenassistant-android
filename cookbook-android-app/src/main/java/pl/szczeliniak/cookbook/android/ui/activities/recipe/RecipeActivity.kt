package pl.szczeliniak.cookbook.android.ui.activities.recipe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.cookbook.android.R
import pl.szczeliniak.cookbook.android.databinding.ActivityRecipeBinding
import pl.szczeliniak.cookbook.android.network.LoadingStateHandler
import pl.szczeliniak.cookbook.android.network.responses.RecipeResponse
import pl.szczeliniak.cookbook.android.ui.adapters.FragmentPagerAdapter
import pl.szczeliniak.cookbook.android.ui.fragments.RecipeActivityFragment
import pl.szczeliniak.cookbook.android.ui.fragments.recipeinfo.RecipeInfoFragment
import pl.szczeliniak.cookbook.android.ui.fragments.recipeingredients.RecipeIngredientsFragment
import pl.szczeliniak.cookbook.android.ui.fragments.recipesteps.RecipeStepsFragment
import pl.szczeliniak.cookbook.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.cookbook.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
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
    lateinit var recipeActivityViewModelFactory: RecipeActivityViewModel.Factory

    private lateinit var binding: ActivityRecipeBinding
    private val recipeLoadingStateHandler: LoadingStateHandler<RecipeResponse.Recipe> =
        prepareRecipeLoadingStateHandler()
    private val observers = mutableListOf<RecipeActivityFragment>()

    private val viewModel: RecipeActivityViewModel by viewModels {
        RecipeActivityViewModel.provideFactory(
            recipeActivityViewModelFactory,
            recipeId
        )
    }

    var recipe: RecipeResponse.Recipe? = null

    val recipeId get() = intent.getIntExtra(RECIPE_ID_EXTRA, -1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPager()
        viewModel.recipe.observe(this) { recipeLoadingStateHandler.handle(it) }
    }

    private fun prepareRecipeLoadingStateHandler(): LoadingStateHandler<RecipeResponse.Recipe> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<RecipeResponse.Recipe> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@RecipeActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: RecipeResponse.Recipe) {
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

}