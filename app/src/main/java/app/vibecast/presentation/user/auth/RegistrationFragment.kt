package app.vibecast.presentation.user.auth


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import app.vibecast.R
import app.vibecast.databinding.FragmentRegistrationBinding
import app.vibecast.presentation.MainActivity
import app.vibecast.presentation.screens.account_screen.AccountViewModel
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import app.vibecast.presentation.user.auth.util.RegistrationResult
import com.google.android.gms.common.SignInButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


@AndroidEntryPoint
class RegistrationFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private val accountViewModel: AccountViewModel by activityViewModels()
    private val imageViewModel: ImageViewModel by activityViewModels()
    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var submitBtn: Button
    private lateinit var userNameInput: EditText
    private lateinit var googleSignINBtn: SignInButton

    private var listener: OnGoogleSignUpClickListener? = null


    interface OnGoogleSignUpClickListener {
        fun onGoogleSignUpClick()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnGoogleSignUpClickListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        googleSignINBtn = binding.signInWithGoogle



        googleSignINBtn.setOnClickListener {
            listener?.onGoogleSignUpClick()

        }

        val bgImage = imageViewModel.pickDefaultBackground()
        binding.backgroundImageView.setImageResource(bgImage)
        emailInput = binding.emailInput
        passwordInput = binding.passwordInput
        confirmPasswordInput = binding.confirmPasswordInput
        submitBtn = binding.submitBtn
        userNameInput = binding.usernameInput

        return binding.root
    }

    private fun showSnackBar(text: String) {
        val snackBar = Snackbar.make(
            requireView(),
            text,
            Snackbar.LENGTH_SHORT
        )
        val snackBarView = snackBar.view
        val snackBarText =
            snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackBarText.gravity = Gravity.CENTER
        snackBarText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        snackBarView.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.snackbar_background)
        snackBar.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        submitBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val userName = userNameInput.text.toString()
            val password1 = passwordInput.text.toString()
            val password2 = confirmPasswordInput.text.toString()
            val result = validateInput(userName, email, password1, password2)
            when (result) {
                RegistrationResult.INVALID_EMAIL ->
                    showSnackBar("Invalid email address")

                RegistrationResult.PASSWORD_MISMATCH ->
                    showSnackBar("Passwords don't match")

                RegistrationResult.EMPTY_PASSWORD ->
                    showSnackBar("Please enter a password")

                RegistrationResult.EMPTY_USERNAME ->
                    showSnackBar("Please enter a username")

                RegistrationResult.EMPTY_EMAIL ->
                    showSnackBar("Please enter an email address")

                RegistrationResult.PASSWORD_TOO_SHORT ->
                    showSnackBar("Password needs to be at least 6 characters")

                RegistrationResult.SUCCESS -> {
                    createAccount(userName, email, password1)
                }
            }
        }
    }

    private fun createAccount(userName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Timber.tag("auth").d("createUserWithEmail:success")
                    val user = auth.currentUser
                    lifecycleScope.launch {
                        accountViewModel.addUserName(user, userName)
                    }

                    accountViewModel.userName.observe(viewLifecycleOwner) {
                        if (it != null) {
                            showSnackBar("Created Account successfully")
                            val intent = Intent(activity, MainActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Timber.tag("auth").w("createUserWithEmail:failure ${task.exception}")
                    showSnackBar("Authentication failed.")
                }
            }
    }

    private fun validateInput(
        userName: String,
        email: String,
        password1: String,
        password2: String,
    ): RegistrationResult {
        if (userName.isEmpty()) return RegistrationResult.EMPTY_USERNAME
        if (password1.isEmpty()) return RegistrationResult.EMPTY_PASSWORD
        if (password1.length < 6) return RegistrationResult.PASSWORD_TOO_SHORT
        if (email.isEmpty()) return RegistrationResult.EMPTY_EMAIL
        if (!doPasswordsMatch(password1, password2)) {
            return RegistrationResult.PASSWORD_MISMATCH
        }
        return if (!isEmailValid(email)) {
            RegistrationResult.INVALID_EMAIL
        } else {
            RegistrationResult.SUCCESS
        }
    }

    private fun doPasswordsMatch(password1: String, password2: String): Boolean {
        return if (password1.length < 6 || password2.length < 6) false
        else {
            password1 == password2
        }

    }

    private fun isEmailValid(email: String): Boolean {
        val validEmail = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")

        return validEmail.matches(email)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegistrationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}