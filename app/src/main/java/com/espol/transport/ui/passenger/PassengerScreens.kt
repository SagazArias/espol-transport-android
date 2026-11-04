package com.espol.transport.ui.passenger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.espol.transport.data.model.PassengerMatchResponse
import com.espol.transport.data.model.PassengerMatchResult
import com.espol.transport.ui.components.Avatar
import com.espol.transport.ui.components.CallButton
import com.espol.transport.ui.components.Pill
import com.espol.transport.ui.components.SectorSuggestions
import com.espol.transport.ui.components.TimeChips
import com.espol.transport.ui.components.AppScreen
import com.espol.transport.ui.components.AppTextField
import com.espol.transport.ui.components.CardDivider
import com.espol.transport.ui.components.EmptyState
import com.espol.transport.ui.components.InfoRow
import com.espol.transport.ui.components.InlineMessage
import com.espol.transport.ui.components.LoadingState
import com.espol.transport.ui.components.PrimaryButton
import com.espol.transport.ui.components.SectionCard
import com.espol.transport.ui.components.SectionHeader
import com.espol.transport.ui.viewmodel.TransportViewModel
import java.time.DayOfWeek
import java.time.LocalDate

/** Lunes–viernes (índice 0–4, convención API). */
val WEEKDAY_LABELS = listOf("Lun", "Mar", "Mié", "Jue", "Vie")

fun todayWeekdayPython(): Int {
    val d = LocalDate.now().dayOfWeek
    return when (d) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        else -> 0
    }.coerceIn(0, 4)
}

/** Control segmentado de una sola opción (equivalente Material del segmented control de iOS). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SegmentedPicker(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                icon = {},
                label = { Text(label, maxLines = 1) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PassengerSearchScreen(
    transportVm: TransportViewModel,
    onBack: () -> Unit,
    onResults: () -> Unit,
) {
    var direction by rememberSaveable { mutableStateOf("ida") }
    var homeSector by rememberSaveable { mutableStateOf("") }
    var weekday by rememberSaveable { mutableIntStateOf(todayWeekdayPython()) }
    var selectedTime by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (transportVm.tripTimes == null) {
            transportVm.loadTripTimes()
        }
        transportVm.loadZones()
    }

    val times = if (direction == "ida") {
        transportVm.tripTimes?.ida.orEmpty()
    } else {
        transportVm.tripTimes?.regreso.orEmpty()
    }

    AppScreen(
        title = "Buscar viaje",
        onBack = onBack,
        floatingBar = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InlineMessage(transportVm.errorMessage, solid = true)
                PrimaryButton(
                    text = "Buscar coincidencias",
                    loading = transportVm.busy,
                    onClick = {
                        transportVm.searchMatches(direction, homeSector, weekday, selectedTime) {
                            onResults()
                        }
                    },
                    enabled = homeSector.isNotBlank() && selectedTime.isNotBlank(),
                )
            }
        },
    ) {
        SectionHeader("Tipo de viaje")
        SegmentedPicker(
            options = listOf("ida" to "Ida a ESPOL", "regreso" to "Regreso a casa"),
            selected = direction,
            onSelect = {
                direction = it
                selectedTime = ""
            },
        )

        SectionHeader(
            if (direction == "ida") "Sector de tu hogar (punto de partida)"
            else "Sector de tu hogar (destino final)",
        )
        AppTextField(
            value = homeSector,
            onValueChange = { transportVm.clearError(); homeSector = it },
            label = "Sector",
            placeholder = "Ej. Romareda, Mucho Lote 2",
        )
        if (transportVm.zones.none { it.equals(homeSector.trim(), ignoreCase = true) }) {
            SectorSuggestions(transportVm.zones, onPick = { transportVm.clearError(); homeSector = it })
        }

        SectionHeader("Día")
        SegmentedPicker(
            options = WEEKDAY_LABELS.mapIndexed { i, label -> i to label },
            selected = weekday,
            onSelect = { weekday = it },
        )

        SectionHeader("Hora")
        if (times.isEmpty()) {
            LoadingState()
        } else {
            TimeChips(times = times, selected = selectedTime, onSelect = { transportVm.clearError(); selectedTime = it })
        }
    }
}

@Composable
fun PassengerResultsScreen(
    response: PassengerMatchResponse,
    homeSector: String,
    transportVm: TransportViewModel,
    onBack: () -> Unit,
    onBooked: (String) -> Unit,
) {
    // Conductor que se está agendando: solo su botón muestra carga y los demás se bloquean.
    var bookingDriverId by remember { mutableStateOf<String?>(null) }
    var bookingError by remember { mutableStateOf<String?>(null) }

    AppScreen(title = "Conductores", onBack = onBack) {
        if (response.results.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = "Sin coincidencias",
                message = response.message ?: "No se encontraron conductores.",
            )
        } else {
            Text(
                if (response.results.size == 1) "1 coincidencia" else "${response.results.size} coincidencias",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            InlineMessage(bookingError)
            response.results.forEach { r ->
                DriverMatchCard(
                    r = r,
                    loading = bookingDriverId == r.driver_id,
                    enabled = bookingDriverId == null,
                    onAgendar = {
                        bookingDriverId = r.driver_id
                        bookingError = null
                        transportVm.bookTrip(r, homeSector) { msg ->
                            bookingDriverId = null
                            if (msg.isNotBlank()) {
                                onBooked(msg)
                            } else {
                                bookingError = transportVm.errorMessage
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DriverRatingRow(avg: Double?, count: Int) {
    if (count == 0 || avg == null) {
        Text(
            "Sin calificaciones aún",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        val fullStars = avg.toInt().coerceIn(0, 5)
        repeat(5) { index ->
            Icon(
                imageVector = if (index < fullStars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            " %.1f · %d".format(avg, count),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DriverMatchCard(
    r: PassengerMatchResult,
    loading: Boolean,
    enabled: Boolean,
    onAgendar: () -> Unit,
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(r.driver_name, size = 52.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(r.driver_name, style = MaterialTheme.typography.titleMedium)
                DriverRatingRow(r.driver_rating_avg, r.driver_rating_count)
            }
            Pill("$${"%.2f".format(r.trip_fare_usd)}")
        }
        Text(
            r.matched_route_summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        CardDivider()
        InfoRow(
            "Cupos",
            if (r.seats_full) "Llenos (${r.seat_capacity}/${r.seat_capacity})"
            else "${r.available_seats} de ${r.seat_capacity}",
        )
        InfoRow(
            "Vehículo",
            listOfNotNull(r.vehicle_brand, r.vehicle_model).joinToString(" ").ifBlank { "—" },
        )
        InfoRow("Placa", r.vehicle_plate ?: "—")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CallButton(r.driver_phone)
            PrimaryButton(
                text = if (r.seats_full) "Cupos llenos" else "Agendar viaje",
                onClick = onAgendar,
                loading = loading,
                enabled = enabled && !r.seats_full,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
