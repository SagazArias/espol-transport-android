package com.espol.transport.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Margen lateral de todas las pantallas. */
val ScreenPadding = 20.dp

/**
 * Pantalla estándar: título grande que se contrae al hacer scroll (como los large titles de iOS),
 * contenido con scroll y padding uniforme. `floatingBar` flota abajo sobre una cápsula de vidrio
 * claro y el contenido pasa por debajo, visible a través de ella.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(title, maxLines = 1) },
                navigationIcon = {
                    if (onBack != null) {
                        GlassIconButton(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            onClick = onBack,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                },
                actions = { actions(); Spacer(Modifier.width(8.dp)) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
    ) { inner ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .consumeWindowInsets(inner) // para que imePadding no sume otra vez la barra de navegación
                .imePadding(),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenPadding)
                    .padding(top = 8.dp, bottom = if (floatingBar != null) 120.dp else 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
            if (floatingBar != null) {
                GlassSurface(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                ) {
                    Box(Modifier.padding(8.dp)) { floatingBar() }
                }
            }
        }
    }
}

/** Botón principal. Mientras `loading`, muestra el spinner dentro sin cambiar de tamaño. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val c = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        // Deshabilitado sólido (no translúcido): sobre vidrio claro el contenido de atrás no debe verse a través.
        // Cargando conserva el color principal.
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = if (loading) c.primary else c.surfaceVariant,
            disabledContentColor = if (loading) c.onPrimary else c.onSurfaceVariant,
        ),
    ) {
        ButtonContent(text, loading)
    }
}

/** Acción secundaria con relleno tonal (menos peso que la principal). */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        ButtonContent(text, loading)
    }
}

/** Acción destructiva (eliminar cuenta, cancelar viaje): texto rojo, sin relleno. */
@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        Text(text, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ButtonContent(text: String, loading: Boolean) {
    Box(contentAlignment = Alignment.Center) {
        // El texto sigue ocupando su espacio para que el botón no cambie de ancho.
        Text(text, color = if (loading) Color.Transparent else Color.Unspecified, maxLines = 1, softWrap = false)
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current,
            )
        }
    }
}

/** Campo de texto relleno, sin línea inferior, sobre fondo de tarjeta (estilo celdas agrupadas de iOS). */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest, // en diálogos: background
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            errorContainerColor = containerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ),
    )
}

/** Título pequeño de sección, en gris, sobre un grupo de contenido. */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 4.dp, top = 8.dp),
    )
}

/** Grupo de contenido sobre fondo blanco (claro) / gris oscuro (oscuro). */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(
            Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

/** Fila "etiqueta ······ valor", como las celdas de detalle de iOS. */
@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Separador fino dentro de una SectionCard. */
@Composable
fun CardDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
}

enum class MessageKind { Error, Success }

/** Mensaje en línea con icono. No se muestra si `text` es null; aparece y desaparece con fundido. */
@Composable
fun InlineMessage(
    text: String?,
    kind: MessageKind = MessageKind.Error,
    modifier: Modifier = Modifier,
    solid: Boolean = false, // true sobre vidrio claro: el texto necesita fondo propio para leerse
) {
    AnimatedVisibility(visible = text != null, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        val (icon, color) = when (kind) {
            MessageKind.Error -> Icons.Filled.Error to MaterialTheme.colorScheme.error
            MessageKind.Success -> Icons.Filled.CheckCircle to MaterialTheme.colorScheme.tertiary
        }
        Row(
            if (solid) {
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.large)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            } else Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp).padding(top = 2.dp))
            Text(text.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = color)
        }
    }
}

/** Estado vacío: icono grande, título y explicación, centrados. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(44.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Carga de pantalla completa o de sección. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Círculo con iniciales: da identidad a cada persona en tarjetas y en la cuenta. */
@Composable
fun Avatar(name: String, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifBlank { "?" }
    Box(
        modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            style = if (size >= 44.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/** Pastilla pequeña para un dato destacado (tarifa, estado). */
@Composable
fun Pill(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/** Fila "etiqueta  [−] valor [+]": cambia un número sin abrir el teclado. */
@Composable
fun StepperRow(
    label: String,
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    canDecrement: Boolean,
    canIncrement: Boolean,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            supportingText?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        FilledTonalIconButton(onClick = onDecrement, enabled = canDecrement) {
            Icon(Icons.Filled.Remove, contentDescription = "Menos $label")
        }
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 56.dp),
        )
        FilledTonalIconButton(onClick = onIncrement, enabled = canIncrement) {
            Icon(Icons.Filled.Add, contentDescription = "Más $label")
        }
    }
}

/** Selector de hora con reloj (24 h). Devuelve "HH:mm". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initial: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val parts = initial?.split(":")
    val state = rememberTimePickerState(
        initialHour = parts?.getOrNull(0)?.toIntOrNull() ?: 7,
        initialMinute = parts?.getOrNull(1)?.toIntOrNull() ?: 0,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elige la hora") },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm("%02d:%02d".format(state.hour, state.minute)) }) { Text("Listo") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

/**
 * Horarios sugeridos como chips + un chip "Otra hora" que abre el reloj.
 * `selected` es la hora elegida ("HH:mm") o "" si ninguna.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeChips(
    times: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var picking by remember { mutableStateOf(false) }
    val custom = selected.isNotBlank() && selected !in times
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        times.forEach { t ->
            FilterChip(selected = selected == t, onClick = { onSelect(if (selected == t) "" else t) }, label = { Text(t) })
        }
        FilterChip(
            selected = custom,
            onClick = { picking = true },
            label = { Text(if (custom) selected else "Otra hora") },
            leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null, Modifier.size(18.dp)) },
        )
    }
    if (picking) {
        TimePickerDialog(
            initial = selected.ifBlank { null },
            onConfirm = { onSelect(it); picking = false },
            onDismiss = { picking = false },
        )
    }
}

/** Sectores del catálogo para tocar en vez de escribir. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SectorSuggestions(
    sectors: List<String>,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
    exclude: Collection<String> = emptyList(),
) {
    val options = sectors.filter { s -> exclude.none { it.equals(s, ignoreCase = true) } }
    if (options.isEmpty()) return
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { s ->
            SuggestionChip(
                onClick = { onPick(s) },
                label = { Text(s) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null, Modifier.size(16.dp)) },
            )
        }
    }
}

/** Botón circular para llamar: abre el marcador con el número (no llama directamente). */
@Composable
fun CallButton(phone: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    FilledTonalIconButton(
        onClick = {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.orEmpty()}")))
        },
        enabled = !phone.isNullOrBlank(),
        modifier = modifier.size(52.dp),
    ) {
        Icon(Icons.Filled.Call, contentDescription = "Llamar")
    }
}
