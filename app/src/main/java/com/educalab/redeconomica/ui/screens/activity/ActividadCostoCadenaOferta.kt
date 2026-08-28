package com.educalab.redeconomica.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.domain.model.ChainStage
import com.educalab.redeconomica.domain.model.ScenarioPayload
import com.educalab.redeconomica.ui.art.PersonajeRetrato
import com.educalab.redeconomica.ui.art.RecursoIcono
import com.educalab.redeconomica.ui.art.nombreLegible
import com.educalab.redeconomica.ui.components.BarraValle
import com.educalab.redeconomica.ui.components.ChipRecurso
import com.educalab.redeconomica.ui.components.SelectorCantidad
import com.educalab.redeconomica.ui.components.TarjetaValle

/**
 * "Lo que dejas de hacer".
 *
 * Dos barras que se mueven a la vez: cuanto más produce de una cosa, menos
 * queda de la otra. El niño ve la renuncia antes de que nadie la nombre.
 */
@Composable
fun ActividadCostoOportunidad(estado: ActividadUiState, vm: ActivityViewModel) {
    val escenario = estado.escenario ?: return
    val payload = escenario.payload as? ScenarioPayload.CostoOportunidad ?: return
    val personaje = escenario.participante(payload.personajeId) ?: return

    val maxA = personaje.produccionPorTurno(payload.recursoElegido)
    val maxB = personaje.produccionPorTurno(payload.recursoRenunciado)
    val elegido = payload.cantidadElegida
    val restante = if (maxA == 0) 0 else (maxB * (maxA - elegido)) / maxA

    Column(Modifier.fillMaxWidth()) {

        TarjetaValle(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PersonajeRetrato(personaje.id, Modifier.size(64.dp), personaje.nombre)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(personaje.nombre, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "En un turno: $maxA ${nombreLegible(payload.recursoElegido).lowercase()} " +
                            "O $maxB ${nombreLegible(payload.recursoRenunciado).lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        TarjetaValle(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
            Text("Este turno dedica el tiempo a:", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RecursoIcono(payload.recursoElegido, Modifier.size(36.dp))
                Spacer(Modifier.width(8.dp))
                Text("$elegido", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(10.dp))
                BarraValle(
                    progreso = if (maxA == 0) 0f else elegido.toFloat() / maxA,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("Y con el tiempo que le queda saca:", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RecursoIcono(payload.recursoRenunciado, Modifier.size(36.dp))
                Spacer(Modifier.width(8.dp))
                Text("$restante", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(10.dp))
                BarraValle(
                    progreso = if (maxB == 0) 0f else restante.toFloat() / maxB,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        TarjetaValle(Modifier.fillMaxWidth()) {
            Text(
                "¿A cuántos ${nombreLegible(payload.recursoRenunciado).lowercase()} está renunciando?",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(10.dp))
            SelectorCantidad(
                valor = estado.respuestaRenuncia,
                minimo = 0,
                maximo = maxB,
                onCambio = { vm.cambiarRenuncia(it) }
            )
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = { vm.confirmarRenuncia() }) { Text("Es esta cantidad") }
    }
}

/**
 * "Cadena de producción".
 *
 * Toca un paso para levantarlo y toca otro para intercambiarlos; también hay
 * flechas arriba/abajo para quien prefiera no arrastrar.
 */
@Composable
fun ActividadCadena(estado: ActividadUiState, vm: ActivityViewModel) {
    val escenario = estado.escenario ?: return
    val payload = escenario.payload as? ScenarioPayload.Cadena ?: return

    Column(Modifier.fillMaxWidth()) {
        TarjetaValle(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer) {
            Text(payload.cadena.titulo, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(payload.cadena.introduccion, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(12.dp))

        estado.ordenActual.forEachIndexed { indice, paso ->
            val levantado = estado.pasoLevantado == paso.id
            TarjetaValle(
                Modifier
                    .fillMaxWidth()
                    .border(
                        if (levantado) 3.dp else 0.dp,
                        if (levantado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        MaterialTheme.shapes.medium
                    )
                    .clickable { vm.tocarPaso(paso.id) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${indice + 1}", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(paso.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            paso.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            etiquetaEtapa(paso.etapa),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    paso.personajeId?.let {
                        PersonajeRetrato(it, Modifier.size(40.dp))
                    }
                    Column {
                        OutlinedButton(
                            onClick = { vm.moverPaso(paso.id, true) },
                            enabled = indice > 0
                        ) { Text("▲") }
                        OutlinedButton(
                            onClick = { vm.moverPaso(paso.id, false) },
                            enabled = indice < estado.ordenActual.lastIndex
                        ) { Text("▼") }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Text(
            if (estado.pasoLevantado != null)
                "Has levantado un paso. Toca otro para cambiarlos de sitio."
            else "Toca un paso para levantarlo, o usa las flechas.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))
        Button(onClick = { vm.confirmarCadena() }) { Text("Comprobar la cadena") }
    }
}

private fun etiquetaEtapa(etapa: ChainStage): String = "${etapa.etiqueta} · ${etapa.explicacion}"

/**
 * "¿Aceptarías este trato?".
 *
 * Se ve la propuesta completa, lo que tiene y necesita cada parte, y el niño
 * decide. Da igual acertar o no: después siempre hay una explicación.
 */
@Composable
fun ActividadEvaluarOferta(estado: ActividadUiState, vm: ActivityViewModel) {
    val escenario = estado.escenario ?: return
    val payload = escenario.payload as? ScenarioPayload.EvaluarOferta ?: return
    val proponente = escenario.participante(payload.oferta.proponenteId)
    val receptor = escenario.participante(payload.oferta.receptorId)

    Column(Modifier.fillMaxWidth()) {

        TarjetaValle(Modifier.fillMaxWidth()) {
            Text("La propuesta", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${proponente?.nombre ?: "Alguien"} te da",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        payload.oferta.entrega.contenido.forEach { (id, n) ->
                            ChipRecurso(id, n, tamano = 32)
                        }
                    }
                }
                Text("⇄", style = MaterialTheme.typography.displaySmall)
                Column(Modifier.weight(1f)) {
                    Text("y te pide", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        payload.oferta.pide.contenido.forEach { (id, n) ->
                            ChipRecurso(id, n, tamano = 32)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        listOfNotNull(proponente, receptor).forEach { quien ->
            TarjetaValle(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PersonajeRetrato(
                        if (quien.id == "jugador") escenario.jugador.avatarId else quien.id,
                        Modifier.size(52.dp),
                        quien.nombre
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            if (quien.id == "jugador") "Tú" else quien.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Tiene: " + (if (quien.inventario.esVacio) "nada"
                            else quien.inventario.contenido.entries.joinToString(", ") {
                                "${it.value} ${nombreLegible(it.key).lowercase()}"
                            }),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Necesita: " + (if (quien.necesidades.isEmpty()) "nada ahora mismo"
                            else quien.necesidades.joinToString(", ") {
                                "${it.cantidad} ${nombreLegible(it.recursoId).lowercase()}"
                            }),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { vm.responderOferta(true) }) { Text("Aceptar el trato") }
            OutlinedButton(onClick = { vm.responderOferta(false) }) { Text("Rechazarlo") }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Piensa qué gana cada uno antes de decidir.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
