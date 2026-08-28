package com.educalab.redeconomica.ui.screens.valley

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.data.repository.MissionStatus
import com.educalab.redeconomica.domain.model.ModuleState
import com.educalab.redeconomica.ui.art.FondoValle
import com.educalab.redeconomica.ui.art.LugarIlustracion
import com.educalab.redeconomica.ui.art.PersonajeRetrato
import com.educalab.redeconomica.ui.components.BarraValle
import com.educalab.redeconomica.ui.components.EtiquetaEstado
import com.educalab.redeconomica.ui.components.TarjetaValle
import com.educalab.redeconomica.ui.components.TiloDice
import com.educalab.redeconomica.ui.navigation.Rutas

/**
 * El mapa del Valle: la pantalla principal.
 *
 * No es una lista de botones: es el pueblo, con su paisaje, sus zonas
 * ilustradas y un camino de misiones que serpentea de arriba abajo.
 */
@Composable
fun PantallaValle(
    viewModel: ValleyViewModel,
    alAbrirMision: (String) -> Unit,
    alAbrirActividad: (String) -> Unit,
    alIrA: (String) -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    Box(Modifier.fillMaxSize()) {
        FondoValle(Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { CabeceraJugador(estado, alIrA) }

            item {
                val siguiente = estado.siguienteMision
                TiloDice(
                    siguiente?.mision?.narrativaInicio
                        ?: "Has recorrido el Valle entero. Puedes repasar o probar cosas en el laboratorio."
                )
            }

            if (estado.reto != null) {
                item {
                    TarjetaRetoDelDia(
                        titulo = estado.reto!!.titulo,
                        tipo = estado.reto!!.tipoEtiqueta,
                        invitacion = estado.reto!!.invitacion,
                        completado = estado.retoCompletado,
                        onAbrir = { alAbrirActividad(estado.reto!!.escenarioId) }
                    )
                }
            }

            item { AccesosRapidos(estado, alIrA) }

            item {
                Text(
                    "El camino del Valle",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(estado.misiones, key = { it.mision.id }) { estadoMision ->
                ParadaDelCamino(
                    estadoMision = estadoMision,
                    aLaDerecha = estadoMision.mision.numero % 2 == 0,
                    onClick = {
                        if (estadoMision.estado != ModuleState.BLOQUEADO) {
                            alAbrirMision(estadoMision.mision.id)
                        }
                    }
                )
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun CabeceraJugador(estado: ValleUiState, alIrA: (String) -> Unit) {
    TarjetaValle(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable { alIrA(Rutas.PERFIL) }
            ) {
                PersonajeRetrato(estado.perfil.avatarId, Modifier.size(64.dp), "Tu avatar")
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(estado.perfil.alias, style = MaterialTheme.typography.titleLarge)
                val resumen = estado.resumen
                Text(
                    resumen?.let { "Nivel ${it.nivel} · ${it.nivelTitulo}" } ?: "Nuevo en el Valle",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                val sellos = resumen?.sellos ?: 0
                val faltan = resumen?.sellosParaSiguienteNivel ?: 0
                BarraValle(
                    progreso = if (faltan == 0) 1f else sellos.toFloat() / (sellos + faltan).toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (faltan > 0) "$sellos sellos · faltan $faltan para el siguiente nivel"
                    else "$sellos sellos · nivel máximo del Valle",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        val r = estado.resumen
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DatoCorto("Misiones", "${r?.misionesCompletadas ?: 0}/${r?.misionesTotales ?: 14}")
            DatoCorto("Tratos", "${r?.intercambiosAceptados ?: 0}")
            DatoCorto("Insignias", "${r?.insigniasConseguidas ?: 0}/${r?.insigniasTotales ?: 11}")
            DatoCorto("Objetos", "${r?.objetosDesbloqueados ?: 0}")
        }
    }
}

@Composable
private fun DatoCorto(etiqueta: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.titleMedium)
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TarjetaRetoDelDia(
    titulo: String,
    tipo: String,
    invitacion: String,
    completado: Boolean,
    onAbrir: () -> Unit
) {
    TarjetaValle(
        Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Text(
            "Reto económico del día",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Spacer(Modifier.height(4.dp))
        Text(titulo, style = MaterialTheme.typography.titleLarge)
        Text(
            "$tipo · $invitacion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Spacer(Modifier.height(10.dp))
        Button(onClick = onAbrir) {
            Text(if (completado) "Volver a jugarlo" else "Empezar el reto")
        }
        if (completado) {
            Spacer(Modifier.height(6.dp))
            Text(
                "✓ Ya lo resolviste hoy. Puedes repetirlo las veces que quieras.",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun AccesosRapidos(estado: ValleUiState, alIrA: (String) -> Unit) {
    val accesos = buildList {
        add(Triple("Mercado", Rutas.MERCADO, true))
        add(Triple("Laboratorio", Rutas.LABORATORIO, estado.misiones.count {
            it.estado == ModuleState.COMPLETADO || it.estado == ModuleState.DOMINADO
        } >= 2))
        add(Triple("Diccionario", Rutas.DICCIONARIO, true))
        add(Triple("Almacén", Rutas.ALMACEN, true))
        add(Triple("Insignias", Rutas.INSIGNIAS, true))
        if (estado.escenariosParaRepasar.isNotEmpty()) add(Triple("Repaso", Rutas.REPASO, true))
    }
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        accesos.forEach { (nombre, ruta, abierto) ->
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (abierto) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable(enabled = abierto) { alIrA(ruta) }
            ) {
                Column(
                    Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(nombre, style = MaterialTheme.typography.labelLarge)
                    if (!abierto) {
                        Text(
                            "Se abre pronto",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParadaDelCamino(
    estadoMision: MissionStatus,
    aLaDerecha: Boolean,
    onClick: () -> Unit
) {
    val bloqueada = estadoMision.estado == ModuleState.BLOQUEADO
    Column(Modifier.fillMaxWidth()) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (aLaDerecha) Arrangement.End else Arrangement.Start
    ) {
        if (aLaDerecha) Spacer(Modifier.width(22.dp))
        TarjetaValle(
            Modifier
                .fillMaxWidth(0.94f)
                .clickable(enabled = !bloqueada) { onClick() },
            color = if (bloqueada) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LugarIlustracion(
                    estadoMision.mision.lugar,
                    Modifier.size(78.dp),
                    apagado = bloqueada
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Misión ${estadoMision.mision.numero} · ${estadoMision.mision.lugar.etiqueta}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(estadoMision.mision.titulo, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (bloqueada) "Termina la misión anterior para abrir esta zona."
                        else estadoMision.mision.objetivoVisible,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EtiquetaEstado(estadoMision.estado)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "${estadoMision.completados}/${estadoMision.total} desafíos",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    if (!bloqueada && estadoMision.completados > 0) {
                        Spacer(Modifier.height(8.dp))
                        BarraValle(
                            progreso = estadoMision.completados.toFloat() / estadoMision.total,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .width(6.dp)
                .height(16.dp)
                .background(Color(0x55FFFFFF))
        )
    }
    }
}
