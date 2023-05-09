package pl.szczeliniak.kitchenassistant.android.ui.fragments.recipeinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentRecipeInfoBinding
import pl.szczeliniak.kitchenassistant.android.network.LoadingStateHandler
import pl.szczeliniak.kitchenassistant.android.services.PhotoService
import pl.szczeliniak.kitchenassistant.android.ui.fragments.RecipeActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.setTextOrDefault
import pl.szczeliniak.kitchenassistant.android.ui.utils.ChipGroupUtils.Companion.add
import java.util.regex.Pattern

@AndroidEntryPoint
class RecipeInfoFragment : RecipeActivityFragment() {

    companion object {
        val YT_VIDEO_ID_PATTERN: Pattern =
            Pattern.compile("^.*((youtu.be\\/)|(v\\/)|(\\/u\\/\\w\\/)|(embed\\/)|(watch\\?))\\??v?=?([^#&?]*).*")

        fun create(): RecipeInfoFragment {
            return RecipeInfoFragment()
        }
    }

    private val viewModel: RecipeInfoFragmentViewModel by viewModels()

    private lateinit var downloadPhotoLoadingStateHandler: LoadingStateHandler<PhotoService.DownloadedPhoto>
    private lateinit var binding: FragmentRecipeInfoBinding

    private var player: YouTubePlayer? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRecipeInfoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        downloadPhotoLoadingStateHandler = prepareDownloadPhotoLoadingStateHandler()
        loadData()
    }

    private fun loadData() {
        recipe?.let { r ->
            binding.recipeName.setTextOrDefault(r.name)
            binding.recipeDescription.setTextOrDefault(r.description)
            binding.recipeAuthor.setTextOrDefault(r.author)
            binding.recipeUrl.setTextOrDefault(r.source)

            getYtVideoId(r.source)?.let { videoId ->
                binding.youtubePlayerLayout.visibility = View.VISIBLE
                loadVideo(videoId)
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

            binding.recipeCategory.setTextOrDefault(r.category?.name)
            r.photoName?.let {
                viewModel.loadPhoto(it).observe(viewLifecycleOwner) {
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

    private fun loadVideo(videoId: String) {

        player?.let {
            it.cueVideo(videoId, 0F)
        } ?: kotlin.run {
            lifecycle.addObserver(binding.youtubePlayer)
            binding.youtubePlayer.initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    this@RecipeInfoFragment.player = youTubePlayer
                    youTubePlayer.cueVideo(videoId, 0F)
                }

            })
        }
    }

    override fun onRecipeChanged() {
        loadData()
    }

    private fun prepareDownloadPhotoLoadingStateHandler(): LoadingStateHandler<PhotoService.DownloadedPhoto> {
        return LoadingStateHandler(
            requireContext(),
            object : LoadingStateHandler.OnStateChanged<PhotoService.DownloadedPhoto> {
                override fun onSuccess(data: PhotoService.DownloadedPhoto) {
                    Picasso.get().load(data.file.toUri()).fit().centerCrop().into(binding.recipePhoto)
                    binding.recipePhotoContainer.visibility = View.VISIBLE
                }
            })
    }

    override fun onPause() {
        this.player?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        lifecycle.removeObserver(binding.youtubePlayer)
        super.onDestroy()
    }

}