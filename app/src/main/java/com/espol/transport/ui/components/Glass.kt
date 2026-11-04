package com.espol.transport.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Liquid glass claro (sin esmerilado): casi transparente, con brillo especular en el borde,
 * un reflejo suave arriba y sombra solo por fuera, para que el contenido de atrás se vea nítido.
 */
@Composable
fun Modifier.liquidGlass(shape: Shape = CircleShape): Modifier {
    val dark = isSystemInDarkTheme()
    val fill = if (dark) Color.White.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.30f)
    val shadow = if (dark) Color.Black.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.14f)
    val hairline = if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.07f)
    val rim = Brush.linearGradient(
        0f to Color.White.copy(alpha = if (dark) 0.55f else 0.95f),
        0.45f to Color.White.copy(alpha = 0.05f),
        1f to Color.White.copy(alpha = if (dark) 0.25f else 0.6f),
        start = Offset.Zero,
        end = Offset.Infinite,
    )
    return this
        .drawBehind {
            val outline = shape.createOutline(size, layoutDirection, this)
            val inside = Path().apply { addOutline(outline) }
            // Sombra solo por fuera: así no ensucia el vidrio transparente.
            clipPath(inside, clipOp = ClipOp.Difference) {
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(8.dp.toPx(), 0f, 3.dp.toPx(), shadow.toArgb()) // cabe en los 10 dp libres de la barra superior
                    }
                    canvas.nativeCanvas.drawPath(inside.asAndroidPath(), paint)
                }
            }
        }
        .clip(shape)
        .background(fill)
        .drawWithContent {
            drawContent()
            val outline = shape.createOutline(size, layoutDirection, this)
            // Reflejo superior, como luz que entra por el borde del vidrio.
            drawRect(
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = if (dark) 0.10f else 0.35f),
                    0.5f to Color.Transparent,
                ),
            )
            drawOutline(outline, hairline, style = Stroke(1.dp.toPx()))
            translate(0.5f, 0.5f) {
                drawOutline(outline, rim, style = Stroke(1.2.dp.toPx()))
            }
        }
}

/** Escala al presionar, con resorte críticamente amortiguado (respuesta inmediata al tocar). */
@Composable
fun Modifier.pressScale(interaction: MutableInteractionSource, pressed: Float = 0.94f): Modifier {
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressed else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow),
        label = "pressScale",
    )
    return graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Botón circular de vidrio (atrás, cuenta, etc.). 44 dp visibles, 48 dp de área táctil. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(48.dp)
            .pressScale(interaction),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .liquidGlass(CircleShape)
                .clickable(interaction, indication = null, role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Contenedor de vidrio con forma de cápsula para barras flotantes. */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    content: @Composable () -> Unit,
) {
    Box(modifier.liquidGlass(shape)) { content() }
}
