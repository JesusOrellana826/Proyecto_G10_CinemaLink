package com.example.proyecto_g10_cinemalink.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto_g10_cinemalink.R
import com.example.proyecto_g10_cinemalink.databinding.FragmentLoginBinding
import com.example.proyecto_g10_cinemalink.utils.SessionManager

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    data class ValidUser(val email: String, val password: String, val name: String)

    private val validUsers = listOf(
        ValidUser("admin@movie.com", "123456", "Jesús Orelana"),
        ValidUser("jesus@movie.com", "password", "Jesús"),
        ValidUser("user@movie.com", "user123", "Usuario Demo")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())


        if (sessionManager.isLoggedIn()) {
            navigateToHome()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                login(email, password)
            }
        }
        binding.btnGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Ingrese su email"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email inválido"
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Ingrese su contraseña"
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Mínimo 6 caracteres"
            return false
        }

        return true
    }

    private fun login(email: String, password: String) {

        val user = validUsers.find {
            it.email == email && it.password == password
        } ?: getRegisteredUser(email, password)

        if (user != null) {
            sessionManager.saveSession(user.email, user.name)
            Toast.makeText(context, "Bienvenido ${user.name}", Toast.LENGTH_SHORT).show()
            navigateToHome()
        } else {
            Toast.makeText(context, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getRegisteredUser(email: String, password: String): ValidUser? {

        val sharedPref = requireContext().getSharedPreferences("users", 0)
        val userData = sharedPref.getString(email, null)

        return if (userData != null) {
            val parts = userData.split(",")
            if (parts.size == 2 && parts[1] == password) {
                ValidUser(email, password, parts[0])
            } else null
        } else null
    }
    private fun navigateToHome() {
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}