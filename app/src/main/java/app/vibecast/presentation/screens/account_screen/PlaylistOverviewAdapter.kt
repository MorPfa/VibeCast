package app.vibecast.presentation.screens.account_screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.data.remote_data.network.music.model.Item
import app.vibecast.databinding.ItemMusicCardviewBinding
import app.vibecast.presentation.screens.main_screen.image.ImageLoader


class PlaylistOverViewAdapter(private val imageLoader: ImageLoader) :
    ListAdapter<Item, PlaylistOverViewAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    /**
     * Captures click on current item and allows for custom logic upon click
     */
    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    inner class PlaylistViewHolder(binding: ItemMusicCardviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val artistList = binding.artist
        val playlistName = binding.song
        val coverArt = binding.savedImage

        init {
            itemView.setOnClickListener {
                onItemClickListener?.invoke(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMusicCardviewBinding.inflate(inflater, parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.playlistName.text = playlist.name
        holder.artistList.text = playlist.owner.displayName
        imageLoader.loadUrlIntoImageView(playlist.images[playlist.images.size - 1].url, holder.coverArt, false,0)
    }
}


class PlaylistDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}

