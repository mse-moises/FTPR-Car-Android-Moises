package com.example.myapitest.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.MainActivity
import com.example.myapitest.databinding.ActivityPhoneAuthBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneAuthBinding
    private lateinit var phoneAuthHelper: PhoneAuthHelper
    private var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phoneAuthHelper = PhoneAuthHelper(this)

        if (phoneAuthHelper.getCurrentUser() != null) {
            startMainActivity()
            return
        }

        setupClickListeners()
        setupPhoneInput()
    }

    private fun setupClickListeners() {
        binding.sendCodeButton.setOnClickListener {
            phoneNumber = binding.phoneInput.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                startPhoneVerification(phoneNumber)
            } else {
                showError("Por favor, insira um número de telefone válido")
            }
        }

        binding.verifyCodeButton.setOnClickListener {
            val code = binding.codeInput.text.toString().trim()
            if (code.isNotEmpty()) {
                verifyPhoneNumberWithCode(code)
            } else {
                showError("Por favor, insira o código de verificação")
            }
        }

        binding.resendCodeButton.setOnClickListener {
            if (phoneNumber.isNotEmpty()) {
                resendVerificationCode(phoneNumber)
            }
        }

        binding.switchToGoogleButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun startPhoneVerification(phoneNumber: String) {
        binding.sendCodeButton.isEnabled = false
        binding.phoneInput.isEnabled = false

        phoneAuthHelper.startPhoneNumberVerification(
            phoneNumber = phoneNumber,
            onCodeSent = { verificationId ->
                binding.sendCodeButton.isEnabled = true
                binding.phoneInput.isEnabled = true
                showVerificationCodeUI()
                Snackbar.make(binding.root, "Código enviado com sucesso!", Snackbar.LENGTH_LONG).show()
            },
            onVerificationCompleted = { credential ->
                signInWithPhoneAuthCredential(credential)
            },
            onVerificationFailed = { e ->
                binding.sendCodeButton.isEnabled = true
                binding.phoneInput.isEnabled = true
                showError("Falha na verificação: ${e.message}")
            }
        )
    }

    private fun verifyPhoneNumberWithCode(code: String) {
        val credential = phoneAuthHelper.verifyPhoneNumberWithCode(code)
        if (credential != null) {
            signInWithPhoneAuthCredential(credential)
        } else {
            showError("Erro ao verificar o código")
        }
    }

    private fun resendVerificationCode(phoneNumber: String) {
        binding.resendCodeButton.isEnabled = false

        phoneAuthHelper.resendVerificationCode(
            phoneNumber = phoneNumber,
            onCodeSent = { verificationId ->
                binding.resendCodeButton.isEnabled = true
                Snackbar.make(binding.root, "Código reenviado com sucesso!", Snackbar.LENGTH_LONG).show()
            },
            onVerificationFailed = { e ->
                binding.resendCodeButton.isEnabled = true
                showError("Falha ao reenviar o código: ${e.message}")
            }
        )
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                phoneAuthHelper.signInWithPhoneAuthCredential(credential).getOrThrow()
                startMainActivity()
            } catch (e: Exception) {
                showError("Falha no login: ${e.message}")
            }
        }
    }

    private fun showVerificationCodeUI() {
        binding.codeInputLayout.visibility = View.VISIBLE
        binding.verifyCodeButton.visibility = View.VISIBLE
        binding.resendCodeButton.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun setupPhoneInput() {
        // Posiciona o cursor após o +55
        binding.phoneInput.post {
            binding.phoneInput.setSelection(binding.phoneInput.text?.length ?: 3)
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
