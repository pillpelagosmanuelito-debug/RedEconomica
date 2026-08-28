package com.educalab.redeconomica.ui.screens.lab

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.domain.engine.LabMode
import com.educalab.redeconomica.domain.engine.LabRun
import com.educalab.redeconomica.ui.art.LugarIlustracion
import com.educalab.redeconomica.ui.components.BarraValle
import com.educalab.redeconomica.ui.components.BarraValleSuperior
import com.educalab.redeconomica.ui.components.ChipRecurso
import com.educalab.redeconomica.ui.components.SelectorCantidad
import com.educalab.redeconomica.ui.components.TarjetaValle
import com.educalab.redeconomica.ui.components.TiloDice
import com.educalab.redeconomica.domain.model.ValleyPlace

/**
 * Laboratorio del Valle.
 *
 * Cambias las condiciones, ejecutas y miras qué pasa. No hay respuesta
 * correcta, no hay puntuación y no se puede perder nada: es el sitio para
 * probar ideas raras.
 */
@Composable
fun PantallaLaboratorio(
    viewModel: LabViewModel,
    alVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(
        topBar = { BarraValleSuperior("Laboratorio del Valle", alVolver) }
    ) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LugarIlustracion(ValleyPlace.LABORATORIO, Modifier.size(80.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Prueba y observa qué ocurre. Aquí nada cuenta para el progreso " +
                            "de las misiones.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item { TiloDice("Cambia una cosa cada vez. Así se ve mejor qué la provocó.") }

            item {
                TarjetaValle(Modifier.fillMaxWidth()) {
                    Text("Condiciones del experimento", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))

                    Text("¿Cuántos habitantes trabajan?", style = MaterialTheme.typography.labelLarge)
                    SelectorCantidad(
                        valor = estado.config.habitantes,
                        minimo = 2, maximo = 6,
                        onCambio = { viewModel.cambiarHabitantes(it) }
                    )

                    Spacer(Modifier.height(10.dp))
                    Text("¿Cuántos turnos?", style = MaterialTheme.typography.labelLarge)
                    SelectorCantidad(
                        valor = estado.config.turnos,
                        minimo = 1, maximo = 6,
                        onCambio = { viewModel.cambiarTurnos(it) }
                    )

                    Spacer(Modifier.height(10.dp))
                    Text("¿Cómo se organizan?", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LabMode.entries.forEach { modo ->
                            FilterChip(
                                selected = estado.config.modo == modo,
                                onClick = { viewModel.cambiarModo(modo) },
                                label = { Text(modo.etiqueta) }
                            )
                        }
                    }
                    Text(
                        estado.config.modo.explicacion,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = estado.config.permiteIntercambio,
                            onCheckedChange = { viewModel.alternarIntercambio() }
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Pueden intercambiar entre ellos", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = estado.config.ponenEnComun,
                            onCheckedChange = { viewModel.alternarPonerEnComun() }
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Ponen los recursos en común", style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.ejecutar() }) { Text("Ejecutar el experimento") }
                }
            }

            estado.ultimo?.let { run ->
                item { TarjetaResultado("Último experimento", run) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { viewModel.guardarComoA() }) {
                            Text("Guardar como A")
                        }
                        OutlinedButton(onClick = { viewModel.guardarComoB() }) {
                            Text("Guardar como B")
                        }
                    }
                }
            }

            estado.guardadoA?.let { item { TarjetaResultado("Experimento A", it) } }
            estado.guardadoB?.let { item { TarjetaResultado("Experimento B", it) } }

            estado.comparacion?.let { comp ->
                item {
                    TarjetaValle(
                        Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text("A frente a B", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Producción útil: ${comp.a.valorTotal} → ${comp.b.valorTotal} " +
                                "(${if (comp.diferenciaValor >= 0) "+" else ""}${comp.diferenciaValor})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Necesidades cubiertas: ${comp.a.necesidadesCubiertas} → " +
                                "${comp.b.necesidadesCubiertas} " +
                                "(${if (comp.diferenciaNecesidades >= 0) "+" else ""}${comp.diferenciaNecesidades})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(comp.conclusion, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.limpiarComparacion() }) {
                            Text("Empezar otra comparación")
                        }
                    }
                }
            }

            item {
                Text(
                    "Experimentos guardados: ${estado.experimentosGuardados}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun TarjetaResultado(titulo: String, run: LabRun) {
    TarjetaValle(Modifier.fillMaxWidth()) {
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(run.resumen, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            run.produccionTotal.contenido.forEach { (id, n) -> ChipRecurso(id, n, tamano = 30) }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Necesidades cubiertas: ${run.necesidadesCubiertas} de ${run.necesidadesTotales}",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(4.dp))
        BarraValle(
            progreso = run.porcentajeNecesidades / 100f,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        run.detalle.forEach {
            Text(
                "· $it",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
