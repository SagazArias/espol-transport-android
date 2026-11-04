package com.espol.transport.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import com.espol.transport.ui.components.Avatar
import com.espol.transport.ui.components.CallButton
import com.espol.transport.ui.components.Pill
import com.espol.transport.ui.components.liquidGlass
import com.espol.transport.ui.components.pressScale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.espol.transport.ui.components.AppScreen
import com.espol.transport.ui.components.AppTextField
import com.espol.transport.ui.components.CardDivider
import com.espol.transport.ui.components.DeleteAccountDialog
import com.espol.transport.ui.components.EmptyState
import com.espol.transport.ui.components.InfoRow
import com.espol.transport.ui.components.InlineMessage
import com.espol.transport.ui.components.LoadingState
import com.espol.transport.ui.components.PrimaryButton
import com.espol.transport.ui.components.SectionCard
import com.espol.transport.ui.components.SectionHeader
import com.espol.transport.ui.viewmodel.AuthViewModel
import com.espol.transport.ui.viewmodel.TransportViewModel

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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Indica el motivo de la cancelación. El otro usuario recibirá este mensaje.")
                    AppTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = "Motivo",
                        singleLine = false,
                        containerColor = MaterialTheme.colorScheme.background, // contrasta con el diálogo en claro y oscuro
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
                ) { Text("Cancelar viaje", color = MaterialTheme.colorScheme.error) }
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

    AppScreen(
        title = "Hola, ${userName.substringBefore(" ")}",
        actions = {
            AccountButton(
                name = userName,
                email = transportVm.currentUser?.email,
                onLogout = onLogout,
                onDeleteAccount = { authVm.clearError(); showDeleteAccount = true },
            )
        },
    ) {
        HeroAction(
            icon = if (isDriver) Icons.Outlined.CalendarMonth else Icons.Outlined.Search,
            title = if (isDriver) "Mi horario semanal" else "Buscar viaje",
            subtitle = if (isDriver) "Tus días, horas y rutas de lunes a viernes"
            else "Conductores hacia ESPOL o de regreso a casa",
            onClick = onPrimaryAction,
        )

        inbox?.alerts?.takeIf { it.isNotEmpty() }?.let { alerts ->
            SectionHeader("Avisos")
            alerts.forEach { AlertCard(it) }
        }

        SectionHeader("Viajes agendados")
        val trips = inbox?.trips.orEmpty()
        when {
            transportVm.busy && inbox == null -> LoadingState()
            trips.isEmpty() -> EmptyState(
                icon = Icons.Outlined.EventAvailable,
                title = "No tienes viajes agendados",
                message = if (isDriver) "Cuando un pasajero agende contigo, lo verás aquí."
                else "Busca un conductor para agendar tu próximo viaje.",
            )
            else -> trips.forEach { trip ->
                TripInboxCard(
                    trip = trip,
                    isDriver = isDriver,
                    busy = transportVm.busy,
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

        InlineMessage(transportVm.errorMessage)
        InlineMessage(authVm.errorMessage)
    }
}

/** Avatar de vidrio con menú de cuenta: cerrar sesión y eliminar cuenta, fuera del flujo principal. */
@Composable
private fun AccountButton(
    name: String,
    email: String?,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    Box {
        Box(Modifier.size(48.dp).pressScale(interaction), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(44.dp)
                    .liquidGlass(CircleShape)
                    .clickable(interaction, indication = null, role = Role.Button) { open = true },
                contentAlignment = Alignment.Center,
            ) {
                Avatar(name, size = 36.dp)
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(name, style = MaterialTheme.typography.titleSmall)
                email?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Cerrar sesión") },
                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
                onClick = { open = false; onLogout() },
            )
            DropdownMenuItem(
                text = { Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                onClick = { open = false; onDeleteAccount() },
            )
        }
    }
}

/** Acción principal de la pantalla como tarjeta grande y táctil, no como un botón más en la pila. */
@Composable
private fun HeroAction(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val c = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .pressScale(interaction, pressed = 0.97f)
            .clip(MaterialTheme.shapes.large)
            .background(Brush.linearGradient(listOf(c.primary, c.primary.copy(alpha = 0.82f))))
            .clickable(interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .size(52.dp)
                .background(c.onPrimary.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = c.onPrimary, modifier = Modifier.size(28.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = c.onPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = c.onPrimary.copy(alpha = 0.85f))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = c.onPrimary)
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
        colors = CardDefaults.cardColors(
            // Azul informativo: los avisos suelen ser buenas noticias (viaje nuevo), no errores.
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null)
            Text(alert.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TripInboxCard(
    trip: TripInboxItem,
    isDriver: Boolean,
    busy: Boolean,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(trip.counterparty_name)
            Column(Modifier.weight(1f)) {
                Text(trip.counterparty_name, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (isDriver) "Pasajero" else "Conductor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Pill("$${"%.2f".format(trip.trip_fare_usd)}")
        }
        Text(
            trip.route_summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        if (!isDriver) {
            CardDivider()
            InfoRow(
                "Vehículo",
                listOfNotNull(trip.vehicle_brand, trip.vehicle_model).joinToString(" ").ifBlank { "—" },
            )
            InfoRow("Placa", trip.vehicle_plate ?: "—")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CallButton(trip.counterparty_phone)
            OutlinedButton(
                onClick = onCancel,
                enabled = !busy,
                modifier = Modifier.weight(1f).heightIn(min = 52.dp),
            ) { Text("Cancelar", color = MaterialTheme.colorScheme.error) }
            PrimaryButton("Concluido", onClick = onComplete, enabled = !busy, modifier = Modifier.weight(1f))
        }
    }
}
