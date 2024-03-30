package app.vibecast.presentation.user.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import app.vibecast.R
import app.vibecast.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint



class LoginActivity : AppCompatActivity() {


    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        supportFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, LoginFragment())
            .commit()


//        binding.backBtn.setOnClickListener{
//            val intent = Intent(this, MainActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            startActivity(intent)
//            finish()
//        }
        }

    }
