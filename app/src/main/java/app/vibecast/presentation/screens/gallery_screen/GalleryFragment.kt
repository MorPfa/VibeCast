@file:Suppress("SameParameterValue")

package app.vibecast.presentation.screens.gallery_screen

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.databinding.FragmentPicturesBinding
import app.vibecast.domain.model.ImageDto
import app.vibecast.presentation.screens.main_screen.image.ImageAdapter
import app.vibecast.presentation.screens.main_screen.image.ImageLoader
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import app.vibecast.presentation.util.ImageItemFragment
import kotlin.math.pow
import kotlin.math.sqrt


class GalleryFragment : Fragment() {


    private var _binding: FragmentPicturesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImageViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {



        _binding = FragmentPicturesBinding.inflate(inflater,container,false)
        val recyclerView: RecyclerView = binding.recyclerView
        val imageLoader = ImageLoader(requireContext())
        val adapter = ImageAdapter(imageLoader, viewModel)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        val spanCount = if (isTablet(requireActivity())) 4 else 3
        val layoutManager = GridLayoutManager(requireContext(), spanCount)
        recyclerView.layoutManager = layoutManager

        adapter.setOnItemClickListener { position ->
            val clickedImage = adapter.currentList[position]
            showImageDialog(clickedImage)
        }

        viewModel.galleryImages.observe(viewLifecycleOwner) { images ->
            adapter.submitList(images)

        }
        viewModel.backgroundImage?.observe(viewLifecycleOwner){ image ->
            if(image != null){
                imageLoader.loadUrlIntoImageView(
                    image,
                    binding.backgroundImageView,
                    true, 0
                )
            } else {
                val bgImage = viewModel.pickDefaultBackground()
                binding.backgroundImageView.setImageResource(bgImage)
            }

        }

        return binding.root
    }

    private fun showImageDialog(image: ImageDto) {
        val dialogFragment = ImageItemFragment.newInstance(image)
        dialogFragment.show(childFragmentManager, "image_dialog")


    }

    /**
     * Determines screenSize to adapt number of columns in recyclerview
     */
    @Suppress("DEPRECATION")
    private fun isTablet(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = activity.windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val screenSize = metrics.bounds.width() - insets.left - insets.right
            screenSize >= dpToPixels(activity, 600)
        } else {
            val display = activity.windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            val screenSize = sqrt(
                (metrics.widthPixels / metrics.xdpi.toDouble()).pow(2.0)
                        + (metrics.heightPixels / metrics.ydpi.toDouble()).pow(2.0)
            )
            screenSize >= 7
        }
    }




    private fun dpToPixels(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}