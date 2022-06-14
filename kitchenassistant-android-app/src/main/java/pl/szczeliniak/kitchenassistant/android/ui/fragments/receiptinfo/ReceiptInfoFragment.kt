package pl.szczeliniak.kitchenassistant.android.ui.fragments.receiptinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptInfoBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.services.ReceiptService
import pl.szczeliniak.kitchenassistant.android.ui.fragments.ReceiptActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.listitems.PhotoItem
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.setTextOrDefault
import pl.szczeliniak.kitchenassistant.android.ui.utils.ChipGroupUtils.Companion.add
import java.util.regex.Pattern

@AndroidEntryPoint
class ReceiptInfoFragment : ReceiptActivityFragment() {

    companion object {
        val YT_VIDEO_ID_PATTERN: Pattern =
            Pattern.compile("^.*((youtu.be\\/)|(v\\/)|(\\/u\\/\\w\\/)|(embed\\/)|(watch\\?))\\??v?=?([^#&?]*).*")

        fun create(): ReceiptInfoFragment {
            return ReceiptInfoFragment()
        }
    }

    private val viewModel: ReceiptInfoFragmentViewModel by viewModels()
    private val photosAdapter = GroupAdapter<GroupieViewHolder>()

    private lateinit var downloadPhotoLoadingStateHandler: LoadingStateHandler<ReceiptService.DownloadedPhoto>
    private lateinit var binding: FragmentReceiptInfoBinding

    private var player: YouTubePlayer? = null

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

            getYtVideoId(r.source)?.let { videoId ->
                binding.youtubePlayerLayout.visibility = View.VISIBLE
                initVideo(videoId)
            } ?: kotlin.run {
                binding.youtubePlayerLayout.visibility = View.GONE
            }

            binding.tagChips.removeAllViews()
            if (r.tags.isEmpty()) {
                binding.tagsLayout.visibility = View.GONE
            } else {
                binding.tagsLayout.visibility = View.VISIBLE
                r.tags.forEach { binding.tagChips.add(layoutInflater, it, false) }
            }

            binding.receiptCategory.setTextOrDefault(r.category?.name)
            photosAdapter.clear()
            r.photos.forEach { photo ->
                viewModel.loadPhoto(photo).observe(viewLifecycleOwner) {
                    downloadPhotoLoadingStateHandler.handle(it)
                }
            }
        }
    }

    private fun getYtVideoId(source: String?): String? {
        if (source == null) {
            return null
        }
        val matcher = YT_VIDEO_ID_PATTERN.matcher(source)
        if (matcher.matches()) {
            val videoId = matcher.group(7)
            if (videoId != null && videoId.length == 11) {
                return videoId
            }
        }
        return null
    }

    private fun initVideo(videoId: String) {
        lifecycle.addObserver(binding.youtubePlayer)
        binding.youtubePlayer.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                this@ReceiptInfoFragment.player = youTubePlayer
                youTubePlayer.cueVideo(videoId, 0F)
            }

        })
    }

    override fun onReceiptChanged() {
        loadData()
    }

    private fun prepareDownloadPhotoLoadingStateHandler(): LoadingStateHandler<ReceiptService.DownloadedPhoto> {
        return LoadingStateHandler(
            requireContext(),
            object : LoadingStateHandler.OnStateChanged<ReceiptService.DownloadedPhoto> {
                override fun onSuccess(data: ReceiptService.DownloadedPhoto) {
                    photosAdapter.add(PhotoItem(requireContext(), data.file.toUri()))
                }
            })
    }

    override fun onPause() {
        this.player?.pause()
        super.onPause()
    }

}