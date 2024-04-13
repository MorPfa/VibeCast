package app.vibecast.presentation.screens.account_screen

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.vibecast.R
import app.vibecast.databinding.FragmentAccountBinding
import app.vibecast.domain.model.SongDto
import app.vibecast.presentation.screens.main_screen.MainViewModel
import app.vibecast.presentation.screens.main_screen.image.ImageLoader
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import app.vibecast.presentation.screens.main_screen.music.MusicViewModel
import app.vibecast.presentation.screens.main_screen.weather.LocationModel
import app.vibecast.presentation.util.LocationAdapter
import app.vibecast.presentation.util.SongAdapter
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val musicViewModel : MusicViewModel by activityViewModels()
    private val accountViewModel : AccountViewModel by activityViewModels()
    private lateinit var profilePic : ImageView
    private lateinit var changePictureBtn : ImageButton
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
        profilePic = binding.profilePicture
        changePictureBtn = binding.editProfileBtn
        binding.username.text = auth.currentUser?.displayName ?: "-"
        binding.userEmail.text = auth.currentUser?.email ?: "-"
        val savedLocationsRv : RecyclerView = binding.savedLocations
        val locationAdapter = LocationAdapter(requireContext())
        savedLocationsRv.adapter = locationAdapter
        savedLocationsRv.setHasFixedSize(true)
        val locationLayoutManager = LinearLayoutManager(requireContext())
        savedLocationsRv.layoutManager = locationLayoutManager
        val savedSongsRv : RecyclerView = binding.savedSongs
        val songAdapter = SongAdapter(musicViewModel)
        savedSongsRv.adapter = songAdapter
        val songLayoutManager = LinearLayoutManager(requireContext())
        savedLocationsRv.layoutManager = songLayoutManager
        savedSongsRv.setHasFixedSize(true)

        val imageLoader = ImageLoader(requireContext())

        changePictureBtn.setOnClickListener{

            val action = AccountFragmentDirections.accountToEditProfile()
            findNavController().navigate(action)
        }
        locationAdapter.setOnItemClickListener { position ->
            val clickedItem = locationAdapter.currentList[position]
            showDeleteConfirmationDialog(clickedItem)
        }

        songAdapter.setOnItemClickListener { position ->
            val clickedItem = songAdapter.currentList[position]
            showSongCard(clickedItem)
        }
        lifecycleScope.launch {
            musicViewModel.savedSongs.observe(viewLifecycleOwner){songs ->
                Timber.tag("music_db").d(songs.toString())
                songAdapter.submitList(songs)
            }
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
                locationAdapter.submitList(locations)
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
//            accountViewModel.deleteLocationFromFirebase(location)
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

    private fun showSongCard(song: SongDto) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        val customView = LayoutInflater.from(requireContext()).inflate(R.layout.music_card, null)
        val removeButton = customView.findViewById<MaterialButton>(R.id.removeBtn)
        val uriText = customView.findViewById<TextView>(R.id.attribution_text)

        removeButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.uri))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val packageManager = context?.packageManager
            if (packageManager != null && intent.resolveActivity(packageManager) != null) {
                // Open the Spotify app
                context?.startActivity(intent)
            } else {
//            // If Spotify app is not installed, open the Spotify web page instead

            }
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