package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.squareup.picasso.Picasso
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemRecipeBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.RecipesResponse
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide
import java.io.File

class RecipeItem(
    private val context: Context,
    private val recipe: RecipesResponse.Recipe,
    private val showCategory: Boolean = false,
    private val onClick: OnClick,
    private val onDeleteClick: OnClick,
    private val onEditClick: OnClick,
    private val onAddRemoveFromFavourites: OnClick,
    private val onAssignToDayPan: OnClick
) : BindableItem<ListItemRecipeBinding>() {

    var photo: File? = null

    override fun bind(binding: ListItemRecipeBinding, position: Int) {
        binding.recipeName.text = recipe.name
        recipe.category?.let {
            binding.recipeCategory.fillOrHide(
                if (showCategory) it.name else "",
                binding.recipeCategory
            )
        }
        binding.recipeAuthor.fillOrHide(recipe.author, binding.recipeAuthor)
        binding.root.setOnClickListener { onClick.onClick(recipe) }
        binding.buttonMore.setOnClickListener { showPopupMenu(it) }
        binding.recipeIsFavorite.visibility = if (recipe.favorite) View.VISIBLE else View.INVISIBLE
        photo?.let {
            Picasso.get().load(it).centerCrop().fit().into(binding.recipePhoto)
        } ?: run {
            binding.recipePhoto.setImageDrawable(null)
        }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_recipe
    }

    override fun initializeViewBinding(view: View): ListItemRecipeBinding {
        return ListItemRecipeBinding.bind(view)
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.recipe_item)

        popupMenu.menu.findItem(R.id.add_remove_from_favorites).title =
            context.getString(if (recipe.favorite) R.string.label_button_remove_from_favorites else R.string.label_button_add_to_favorites)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    onDeleteClick.onClick(recipe)
                    return@setOnMenuItemClickListener true
                }

                R.id.edit -> {
                    onEditClick.onClick(recipe)
                    return@setOnMenuItemClickListener true
                }

                R.id.add_remove_from_favorites -> {
                    onAddRemoveFromFavourites.onClick(recipe)
                    return@setOnMenuItemClickListener true
                }

                R.id.add_to_day_plan -> {
                    onAssignToDayPan.onClick(recipe)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    fun interface OnClick {
        fun onClick(recipe: RecipesResponse.Recipe)
    }

    val photoName: String?
        get() {
            return recipe.photoName
        }

}