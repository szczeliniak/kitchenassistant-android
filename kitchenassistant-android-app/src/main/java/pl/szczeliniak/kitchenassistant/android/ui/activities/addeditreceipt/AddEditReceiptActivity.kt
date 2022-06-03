package pl.szczeliniak.kitchenassistant.android.ui.activities.addeditreceipt

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
import pl.szczeliniak.kitchenassistant.android.databinding.ActivityAddEditReceiptBinding
import pl.szczeliniak.kitchenassistant.android.events.ReloadReceiptsEvent
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.network.requests.AddReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateReceiptRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.services.FileService
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.ReceiptActivity
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
import javax.inject.Inject

@AndroidEntryPoint
class AddEditReceiptActivity : AppCompatActivity() {

    companion object {
        private const val RECEIPT_EXTRA = "RECEIPT_EXTRA"

        fun start(context: Context, receipt: Receipt? = null) {
            val intent = Intent(context, AddEditReceiptActivity::class.java)
            receipt?.let { intent.putExtra(RECEIPT_EXTRA, it) }
            context.startActivity(intent)
        }
    }

    private val viewModel: AddEditReceiptActivityViewModel by viewModels()
    private val saveReceiptLoadingStateHandler = prepareSaveReceiptLoadingStateHandler()
    private val uploadPhotosLoadingStateHandler = prepareUploadPhotosLoadingStateHandler()
    private val loadCategoriesLoadingStateHandler = prepareLoadCategoriesLoadingStateHandler()
    private val loadTagsLoadingStateHandler = prepareLoadTagsLoadingStateHandler()
    private val loadAuthorsLoadingStateHandler = prepareLoadAuthorsLoadingStateHandler()
    private val downloadPhotoFileLoadingStateHandler = prepareDownloadPhotoLoadingStateHandler()
    private val photosAdapter = GroupAdapter<GroupieViewHolder>()

    @Inject
    lateinit var localStorageService: LocalStorageService

    @Inject
    lateinit var eventBus: EventBus
    private lateinit var binding: ActivityAddEditReceiptBinding
    private lateinit var categoriesDropdownAdapter: CategoryDropdownArrayAdapter
    private lateinit var tagsArrayAdapter: TagDropdownArrayAdapter
    private lateinit var authorsArrayAdapter: AuthorDropdownArrayAdapter
    private lateinit var easyImage: EasyImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tagsArrayAdapter = TagDropdownArrayAdapter(this)
        authorsArrayAdapter = AuthorDropdownArrayAdapter(this)
        categoriesDropdownAdapter = CategoryDropdownArrayAdapter(this)

        initLayout()

