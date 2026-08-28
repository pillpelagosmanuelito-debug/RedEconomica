package com.educalab.redeconomica.ui.screens.activity

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.domain.model.ScenarioPayload
import com.educalab.redeconomica.ui.components.BarraValle
import com.educalab.redeconomica.ui.components.FichaHabitante
import com.educalab.redeconomica.ui.components.TarjetaValle

/**
 * "Trabajo en equipo".
 *
 * Cada etapa es un eslabón de la cadena. El niño coloca a cada habitante donde
 * quiere y ve la capacidad de cada etapa; lo que sale al final es el mínimo,
 * y la app señala exactamente qué eslabón frena a los demás.
 */
@Composable
fun ActividadCooperacion(estado: ActividadUiState, vm: ActivityViewModel) {
    val escenario = estado.escenario ?: return
    val payload = escenario.payload as? ScenarioPayload.Cooperacion ?: return
    val etapas = payload.etapas.sortedBy { it.orden }

    Column(Modifier.fillMaxWidth()) {

        TarjetaValle(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer) {
            Text("El pedido", style = MaterialTheme.typography.labelLarge)
            Text(
                "Hay que conseguir ${payload.objetivo} unidades.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "La cadena tiene ${etapas.size} etapas. Si una se queda vacía, no llega nada al final.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(12.dp))

        escenario.participantes.forEach { habitante ->
            TarjetaValle(Modifier.fillMaxWidth()) {
                FichaHabitante(habitante.id, habitante.nombre, habitante.oficio)
                Spacer(Modifier.height(8.dp))
                Text("¿En qué etapa lo pones?", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    etapas.forEach { etapa ->
                        val aporta = etapa.rendimientoDe(habitante.id)
                        FilterChip(
                            selected = estado.planCooperacion.etapaDe(habitante.id) == etapa.id,
                            onClick = { vm.asignarEtapa(habitante.id, etapa.id) },
                            label = { Text("${etapa.nombre} (+$aporta)") }
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        TarjetaValle(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
            Text("La cadena", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            etapas.forEach { etapa ->
                val gente = estado.planCooperacion.personajesEn(etapa.id)
                val capacidad = gente.sumOf { etapa.rendimientoDe(it) }
                Column(Modifier.padding(vertical = 5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${etapa.orden}. ${etapa.nombre}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text("$capacidad", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        etapa.descripcion,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    BarraValle(
                        progreso = capacidad.toFloat() / payload.objetivo.coerceAtLeast(1),
                        modifier = Modifier.fillMaxWidth(),
                        color = if (capacidad == 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        if (gente.isEmpty()) "Nadie en esta etapa"
                        else "Trabajan aquí: ${gente.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { vm.ejecutarCooperacion() },
            enabled = estado.planCooperacion.asignaciones.isNotEmpty()
        ) { Text("Empezar el trabajo") }

        val salida = estado.resultadoCooperacion
        if (salida != null) {
            Spacer(Modifier.height(12.dp))
            TarjetaValle(
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(salida.mensaje, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(salida.explicacion, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Row {
                    Column(Modifier.weight(1f)) {
                        Text("Trabajando juntos", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${salida.resultado}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Cada uno por su cuenta", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${salida.resultadoSinCooperar}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }
}
