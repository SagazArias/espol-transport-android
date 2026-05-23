package com.espol.transport.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.espol.transport.AppContainer
import com.espol.transport.data.api.ApiService
import com.espol.transport.data.local.TokenStore
import com.espol.transport.data.model.AccountDeleteRequest
import com.espol.transport.data.model.DriverRegisterRequest
import com.espol.transport.data.model.LoginRequest
import com.espol.transport.data.model.PassengerRegisterRequest
import com.espol.transport.data.model.PasswordRecoveryResetRequest
import com.espol.transport.data.model.PasswordRecoveryVerifyRequest
import com.espol.transport.data.model.UserMe
import com.espol.transport.ui.util.toUserMessage
import kotlinx.coroutines.launch

class AuthViewModel(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) : ViewModel() {

    var busy by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
    var pendingResetToken by mutableStateOf<String?>(null)

    fun clearError() {
        errorMessage = null
    }

    fun registerPassenger(
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        password: String,
        recoveryPhrase: String,
        onSuccess: () -> Unit,
    ) {
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                api.registerPassenger(
                    PassengerRegisterRequest(
                        first_name = firstName.trim(),
                        last_name = lastName.trim(),
                        phone = phone.trim(),
                        email = email.trim().lowercase(),
                        password = password,
                        recovery_phrase = recoveryPhrase,
                    ),
                )
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun registerDriver(
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        password: String,
        recoveryPhrase: String,
        plate: String,
        brand: String,
        model: String,
        onSuccess: () -> Unit,
    ) {
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                api.registerDriver(
                    DriverRegisterRequest(
                        first_name = firstName.trim(),
                        last_name = lastName.trim(),
                        phone = phone.trim(),
                        email = email.trim().lowercase(),
                        password = password,
                        recovery_phrase = recoveryPhrase,
                        plate = plate.trim(),
                        brand = brand.trim(),
                        model = model.trim(),
                    ),
                )
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    suspend fun hasPersistedSession(): Boolean = tokenStore.shouldAutoLogin()

    fun verifyRecoveryPhrase(email: String, recoveryPhrase: String, onSuccess: () -> Unit) {
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val res = api.verifyPasswordRecovery(
                    PasswordRecoveryVerifyRequest(
                        email = email.trim().lowercase(),
                        recovery_phrase = recoveryPhrase,
                    ),
                )
                pendingResetToken = res.reset_token
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun resetPassword(newPassword: String, onSuccess: () -> Unit) {
        val token = pendingResetToken
        if (token.isNullOrBlank()) {
            errorMessage = "Sesión de recuperación expirada. Vuelve a ingresar tu palabra clave."
            return
        }
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                api.resetPassword(PasswordRecoveryResetRequest(reset_token = token, new_password = newPassword))
                pendingResetToken = null
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun login(email: String, password: String, stayLoggedIn: Boolean, onSuccess: () -> Unit) {
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val token = api.login(LoginRequest(email.trim().lowercase(), password))
                tokenStore.saveToken(token.access_token, stayLoggedIn)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun deleteAccount(password: String, onSuccess: () -> Unit) {
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                api.deleteAccount(AccountDeleteRequest(password))
                tokenStore.clear()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    suspend fun fetchMe(): UserMe = api.me()

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            tokenStore.clear()
            onDone()
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(container.api, container.tokenStore) as T
        }
    }
}
