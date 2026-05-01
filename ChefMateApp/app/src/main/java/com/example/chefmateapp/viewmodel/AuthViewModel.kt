package com.example.chefmateapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefmateapp.data.model.auth.RegisterRequest
import com.example.chefmateapp.data.model.auth.User
import com.example.chefmateapp.data.repository.AuthRepository
import com.example.chefmateapp.data.remote.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode.asStateFlow()

    private val _loginEmail = MutableStateFlow("")
    val loginEmail: StateFlow<String> = _loginEmail.asStateFlow()

    private val _loginPassword = MutableStateFlow("")
    val loginPassword: StateFlow<String> = _loginPassword.asStateFlow()

    private val _registerEmail = MutableStateFlow("")
    val registerEmail: StateFlow<String> = _registerEmail.asStateFlow()

    private val _registerPassword = MutableStateFlow("")
    val registerPassword: StateFlow<String> = _registerPassword.asStateFlow()

    private val _registerName = MutableStateFlow("")
    val registerName: StateFlow<String> = _registerName.asStateFlow()

    private val _captchaResponse = MutableStateFlow("")
    val captchaResponse: StateFlow<String> = _captchaResponse.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _loginGeneralError = MutableStateFlow<String?>(null)
    val loginGeneralError: StateFlow<String?> = _loginGeneralError.asStateFlow()

    private val _registerGeneralError = MutableStateFlow<String?>(null)
    val registerGeneralError: StateFlow<String?> = _registerGeneralError.asStateFlow()

    private val _passwordStrength = MutableStateFlow(0f)
    val passwordStrength: StateFlow<Float> = _passwordStrength.asStateFlow()

    val isLoginValid: StateFlow<Boolean> = combine(_loginEmail, _loginPassword, _captchaResponse) { email, password, captcha ->
        isValidEmail(email) && password.length >= 6 && captcha.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isRegisterValid: StateFlow<Boolean> = combine(_registerName, _registerEmail, _registerPassword) { name, email, password ->
        name.isNotEmpty() && isValidEmail(email) && password.length >= 6
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onLoginEmailChange(value: String) {
        _loginEmail.value = value
        _emailError.value = null
        _loginGeneralError.value = null
    }

    fun onLoginPasswordChange(value: String) {
        _loginPassword.value = value
        _passwordError.value = null
        _loginGeneralError.value = null
    }

    fun onRegisterEmailChange(value: String) {
        _registerEmail.value = value
        _emailError.value = null
        _registerGeneralError.value = null
    }

    fun onRegisterPasswordChange(value: String) {
        _registerPassword.value = value
        _passwordError.value = null
        _registerGeneralError.value = null
        calculatePasswordStrength(value)
    }

    fun onRegisterNameChange(value: String) {
        _registerName.value = value
        _registerGeneralError.value = null
    }

    fun onCaptchaChange(value: String) {
        _captchaResponse.value = value
        _loginGeneralError.value = null
    }

    fun toggleMode() {
        _isLoginMode.value = !_isLoginMode.value
        clearErrors()
    }

    private fun clearErrors() {
        _emailError.value = null
        _passwordError.value = null
        _loginGeneralError.value = null
        _registerGeneralError.value = null
    }

    private fun calculatePasswordStrength(password: String) {
        var strength = 0f
        if (password.length >= 8) strength += 0.25f
        if (password.any { it.isUpperCase() }) strength += 0.25f
        if (password.any { it.isDigit() }) strength += 0.25f
        if (password.any { !it.isLetterOrDigit() }) strength += 0.25f
        _passwordStrength.value = strength
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun login() {
        viewModelScope.launch {
            _isLoading.value = true
            _loginGeneralError.value = null
            try {
                val response = repository.login(_loginEmail.value, _loginPassword.value, _captchaResponse.value)
                if (response.isSuccessful && response.body() != null) {
                    val authBody = response.body()!!
                    val access = authBody.accessToken
                    val refresh = authBody.refreshToken
                    if (access != null && refresh != null) {
                        NetworkModule.setTokens(access, refresh)
                    }
                    _loginSuccess.value = true
                    fetchUserProfile()
                } else {
                    _loginGeneralError.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _loginGeneralError.value = "Error de red: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val response = repository.getCurrentUser()
                if (response.isSuccessful) {
                    _currentUser.value = response.body()
                }
            } catch (e: Exception) {
                // Silently fail or handle profile fetch error
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            _isLoading.value = true
            _registerGeneralError.value = null
            try {
                val response = repository.register(RegisterRequest(_registerName.value, _registerEmail.value, _registerPassword.value))
                if (response.isSuccessful) {
                    _registerGeneralError.value = "Registro exitoso. Por favor inicia sesión."
                } else {
                    _registerGeneralError.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _registerGeneralError.value = "Error de red: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.logout()
                NetworkModule.clearTokens()
                _loginSuccess.value = false
                _currentUser.value = null
                onSuccess()
            } catch (e: Exception) {
                NetworkModule.clearTokens()
                _loginSuccess.value = false
                _currentUser.value = null
                onSuccess()
            }
        }
    }
}