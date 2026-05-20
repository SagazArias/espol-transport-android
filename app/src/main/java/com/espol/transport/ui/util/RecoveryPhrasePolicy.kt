package com.espol.transport.ui.util

private val RECOVERY_PHRASE = Regex("^[^\\s]{4,64}$")

fun isValidRecoveryPhrase(phrase: String): Boolean {
    if (phrase != phrase.trim() || phrase.any { it.isWhitespace() }) return false
    return RECOVERY_PHRASE.matches(phrase)
}

const val RECOVERY_PHRASE_HINT =
    "Solo para recuperar tu contraseña si la olvidas. Elige una palabra o frase corta que solo tú conozcas " +
        "(no es tu contraseña). La pediremos al restablecer el acceso."
