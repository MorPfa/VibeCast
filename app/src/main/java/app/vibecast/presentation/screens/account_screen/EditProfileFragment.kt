package app.vibecast.presentation.screens.account_screen

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import app.vibecast.databinding.FragmentEditProfileBinding
import app.vibecast.presentation.screens.main_screen.image.ImageLoader
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class EditProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private val imageViewModel: ImageViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var profilePic: ImageView
    private lateinit var userName: EditText
    private lateinit var userEmail: EditText
    private lateinit var submitBTn: Button
    private var currentUser: FirebaseUser? = null
    private lateinit var currentUsername : String
    private lateinit var currentEmail : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        currentUser = auth.currentUser
        currentUsername = currentUser?.displayName.toString()
        currentEmail = currentUser?.email.toString()
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        profilePic = binding.profilePicture
        userName = binding.emailInput
        userEmail = binding.usernameInput
        submitBTn = binding.submitBtn
        val imageLoader = ImageLoader(requireContext())
        userEmail.setText(currentUser?.email ?: "")
        userName.setText(currentUser?.displayName ?: "")


        submitBTn.setOnClickListener {
            submitChanges()
        }

        profilePic.setOnClickListener {
            imageChooser()
        }

        lifecycleScope.launch {

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
        }
        return binding.root
    }

    private fun submitChanges() {
        val email = userEmail.text.toString()
        val userName = userName.text.toString()
        val validInput = validateInput(email, userName)

        if (validInput) {
            Timber.tag("auth").d(currentEmail)
            Timber.tag("auth").d(currentUsername)
            val credential = EmailAuthProvider
                .getCredential(currentEmail, currentUsername)
            currentUser?.reauthenticate(credential)
                ?.addOnSuccessListener {
                    Timber.tag("auth").d("User re-authenticated.")
                    updateUser(userName, email)
                }
                ?.addOnFailureListener {
                    Timber.tag("auth").d("failed to re-authenticate.")
                    Timber.tag("auth").d(it.localizedMessage)
                }

        }
    }


    private fun updateUser(userName: String, email: String){
        editUserName(currentUser, userName)
        editUserEmail(currentUser, email)
    }


    private fun editUserName(user: FirebaseUser?, userName: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .build()
        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { profileUpdateTask ->
                if (profileUpdateTask.isSuccessful) {
                    Timber.tag("auth").d("updated username")
                    accountViewModel.updateUserName(userName)
                } else {
                    Timber.tag("auth").e(profileUpdateTask.exception, "Failed to update username")
                }
            }
    }

    private fun editUserEmail(user: FirebaseUser?, email: String) {
        user!!.verifyBeforeUpdateEmail(email)
            .addOnCompleteListener { profileUpdateTask ->
                if (profileUpdateTask.isSuccessful) {
                    Timber.tag("auth").d("User profile updated with username")
                } else {
                    Timber.tag("auth").e(profileUpdateTask.exception, "Failed to update  email")
                }
            }
    }

    private fun validateInput(email: String, username: String): Boolean {
        Timber.tag("auth").d("$email, $username")
        val validemail = isEmailValid(email)
        Timber.tag("auth").d("Email valid: $validemail")
        val validuser = isUserNameValid(username)
        Timber.tag("auth").d("User valid: $validuser")
        return isEmailValid(email) && isUserNameValid(username)
    }

    private fun isUserNameValid(userName: String) = userName.isNotEmpty()

    private fun isEmailValid(email: String): Boolean {
        if (email.isEmpty()) {
//            passwordInput.error = "Test"
            //TODO customize this
            return false
        }
        val validEmail = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return validEmail.matches(email)
    }


    private fun imageChooser() {
        val i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)
        launchSomeActivity.launch(i)
    }

    private var launchSomeActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode
            == Activity.RESULT_OK
        ) {
            val data = result.data
            // do your operation from here....
            if (data != null
                && data.data != null
            ) {
                val selectedImageUri: Uri? = data.data
                val selectedImageBitmap: Bitmap
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver,
                        selectedImageUri
                    )
                    profilePic.setImageBitmap(
                        selectedImageBitmap
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}