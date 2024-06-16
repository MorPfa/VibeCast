package app.vibecast.presentation.util

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import app.vibecast.R
import app.vibecast.databinding.FragmentImageItemBinding
import app.vibecast.domain.model.ImageDto
import app.vibecast.presentation.screens.account_screen.AccountViewModel
import app.vibecast.presentation.screens.main_screen.image.ImageLoader
import app.vibecast.presentation.screens.main_screen.image.ImageSaver
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

private const val IMAGE = "image"

class ImageItemFragment : DialogFragment() {
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by activityViewModels()
    private var image: ImageDto? = null
    private lateinit var snackBar: Snackbar

    private lateinit var binding: FragmentImageItemBinding
    private lateinit var imageLoader: ImageLoader


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            image = it.parcelable(IMAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImageItemBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_fragment_background)
        imageLoader = ImageLoader(requireContext())
        image?.urls?.let {
            imageLoader.loadUrlIntoImageView(
                it.regular,
                binding.savedImage,
                false,
                0
            )
        }
        val userName = image?.user?.name
        val unsplashText = "Unsplash"
        val userUrl = image?.user?.attributionUrl
        val unsplashUrl = "https://unsplash.com/?utm_source=vibecast&utm_medium=referral"
        val userLink = SpannableString(userName)
        val unsplashLink = SpannableString(unsplashText)
        val linkColor = ContextCompat.getColor(requireContext(), R.color.white)
        val linkColorSpanUser = ForegroundColorSpan(linkColor)
        val linkColorSpanUnsplash = ForegroundColorSpan(linkColor)
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
            userLink.setSpan(
                clickableSpanUser,
                0,
                userName!!.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            userLink.setSpan(
                linkColorSpanUser,
                0,
                userName.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            unsplashLink.setSpan(
                clickableSpanUnsplash,
                0,
                unsplashText.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            unsplashLink.setSpan(
                linkColorSpanUnsplash,
                0,
                unsplashText.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }


        val attributionTextSpanBuilder = SpannableStringBuilder()
            .append("Photo by ")
            .append(userLink)
            .append(" on ")
            .append(unsplashLink)
        binding.attributionText.text = attributionTextSpanBuilder
        binding.attributionText.movementMethod = LinkMovementMethod.getInstance()

        val savedDate = convertUnixTimestamp(image?.timestamp)

        val dateSavedTextSpanBuilder = SpannableStringBuilder()
            .append("Saved on - ")
            .append(savedDate)
        binding.dateSavedText.text = dateSavedTextSpanBuilder
        binding.attributionText.movementMethod = LinkMovementMethod.getInstance()


        binding.removeBtn.setOnClickListener {
            image?.let { image ->
                imageViewModel.deleteImage(image)
                accountViewModel.deleteImageFromFirebase(image)
            }
            dialog?.dismiss()
        }
        binding.downloadBtn.setOnClickListener {
            lifecycleScope.launch {
                val image = imageViewModel.getImageForDownload(image?.links!!.downloadLink)
                if (image.data != null) {
                    ImageSaver.saveImageFromUrlToGallery(image.data, requireContext())
                }else {
                    snackBar = Snackbar.make(
                        requireView(),
                        getString(R.string.error_loading_image),
                        Snackbar.LENGTH_SHORT
                    )

                    val snackbarView = snackBar.view
                    val snackbarText =
                        snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    snackbarText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    snackbarView.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
                    snackBar.show()
                }
            }
        }
        return binding.root
    }

    private fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context?.startActivity(intent, null)
    }

    private fun convertUnixTimestamp(unixTimestamp: Long?): String? {
        return if (unixTimestamp != null) {
            val date = Date(unixTimestamp)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(date)
        } else {
            "(Date not found)"
        }

    }

    /**
     * Retrieves a Parcelable object from the Bundle associated with the given key.
     *
     * This extension function is declared as `inline` to allow reified type parameters,
     * providing a convenient and type-safe way to extract Parcelable objects from a Bundle.
     *
     * @param key The key associated with the Parcelable object in the Bundle.
     * @return The Parcelable object of type [T], or `null` if not found.
     *
     * @see Parcelable
     * @see Bundle.getParcelable
     * @see Bundle.getParcelable(Class)
     */
    private inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (::snackBar.isInitialized) {
            snackBar.dismiss()
        }
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