package com.espol.transport.data.model

data class PassengerRegisterRequest(
    val first_name: String,
    val last_name: String,
    val phone: String,
    val email: String,
    val password: String,
    val recovery_phrase: String,
)

data class DriverRegisterRequest(
    val first_name: String,
    val last_name: String,
    val phone: String,
    val email: String,
    val password: String,
    val recovery_phrase: String,
    val plate: String,
    val brand: String,
    val model: String,
)

data class LoginRequest(val email: String, val password: String)

data class TokenResponse(val access_token: String, val token_type: String)

data class AccountDeleteRequest(val password: String)

data class PasswordRecoveryVerifyRequest(
    val email: String,
    val recovery_phrase: String,
)

data class PasswordRecoveryVerifyResponse(
    val reset_token: String,
    val expires_in_minutes: Int,
)

data class PasswordRecoveryResetRequest(
    val reset_token: String,
    val new_password: String,
)

data class PasswordRecoveryResetResponse(val message: String)

data class UserMe(
    val id: String,
    val email: String,
    val full_name: String,
    val first_name: String?,
    val last_name: String?,
    val phone: String?,
    val espol_verified_at: String?,
    val role: String,
    val espol_student_verified: Boolean,
)
