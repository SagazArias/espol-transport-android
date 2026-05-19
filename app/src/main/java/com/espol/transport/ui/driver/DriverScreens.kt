package com.espol.transport.ui.driver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.espol.transport.data.model.DriverDayPlanIn
import com.espol.transport.ui.viewmodel.TransportViewModel

private val WEEKDAYS_MON_FRI = listOf(
    "Lunes" to 0,
    "Martes" to 1,
    "Miércoles" to 2,
    "Jueves" to 3,
    "Viernes" to 4,
)

class DayScheduleUi(val weekday: Int) {
    var idaTime by mutableStateOf("")
    var idaCustomTime by mutableStateOf("")
    var regresoTime by mutableStateOf("")
    var regresoCustomTime by mutableStateOf("")
    var idaRouteText by mutableStateOf("")
    var regresoRouteText by mutableStateOf("")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DriverScheduleScreen(
    transportVm: TransportViewModel,
    userName: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    var homeSector by rememberSaveable { mutableStateOf("") }
    var seatCapacity by rememberSaveable { mutableStateOf("3") }
    var selectedFare by rememberSaveable { mutableStateOf(1.50) }
    val dayStates = remember {
        mutableStateListOf(*WEEKDAYS_MON_FRI.map { DayScheduleUi(it.second) }.toTypedArray())
    }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        transportVm.loadTripTimes()
        transportVm.loadDriverSchedule {
            transportVm.driverSchedule?.home_zone?.name?.let { homeSector = it }
            transportVm.driverSchedule?.seat_capacity?.let { seatCapacity = it.toString() }
            transportVm.driverSchedule?.trip_fare_usd?.let { selectedFare = it }
            transportVm.driverSchedule?.days?.forEach { day ->
                val ui = dayStates.find { it.weekday == day.weekday } ?: return@forEach
                ui.idaTime = TransportViewModel.formatTimeForChip(day.ida_time) ?: ""
                ui.regresoTime = TransportViewModel.formatTimeForChip(day.regreso_time) ?: ""
                ui.idaRouteText = day.ida_sectors.joinToString(", ") { it.name }
                ui.regresoRouteText = day.regreso_sectors.joinToString(", ") { it.name }
            }
        }
    }

    val idaTimes = transportVm.tripTimes?.ida.orEmpty()
    val fares = transportVm.allowedFares.ifEmpty { listOf(1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5) }
    val regresoTimes = transportVm.tripTimes?.regreso.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Horario — $userName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    androidx.compose.material3.TextButton(onClick = onLogout) {
                        Text("Salir")
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
            Text(
                "Configura tu ruta de lunes a viernes. Puedes guardar cambios cuando quieras, aunque sea un solo día.",
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedTextField(
                value = homeSector,
                onValueChange = { transportVm.clearError(); homeSector = it },
                label = { Text("Origen semanal (sector)") },
                placeholder = { Text("Ej. Urdesa, Samborondón") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = seatCapacity,
                onValueChange = { seatCapacity = it.filter { c -> c.isDigit() }.take(2) },
                label = { Text("Cupos (pasajeros que puedes llevar)") },
                supportingText = { Text("Aplica a todos los días de la semana") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Costo del viaje (USD)", style = MaterialTheme.typography.titleSmall)
            Text("Entre \$1.00 y \$2.50", style = MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fares.forEach { fare ->
                    FilterChip(
                        selected = selectedFare == fare,
                        onClick = { selectedFare = fare },
                        label = { Text("$${TransportViewModel.formatFare(fare)}") },
                    )
                }
            }

            dayStates.forEach { day ->
                val label = WEEKDAYS_MON_FRI.first { it.second == day.weekday }.first
                DayScheduleCard(
                    day = day,
                    label = label,
                    idaTimes = idaTimes,
                    regresoTimes = regresoTimes,
                )
            }

            transportVm.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            savedMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            if (transportVm.busy) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        val cupos = seatCapacity.toIntOrNull() ?: 3
                        val days = dayStates.mapNotNull { d ->
                            val idaT = d.idaCustomTime.ifBlank { d.idaTime }
                            val regT = d.regresoCustomTime.ifBlank { d.regresoTime }
                            val idaSectors = d.idaRouteText.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }
                            val regSectors = d.regresoRouteText.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }
                            if (idaT.isBlank() && regT.isBlank() && idaSectors.isEmpty() && regSectors.isEmpty()) {
                                null
                            } else {
                                DriverDayPlanIn(
                                    weekday = d.weekday,
                                    ida_time = idaT.takeIf { it.isNotBlank() }?.let { TransportViewModel.toApiTime(it) },
                                    regreso_time = regT.takeIf { it.isNotBlank() }?.let { TransportViewModel.toApiTime(it) },
                                    ida_sector_texts = idaSectors,
                                    regreso_sector_texts = regSectors,
                                )
                            }
                        }
                        transportVm.saveDriverSchedule(homeSector, cupos, selectedFare, days) {
                            savedMessage = "Horario actualizado correctamente."
                        }
                    },
                    enabled = homeSector.isNotBlank() && (seatCapacity.toIntOrNull() ?: 0) >= 1,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Guardar / actualizar horario")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayScheduleCard(
    day: DayScheduleUi,
    label: String,
    idaTimes: List<String>,
    regresoTimes: List<String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall)

            Text("Ida (hacia ESPOL)", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                idaTimes.forEach { t ->
                    FilterChip(
                        selected = day.idaTime == t && day.idaCustomTime.isBlank(),
                        onClick = {
                            day.idaTime = if (day.idaTime == t) "" else t
                            day.idaCustomTime = ""
                        },
                        label = { Text(t) },
                    )
                }
            }
            OutlinedTextField(
                value = day.idaCustomTime,
                onValueChange = {
                    day.idaCustomTime = it
                    if (it.isNotBlank()) day.idaTime = ""
                },
                label = { Text("Otra hora ida (HH:MM)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = day.idaRouteText,
                onValueChange = { day.idaRouteText = it },
                label = { Text("Ruta de ida (sectores, separados por coma)") },
                placeholder = { Text("Romareda, La Joya, ESPOL") },
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider()

            Text("Regreso (a casa)", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                regresoTimes.forEach { t ->
                    FilterChip(
                        selected = day.regresoTime == t && day.regresoCustomTime.isBlank(),
                        onClick = {
                            day.regresoTime = if (day.regresoTime == t) "" else t
                            day.regresoCustomTime = ""
                        },
                        label = { Text(t) },
                    )
                }
            }
            OutlinedTextField(
                value = day.regresoCustomTime,
                onValueChange = {
                    day.regresoCustomTime = it
                    if (it.isNotBlank()) day.regresoTime = ""
                },
                label = { Text("Otra hora regreso (HH:MM)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = day.regresoRouteText,
                onValueChange = { day.regresoRouteText = it },
                label = { Text("Ruta de regreso (sectores, separados por coma)") },
                placeholder = { Text("ESPOL, Urdesa, La Joya") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
