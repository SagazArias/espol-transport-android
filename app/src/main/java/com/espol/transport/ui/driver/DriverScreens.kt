package com.espol.transport.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.espol.transport.data.model.DriverDayPlanIn
import com.espol.transport.ui.components.AppScreen
import com.espol.transport.ui.components.AppTextField
import com.espol.transport.ui.components.CardDivider
import com.espol.transport.ui.components.InlineMessage
import com.espol.transport.ui.components.MessageKind
import com.espol.transport.ui.components.PrimaryButton
import com.espol.transport.ui.components.SectionCard
import com.espol.transport.ui.components.SectionHeader
import com.espol.transport.ui.components.SectorSuggestions
import com.espol.transport.ui.components.StepperRow
import com.espol.transport.ui.components.TimeChips
import com.espol.transport.ui.viewmodel.TransportViewModel
import kotlinx.coroutines.launch

private val WEEKDAYS_MON_FRI = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
private val WEEKDAYS_SHORT = listOf("Lun", "Mar", "Mié", "Jue", "Vie")
private const val MAX_SEATS = 8 // shortcut: tope de UI; si la API admite más, subirlo aquí.

class DayScheduleUi(val weekday: Int) {
    var idaTime by mutableStateOf("")
    var regresoTime by mutableStateOf("")
    var idaRoute by mutableStateOf<List<String>>(emptyList())
    var regresoRoute by mutableStateOf<List<String>>(emptyList())

    val hasIda get() = idaTime.isNotBlank() || idaRoute.isNotEmpty()
    val hasRegreso get() = regresoTime.isNotBlank() || regresoRoute.isNotEmpty()

    fun summary(): String = listOfNotNull(
        if (hasIda) "Ida ${idaTime.ifBlank { "sin hora" }}" else null,
        if (hasRegreso) "Regreso ${regresoTime.ifBlank { "sin hora" }}" else null,
    ).joinToString(" · ").ifBlank { "Sin viajes" }
}

/** Copia editable de un día, usada en la hoja inferior hasta que el conductor toca "Aplicar". */
private class DayDraft(source: DayScheduleUi, homeSector: String) {
    var days by mutableStateOf(setOf(source.weekday))
    var idaOn by mutableStateOf(source.hasIda)
    var idaTime by mutableStateOf(source.idaTime)
    var idaRoute by mutableStateOf(source.idaRoute.ifEmpty { listOfNotNull(homeSector.trim().ifBlank { null }) })
    var regresoOn by mutableStateOf(source.hasRegreso)
    var regresoTime by mutableStateOf(source.regresoTime)
    var regresoRoute by mutableStateOf(source.regresoRoute)

    fun applyTo(target: DayScheduleUi) {
        target.idaTime = if (idaOn) idaTime else ""
        target.idaRoute = if (idaOn) idaRoute else emptyList()
        target.regresoTime = if (regresoOn) regresoTime else ""
        target.regresoRoute = if (regresoOn) regresoRoute else emptyList()
    }
}

