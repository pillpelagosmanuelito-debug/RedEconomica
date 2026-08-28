package com.educalab.redeconomica.ui.screens.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.AppContainer
import com.educalab.redeconomica.ui.art.ConceptoIlustracion
import com.educalab.redeconomica.ui.components.BarraValleSuperior
import com.educalab.redeconomica.ui.components.TarjetaValle
import com.educalab.redeconomica.ui.components.TiloDice

/**
 * "Practicar otra vez".
 *
 * Reúne los desafíos que todavía no salieron. No es un diagnóstico ni una
 * nota: es una lista de cosas por volver a intentar, sin prisa.
 */
@Composable
fun PantallaRepaso(
    contenedor: AppContainer,
    alAbrirActividad: (String) -> Unit,
    alVolver: () -> Unit
) {
    val pendientes by contenedor.progreso.escenariosParaRepasarFlow()
        .collectAsState(initial = emptyList())

    Scaffold(topBar = { BarraValleSuperior("Practicar otra vez", alVolver) }) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                TiloDice(
                    if (pendientes.isEmpty())
                        "No te queda nada pendiente. Puedes volver al Valle cuando quieras."
                    else "Estos se te resistieron. Con calma, seguro que salen."
                )
            }

            items(pendientes, key = { it }) { escenarioId ->
                val escenario = contenedor.catalogo.escenario(escenarioId)
                TarjetaValle(
                    Modifier
                        .fillMaxWidth()
                        .clickable { alAbrirActividad(escenarioId) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ConceptoIlustracion(
                            arteId = when (escenario.conceptoId) {
                                "NECESIDADES" -> "arte_necesidad"
                                "RECURSOS" -> "arte_escasez"
                                "INTERCAMBIO" -> "arte_intercambio"
                                "ESPECIALIZACION" -> "arte_especializacion"
                                "COOPERACION" -> "arte_cooperacion"
                                "ELECCION" -> "arte_costo"
                                else -> "arte_mercado"
                            },
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(escenario.titulo, style = MaterialTheme.typography.titleMedium)
                            Text(
                                escenario.tipo.etiqueta,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                escenario.instruccion,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
