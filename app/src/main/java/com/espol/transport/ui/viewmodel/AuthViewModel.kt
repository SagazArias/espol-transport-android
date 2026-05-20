package com.espol.transport.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.espol.transport.AppContainer
import com.espol.transport.BuildConfig
import com.espol.transport.data.api.ApiService
import com.espol.transport.data.local.TokenStore
import com.espol.transport.data.model.DriverRegisterRequest
import com.espol.transport.data.model.LoginRequest
import com.espol.transport.data.model.PassengerRegisterRequest
import com.espol.transport.data.model.PassengerSendOtpRequest
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

    fun clearError() {
        errorMessage = null
    }

    fun sendPassengerOtp(email: String, onSuccess: (devCode: String?) -> Unit) {
        busy = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val res = api.sendPassengerOtp(PassengerSendOtpRequest(email.trim().lowercase()))
                val devCode = if (BuildConfig.DEBUG) res.dev_code else null
                onSuccess(devCode)
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
            } finally {
                busy = false
            }
        }
    }

    fun registerPassenger(
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        password: String,
        otp: String,
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
                        otp_code = otp.trim(),
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
