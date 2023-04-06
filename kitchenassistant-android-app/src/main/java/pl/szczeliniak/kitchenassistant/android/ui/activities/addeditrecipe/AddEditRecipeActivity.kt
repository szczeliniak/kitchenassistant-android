package pl.szczeliniak.kitchenassistant.android.ui.activities.addeditrecipe

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import pl.aprilapps.easyphotopicker.*
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityAddEditRecipeBinding
import pl.szczeliniak.kitchenassistant.android.events.RecipeSavedEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientGroupRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.AddRecipeRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateRecipeRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.RecipeDetails
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.services.RecipeService
import pl.szczeliniak.kitchenassistant.android.ui.activities.recipe.RecipeActivity
import pl.szczeliniak.kitchenassistant.android.ui.adapters.AuthorDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.adapters.CategoryDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.adapters.TagDropdownArrayAdapter
import pl.szczeliniak.kitchenassistant.android.ui.listitems.PhotoItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatAutoCompleteTextViewUtils.Companion.getTextOrNull
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatEditTextUtils.Companion.getTextOrNull
import pl.szczeliniak.kitchenassistant.android.ui.utils.ChipGroupUtils.Companion.add
import pl.szczeliniak.kitchenassistant.android.ui.utils.ChipGroupUtils.Companion.getTextInChips
import pl.szczeliniak.kitchenassistant.android.ui.utils.ContextUtils.Companion.toast
import pl.szczeliniak.kitchenassistant.android.ui.utils.GroupAdapterUtils.Companion.getItems
import pl.szczeliniak.kitchenassistant.android.ui.utils.ToolbarUtils.Companion.init
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.hideProgressSpinner
import pl.szczeliniak.kitchenassistant.android.ui.utils.ViewGroupUtils.Companion.showProgressSpinner
import java.io.File
import java.net.URI
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddEditRecipeActivity : AppCompatActivity() {

    companion object {
        private const val RECIPE_ID_EXTRA = "RECIPE_ID_EXTRA"

        fun start(context: Context, recipeId: Int? = null) {
            val intent = Intent(context, AddEditRecipeActivity::class.java)
            recipeId?.let { intent.putExtra(RECIPE_ID_EXTRA, it) }
            context.startActivity(intent)
        }
    }

    private val viewModel: AddEditRecipeActivityViewModel by viewModels {
        AddEditRecipeActivityViewModel.provideFactory(addEditRecipeActivityViewModelFactory, recipeId)
    }

    private val saveRecipeLoadingStateHandler = prepareSaveRecipeLoadingStateHandler()
    private val uploadPhotosLoadingStateHandler = prepareUploadPhotosLoadingStateHandler()
    private val loadCategoriesLoadingStateHandler = prepareLoadCategoriesLoadingStateHandler()
    private val loadTagsLoadingStateHandler = prepareLoadTagsLoadingStateHandler()
    private val loadAuthorsLoadingStateHandler = prepareLoadAuthorsLoadingStateHandler()
    private val downloadPhotoFileLoadingStateHandler = prepareDownloadPhotoLoadingStateHandler()
    private val recipeLoadingStateHandler: LoadingStateHandler<RecipeDetails> = prepareRecipeLoadingStateHandler()
    private val photosAdapter = GroupAdapter<GroupieViewHolder>()

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var localStorageService: LocalStorageService

    @Inject
    lateinit var addEditRecipeActivityViewModelFactory: AddEditRecipeActivityViewModel.Factory

    private lateinit var binding: ActivityAddEditRecipeBinding
    private lateinit var categoriesDropdownAdapter: CategoryDropdownArrayAdapter
    private lateinit var tagsArrayAdapter: TagDropdownArrayAdapter
    private lateinit var authorsArrayAdapter: AuthorDropdownArrayAdapter
    private lateinit var easyImage: EasyImage

    private var recipe: RecipeDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tagsArrayAdapter = TagDropdownArrayAdapter(this)
        authorsArrayAdapter = AuthorDropdownArrayAdapter(this)
        categoriesDropdownAdapter = CategoryDropdownArrayAdapter(this)

        initLayout()

        recipeId?.let {
            binding.toolbarLayout.toolbar.init(this@AddEditRecipeActivity, R.string.title_activity_edit_recipe)
        } ?: kotlin.run {
            binding.toolbarLayout.toolbar.init(this@AddEditRecipeActivity, R.string.title_activity_new_recipe)
        }

        viewModel.recipe.observe(this) { recipeLoadingStateHandler.handle(it) }
        viewModel.tags.observe(this) { loadTagsLoadingStateHandler.handle(it) }
        viewModel.authors.observe(this) { loadAuthorsLoadingStateHandler.handle(it) }
        viewModel.categories.observe(this) { loadCategoriesLoadingStateHandler.handle(it) }
    }

    private fun initLayout() {
        binding = ActivityAddEditRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tag.setAdapter(tagsArrayAdapter)
        binding.tag.setOnKeyListener { _, keyCode, event -> onKeyInTagPressed(keyCode, event) }
        binding.tag.setOnItemClickListener { _, _, position, _ ->
            addTagChip(tagsArrayAdapter.getItem(position)!!)
        }

        binding.recipeAuthor.setAdapter(authorsArrayAdapter)
        binding.recipeAuthor.setOnItemClickListener { _, _, position, _ ->
            binding.recipeAuthor.setText(authorsArrayAdapter.getItem(position))
        }

        binding.recipeCategory.adapter = categoriesDropdownAdapter

        binding.recipeName.doOnTextChanged { _, _, _, _ ->
            if (name.isNullOrEmpty()) {
                binding.recipeNameLayout.error = getString(R.string.message_recipe_name_is_empty)
            } else {
                binding.recipeNameLayout.error = null
            }
        }

        easyImage = EasyImage.Builder(this)
            .setChooserType(ChooserType.CAMERA_AND_GALLERY)
            .allowMultiple(true)
            .build()

        binding.buttonAddPhotos.setOnClickListener { easyImage.openChooser(this) }

        binding.photosRecyclerView.adapter = photosAdapter
    }

    private fun onKeyInTagPressed(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN
            || keyCode != KeyEvent.KEYCODE_ENTER
            || binding.tag.text.toString().isBlank()
        ) {
            return false
        }
        addTagChip(binding.tag.text.toString())
        return true
    }

    private fun addTagChip(item: String) {
        binding.tagChips.add(layoutInflater, item, true)
        binding.tag.setText("")
    }

    private fun prepareSaveRecipeLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddEditRecipeActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                if (recipeId == null) {
                    RecipeActivity.start(this@AddEditRecipeActivity, data)
                }
                eventBus.post(RecipeSavedEvent())
                finish()
            }
        })
    }

    private fun prepareUploadPhotosLoadingStateHandler(): LoadingStateHandler<List<Int>> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<List<Int>> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddEditRecipeActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: List<Int>) {
                recipeId?.let {
                    val photoIds =
                        ArrayList(photosAdapter.getItems<PhotoItem>().filter { photo -> photo.fileId != null }
                            .map { photo -> photo.fileId!! })
                    photoIds.addAll(data)
                    viewModel.updateRecipe(
                        it,
                        UpdateRecipeRequest(
                            name!!,
                            author,
                            url,
                            description,
                            categoryId,
                            tags,
                            photoIds
                        )
                    )
                        .observe(this@AddEditRecipeActivity) { saveRecipeLoadingStateHandler.handle(it) }
                } ?: kotlin.run {
                    viewModel.addRecipe(
                        AddRecipeRequest(
                            name!!,
                            author,
                            url,
                            description,
                            localStorageService.getId(),
                            categoryId,
                            tags,
                            data,
                            Collections.singletonList(AddIngredientGroupRequest("default"))
                        )
                    ).observe(this@AddEditRecipeActivity) { saveRecipeLoadingStateHandler.handle(it) }
                }
            }
        })
    }

    private fun prepareLoadCategoriesLoadingStateHandler(): LoadingStateHandler<List<Category>> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<List<Category>> {
            override fun onSuccess(data: List<Category>) {
                categoriesDropdownAdapter.clear()
                categoriesDropdownAdapter.add(null)
                categoriesDropdownAdapter.addAll(data)
                setCurrentCategory()
            }
        })
    }

    private fun setCurrentCategory() {
        recipe?.category?.let { category ->
            categoriesDropdownAdapter.getPositionById(category.id)?.let { position ->
                if (binding.recipeCategory.adapter.count > position) {
                    binding.recipeCategory.setSelection(position)
                }
            }
        }
    }

    private fun prepareLoadTagsLoadingStateHandler(): LoadingStateHandler<List<String>> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<List<String>> {
            override fun onSuccess(data: List<String>) {
                tagsArrayAdapter.refresh(data)
            }
        })
    }

    private fun prepareLoadAuthorsLoadingStateHandler(): LoadingStateHandler<List<String>> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<List<String>> {
            override fun onSuccess(data: List<String>) {
                authorsArrayAdapter.refresh(data)
            }
        })
    }

    private fun prepareDownloadPhotoLoadingStateHandler(): LoadingStateHandler<RecipeService.DownloadedPhoto> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<RecipeService.DownloadedPhoto> {
            override fun onSuccess(data: RecipeService.DownloadedPhoto) {
                photosAdapter.add(PhotoItem(this@AddEditRecipeActivity, data.file.toUri(), data.fileId) { item ->
                    photosAdapter.remove(item)
                })
            }
        })
    }

    private fun prepareRecipeLoadingStateHandler(): LoadingStateHandler<RecipeDetails> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<RecipeDetails> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddEditRecipeActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: RecipeDetails) {
                recipe = data
                fillRecipe()
            }
        })
    }

    private fun fillRecipe() {
        recipe?.let {
            binding.toolbarLayout.toolbar.init(this@AddEditRecipeActivity, it.name)
            binding.recipeName.setText(it.name)
            binding.recipeDescription.setText(it.description)
            binding.recipeAuthor.setText(it.author)
            binding.recipeUrl.setText(it.source)
            it.tags.forEach { tag -> addTagChip(tag) }
            it.photos.forEach { photo ->
                viewModel.loadPhoto(photo).observe(this@AddEditRecipeActivity) { downloadedPhoto ->
                    downloadPhotoFileLoadingStateHandler.handle(downloadedPhoto)
                }
            }
            setCurrentCategory()
        }
    }

    private fun saveRecipe() {
        if (name.isNullOrEmpty()) {
            return
        }

        viewModel.uploadPhotos(photosAdapter.getItems<PhotoItem>()
            .filter { it.fileId == null }
            .map { File(URI.create(it.uri.toString())) })
            .observe(this) {
                uploadPhotosLoadingStateHandler.handle(it)
            }
    }

    private val name: String?
        get() {
            return binding.recipeName.getTextOrNull()
        }

    private val author: String?
        get() {
            return binding.recipeAuthor.getTextOrNull()
        }

    private val url: String?
        get() {
            return binding.recipeUrl.getTextOrNull()
        }

    private val description: String?
        get() {
            return binding.recipeDescription.getTextOrNull()
        }

    private val categoryId: Int?
        get() {
            val position = binding.recipeCategory.selectedItemPosition
            return if (position == 0) null else categoriesDropdownAdapter.getItem(position)?.id
        }

    private val tags: List<String>
        get() {
            return binding.tagChips.getTextInChips()
        }

    private val recipeId: Int?
        get() {
            val id = intent.getIntExtra(RECIPE_ID_EXTRA, -1)
            return if (id != -1) id else null
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_add_recipe, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            saveRecipe()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                data?.let {
                    UCrop.getOutput(data)?.let {
                        photosAdapter.add(
                            PhotoItem(
                                this@AddEditRecipeActivity,
                                it
                            ) { item -> photosAdapter.remove(item) })
                    }
                }
                return
            } else if (resultCode == UCrop.RESULT_ERROR) {
                this@AddEditRecipeActivity.toast(R.string.message_image_crop_error)
                return
            }
        }

        easyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                imageFiles.forEach {
                    val uri = it.file.toUri()
                    val uriParts = uri.toString().split(File.separator)
                    val fileName = uriParts[uriParts.size - 1]
                    UCrop.of(uri, Uri.fromFile(File(cacheDir, fileName)))
                        .withAspectRatio(1F, 1F)
                        .start(this@AddEditRecipeActivity)
                }
            }
        })
    }

}