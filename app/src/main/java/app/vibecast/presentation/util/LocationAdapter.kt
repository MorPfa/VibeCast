package app.vibecast.presentation.util

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.R
import app.vibecast.databinding.LocationItemCardBinding
import app.vibecast.presentation.screens.main_screen.weather.LocationModel

class LocationAdapter(
    private val context: Context,
) : ListAdapter<LocationModel, LocationAdapter.LocationViewHolder>(LocationDiffCallback()) {

    /**
     * Captures click on current item and allows for custom logic upon click
     */
    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    inner class LocationViewHolder(binding: LocationItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
            val locationTv = binding.location

        init {
            itemView.setOnClickListener {
                onItemClickListener?.invoke(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LocationItemCardBinding.inflate(inflater, parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = getItem(position)
        holder.locationTv.text = context.getString(R.string.saved_location, location.cityName, location.country)



    }

    class LocationDiffCallback : DiffUtil.ItemCallback<LocationModel>() {
        override fun areItemsTheSame(oldItem: LocationModel, newItem: LocationModel): Boolean {
            return oldItem.cityName == newItem.cityName
        }

        override fun areContentsTheSame(oldItem: LocationModel, newItem: LocationModel): Boolean {
            return oldItem == newItem
        }
    }
}
