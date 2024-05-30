package app.vibecast.presentation.screens.main_screen.image

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.databinding.ItemCardviewBinding
import app.vibecast.domain.model.ImageDto
import app.vibecast.presentation.screens.account_screen.AccountViewModel

class ImageAdapter(
    private val imageLoader: ImageLoader,
    private val imageViewModel: ImageViewModel,
    private val accountViewModel: AccountViewModel
) : ListAdapter<ImageDto, ImageAdapter.PictureViewHolder>(ImageDiffCallback()) {

    /**
     * Captures click on current item and allows for custom logic upon click
     */
    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }


    inner class PictureViewHolder(binding: ItemCardviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val savedImage = binding.savedImage

        val removeButton = binding.removeBtn

        init {
            itemView.setOnClickListener {
                onItemClickListener?.invoke(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCardviewBinding.inflate(inflater, parent, false)
        return PictureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        val image = getItem(position)
        imageLoader.loadUrlIntoImageView(image.urls.regular, holder.savedImage, false, 0)


        holder.removeButton.setOnClickListener {
            imageViewModel.deleteImage(image)
            accountViewModel.deleteImageFromFirebase(image)
        }
    }

    class ImageDiffCallback : DiffUtil.ItemCallback<ImageDto>() {
        override fun areItemsTheSame(oldItem: ImageDto, newItem: ImageDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ImageDto, newItem: ImageDto): Boolean {
            return oldItem == newItem
        }
    }
}
