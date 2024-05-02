package app.vibecast.presentation.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.databinding.ItemMusicCardviewBinding
import app.vibecast.domain.model.SongDto
import app.vibecast.presentation.screens.main_screen.music.MusicViewModel
import com.spotify.protocol.types.Image
import timber.log.Timber


class SongAdapter(private val musicViewModel: MusicViewModel) :
    ListAdapter<SongDto, SongAdapter.SongViewHolder>(SongDiffCallback()) {

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
        val song = getItem(position)
        Timber.tag("imageTest").d("curr uri ${song.imageUri}")
        holder.songName.text = song.name
        holder.artistName.text = song.artist
        musicViewModel.assertAppRemoteConnected()
            .imagesApi
            .getImage(song.imageUri, Image.Dimension.X_SMALL)
            .setResultCallback { bitmap ->
                holder.coverArt.setImageBitmap(bitmap)
            }
    }
}


class SongDiffCallback : DiffUtil.ItemCallback<SongDto>() {
    override fun areItemsTheSame(oldItem: SongDto, newItem: SongDto): Boolean {
        return oldItem.trackUri == newItem.trackUri
    }

    override fun areContentsTheSame(oldItem: SongDto, newItem: SongDto): Boolean {
        return oldItem == newItem
    }
}

