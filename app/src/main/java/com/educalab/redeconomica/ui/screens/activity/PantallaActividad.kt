package com.educalab.redeconomica.ui.screens.activity

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.domain.model.ScenarioPayload
import com.educalab.redeconomica.ui.art.InsigniaIlustracion
import com.educalab.redeconomica.ui.art.RecursoIcono
import com.educalab.redeconomica.ui.components.BarraValleSuperior
import com.educalab.redeconomica.ui.components.CabeceraActividad
import com.educalab.redeconomica.ui.components.PanelResultado
import com.educalab.redeconomica.ui.components.TarjetaValle

/**
 * Pantalla de actividad.
 *
 * Es una sola pantalla que se transforma según la mecánica del escenario:
 * mesa de trueques, reparto de oficios, trabajo en equipo, reparto de lo
 * escaso, decisión con presupuesto, comparación de lo que dejas de hacer,
 * cadena de producción o evaluación de una oferta.
 */
@Composable
fun PantallaActividad(
    viewModel: ActivityViewModel,
    alVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val escenario = estado.escenario

    Scaffold(
        topBar = {
            BarraValleSuperior(
                titulo = escenario?.tipo?.etiqueta ?: "Actividad",
                alVolver = alVolver
            )
        }
    ) { relleno ->
        if (escenario == null) {
            Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) {
                Text("Preparando el desafío…")
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
                CabeceraActividad(
                    titulo = escenario.titulo,
                    situacion = escenario.situacion,
                    instruccion = escenario.instruccion
                )
            }

            item {
                when (escenario.payload) {
                    is ScenarioPayload.Intercambio -> ActividadIntercambio(estado, viewModel)
                    is ScenarioPayload.EvaluarOferta -> ActividadEvaluarOferta(estado, viewModel)
                    is ScenarioPayload.Especializacion -> ActividadEspecializacion(estado, viewModel)
                    is ScenarioPayload.CostoOportunidad -> ActividadCostoOportunidad(estado, viewModel)
                    is ScenarioPayload.Cooperacion -> ActividadCooperacion(estado, viewModel)
                    is ScenarioPayload.Escasez -> ActividadEscasez(estado, viewModel)
                    is ScenarioPayload.Decision -> ActividadDecision(estado, viewModel)
                    is ScenarioPayload.Cadena -> ActividadCadena(estado, viewModel)
                }
            }

            estado.pista?.let { texto ->
                item {
                    TarjetaValle(
                        Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text("Pista de Tilo", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(texto, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                val resultado = estado.resultado
                PanelResultado(
                    visible = estado.mostrarResultado && resultado != null,
                    logrado = resultado?.logrado == true,
                    mensaje = resultado?.mensaje.orEmpty(),
                    explicacion = resultado?.explicacion.orEmpty()
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (resultado?.logrado == true) {
                            Button(onClick = alVolver) { Text("Seguir en el Valle") }
                            OutlinedButton(onClick = { viewModel.reintentar() }) {
                                Text("Probar otra idea")
                            }
                        } else {
                            Button(onClick = { viewModel.reintentar() }) { Text("Volver a probar") }
                        }
                    }
                }
            }

            item {
                Text(
                    "Intentos en este desafío: ${estado.intentos}. Aquí no se pierde nada por probar.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item { Spacer(Modifier.height(30.dp)) }
        }
    }

    val actualizacion = estado.actualizacion
    if (actualizacion != null && actualizacion.hayAlgoQueCelebrar) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarCelebracion() },
            confirmButton = {
                TextButton(onClick = { viewModel.cerrarCelebracion() }) { Text("¡Genial!") }
            },
            title = { Text("El Valle avanza") },
            text = {
                Column {
                    actualizacion.misionCompletada?.let {
                        Text(it.narrativaFinal, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(10.dp))
                    }
                    if (actualizacion.nuevosObjetos.isNotEmpty()) {
                        Text("Nuevo en el Almacén:", style = MaterialTheme.typography.labelLarge)
                        actualizacion.nuevosObjetos.forEach { objeto ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                objeto.recursoId?.let {
                                    RecursoIcono(it, Modifier.size(34.dp))
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(objeto.nombre, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    if (actualizacion.nuevasInsignias.isNotEmpty()) {
                        Text("Insignias nuevas:", style = MaterialTheme.typography.labelLarge)
                        actualizacion.nuevasInsignias.forEach { insignia ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                InsigniaIlustracion(insignia.arteId, true, Modifier.size(42.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(insignia.nombre, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        insignia.descripcion,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
