package app.vibecast.presentation.image

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.databinding.ItemCardviewBinding
import app.vibecast.domain.entity.ImageDto
import app.vibecast.presentation.mainscreen.MainScreenViewModel
import java.lang.ref.WeakReference

class ImageAdapter(
    private val imageLoader: ImageLoader,
    context: Context,
    private val viewModel: MainScreenViewModel
) : ListAdapter<ImageDto, ImageAdapter.PictureViewHolder>(ImageDiffCallback()) {


    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    private val contextReference: WeakReference<Context> = WeakReference(context)
    inner class PictureViewHolder(binding: ItemCardviewBinding) : RecyclerView.ViewHolder(binding.root) {
        val savedImage = binding.savedImage
        val title = binding.title
        val removeButton = binding.removeBtn
        init {
            itemView.setOnClickListener{
                onItemClickListener?.invoke(bindingAdapterPosition)
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
        imageLoader.loadUrlIntoImageView(image.urls.regular, holder.savedImage)

        val userName = image.user.name
        val unsplashText = "Unsplash"
        val userUrl = image.user.attributionUrl
        val unsplashUrl = "https://unsplash.com/?vibecast&utm_medium=referral"
        val userLink = SpannableString(userName)
        val unsplashLink = SpannableString(unsplashText)

        val context = contextReference.get()
        if (context != null) {
            val clickableSpanUser = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openUrlInBrowser(context, userUrl)
                }
            }
            val clickableSpanUnsplash = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openUrlInBrowser(context, unsplashUrl)
                }
            }
            userLink.setSpan(clickableSpanUser, 0, userName.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            unsplashLink.setSpan(clickableSpanUnsplash, 0, unsplashText.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }


        val spannableStringBuilder = SpannableStringBuilder()
            .append("Photo by ")
            .append(userLink)
            .append(" on ") // Add a space or any other separator if needed
            .append(unsplashLink)
        holder.title.text = spannableStringBuilder
        holder.title.movementMethod = LinkMovementMethod.getInstance()

        holder.removeButton.setOnClickListener {
            viewModel.deleteImage(image)
        }
    }

    private fun openUrlInBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent, null)
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
