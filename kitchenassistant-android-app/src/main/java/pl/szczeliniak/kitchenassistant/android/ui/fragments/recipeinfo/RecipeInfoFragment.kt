package pl.szczeliniak.kitchenassistant.android.ui.fragments.recipeinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentRecipeInfoBinding
import pl.szczeliniak.kitchenassistant.android.ui.fragments.RecipeActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.setTextOrDefault
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

    private lateinit var binding: FragmentRecipeInfoBinding

    private var player: YouTubePlayer? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRecipeInfoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

            binding.recipeCategory.setTextOrDefault(r.category?.name)
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
        player?.cueVideo(videoId, 0F) ?: kotlin.run {
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

    override fun onPause() {
        this.player?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        lifecycle.removeObserver(binding.youtubePlayer)
        super.onDestroy()
    }

}