package com.espol.transport.ui.util

private val STRONG_PASSWORD = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,128}$")

fun isStrongPassword(password: String): Boolean {
    if (password != password.trim() || password.contains(' ')) return false
    return STRONG_PASSWORD.matches(password)
}

const val PASSWORD_HINT =
    "Mín. 8 caracteres, mayúscula, minúscula, número y símbolo, sin espacios (ej. Espol#2026)."
