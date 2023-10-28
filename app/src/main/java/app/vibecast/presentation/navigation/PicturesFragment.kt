package app.vibecast.presentation.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.R
import app.vibecast.databinding.FragmentPicturesBinding
import app.vibecast.domain.entity.Picture

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PicturesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PicturesFragment : Fragment() {

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
    ): View? {
        binding = FragmentPicturesBinding.inflate(inflater,container,false)

        val pictureItems = listOf(
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 1", "Title 1", "Description 1"),
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 2", "Title 2", "Description 2"),
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 1", "Title 1", "Description 1"),
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 2", "Title 2", "Description 2"),
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 1", "Title 1", "Description 1"),
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 2", "Title 2", "Description 2"),
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 1", "Title 1", "Description 1"),
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 2", "Title 2", "Description 2"),
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 1", "Title 1", "Description 1"),
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 2", "Title 2", "Description 2"),
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 1", "Title 1", "Description 1"),
            Picture(R.drawable.osman_rana_gxezuwo5m4i_unsplash, "Header 2", "Title 2", "Description 2"),
            // Add more PictureItem objects as needed
        )

        val recyclerView: RecyclerView = binding.recyclerView
        val adapter = PictureAdapter(pictureItems)
        recyclerView.adapter = adapter

        val layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.layoutManager = layoutManager


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
            PicturesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}