package app.vibecast.presentation.navigation

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
import app.vibecast.domain.entity.ImageDto
import app.vibecast.presentation.mainscreen.MainScreenViewModel
import app.vibecast.presentation.image.ImageAdapter
import app.vibecast.presentation.image.ImageLoader
import kotlin.math.pow
import kotlin.math.sqrt


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class GalleryFragment : Fragment() {


    private var _binding: FragmentPicturesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainScreenViewModel by activityViewModels()
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPicturesBinding.inflate(inflater,container,false)
        val recyclerView: RecyclerView = binding.recyclerView
        val imageLoader = ImageLoader(requireContext())
        val adapter = ImageAdapter(imageLoader, requireContext(), viewModel)
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

        return binding.root
    }

    private fun showImageDialog(image: ImageDto) {
        val dialogFragment = ImageItemFragment.newInstance(image)
        dialogFragment.show(childFragmentManager, "image_dialog")


    }
    @Suppress("DEPRECATION")
    private fun isTablet(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = activity.windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val screenSize = metrics.bounds.width() - insets.left - insets.right
            screenSize >= dpToPixels(activity, 600) // Adjust the threshold as needed
        } else {
            val display = activity.windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            val screenSize = sqrt(
                (metrics.widthPixels / metrics.xdpi.toDouble()).pow(2.0)
                        + (metrics.heightPixels / metrics.ydpi.toDouble()).pow(2.0)
            )
            screenSize >= 7 // Adjust the threshold as needed
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

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GalleryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}