        viewModel.tags.observe(this) { loadTagsLoadingStateHandler.handle(it) }
        viewModel.authors.observe(this) { loadAuthorsLoadingStateHandler.handle(it) }
        viewModel.categories.observe(this) { loadCategoriesLoadingStateHandler.handle(it) }
    }

    private fun initLayout() {
        binding = ActivityAddEditReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receipt?.let { r ->
            binding.toolbarLayout.toolbar.init(this@AddEditReceiptActivity, R.string.title_activity_edit_receipt)
            binding.receiptName.setText(r.name)
            binding.receiptDescription.setText(r.description)
            binding.receiptAuthor.setText(r.author)
            binding.receiptUrl.setText(r.source)
            r.tags.forEach { addTagChip(it) }
            r.photos.forEach { photo ->
                viewModel.loadFile(photo.fileId).observe(this@AddEditReceiptActivity) {
                    downloadPhotoFileLoadingStateHandler.handle(it)
                }
            }
        } ?: kotlin.run {
            binding.toolbarLayout.toolbar.init(this@AddEditReceiptActivity, R.string.title_activity_new_receipt)
        }

        binding.tag.setAdapter(tagsArrayAdapter)
        binding.tag.setOnKeyListener { _, keyCode, event -> onKeyInTagPressed(keyCode, event) }
        binding.tag.setOnItemClickListener { _, _, position, _ ->
            addTagChip(tagsArrayAdapter.getItem(position)!!)
        }

        binding.receiptAuthor.setAdapter(authorsArrayAdapter)
        binding.receiptAuthor.setOnItemClickListener { _, _, position, _ ->
            binding.receiptAuthor.setText(authorsArrayAdapter.getItem(position))
        }

        binding.receiptCategory.adapter = categoriesDropdownAdapter

        binding.receiptName.doOnTextChanged { _, _, _, _ ->
            if (name.isNullOrEmpty()) {
                binding.receiptNameLayout.error = getString(R.string.message_receipt_name_is_empty)
            } else {
                binding.receiptNameLayout.error = null
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

    private fun prepareSaveReceiptLoadingStateHandler(): LoadingStateHandler<Int> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<Int> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddEditReceiptActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: Int) {
                eventBus.post(ReloadReceiptsEvent())
                if (receipt == null) {
                    ReceiptActivity.start(this@AddEditReceiptActivity, data)
                }
                finish()
            }
        })
    }

    private fun prepareUploadPhotosLoadingStateHandler(): LoadingStateHandler<List<Int>> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<List<Int>> {
            override fun onInProgress() {
                binding.root.showProgressSpinner(this@AddEditReceiptActivity)
            }

            override fun onFinish() {
                binding.root.hideProgressSpinner()
            }

            override fun onSuccess(data: List<Int>) {
                receipt?.let { r ->
                    val photoIds = ArrayList<Int>(photosAdapter.getItems<PhotoItem>().filter { it.fileId != null }
                        .map { it.fileId!! })
                    photoIds.addAll(data)
                    viewModel.updateReceipt(
                        r.id,
                        UpdateReceiptRequest(
                            name!!,
                            author,
                            url,
                            description,
                            categoryId,
                            tags,
                            photoIds
                        )
                    )
                        .observe(this@AddEditReceiptActivity) { saveReceiptLoadingStateHandler.handle(it) }
                } ?: kotlin.run {
                    viewModel.addReceipt(
                        AddReceiptRequest(
                            name!!,
                            author,
                            url,
                            description,
                            localStorageService.getId(),
                            categoryId,
                            tags,
                            data
                        )
                    ).observe(this@AddEditReceiptActivity) { saveReceiptLoadingStateHandler.handle(it) }
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

                receipt?.category?.let { category ->
                    categoriesDropdownAdapter.getPositionById(category.id)?.let { position ->
                        binding.receiptCategory.setSelection(position)
                    }
                }
            }
        })
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

    private fun prepareDownloadPhotoLoadingStateHandler(): LoadingStateHandler<FileService.DownloadedFile> {
        return LoadingStateHandler(this, object : LoadingStateHandler.OnStateChanged<FileService.DownloadedFile> {
            override fun onSuccess(data: FileService.DownloadedFile) {
                photosAdapter.add(PhotoItem(this@AddEditReceiptActivity, data.file.toUri(), data.fileId) { item ->
                    photosAdapter.remove(item)
                })
            }
        })
    }

    private fun saveReceipt() {
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
            return binding.receiptName.getTextOrNull()
        }

    private val author: String?
        get() {
            return binding.receiptAuthor.getTextOrNull()
        }

    private val url: String?
        get() {
            return binding.receiptUrl.getTextOrNull()
        }

    private val description: String?
        get() {
            return binding.receiptDescription.getTextOrNull()
        }

    private val categoryId: Int?
        get() {
            val position = binding.receiptCategory.selectedItemPosition
            return if (position == 0) null else categoriesDropdownAdapter.getItem(position)?.id
        }

    private val tags: List<String>
        get() {
            return binding.tagChips.getTextInChips()
        }

    private val receipt: Receipt?
        get() {
            return intent.getParcelableExtra(RECEIPT_EXTRA)
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_add_receipt, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            saveReceipt()
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
                                this@AddEditReceiptActivity,
                                it
                            ) { item -> photosAdapter.remove(item) })
                    }
                }
                return
            } else if (resultCode == UCrop.RESULT_ERROR) {
                this@AddEditReceiptActivity.toast(R.string.message_image_crop_error)
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
                        .start(this@AddEditReceiptActivity)
                }
            }
        })
    }

}