package com.educalab.redeconomica.ui.screens.activity

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.domain.model.ScenarioPayload
import com.educalab.redeconomica.ui.art.PersonajeRetrato
import com.educalab.redeconomica.ui.art.RecursoIcono
import com.educalab.redeconomica.ui.components.ChipRecurso
import com.educalab.redeconomica.ui.components.EtiquetaUrgencia
import com.educalab.redeconomica.ui.components.SelectorCantidad
import com.educalab.redeconomica.ui.components.TarjetaValle

/**
 * "No alcanza para todos".
 *
 * Se ve cuánto hay, cuánto se pide y quién lo necesita con más urgencia. El
 * niño reparte con botones grandes y la app le impide repartir más de lo que
 * existe: la escasez se toca, no se lee.
 */
@Composable
fun ActividadEscasez(estado: ActividadUiState, vm: ActivityViewModel) {
    val escenario = estado.escenario ?: return
    val payload = escenario.payload as? ScenarioPayload.Escasez ?: return
    val caso = payload.caso
    val repartido = estado.reparto.entregado
    val quedan = caso.disponible - repartido

    Column(Modifier.fillMaxWidth()) {

        TarjetaValle(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RecursoIcono(caso.recursoId, Modifier.size(56.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Hay ${caso.disponible}", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Se piden ${caso.demandaTotal}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (caso.hayEscasez) {
                        Text(
                            "Faltan ${caso.faltante}: no llega para todos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(caso.disponible) { i ->
                    RecursoIcono(
                        caso.recursoId,
                        Modifier
                            .size(34.dp)
                            .border(
                                1.dp,
                                if (i < repartido) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                MaterialTheme.shapes.extraSmall
                            )
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Sin repartir: $quedan",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(12.dp))

        caso.demandas.forEach { demanda ->
            val dado = estado.reparto.para(demanda.personajeId)
            TarjetaValle(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PersonajeRetrato(demanda.personajeId, Modifier.size(52.dp), demanda.nombre)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(demanda.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Pide ${demanda.cantidad} · ${demanda.motivo}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        EtiquetaUrgencia(demanda.urgencia)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SelectorCantidad(
                        valor = dado,
                        minimo = 0,
                        maximo = minOf(demanda.cantidad, dado + quedan),
                        onCambio = { vm.cambiarReparto(demanda.personajeId, it) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        when {
                            dado >= demanda.cantidad -> "Cubierto"
                            dado > 0 -> "A medias"
                            else -> "Sin nada"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Button(onClick = { vm.confirmarReparto() }, enabled = repartido > 0) {
            Text("Repartir así")
        }

        val res = estado.resultadoReparto
        if (res != null) {
            Spacer(Modifier.height(12.dp))
            TarjetaValle(
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(res.mensaje, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(res.explicacion, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/**
 * "La gran decisión".
 *
 * Un presupuesto pequeño y varias cosas que se podrían hacer. La app muestra
 * en todo momento lo que costaría y a qué se renunciaría.
 */
@Composable
fun ActividadDecision(estado: ActividadUiState, vm: ActivityViewModel) {
    val escenario = estado.escenario ?: return
    val payload = escenario.payload as? ScenarioPayload.Decision ?: return
    val caso = payload.caso
    val catalogo = SeedCatalogoLocal.catalogo

    Column(Modifier.fillMaxWidth()) {

        TarjetaValle(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer) {
            Text(caso.titulo, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Tienes:", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                caso.presupuesto.contenido.forEach { (id, n) -> ChipRecurso(id, n) }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Puedes elegir como mucho ${caso.maxSelecciones}.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(12.dp))

        caso.opciones.forEach { opcion ->
            val elegida = opcion.id in estado.seleccion
            TarjetaValle(
                Modifier
                    .fillMaxWidth()
                    .clickable { vm.alternarOpcion(opcion.id) },
                color = if (elegida) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surface
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (elegida) "✓" else "○",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(opcion.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            opcion.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cuesta:", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        opcion.costo.contenido.forEach { (id, n) ->
                            ChipRecurso(id, n, tamano = 28)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Consigues:", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        opcion.obtiene.contenido.forEach { (id, n) ->
                            ChipRecurso(id, n, tamano = 28)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        val seleccionadas = caso.opciones.filter { it.id in estado.seleccion }
        if (seleccionadas.isNotEmpty()) {
            val costo = seleccionadas.fold(
                com.educalab.redeconomica.domain.model.Inventory.VACIO
            ) { acc, o -> acc.mas(o.costo) }
            Text(
                "Con esta elección gastas ${costo.descripcion(catalogo)}.",
                style = MaterialTheme.typography.bodyMedium
            )
            val renuncias = caso.opciones.filter { it.id !in estado.seleccion }.map { it.nombre }
            if (renuncias.isNotEmpty()) {
                Text(
                    "Y te quedas sin: ${renuncias.joinToString(", ")}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        Button(onClick = { vm.confirmarDecision() }, enabled = estado.seleccion.isNotEmpty()) {
            Text("Decidir")
        }

        val res = estado.resultadoDecision
        if (res != null) {
            Spacer(Modifier.height(12.dp))
            TarjetaValle(
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(res.mensaje, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(res.explicacion, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