@Composable
fun DriverScheduleScreen(
    transportVm: TransportViewModel,
    onBack: () -> Unit,
) {
    var homeSector by rememberSaveable { mutableStateOf("") }
    var seats by rememberSaveable { mutableStateOf(3) }
    var selectedFare by rememberSaveable { mutableStateOf(1.50) }
    val dayStates = remember { mutableStateListOf(*List(5) { DayScheduleUi(it) }.toTypedArray()) }
    var savedMessage by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<DayDraft?>(null) }

    LaunchedEffect(Unit) {
        transportVm.loadTripTimes()
        transportVm.loadZones()
        transportVm.loadDriverSchedule {
            val s = transportVm.driverSchedule ?: return@loadDriverSchedule
            s.home_zone?.name?.let { homeSector = it }
            seats = s.seat_capacity
            selectedFare = s.trip_fare_usd
            s.days.forEach { day ->
                val ui = dayStates.getOrNull(day.weekday) ?: return@forEach
                ui.idaTime = TransportViewModel.formatTimeForChip(day.ida_time) ?: ""
                ui.regresoTime = TransportViewModel.formatTimeForChip(day.regreso_time) ?: ""
                ui.idaRoute = day.ida_sectors.map { it.name }
                ui.regresoRoute = day.regreso_sectors.map { it.name }
            }
        }
    }

    val fares = transportVm.allowedFares.ifEmpty { listOf(1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5) }
    val fareIndex = fares.indexOfFirst { it == selectedFare }.coerceAtLeast(0)

    AppScreen(
        title = "Mi horario",
        onBack = onBack,
        floatingBar = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InlineMessage(transportVm.errorMessage, solid = true)
                InlineMessage(savedMessage, kind = MessageKind.Success, solid = true)
                PrimaryButton(
                    text = "Guardar horario",
                    loading = transportVm.busy,
                    enabled = homeSector.isNotBlank() && seats >= 1,
                    onClick = {
                        savedMessage = null
                        // Se envían los 5 días: la API deja intacto un día omitido, así que "Sin viajes"
                        // debe llegar como día vacío para borrarse de verdad.
                        val days = dayStates.map { d ->
                            DriverDayPlanIn(
                                weekday = d.weekday,
                                ida_time = d.idaTime.takeIf { it.isNotBlank() }?.let { TransportViewModel.toApiTime(it) },
                                regreso_time = d.regresoTime.takeIf { it.isNotBlank() }?.let { TransportViewModel.toApiTime(it) },
                                ida_sector_texts = d.idaRoute,
                                regreso_sector_texts = d.regresoRoute,
                            )
                        }
                        transportVm.saveDriverSchedule(homeSector, seats, selectedFare, days) {
                            savedMessage = "Horario guardado."
                        }
                    },
                )
            }
        },
    ) {
        SectionHeader("General")
        SectionCard {
            AppTextField(
                value = homeSector,
                onValueChange = { transportVm.clearError(); savedMessage = null; homeSector = it },
                label = "Origen semanal (sector)",
                placeholder = "Ej. Urdesa, Samborondón",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            if (transportVm.zones.none { it.equals(homeSector.trim(), ignoreCase = true) }) {
                SectorSuggestions(transportVm.zones, onPick = { homeSector = it; savedMessage = null })
            }
            CardDivider()
            StepperRow(
                label = "Cupos",
                supportingText = "Pasajeros por viaje, todos los días",
                value = "$seats",
                onDecrement = { seats--; savedMessage = null },
                onIncrement = { seats++; savedMessage = null },
                canDecrement = seats > 1,
                canIncrement = seats < MAX_SEATS,
            )
            CardDivider()
            StepperRow(
                label = "Tarifa",
                supportingText = "Por pasajero, en USD",
                value = "$${TransportViewModel.formatFare(fares[fareIndex])}",
                onDecrement = { selectedFare = fares[fareIndex - 1]; savedMessage = null },
                onIncrement = { selectedFare = fares[fareIndex + 1]; savedMessage = null },
                canDecrement = fareIndex > 0,
                canIncrement = fareIndex < fares.lastIndex,
            )
        }

        SectionHeader("Semana")
        Text(
            "Toca un día para editarlo. En el editor puedes aplicar lo mismo a varios días a la vez.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        SectionCard(contentPadding = PaddingValues(vertical = 4.dp)) {
            dayStates.forEachIndexed { i, day ->
                if (i > 0) Box(Modifier.padding(start = 72.dp)) { CardDivider() }
                DayRow(day) { editing = DayDraft(day, homeSector) }
            }
        }
    }

    editing?.let { draft ->
        DayEditorSheet(
            draft = draft,
            idaTimes = transportVm.tripTimes?.ida.orEmpty(),
            regresoTimes = transportVm.tripTimes?.regreso.orEmpty(),
            zones = transportVm.zones,
            onApply = {
                draft.days.forEach { draft.applyTo(dayStates[it]) }
                savedMessage = null
                editing = null
            },
            onDismiss = { editing = null },
        )
    }
}

