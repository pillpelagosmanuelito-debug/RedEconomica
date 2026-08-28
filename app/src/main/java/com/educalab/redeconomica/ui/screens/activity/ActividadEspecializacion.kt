package com.educalab.redeconomica.ui.screens.activity

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.domain.model.ScenarioPayload
import com.educalab.redeconomica.domain.model.WorkMode
import com.educalab.redeconomica.ui.art.RecursoIcono
import com.educalab.redeconomica.ui.art.nombreLegible
import com.educalab.redeconomica.ui.components.BarraValle
import com.educalab.redeconomica.ui.components.ChipRecurso
import com.educalab.redeconomica.ui.components.FichaHabitante
import com.educalab.redeconomica.ui.components.TarjetaValle

/**
 * "Elige tu oficio".
 *
 * El niño asigna a cada habitante una tarea (o le deja hacer de todo), ejecuta
 * el turno y ve el resultado al lado del resultado de "todos hacen de todo".
 * La comparación no es un texto: son dos producciones calculadas.
 */
@Composable
fun ActividadEspecializacion(estado: ActividadUiState, vm: ActivityViewModel) {
    val escenario = estado.escenario ?: return
    val payload = escenario.payload as? ScenarioPayload.Especializacion ?: return
    val catalogo = SeedCatalogoLocal.catalogo

    Column(Modifier.fillMaxWidth()) {

        TarjetaValle(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer) {
            Text("El Valle necesita", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                payload.objetivo.contenido.forEach { (id, n) -> ChipRecurso(id, n) }
            }
        }

        Spacer(Modifier.height(12.dp))

        escenario.participantes.forEach { habitante ->
            TarjetaValle(Modifier.fillMaxWidth()) {
                FichaHabitante(
                    personajeId = habitante.id,
                    nombre = habitante.nombre,
                    oficio = habitante.oficio
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "En un turno entero puede conseguir:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                habitante.oficiosPosibles().forEach { recurso ->
                    val cuanto = habitante.produccionPorTurno(recurso)
                    val maximo = escenario.participantes.maxOf { it.produccionPorTurno(recurso) }
                        .coerceAtLeast(1)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        RecursoIcono(recurso, Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "$cuanto ${nombreLegible(recurso).lowercase()}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(130.dp)
                        )
                        BarraValle(
                            progreso = cuanto.toFloat() / maximo,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("¿A qué se dedica este turno?", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    habitante.oficiosPosibles().forEach { recurso ->
                        FilterChip(
                            selected = estado.plan.asignaciones[habitante.id] == recurso,
                            onClick = { vm.asignarOficio(habitante.id, recurso) },
                            label = { Text(nombreLegible(recurso)) }
                        )
                    }
                    FilterChip(
                        selected = estado.plan.modoDe(habitante.id) == WorkMode.DE_TODO,
                        onClick = { vm.asignarOficio(habitante.id, null) },
                        label = { Text("Un poco de todo") }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { vm.ejecutarProduccion() },
                enabled = estado.plan.asignaciones.isNotEmpty()
            ) { Text("Trabajar el turno") }
            OutlinedButton(onClick = {
                escenario.participantes.forEach { vm.asignarOficio(it.id, null) }
            }) { Text("Que hagan de todo") }
        }

        val produccion = estado.produccion
        if (produccion != null) {
            Spacer(Modifier.height(12.dp))
            TarjetaValle(Modifier.fillMaxWidth()) {
                Text("Lo que ha salido del turno", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    produccion.total.contenido.forEach { (id, n) -> ChipRecurso(id, n) }
                }
                Spacer(Modifier.height(10.dp))
                produccion.lineas.forEach { linea ->
                    Text(
                        "· ${linea.comentario}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                val falta = produccion.faltaPara(payload.objetivo)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (falta.esVacio) "Objetivo cubierto."
                    else "Todavía falta: ${falta.descripcion(catalogo)}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        val comparacion = estado.comparacion
        if (comparacion != null) {
            Spacer(Modifier.height(12.dp))
            TarjetaValle(
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text("Comparación", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Todos hacen de todo: ${comparacion.resultadoA.total.descripcion(catalogo)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Como lo has organizado tú: ${comparacion.resultadoB.total.descripcion(catalogo)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(comparacion.explicacion, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
