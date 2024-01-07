package app.vibecast.presentation.navigation

import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import app.vibecast.R
import app.vibecast.databinding.FragmentImageItemBinding
import app.vibecast.domain.entity.ImageDto
import app.vibecast.presentation.image.ImageLoader
import app.vibecast.presentation.mainscreen.MainScreenViewModel

private const val IMAGE = "image"
class ImageItemFragment : DialogFragment() {
    private val viewModel: MainScreenViewModel by activityViewModels()
    private var image: ImageDto? = null


    private lateinit var binding : FragmentImageItemBinding
    private lateinit var imageLoader : ImageLoader


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            image = it.parcelable(IMAGE)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageItemBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_fragment_background)
        imageLoader = ImageLoader(requireContext())
        image?.urls?.let { imageLoader.loadUrlIntoImageView(it.regular, binding.savedImage) }
        val userName = image?.user?.name
        val unsplashText = "Unsplash"
        val userUrl = image?.user?.attributionUrl
        val unsplashUrl = "https://unsplash.com/?vibecast&utm_medium=referral"
        val userLink = SpannableString(userName)
        val unsplashLink = SpannableString(unsplashText)
        if (context != null) {
            val clickableSpanUser = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    if (userUrl != null) {
                        openUrlInBrowser(userUrl)
                    }
                }
            }
            val clickableSpanUnsplash = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openUrlInBrowser(unsplashUrl)
                }
            }
            userLink.setSpan(clickableSpanUser, 0, userName?.length!!, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            unsplashLink.setSpan(clickableSpanUnsplash, 0, unsplashText.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }


        val spannableStringBuilder = SpannableStringBuilder()
            .append("Photo by ")
            .append(userLink)
            .append(" on ") // Add a space or any other separator if needed
            .append(unsplashLink)
            binding.title.text = spannableStringBuilder
            binding.title.movementMethod = LinkMovementMethod.getInstance()

        binding.removeBtn.setOnClickListener {
            image?.let { imageDto -> viewModel.deleteImage(imageDto) }
        }
        return binding.root
    }

    private fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context?.startActivity(intent, null)
    }

    private inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T
    }

    companion object {
        @JvmStatic
        fun newInstance(image: ImageDto): ImageItemFragment {
            val fragment = ImageItemFragment()
            val bundle = Bundle().apply {
                putParcelable(IMAGE, image)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}