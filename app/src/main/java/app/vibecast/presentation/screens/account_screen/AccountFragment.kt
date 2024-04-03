package app.vibecast.presentation.screens.account_screen

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.R
import app.vibecast.databinding.FragmentAccountBinding
import app.vibecast.presentation.screens.main_screen.MainViewModel
import app.vibecast.presentation.screens.main_screen.image.ImageLoader
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import app.vibecast.presentation.screens.main_screen.weather.LocationModel
import app.vibecast.presentation.util.LocationAdapter
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        imageViewModel.setImageCountLiveData()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAccountBinding.inflate(inflater,container,false)
        binding.username.text = auth.currentUser?.displayName ?: "-"
        binding.userEmail.text = auth.currentUser?.email ?: "-"
        val savedLocationsRv : RecyclerView = binding.savedLocations
        val adapter = LocationAdapter(requireContext())
        savedLocationsRv.adapter = adapter
        savedLocationsRv.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        savedLocationsRv.layoutManager = layoutManager
        val imageLoader = ImageLoader(requireContext())
        adapter.setOnItemClickListener { position ->
            val clickedItem = adapter.currentList[position]
            showDeleteConfirmationDialog(clickedItem)
        }
        lifecycleScope.launch {
            imageViewModel.imageCount.observe(viewLifecycleOwner){
                binding.savedImageCount.text = getString(R.string.saved_image_count, it)

            }
            imageViewModel.backgroundImage.observe(viewLifecycleOwner){ image ->
                if(image != null){
                    imageLoader.loadUrlIntoImageView(
                        image,
                        binding.backgroundImageView,
                        true, 0
                    )
                } else {
                    val bgImage = imageViewModel.pickDefaultBackground()
                    binding.backgroundImageView.setImageResource(bgImage)
                }

            }
            mainViewModel.locations.observe(viewLifecycleOwner){ locations->
                binding.savedLocationsCount.text =  getString(R.string.saved_location_count, locations.size)
                adapter.submitList(locations)
            }
        }


        return binding.root
    }


    private var alertDialog: AlertDialog? = null

    /**
     * Shows pop up window to confirm deletion or cancel
     */
    private fun showDeleteConfirmationDialog(location: LocationModel) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        val customView = LayoutInflater.from(requireContext()).inflate(R.layout.confirm_delete_dialog, null)
        val removeButton = customView.findViewById<MaterialButton>(R.id.removeBtn)
        val cancelButton = customView.findViewById<MaterialButton>(R.id.cancelBtn)

        removeButton.setOnClickListener {
            mainViewModel.deleteLocation(location)
            mainViewModel.resetIndex()
            alertDialog?.dismiss()
        }

        cancelButton.setOnClickListener {
            alertDialog?.dismiss()
        }

        alertDialogBuilder.setView(customView)
        alertDialog = alertDialogBuilder.create()
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mainViewModel.locations.removeObservers(viewLifecycleOwner)
    }

}