package pl.szczeliniak.kitchenassistant.android.ui.listitems

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.squareup.picasso.Picasso
import com.xwray.groupie.Group
import com.xwray.groupie.viewbinding.BindableItem
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.ListItemPhotoBinding

class PhotoItem constructor(
    private val context: Context,
    val uri: Uri,
    val fileId: Int? = null,
    private val onDeleteClick: OnClick? = null
) : BindableItem<ListItemPhotoBinding>() {

    override fun bind(binding: ListItemPhotoBinding, position: Int) {
        Picasso.get().load(uri).fit().centerCrop().into(binding.photoImageView)

        onDeleteClick?.let {
            binding.photoImageView.setOnLongClickListener { showPopupMenu(it) }
        }
    }

    override fun getLayout(): Int {
        return R.layout.list_item_photo
    }

    override fun initializeViewBinding(view: View): ListItemPhotoBinding {
        return ListItemPhotoBinding.bind(view)
    }

    private fun showPopupMenu(view: View): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.photo_item)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.photo_item_menu_item_delete -> {
                    onDeleteClick?.onClick(this)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
        return true
    }

    fun interface OnClick {
        fun onClick(group: Group)
    }

}