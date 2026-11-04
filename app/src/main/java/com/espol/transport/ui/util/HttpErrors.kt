package com.espol.transport.ui.util

import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

fun Throwable.toUserMessage(): String {
    if (this is HttpException) {
        val raw = response()?.errorBody()?.use { it.string() }
        if (!raw.isNullOrBlank()) {
            parseApiDetail(raw)?.let { return it }
            val trimmed = raw.trim()
            if (trimmed.length < 400 && !trimmed.equals("Internal Server Error", ignoreCase = true)) {
                return trimmed
            }
        }
        return when (code()) {
            500 -> "Error interno del servidor. Revisa que la API y PostgreSQL estén activos (ver consola de uvicorn)."
            502 -> "No se pudo enviar el correo / servicio externo falló."
            409 -> "Ya existe una cuenta con ese correo."
            401 -> "Correo o contraseña incorrectos."
            else -> "Error del servidor (${code()})"
        }
    }
    return when (this) {
        // El servidor gratuito de Render se duerme y tarda en despertar: no es un error del usuario.
        is SocketTimeoutException -> "El servidor tardó en responder. Puede estar iniciando; intenta de nuevo en unos segundos."
        is IOException -> "Sin conexión con el servidor. Revisa tu internet e intenta de nuevo."
        else -> localizedMessage ?: "Ocurrió un error inesperado."
    }
}

private fun parseApiDetail(raw: String): String? {
    return try {
        val json = JsonParser.parseString(raw)
        if (!json.isJsonObject) return null
        val obj = json.asJsonObject
        when {
            obj.has("detail") && obj.get("detail").isJsonPrimitive -> obj.get("detail").asString
            obj.has("detail") && obj.get("detail").isJsonArray -> {
                obj.getAsJsonArray("detail").joinToString("\n") { el ->
                    if (el.isJsonObject) {
                        val o = el.asJsonObject
                        val msg = o.get("msg")?.asString ?: el.toString()
                        val loc = o.getAsJsonArray("loc")?.joinToString(".") { it.asString } ?: ""
                        if (loc.isNotBlank()) "$loc: $msg" else msg
                    } else {
                        el.toString()
                    }
                }
            }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}
