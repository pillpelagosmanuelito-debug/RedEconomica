package com.educalab.redeconomica.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.ModuleState
import com.educalab.redeconomica.domain.model.ResourceDef
import com.educalab.redeconomica.domain.model.Urgency
import com.educalab.redeconomica.ui.art.PersonajeRetrato
import com.educalab.redeconomica.ui.art.RecursoIcono
import com.educalab.redeconomica.ui.art.nombreLegible
import com.educalab.redeconomica.ui.theme.ValleColors

/** Tarjeta base del Valle: bordes redondeados y un borde cálido. */
@Composable
fun TarjetaValle(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) { contenido() }
    }
}

/** Ficha de un recurso con su dibujo y la cantidad SIEMPRE visible en número. */
@Composable
fun ChipRecurso(
    recursoId: String,
    cantidad: Int,
    modifier: Modifier = Modifier,
    seleccionado: Boolean = false,
    tamano: Int = 40,
    onClick: (() -> Unit)? = null
) {
    val borde = if (seleccionado) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (seleccionado) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(if (seleccionado) 2.dp else 1.dp, borde, RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        RecursoIcono(recursoId, Modifier.size(tamano.dp))
        Spacer(Modifier.width(6.dp))
        Column {
            Text(
                text = "×$cantidad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = nombreLegible(recursoId),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Todos los recursos de un inventario, con su número. */
@Composable
fun FilaInventario(
    inventario: Inventory,
    modifier: Modifier = Modifier,
    tamano: Int = 36,
    vacioTexto: String = "Nada por ahora"
) {
    if (inventario.esVacio) {
        Text(
            vacioTexto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
        return
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        inventario.contenido.forEach { (id, n) ->
            ChipRecurso(id, n, tamano = tamano)
        }
    }
}

/** Barra de progreso dibujada a mano, con el número siempre al lado. */
@Composable
fun BarraValle(
    progreso: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    alto: Int = 12
) {
    val animado by animateFloatAsState(
        targetValue = progreso.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "barra"
    )
    val fondo = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier.height(alto.dp)) {
        val r = size.height / 2f
        drawRoundRect(
            color = fondo,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
        )
        if (animado > 0f) {
            drawRoundRect(
                color = color,
                size = androidx.compose.ui.geometry.Size(size.width * animado, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
            )
        }
    }
}

/** Retrato + nombre + oficio de un habitante. */
@Composable
fun FichaHabitante(
    personajeId: String,
    nombre: String,
    oficio: String,
    modifier: Modifier = Modifier,
    tamano: Int = 56,
    seleccionado: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                if (seleccionado) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(6.dp)
    ) {
        Box(
            Modifier
                .size(tamano.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            PersonajeRetrato(personajeId, Modifier.size(tamano.dp), "Retrato de $nombre")
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                oficio,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Etiqueta de urgencia: icono textual + palabra, nunca solo color. */
@Composable
fun EtiquetaUrgencia(urgencia: Urgency, modifier: Modifier = Modifier) {
    val (simbolo, color) = when (urgencia) {
        Urgency.ALTA -> "!!!" to ValleColors.Rojo
        Urgency.MEDIA -> "!!" to ValleColors.Naranja
        Urgency.BAJA -> "!" to ValleColors.Azul
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(simbolo, style = MaterialTheme.typography.labelMedium, color = color)
        Spacer(Modifier.width(5.dp))
        Text(
            urgencia.etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Estado de un módulo, con palabra y símbolo además del color. */
@Composable
fun EtiquetaEstado(estado: ModuleState, modifier: Modifier = Modifier) {
    val (simbolo, color) = when (estado) {
        ModuleState.BLOQUEADO -> "⊘" to MaterialTheme.colorScheme.onSurfaceVariant
        ModuleState.DISPONIBLE -> "▶" to MaterialTheme.colorScheme.primary
        ModuleState.INICIADO -> "…" to ValleColors.Naranja
        ModuleState.COMPLETADO -> "✓" to ValleColors.Verde
        ModuleState.DOMINADO -> "★" to ValleColors.Trigo
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(simbolo, style = MaterialTheme.typography.labelMedium, color = color)
        Spacer(Modifier.width(5.dp))
        Text(estado.etiqueta, style = MaterialTheme.typography.labelMedium)
    }
}

/** Tilo, el zorro cartero, diciendo algo corto. No aparece si no hace falta. */
@Composable
fun TiloDice(texto: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(ValleColors.NaranjaSuave)
        ) {
            PersonajeRetrato("tilo", Modifier.size(52.dp), "Tilo, el cartero del Valle")
        }
        Spacer(Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

/**
 * Panel de respuesta.
 *
 * Nunca dice solo "correcto" o "incorrecto": siempre explica la consecuencia
 * y, si no salió, ofrece una pista y la posibilidad de volver a probar.
 */
@Composable
fun PanelResultado(
    visible: Boolean,
    logrado: Boolean,
    mensaje: String,
    explicacion: String,
    modifier: Modifier = Modifier,
    accion: @Composable (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val color = if (logrado) ValleColors.VerdeSuave else ValleColors.NaranjaSuave
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = color,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    if (logrado) "✓  $mensaje" else "↺  $mensaje",
                    style = MaterialTheme.typography.titleMedium,
                    color = ValleColors.Tinta
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    explicacion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ValleColors.TintaSuave
                )
                if (accion != null) {
                    Spacer(Modifier.height(10.dp))
                    accion()
                }
            }
        }
    }
}

/** Cabecera de una actividad: dónde estamos, qué pasa y qué hay que hacer. */
@Composable
fun CabeceraActividad(
    titulo: String,
    situacion: String,
    instruccion: String,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))
        Text(
            situacion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Text(
                instruccion,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

/** Selector de cantidad con botones grandes: alternativa accesible al arrastre. */
@Composable
fun SelectorCantidad(
    valor: Int,
    minimo: Int,
    maximo: Int,
    onCambio: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        BotonRedondo("−", habilitado = valor > minimo) { onCambio(valor - 1) }
        Text(
            valor.toString(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        BotonRedondo("+", habilitado = valor < maximo) { onCambio(valor + 1) }
    }
}

@Composable
fun BotonRedondo(texto: String, habilitado: Boolean = true, onClick: () -> Unit) {
    val fondo = if (habilitado) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(fondo)
            .clickable(enabled = habilitado) { onClick() }
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.headlineSmall,
            color = if (habilitado) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Texto corto para describir un inventario con palabras. */
fun describir(inventario: Inventory, catalogo: Map<String, ResourceDef>): String =
    inventario.descripcion(catalogo)
