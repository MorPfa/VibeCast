package app.vibecast.presentation.screens.settings_screen


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.R
import app.vibecast.databinding.ImageSelectorItemBinding
import app.vibecast.domain.model.ImageDto
import app.vibecast.presentation.screens.main_screen.image.ImageAdapter
import app.vibecast.presentation.screens.main_screen.image.ImageLoader
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel

class ImageSelectorAdapter(
    private val imageLoader: ImageLoader,
    private val owner: LifecycleOwner,
    private val viewModel: ImageViewModel,
) : ListAdapter<ImageDto, ImageSelectorAdapter.ImageViewHolder>(ImageAdapter.ImageDiffCallback()) {


    inner class ImageViewHolder(binding: ImageSelectorItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val savedImage = binding.savedImage
        val selectedIcon = binding.selectedIcon
        init {
            // Remove observer when view is detached from window
            itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    // Do nothing
                }

                override fun onViewDetachedFromWindow(v: View) {
                    viewModel.backgroundImage.removeObservers(owner)
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ImageSelectorItemBinding.inflate(inflater, parent, false)
        return ImageViewHolder(binding)
    }




    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = getItem(position)
        imageLoader.loadUrlIntoImageView(image.urls.regular, holder.savedImage, true, 32)
        viewModel.backgroundImage.observe(owner) { backgroundImage ->
            if (image.urls.regular == backgroundImage) {
                holder.selectedIcon.setImageResource(R.drawable.item_selected_icon)
            } else {
                holder.selectedIcon.setImageResource(R.drawable.item_not_selected_icon)
            }
            holder.savedImage.setOnClickListener {
                if (image.urls.regular == backgroundImage) {
                    holder.selectedIcon.setImageResource(R.drawable.item_not_selected_icon)
                    viewModel.saveBackgroundImage(image.urls.regular)
                } else {
                    viewModel.saveBackgroundImage(image.urls.regular)
                    holder.selectedIcon.setImageResource(R.drawable.item_selected_icon)

                }

            }
        }


    }


}


