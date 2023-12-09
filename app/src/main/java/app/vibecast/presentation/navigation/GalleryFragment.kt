package app.vibecast.presentation.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.databinding.FragmentPicturesBinding
import app.vibecast.presentation.mainscreen.MainScreenViewModel
import app.vibecast.presentation.image.ImageAdapter
import app.vibecast.presentation.image.ImageLoader


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
        val layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.layoutManager = layoutManager
        // Observe the LiveData in the ViewModel
        viewModel.galleryImages.observe(viewLifecycleOwner) { images ->
            adapter.submitList(images)
        }

        return binding.root
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