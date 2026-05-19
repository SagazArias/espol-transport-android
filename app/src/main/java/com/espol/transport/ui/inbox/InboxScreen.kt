package com.espol.transport.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espol.transport.data.model.InboxAlert
import com.espol.transport.data.model.TripInboxItem
import com.espol.transport.ui.viewmodel.TransportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    transportVm: TransportViewModel,
    userName: String,
    isDriver: Boolean,
    onPrimaryAction: () -> Unit,
    onLogout: () -> Unit,
) {
    var refreshKey by remember { mutableStateOf(0) }
    var cancelTarget by remember { mutableStateOf<TripInboxItem?>(null) }
    var cancelReason by remember { mutableStateOf("") }

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
                    )
                }
            }

            transportVm.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
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
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar viaje")
            }
        }
    }
}
