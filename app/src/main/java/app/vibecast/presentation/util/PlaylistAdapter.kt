package app.vibecast.presentation.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.data.remote_data.network.music.model.UserItem
import app.vibecast.databinding.ItemMusicCardviewBinding
import app.vibecast.presentation.screens.main_screen.image.ImageLoader
import app.vibecast.presentation.screens.main_screen.music.MusicViewModel


class PlaylistAdapter(private val imageLoader: ImageLoader, private val musicViewModel: MusicViewModel) :
    ListAdapter<UserItem, PlaylistAdapter.SongViewHolder>(SongDiffCallback()) {

    /**
     * Captures click on current item and allows for custom logic upon click
     */
    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    inner class SongViewHolder(binding: ItemMusicCardviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val artistName = binding.artist
        val songName = binding.song
        val coverArt = binding.savedImage

        init {
            itemView.setOnClickListener {
                onItemClickListener?.invoke(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMusicCardviewBinding.inflate(inflater, parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val item = getItem(position)
        holder.songName.text = item.track.name
        holder.artistName.text = item.track.album.name
//        imageLoader.loadUrlIntoImageView(item.track, holder.coverArt, false,0)
//        musicViewModel.assertAppRemoteConnected()
//            .imagesApi
//            .getImage(song.imageUri, Image.Dimension.X_SMALL)
//            .setResultCallback { bitmap ->
//                holder.coverArt.setImageBitmap(bitmap)
//            }
    }
}


class SongDiffCallback : DiffUtil.ItemCallback<UserItem>() {
    override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem.track.id == newItem.track.id
    }

    override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
        return oldItem == newItem
    }
}

