package app.vibecast.presentation.navigation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.databinding.ItemCardviewBinding
import app.vibecast.domain.entity.ImageDto

// Replace with the correct package name

class PictureAdapter(private val items: List<ImageDto>) : RecyclerView.Adapter<PictureAdapter.PictureViewHolder>() {

    class PictureViewHolder(private val binding: ItemCardviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Use binding to access views in the layout
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
        val item = items[position]

        // Bind data to views using ViewBinding



//        holder.removeButton.setOnClickListener {
//            //TODO
//        }

        // Set click listeners or other actions as needed


    }
}

