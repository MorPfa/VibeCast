package app.vibecast.presentation.image

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.databinding.ItemCardviewBinding
import app.vibecast.domain.entity.ImageDto


class ImageAdapter(
    private val imageLoader: ImageLoader,
    private val items: List<ImageDto>,
) : RecyclerView.Adapter<ImageAdapter.PictureViewHolder>() {

    class PictureViewHolder(binding: ItemCardviewBinding) : RecyclerView.ViewHolder(binding.root) {
        val savedImage = binding.savedImage
        val header = binding.header
        val title = binding.title
        val description = binding.description
        val removeButton = binding.removeBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCardviewBinding.inflate(inflater, parent, false)
        return PictureViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        val imageUrl = items[position]
        imageLoader.loadUrlIntoImageView(imageUrl.urls.regular,holder.savedImage)

//        holder.removeButton.setOnClickListener {
//
        //TODO figure out deleting images
//
//        }




    }
}

