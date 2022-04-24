package pl.szczeliniak.kitchenassistant.android.ui.fragments.receiptinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.viewModels
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptInfoBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.ui.fragments.ReceiptActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.setTextOrDefault
import java.io.File

@AndroidEntryPoint
class ReceiptInfoFragment : ReceiptActivityFragment() {

    companion object {
        fun create(): ReceiptInfoFragment {
            return ReceiptInfoFragment()
        }
    }

    private val viewModel: ReceiptInfoFragmentViewModel by viewModels()

    private lateinit var downloadPhotoLoadingStateHandler: LoadingStateHandler<File>

    private lateinit var binding: FragmentReceiptInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptInfoBinding.inflate(inflater)
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
            binding.photosContainer.removeAllViews()
            r.photos.forEach { photo ->
                viewModel.loadPhoto(photo.id).observe(viewLifecycleOwner) {
                    downloadPhotoLoadingStateHandler.handle(it)
                }
            }
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

    private fun prepareDownloadPhotoLoadingStateHandler(): LoadingStateHandler<File> {
        return LoadingStateHandler(requireContext(), object : LoadingStateHandler.OnStateChanged<File> {
            override fun onSuccess(data: File) {
                val photoView =
                    layoutInflater.inflate(R.layout.photo, binding.photosContainer, false) as AppCompatImageView
                Picasso.get().load(data).fit().centerCrop().into(photoView)
                binding.photosContainer.addView(photoView)
            }
        })
    }

}