package com.espol.transport.ui.passenger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerHomeScreen(
    transportVm: TransportViewModel,
    userName: String,
    onSearch: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hola, $userName") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Text("Salir", style = MaterialTheme.typography.labelMedium)
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Busca un viaje compartido hacia ESPOL o de regreso a casa (lunes a viernes).",
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onSearch, modifier = Modifier.fillMaxWidth()) {
                Text("Buscar conductor")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var customTime by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (transportVm.tripTimes == null) {
            transportVm.loadTripTimes()
        }
    }

    val times = if (direction == "ida") {
        transportVm.tripTimes?.ida.orEmpty()
    } else {
        transportVm.tripTimes?.regreso.orEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar viaje") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
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
            Text("Tipo de viaje", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = direction == "ida",
                    onClick = {
                        direction = "ida"
                        selectedTime = ""
                    },
                    label = { Text("Ida (hacia ESPOL)") },
                )
                FilterChip(
                    selected = direction == "regreso",
                    onClick = {
                        direction = "regreso"
                        selectedTime = ""
                    },
                    label = { Text("Regreso (a casa)") },
                )
            }

            Text(
                if (direction == "ida") "Sector de tu hogar (punto de partida)"
                else "Sector de tu hogar (destino final)",
                style = MaterialTheme.typography.titleSmall,
            )
            OutlinedTextField(
                value = homeSector,
                onValueChange = { transportVm.clearError(); homeSector = it },
                label = { Text("Ej. Romareda, Mucho Lote 2") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Día (lunes a viernes)", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WEEKDAY_LABELS.forEachIndexed { index, label ->
                    FilterChip(
                        selected = weekday == index,
                        onClick = { weekday = index },
                        label = { Text(label) },
                    )
                }
            }

            Text("Horario", style = MaterialTheme.typography.titleSmall)
            if (times.isEmpty()) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    times.forEach { t ->
                        FilterChip(
                            selected = selectedTime == t && customTime.isBlank(),
                            onClick = {
                                selectedTime = t
                                customTime = ""
                            },
                            label = { Text(t) },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = customTime,
                onValueChange = {
                    transportVm.clearError()
                    customTime = it
                    if (it.isNotBlank()) selectedTime = ""
                },
                label = { Text("Otra hora (HH:MM)") },
                placeholder = { Text("ej. 08:30") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            transportVm.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            val tripTime = customTime.ifBlank { selectedTime }
            if (transportVm.busy) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        transportVm.searchMatches(direction, homeSector, weekday, tripTime) {
                            onResults()
                        }
                    },
                    enabled = homeSector.isNotBlank() && tripTime.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Buscar coincidencias")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerResultsScreen(
    response: PassengerMatchResponse,
    homeSector: String,
    transportVm: TransportViewModel,
    onBack: () -> Unit,
    onBooked: (String) -> Unit,
) {
    var bookingBusy by remember { mutableStateOf(false) }
    var bookingMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conductores") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
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
            bookingMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            if (response.results.isEmpty()) {
                Text(
                    response.message ?: "No se encontraron conductores.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                Text("${response.results.size} coincidencia(s)")
                response.results.forEach { r ->
                    DriverMatchCard(
                        r = r,
                        bookingBusy = bookingBusy,
                        onAgendar = {
                            bookingBusy = true
                            transportVm.bookTrip(r, homeSector) { msg ->
                                bookingBusy = false
                                if (msg.isNotBlank()) {
                                    bookingMsg = msg
                                    onBooked(msg)
                                } else {
                                    bookingMsg = transportVm.errorMessage
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverRatingRow(avg: Double?, count: Int) {
    if (count == 0 || avg == null) {
        Text("Sin calificaciones aún", style = MaterialTheme.typography.bodySmall)
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Calificación: %.1f".format(avg), style = MaterialTheme.typography.bodyMedium)
        val fullStars = avg.toInt().coerceIn(0, 5)
        repeat(5) { index ->
            Icon(
                imageVector = if (index < fullStars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
        Text("($count)", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DriverMatchCard(
    r: PassengerMatchResult,
    bookingBusy: Boolean,
    onAgendar: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(r.driver_name, style = MaterialTheme.typography.titleLarge)
            Text(
                r.matched_route_summary,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            HorizontalDivider()
            Text("Contacto", style = MaterialTheme.typography.labelLarge)
            Text("Celular: ${r.driver_phone ?: "—"}")
            HorizontalDivider()
            Text("Vehículo", style = MaterialTheme.typography.labelLarge)
            Text("Placa: ${r.vehicle_plate ?: "—"}")
            Text("Marca: ${r.vehicle_brand ?: "—"}")
            Text("Modelo: ${r.vehicle_model ?: "—"}")
            Text("Tarifa: $${"%.2f".format(r.trip_fare_usd)}", style = MaterialTheme.typography.bodyMedium)
            DriverRatingRow(r.driver_rating_avg, r.driver_rating_count)
            Text(
                if (r.seats_full) "Sin cupos disponibles (${r.seat_capacity}/${r.seat_capacity} ocupados)"
                else "Cupos disponibles: ${r.available_seats} de ${r.seat_capacity}",
                style = MaterialTheme.typography.bodySmall,
                color = if (r.seats_full) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (bookingBusy) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = onAgendar,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !r.seats_full,
                ) {
                    Text(if (r.seats_full) "Cupos llenos" else "Agendar viaje")
                }
            }
        }
    }
}
