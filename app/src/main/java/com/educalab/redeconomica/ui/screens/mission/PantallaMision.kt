package com.educalab.redeconomica.ui.screens.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.AppContainer
import com.educalab.redeconomica.domain.model.MissionDef
import com.educalab.redeconomica.domain.model.Scenario
import com.educalab.redeconomica.ui.art.ConceptoIlustracion
import com.educalab.redeconomica.ui.art.LugarIlustracion
import com.educalab.redeconomica.ui.components.BarraValleSuperior
import com.educalab.redeconomica.ui.components.TarjetaValle
import com.educalab.redeconomica.ui.components.TiloDice

/**
 * Detalle de una misión: la historia, los desafíos que la componen y lo que
 * se gana al terminarla.
 */
@Composable
fun PantallaMision(
    contenedor: AppContainer,
    misionId: String,
    alAbrirActividad: (String) -> Unit,
    alVolver: () -> Unit
) {
    val mision by produceState<MissionDef?>(initialValue = null, misionId) {
        value = contenedor.catalogo.mision(misionId)
    }
    val escenarios by produceState<List<Scenario>>(initialValue = emptyList(), misionId) {
        value = contenedor.catalogo.escenariosDe(misionId)
    }
    val logrados by contenedor.progreso.escenariosLogradosFlow()
        .collectAsState(initial = emptySet())

    Scaffold(
        topBar = {
            BarraValleSuperior(
                titulo = mision?.titulo ?: "Misión del Valle",
                alVolver = alVolver
            )
        }
    ) { relleno ->
        val m = mision
        if (m == null) {
            Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) {
                Text("Cargando la misión…")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TarjetaValle(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LugarIlustracion(m.lugar, Modifier.size(84.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Misión ${m.numero} · ${m.lugar.etiqueta}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(m.titulo, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                m.lugar.funcion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(m.objetivoVisible, style = MaterialTheme.typography.titleMedium)
                }
            }

            item { TiloDice(m.narrativaInicio) }

            item {
                Text("Desafíos", style = MaterialTheme.typography.headlineSmall)
            }

            itemsIndexed(escenarios, key = { _, e -> e.id }) { indice, escenario ->
                TarjetaDesafio(
                    numero = indice + 1,
                    escenario = escenario,
                    hecho = escenario.id in logrados,
                    onClick = { alAbrirActividad(escenario.id) }
                )
            }

            item {
                TarjetaValle(
                    Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text("Al terminar la misión", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "· ${m.recompensa.sellos} sellos del Valle",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    m.recompensa.objetos.forEach {
                        Text("· Objeto nuevo para el Almacén", style = MaterialTheme.typography.bodyMedium)
                    }
                    m.recompensa.insigniaId?.let {
                        Text("· Una insignia", style = MaterialTheme.typography.bodyMedium)
                    }
                    m.recompensa.zonaDesbloqueada?.let {
                        Text("· Se abre: ${it.etiqueta}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun TarjetaDesafio(
    numero: Int,
    escenario: Scenario,
    hecho: Boolean,
    onClick: () -> Unit
) {
    TarjetaValle(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (hecho) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (hecho) "✓" else numero.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (hecho) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(escenario.titulo, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${escenario.tipo.etiqueta} · ${escenario.tipo.comoSeJuega}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Dificultad " + "●".repeat(escenario.dificultad) +
                        "○".repeat(5 - escenario.dificultad),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ConceptoIlustracion(
                arteId = arteDeConcepto(escenario.conceptoId),
                modifier = Modifier.size(46.dp)
            )
        }
    }
}

private fun arteDeConcepto(conceptoId: String): String = when (conceptoId) {
    "NECESIDADES" -> "arte_necesidad"
    "RECURSOS" -> "arte_recurso"
    "INTERCAMBIO" -> "arte_intercambio"
    "ESPECIALIZACION" -> "arte_especializacion"
    "COOPERACION" -> "arte_cooperacion"
    "ELECCION" -> "arte_costo"
    else -> "arte_mercado"
}
