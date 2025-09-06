package com.example.myapitest.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.MainActivity
import com.example.myapitest.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInHelper: GoogleSignInHelper

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val account = googleSignInHelper.handleSignInResult(result).getOrThrow()
                val authResult = googleSignInHelper.firebaseAuthWithGoogle(account).getOrThrow()
                onSignInSuccess()
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Falha no login: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        googleSignInHelper = GoogleSignInHelper(this)
        
        // Se já estiver logado, vai direto para a MainActivity
        if (googleSignInHelper.getCurrentUser() != null) {
            startMainActivity()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.googleSignInButton.setOnClickListener {
            signInLauncher.launch(googleSignInHelper.getSignInIntent())
        }
    }

    private fun onSignInSuccess() {
        Snackbar.make(binding.root, "Login realizado com sucesso!", Snackbar.LENGTH_SHORT).show()
        startMainActivity()
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
