package com.espol.transport

import com.espol.transport.ui.driver.DayScheduleUi
import com.espol.transport.ui.util.isStrongPassword
import com.espol.transport.ui.util.isValidRecoveryPhrase
import com.espol.transport.ui.util.toUserMessage
import com.espol.transport.ui.viewmodel.TransportViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class PasswordPolicyTest {
    @Test fun aceptaContrasenaFuerte() = assertTrue(isStrongPassword("Espol#2026"))

    @Test fun rechazaContrasenasDebiles() {
        listOf("corta1!", "sinmayuscula1!", "SINMINUSCULA1!", "SinNumero!!", "SinSimbolo123", "Con espacio1!", " Espol#2026")
            .forEach { assertFalse("debería rechazar '$it'", isStrongPassword(it)) }
    }

    @Test fun fraseDeRecuperacionSinEspaciosDe4a64() {
        assertTrue(isValidRecoveryPhrase("clave-prueba"))
        assertFalse(isValidRecoveryPhrase("abc"))
        assertFalse(isValidRecoveryPhrase("dos palabras"))
        assertFalse(isValidRecoveryPhrase("x".repeat(65)))
    }
}

class FormatoTest {
    @Test fun horaParaLaApi() {
        assertEquals("08:30:00", TransportViewModel.toApiTime("08:30"))
        assertEquals("08:30:00", TransportViewModel.toApiTime("08:30:00"))
    }

    @Test fun horaDeLaApiParaChips() {
        assertEquals("09:00", TransportViewModel.formatTimeForChip("09:00:00"))
        assertNull(TransportViewModel.formatTimeForChip(null))
        assertNull(TransportViewModel.formatTimeForChip(" "))
    }

    @Test fun tarifaConDosDecimales() = assertEquals("1.50", TransportViewModel.formatFare(1.5).replace(',', '.'))
}

class DayScheduleUiTest {
    @Test fun diaVacioDiceSinViajes() = assertEquals("Sin viajes", DayScheduleUi(0).summary())

    @Test fun resumenConIdaYRegreso() {
        val d = DayScheduleUi(1).apply { idaTime = "09:00"; regresoTime = "17:00" }
        assertEquals("Ida 09:00 · Regreso 17:00", d.summary())
    }

    @Test fun rutaSinHoraCuentaComoViaje() {
        val d = DayScheduleUi(2).apply { idaRoute = listOf("La Joya") }
        assertTrue(d.hasIda)
        assertEquals("Ida sin hora", d.summary())
    }
}

class ErroresTest {
    private fun http(code: Int, body: String) =
        HttpException(Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())))

    @Test fun usaElDetalleDeFastApi() =
        assertEquals("Los cupos deben ser entre 1 y 20.", http(400, """{"detail":"Los cupos deben ser entre 1 y 20."}""").toUserMessage())

    @Test fun timeoutDelServidorDespertandoEnEspanol() =
        assertTrue(SocketTimeoutException("timeout").toUserMessage().startsWith("El servidor tardó en responder"))

    @Test fun sinRedEnEspanol() =
        assertTrue(IOException("Unable to resolve host").toUserMessage().startsWith("Sin conexión con el servidor"))
}
