package app.vibecast.presentation.navigation

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.vibecast.R
import app.vibecast.databinding.FragmentAccountBinding
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.repository.LocationRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.circularreveal.cardview.CircularRevealCardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AccountFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentAccountBinding
    private lateinit var locationList : LinearLayout
    private val viewModel: AccountViewModel by activityViewModels()



    @Inject
    lateinit var locationRepository: LocationRepository
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
        binding = FragmentAccountBinding.inflate(inflater,container,false)
        locationList = binding.savedLocations
        loadLocations()
        return binding.root
    }


    private fun loadLocations() {
        viewModel.savedLocations.observe(viewLifecycleOwner) { locations ->
            val currentItemCount = locationList.childCount
            val max = locations.size
            if (max == 0) {
                val item = createDefaultTv()
                locationList.addView(item)
            } else {
                for (i in currentItemCount until max) {
                    val item = createItemView(i, locations)
                    locationList.addView(item)
                }
            }
        }
    }
    private fun createItemView(index: Int, locations: List<LocationDto>): View {
        val item = TextView(requireContext())
        item.text = locations.getOrNull(index)?.cityName ?: ""
        item.setTextColor(Color.WHITE)
        item.setBackgroundResource(R.drawable.rounded_black_background)
        item.gravity = Gravity.START
        item.textSize = 18f
        val paddingInDpVertical = 8
        val paddingInPxVertical = (paddingInDpVertical * resources.displayMetrics.density).toInt()
        val paddingInDpLeft = 12
        val paddingInPxLeft = (paddingInDpLeft * resources.displayMetrics.density).toInt()
        val paddingInDpRight = 12
        val paddingInPxRight = (paddingInDpRight * resources.displayMetrics.density).toInt()
        item.setPadding(paddingInPxLeft, paddingInPxVertical, paddingInPxRight, paddingInPxVertical)
        item.setOnLongClickListener {
            showDeleteConfirmationDialog(index, locations[index])
            true
        }

        return item
    }
    private var alertDialog: AlertDialog? = null

    private fun showDeleteConfirmationDialog(index: Int, location: LocationDto) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        val customView = LayoutInflater.from(requireContext()).inflate(R.layout.confirm_delete_dialog, null)
        val cardView = customView.findViewById<CircularRevealCardView>(R.id.dialog_card)
        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        cardView.radius = 36f
        cardView.clipToOutline = true

        val removeButton = customView.findViewById<MaterialButton>(R.id.removeBtn)
        val cancelButton = customView.findViewById<MaterialButton>(R.id.cancelBtn)

        // Set click listeners
        removeButton.setOnClickListener {
            // Handle click on the "Remove" button
            removeFixedItem(index, location)
            alertDialog?.dismiss() // Dismiss the dialog after handling the action
        }

        cancelButton.setOnClickListener {
            // Handle click on the "Cancel" button
            alertDialog?.dismiss() // Dismiss the dialog without performing the action
        }

        alertDialogBuilder.setView(customView)

        // Create the dialog and assign it to the global variable
        alertDialog = alertDialogBuilder.create()

        // Set background color of the dialog window to be transparent
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Show the dialog
        alertDialog?.show()
    }



    private fun removeFixedItem(index: Int, location: LocationDto) {
        if (index >= 0 && index < locationList.childCount) {
            locationList.removeViewAt(index)
            viewModel.deleteLocation(location)
        }
    }

    private fun createDefaultTv(): View {
        val item = TextView(requireContext())
        item.text = getString(R.string.none_saved)
        item.setTextColor(Color.WHITE)
        item.textSize = 18.0f
        item.gravity = 1


        return item
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CurrentLocationFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}