package pl.szczeliniak.kitchenassistant.android.ui.fragments.receiptinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptInfoBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.services.FileService
import pl.szczeliniak.kitchenassistant.android.ui.fragments.ReceiptActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.listitems.PhotoItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.setTextOrDefault

@AndroidEntryPoint
class ReceiptInfoFragment : ReceiptActivityFragment() {

    companion object {
        fun create(): ReceiptInfoFragment {
            return ReceiptInfoFragment()
        }
    }

    private val viewModel: ReceiptInfoFragmentViewModel by viewModels()
    private val photosAdapter = GroupAdapter<GroupieViewHolder>()

    private lateinit var downloadPhotoLoadingStateHandler: LoadingStateHandler<FileService.DownloadedFile>
    private lateinit var binding: FragmentReceiptInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptInfoBinding.inflate(inflater)
        binding.photosRecyclerView.adapter = photosAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        downloadPhotoLoadingStateHandler = prepareDownloadPhotoLoadingStateHandler()
        loadData()
    }

    private fun loadData() {
        receipt?.let { r ->
            binding.receiptDescription.setTextOrDefault(r.description)
            binding.receiptAuthor.setTextOrDefault(r.author)
            binding.receiptUrl.setTextOrDefault(r.source)
            binding.receiptTags.setTextOrDefault(r.tags.joinToString())
            binding.receiptCategory.setTextOrDefault(r.category?.name)
            photosAdapter.clear()
            r.photos.forEach { photo ->
                viewModel.loadPhoto(photo.fileId).observe(viewLifecycleOwner) {
                    downloadPhotoLoadingStateHandler.handle(it)
                }
            }
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

    private fun prepareDownloadPhotoLoadingStateHandler(): LoadingStateHandler<FileService.DownloadedFile> {
        return LoadingStateHandler(
            requireContext(),
            object : LoadingStateHandler.OnStateChanged<FileService.DownloadedFile> {
                override fun onSuccess(data: FileService.DownloadedFile) {
                    photosAdapter.add(PhotoItem(requireContext(), data.file.toUri()))
                }
            })
    }

}