@Composable
private fun DayRow(day: DayScheduleUi, onClick: () -> Unit) {
    val active = day.hasIda || day.hasRegreso
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(
                    if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                WEEKDAYS_SHORT[day.weekday],
                style = MaterialTheme.typography.labelMedium,
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(WEEKDAYS_MON_FRI[day.weekday], style = MaterialTheme.typography.titleMedium)
            Text(
                day.summary(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayEditorSheet(
    draft: DayDraft,
    idaTimes: List<String>,
    regresoTimes: List<String>,
    zones: List<String>,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun close(then: () -> Unit) = scope.launch { sheetState.hide() }.invokeOnCompletion { then() }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                if (draft.days.size == 1) WEEKDAYS_MON_FRI[draft.days.first()] else "${draft.days.size} días",
                style = MaterialTheme.typography.headlineSmall,
            )

            SheetLabel("Aplicar a")
            MultiChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                WEEKDAYS_SHORT.forEachIndexed { i, label ->
                    SegmentedButton(
                        checked = i in draft.days,
                        onCheckedChange = { on ->
                            draft.days = if (on) draft.days + i else (draft.days - i).ifEmpty { draft.days }
                        },
                        shape = SegmentedButtonDefaults.itemShape(i, WEEKDAYS_SHORT.size),
                        icon = {},
                        label = { Text(label, maxLines = 1) },
                    )
                }
            }

            DirectionEditor(
                title = "Ida a ESPOL",
                enabled = draft.idaOn,
                onEnabled = { draft.idaOn = it },
                times = idaTimes,
                time = draft.idaTime,
                onTime = { draft.idaTime = it },
                route = draft.idaRoute,
                onRoute = { draft.idaRoute = it },
                zones = zones,
            )
            DirectionEditor(
                title = "Regreso a casa",
                enabled = draft.regresoOn,
                onEnabled = { draft.regresoOn = it },
                times = regresoTimes,
                time = draft.regresoTime,
                onTime = { draft.regresoTime = it },
                route = draft.regresoRoute,
                onRoute = { draft.regresoRoute = it },
                zones = zones,
                // La API guarda los sectores como conjunto (sin orden), así que se copian tal cual.
                extraAction = if (draft.idaRoute.isNotEmpty() && draft.regresoRoute.toSet() != draft.idaRoute.toSet()) {
                    {
                        AssistChip(
                            onClick = { draft.regresoRoute = draft.idaRoute },
                            label = { Text("Usar los sectores de la ida") },
                            leadingIcon = { Icon(Icons.Filled.SwapVert, contentDescription = null, Modifier.size(18.dp)) },
                        )
                    }
                } else null,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { draft.idaOn = false; draft.regresoOn = false; close(onApply) },
                    modifier = Modifier.weight(1f),
                ) { Text("Sin viajes", color = MaterialTheme.colorScheme.error) }
                PrimaryButton(
                    text = if (draft.days.size == 1) "Aplicar" else "Aplicar a ${draft.days.size} días",
                    onClick = { close(onApply) },
                    modifier = Modifier.weight(2f),
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SheetLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DirectionEditor(
    title: String,
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit,
    times: List<String>,
    time: String,
    onTime: (String) -> Unit,
    route: List<String>,
    onRoute: (List<String>) -> Unit,
    zones: List<String>,
    extraAction: (@Composable () -> Unit)? = null,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Switch) { onEnabled(!enabled) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = null)
        }
        if (!enabled) return@Column

        SheetLabel("Hora de salida")
        TimeChips(times = times, selected = time, onSelect = onTime)

        SheetLabel("Sectores de la ruta")
        if (route.isEmpty()) {
            Text(
                "Toca los sectores por donde pasas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                route.forEach { sector ->
                    InputChip(
                        selected = true,
                        onClick = { onRoute(route - sector) },
                        label = { Text(sector) },
                        trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Quitar $sector", Modifier.size(16.dp)) },
                    )
                }
            }
        }
        extraAction?.invoke()
        SectorSuggestions(zones, onPick = { onRoute(route + it) }, exclude = route)
        OtherSectorField(onAdd = { onRoute(route + it) })
    }
}

/** Para sectores fuera del catálogo: se escriben y se agregan al final de la ruta. */
@Composable
private fun OtherSectorField(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AppTextField(
        value = text,
        onValueChange = { text = it },
        label = "Otro sector",
        trailingIcon = {
            IconButton(
                onClick = { onAdd(text.trim()); text = "" },
                enabled = text.isNotBlank(),
            ) { Icon(Icons.Filled.Add, contentDescription = "Agregar sector") }
        },
    )
}
