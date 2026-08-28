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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.ScenarioPayload
import com.educalab.redeconomica.domain.model.TradeEvaluation
import com.educalab.redeconomica.ui.art.RecursoIcono
import com.educalab.redeconomica.ui.components.ChipRecurso
import com.educalab.redeconomica.ui.components.EtiquetaUrgencia
import com.educalab.redeconomica.ui.components.FichaHabitante
import com.educalab.redeconomica.ui.components.SelectorCantidad
import com.educalab.redeconomica.ui.components.TarjetaValle
import com.educalab.redeconomica.ui.theme.ValleColors

/**
 * La mesa de trueques.
 *
 * A la izquierda lo que tienes, a la derecha lo que tiene el vecino, y en el
 * medio la mesa: eliges cuánto pones y cuánto pides. La app no te dice si
 * está bien hasta que lo propones, y si el vecino dice que no, te explica
 * exactamente por qué.
 */
@Composable
fun ActividadIntercambio(estado: ActividadUiState, vm: ActivityViewModel) {
    val sesion = estado.sesion ?: return
    val escenario = estado.escenario ?: return
    val catalogo = SeedCatalogoLocal.catalogo
    val socio = sesion.otros.firstOrNull { it.id == estado.socioSeleccionado }

    Column(Modifier.fillMaxWidth()) {

        // Objetivo
        TarjetaValle(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer) {
            Text("Lo que necesitas conseguir", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sesion.objetivo.contenido.forEach { (id, n) ->
                    val falta = sesion.faltaParaObjetivo().cantidad(id)
                    ChipRecurso(id, n, seleccionado = falta == 0)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (sesion.objetivoCumplido) "¡Ya lo tienes todo!"
                else "Te falta: ${sesion.faltaParaObjetivo().descripcion(catalogo)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(12.dp))

        // Tu cesta
        TarjetaValle(Modifier.fillMaxWidth()) {
            Text("Tu cesta", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (sesion.jugador.inventario.esVacio) {
                Text(
                    "Te has quedado sin nada que ofrecer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sesion.jugador.inventario.contenido.forEach { (id, n) ->
                        ChipRecurso(
                            recursoId = id,
                            cantidad = n,
                            seleccionado = estado.ofrezco.containsKey(id),
                            onClick = {
                                val actual = estado.ofrezco[id] ?: 0
                                vm.cambiarOferta(id, if (actual >= n) 0 else actual + 1)
                            }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Toca un producto para ponerlo en la mesa. Al llegar al máximo, vuelve a cero.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Vecinos
        Text("¿Con quién hablas?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sesion.otros.forEach { vecino ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (vecino.id == estado.socioSeleccionado)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ) {
                    FichaHabitante(
                        personajeId = vecino.id,
                        nombre = vecino.nombre,
                        oficio = vecino.oficio,
                        seleccionado = vecino.id == estado.socioSeleccionado,
                        onClick = { vm.elegirSocio(vecino.id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (socio != null) {
            PanelVecino(socio, estado, vm)
            Spacer(Modifier.height(12.dp))
            LaMesa(estado, vm, socio)
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { vm.proponerIntercambio() },
                enabled = estado.ofrezco.isNotEmpty() && estado.pido.isNotEmpty()
            ) { Text("Proponer el trato") }
            OutlinedButton(onClick = { vm.limpiarMesa() }) { Text("Vaciar la mesa") }
            TextButton(onClick = { vm.pedirPista() }) { Text("Pista") }
        }

        val evaluacion = estado.ultimaEvaluacion
        if (evaluacion != null) {
            Spacer(Modifier.height(12.dp))
            val esSi = evaluacion is TradeEvaluation.Aceptado
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (esSi) ValleColors.VerdeSuave else ValleColors.NaranjaSuave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    when (evaluacion) {
                        is TradeEvaluation.Aceptado -> {
                            Text(
                                evaluacion.mensaje,
                                style = MaterialTheme.typography.titleMedium,
                                color = ValleColors.Tinta
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                evaluacion.loQueGanaCadaUno,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ValleColors.TintaSuave
                            )
                        }
                        is TradeEvaluation.Rechazado -> {
                            Text(
                                evaluacion.mensaje,
                                style = MaterialTheme.typography.titleMedium,
                                color = ValleColors.Tinta
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                evaluacion.pista,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ValleColors.TintaSuave
                            )
                        }
                    }
                }
            }
        }

        (escenario.payload as? ScenarioPayload.Intercambio)?.let {
            if (sesion.historial.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Tratos cerrados hoy: ${sesion.historial.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PanelVecino(
    socio: EconomicCharacter,
    estado: ActividadUiState,
    vm: ActivityViewModel
) {
    TarjetaValle(Modifier.fillMaxWidth()) {
        Text("${socio.nombre} tiene", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (socio.inventario.esVacio) {
                Text("Hoy no le queda nada.", style = MaterialTheme.typography.bodyMedium)
            }
            socio.inventario.contenido.forEach { (id, n) ->
                ChipRecurso(
                    recursoId = id,
                    cantidad = n,
                    seleccionado = estado.pido.containsKey(id),
                    onClick = {
                        val actual = estado.pido[id] ?: 0
                        vm.cambiarPeticion(id, if (actual >= n) 0 else actual + 1)
                    }
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("${socio.nombre} necesita", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        socio.necesidades.forEach { necesidad ->
            val falta = socio.faltante(necesidad.recursoId)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                RecursoIcono(necesidad.recursoId, Modifier.size(30.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (falta > 0) "Le faltan $falta" else "Ya lo tiene cubierto",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(8.dp))
                EtiquetaUrgencia(necesidad.urgencia)
            }
        }
    }
}

@Composable
private fun LaMesa(
    estado: ActividadUiState,
    vm: ActivityViewModel,
    socio: EconomicCharacter
) {
    val sesion = estado.sesion ?: return
    TarjetaValle(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text("La mesa del trato", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        Text("Tú entregas", style = MaterialTheme.typography.labelLarge)
        if (estado.ofrezco.isEmpty()) {
            Text("Todavía nada.", style = MaterialTheme.typography.bodyMedium)
        }
        estado.ofrezco.forEach { (id, n) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                RecursoIcono(id, Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                SelectorCantidad(
                    valor = n,
                    minimo = 0,
                    maximo = sesion.jugador.inventario.cantidad(id),
                    onCambio = { vm.cambiarOferta(id, it) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Text("${socio.nombre} te da", style = MaterialTheme.typography.labelLarge)
        if (estado.pido.isEmpty()) {
            Text("Todavía nada.", style = MaterialTheme.typography.bodyMedium)
        }
        estado.pido.forEach { (id, n) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                RecursoIcono(id, Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                SelectorCantidad(
                    valor = n,
                    minimo = 0,
                    maximo = socio.inventario.cantidad(id),
                    onCambio = { vm.cambiarPeticion(id, it) }
                )
            }
        }
    }
}
