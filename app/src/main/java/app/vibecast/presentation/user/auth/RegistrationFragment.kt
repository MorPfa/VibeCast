package app.vibecast.presentation.user.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import app.vibecast.databinding.FragmentRegistrationBinding
import app.vibecast.presentation.MainActivity
import app.vibecast.presentation.user.auth.util.RegistrationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class RegistrationFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var submitBtn: Button

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
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        emailInput = binding.emailInput
        passwordInput = binding.passwordInput
        confirmPasswordInput = binding.confirmPasswordInput
        submitBtn = binding.submitBtn
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        submitBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password1 = passwordInput.text.toString()
            val password2 = confirmPasswordInput.text.toString()
            val result = validateInput(email, password1, password2)
            when (result) {
                RegistrationResult.INVALID_EMAIL -> {
                    Toast.makeText(
                        requireContext(),
                        "Invalid email address", Toast.LENGTH_SHORT
                    ).show()
                }

                RegistrationResult.PASSWORD_MISMATCH -> {
                    Toast.makeText(
                        requireContext(),
                        "Passwords don't match", Toast.LENGTH_SHORT
                    ).show()
                }
                RegistrationResult.EMPTY_PASSWORD -> {
                    Toast.makeText(
                        requireContext(),
                        "Please enter a password", Toast.LENGTH_SHORT
                    ).show()
                }
                RegistrationResult.EMPTY_EMAIL -> {
                    Toast.makeText(
                        requireContext(),
                        "Please enter an email address", Toast.LENGTH_SHORT
                    ).show()
                }
                RegistrationResult.PASSWORD_TOO_SHORT -> {
                    Toast.makeText(
                        requireContext(),
                        "Password needs to be at least 6 characters", Toast.LENGTH_SHORT
                    ).show()
                }
                RegistrationResult.SUCCESS -> {
                    createAccount(email, password1)
            }
        }
    }
}

    private fun createAccount(email : String, password : String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Timber.tag("auth").d("createUserWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(
                        requireContext(),
                        "Created Account successfully", Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.tag("auth").w("createUserWithEmail:failure ${task.exception}")
                    Toast.makeText(
                        requireContext(),
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
//                    updateUI(null)
                }
            }
    }

private fun validateInput(email: String, password1: String, password2: String): RegistrationResult {
    if (password1.isEmpty()) return RegistrationResult.EMPTY_PASSWORD
    if(password1.length < 6) return RegistrationResult.PASSWORD_TOO_SHORT
    if(email.isEmpty()) return RegistrationResult.EMPTY_EMAIL
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
    return if(password1.length < 6 || password2.length < 6) false
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