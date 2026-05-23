package com.espol.transport.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espol.transport.data.model.InboxAlert
import com.espol.transport.data.model.TripBookingCompleteOut
import com.espol.transport.data.model.TripInboxItem
import com.espol.transport.ui.components.DeleteAccountDialog
import com.espol.transport.ui.viewmodel.AuthViewModel
import com.espol.transport.ui.viewmodel.TransportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    transportVm: TransportViewModel,
    authVm: AuthViewModel,
    userName: String,
    isDriver: Boolean,
    onPrimaryAction: () -> Unit,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit,
) {
    var refreshKey by remember { mutableStateOf(0) }
    var cancelTarget by remember { mutableStateOf<TripInboxItem?>(null) }
    var cancelReason by remember { mutableStateOf("") }
    var ratingBookingId by remember { mutableStateOf<String?>(null) }
    var ratingStars by remember { mutableIntStateOf(0) }
    var showDeleteAccount by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    LaunchedEffect(refreshKey) {
        transportVm.loadInbox()
    }

    val inbox = transportVm.inbox

    if (cancelTarget != null) {
        AlertDialog(
            onDismissRequest = { cancelTarget = null; cancelReason = "" },
            title = { Text("Cancelar viaje") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Indica el motivo de la cancelación. El otro usuario recibirá este mensaje.")
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Motivo") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = cancelTarget!!.id
                        transportVm.cancelTrip(id, cancelReason) {
                            cancelTarget = null
                            cancelReason = ""
                            refreshKey++
                        }
                    },
                    enabled = cancelReason.trim().length >= 3,
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null; cancelReason = "" }) { Text("Volver") }
            },
        )
    }

    if (showDeleteAccount) {
        DeleteAccountDialog(
            password = deletePassword,
            onPasswordChange = { deletePassword = it.filter { ch -> !ch.isWhitespace() } },
            busy = authVm.busy,
            onConfirm = {
                authVm.deleteAccount(deletePassword) {
                    showDeleteAccount = false
                    deletePassword = ""
                    onAccountDeleted()
                }
            },
            onDismiss = {
                if (!authVm.busy) {
                    showDeleteAccount = false
                    deletePassword = ""
                    authVm.clearError()
                }
            },
        )
    }

    if (ratingBookingId != null) {
        AlertDialog(
            onDismissRequest = {
                ratingBookingId = null
                ratingStars = 0
            },
            title = { Text("Califica al conductor") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "¿Cómo fue tu experiencia en este viaje?",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    StarRatingPicker(stars = ratingStars, onStarsChange = { ratingStars = it })
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = ratingBookingId!!
                        transportVm.rateDriver(id, ratingStars) {
                            ratingBookingId = null
                            ratingStars = 0
                            refreshKey++
                        }
                    },
                    enabled = ratingStars in 1..5,
                ) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { ratingBookingId = null; ratingStars = 0 }) { Text("Omitir") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bandeja — $userName") },
                actions = {
                    TextButton(onClick = onLogout) { Text("Salir") }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = onPrimaryAction, modifier = Modifier.fillMaxWidth()) {
                Text(if (isDriver) "Modificar horario de viajes" else "Buscar viaje")
            }

            if (transportVm.busy && inbox == null) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            }

            inbox?.alerts?.takeIf { it.isNotEmpty() }?.let { alerts ->
                Text("Avisos", style = MaterialTheme.typography.titleMedium)
                alerts.forEach { alert ->
                    AlertCard(alert)
                }
            }

            Text("Viajes agendados", style = MaterialTheme.typography.titleMedium)
            val trips = inbox?.trips.orEmpty()
            if (trips.isEmpty()) {
                Text(
                    "No tienes viajes agendados por ahora.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                trips.forEach { trip ->
                    TripInboxCard(
                        trip = trip,
                        isDriver = isDriver,
                        onCancel = { cancelTarget = trip },
                        onComplete = {
                            transportVm.completeTrip(trip.id) { result ->
                                refreshKey++
                                handleCompleteResult(result, isDriver) { bookingId ->
                                    ratingBookingId = bookingId
                                }
                            }
                        },
                    )
                }
            }

            transportVm.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            authVm.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            HorizontalDivider()
            OutlinedButton(
                onClick = { authVm.clearError(); showDeleteAccount = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Eliminar mi cuenta", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun handleCompleteResult(
    result: TripBookingCompleteOut,
    isDriver: Boolean,
    onPromptRating: (String) -> Unit,
) {
    if (!isDriver && result.prompt_passenger_rating) {
        onPromptRating(result.booking_id)
    }
}

@Composable
private fun StarRatingPicker(stars: Int, onStarsChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            IconButton(onClick = { onStarsChange(i) }) {
                Icon(
                    imageVector = if (i <= stars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "$i estrellas",
                    tint = if (i <= stars) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AlertCard(alert: InboxAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Text(
            alert.message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TripInboxCard(
    trip: TripInboxItem,
    isDriver: Boolean,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (isDriver) "Pasajero: ${trip.counterparty_name}" else "Conductor: ${trip.counterparty_name}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(trip.route_summary, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
            Text("Tarifa: $${"%.2f".format(trip.trip_fare_usd)}")
            Text("Celular: ${trip.counterparty_phone ?: "—"}")
            if (!isDriver) {
                Text("Vehículo: ${listOfNotNull(trip.vehicle_brand, trip.vehicle_model).joinToString(" ")}")
                Text("Placa: ${trip.vehicle_plate ?: "—"}")
            }
            HorizontalDivider()
            Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                Text("Viaje concluido")
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar viaje")
            }
        }
    }
}
