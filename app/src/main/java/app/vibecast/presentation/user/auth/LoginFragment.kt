package app.vibecast.presentation.user.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import app.vibecast.databinding.FragmentLoginBinding
import app.vibecast.presentation.MainActivity
import app.vibecast.presentation.user.auth.util.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class LoginFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signInBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        emailInput = binding.emailInput
        passwordInput = binding.passwordInput
        signInBtn = binding.signInBtn
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.noAccount.setOnClickListener {
            val action = LoginFragmentDirections.loginToRegistration()
            findNavController().navigate(action)
        }
        signInBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val result = validateInput(email, password)
            when (result) {
                LoginResult.INVALID_EMAIL -> {
                    Toast.makeText(
                        requireContext(),
                        "Invalid email", Toast.LENGTH_SHORT
                    ).show()
                }

                LoginResult.NO_PASSWORD -> {
                    Toast.makeText(
                        requireContext(),
                        "Please enter your password", Toast.LENGTH_SHORT
                    ).show()
                }

                LoginResult.SUCCESS -> {
                    signInWithEmail(email, password)
                }
            }
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {

                    Timber.tag("auth").d("signInWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(
                        requireContext(),
                        "Signed in successfully", Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Timber.tag("auth").w("signInWithEmail:failure ${task.exception}")
                    try {
                        throw task.exception!!
                    } catch (invalidCredentials: FirebaseAuthInvalidCredentialsException) {
                        // Handle incorrect password
                        Toast.makeText(requireContext(), "Email and password do not match", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Handle other exceptions
                        Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


    private fun validateInput(email: String, password: String): LoginResult {
        if (!isEmailValid(email)) {
            return LoginResult.INVALID_EMAIL
        }
        return if (!isPasswordCorrect(password)) {
            LoginResult.NO_PASSWORD
        } else {
            LoginResult.SUCCESS
        }
    }


    private fun isEmailValid(email: String): Boolean {
        if (email.isEmpty()) return false
        val validEmail = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return validEmail.matches(email)
    }

    private fun isPasswordCorrect(password: String): Boolean {
        if (password.isEmpty()) return false
        return true
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}