package app.vibecast.presentation.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.databinding.FragmentPicturesBinding
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.presentation.image.ImageAdapter
import app.vibecast.presentation.image.ImageLoader
import app.vibecast.presentation.weather.CurrentLocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class GalleryFragment : Fragment() {

    @Inject
    lateinit var imageRepository: ImageRepository

    private lateinit var binding : FragmentPicturesBinding
    private val viewModel: CurrentLocationViewModel by activityViewModels()
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
        binding = FragmentPicturesBinding.inflate(inflater,container,false)
        val recyclerView: RecyclerView = binding.recyclerView
        val imageLoader = ImageLoader(requireContext())
        val adapter = ImageAdapter(imageLoader, requireContext(), viewModel)
        recyclerView.adapter = adapter
        val layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.layoutManager = layoutManager
        // Observe the LiveData in the ViewModel
        viewModel.galleryImages.observe(viewLifecycleOwner) { images ->
            adapter.submitList(images)
        }

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PicturesFragment.
         */
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