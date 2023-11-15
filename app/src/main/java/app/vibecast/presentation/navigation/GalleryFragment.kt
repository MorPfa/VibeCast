package app.vibecast.presentation.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.databinding.FragmentPicturesBinding
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.presentation.image.ImageAdapter
import app.vibecast.presentation.image.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
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
    // TODO: Rename and change types of parameters
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

        viewLifecycleOwner.lifecycleScope.launch  {
                imageRepository.getLocalImages().flowOn(Dispatchers.IO) // Move to IO thread for database access
                .collect { imageList ->
                    // Use the collected list to initialize the adapter
                    val recyclerView: RecyclerView = binding.recyclerView
                    val imageLoader = ImageLoader(requireContext())
                    val adapter = ImageAdapter(imageLoader, imageList)
                    recyclerView.adapter = adapter

                    val layoutManager = GridLayoutManager(requireContext(), 3)
                    recyclerView.layoutManager = layoutManager
                }
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
        // TODO: Rename and change types and number of parameters
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