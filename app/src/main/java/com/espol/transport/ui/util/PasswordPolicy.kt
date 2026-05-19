package com.espol.transport.ui.util

private val STRONG_PASSWORD = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,128}$")

fun isStrongPassword(password: String): Boolean = STRONG_PASSWORD.matches(password)

const val PASSWORD_HINT =
    "Mín. 8 caracteres, con mayúscula, minúscula, número y símbolo (ej. Espol#2026)."
