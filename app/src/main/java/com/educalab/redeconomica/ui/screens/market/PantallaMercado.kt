package com.educalab.redeconomica.ui.screens.market

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.AppContainer
import com.educalab.redeconomica.data.seed.SeedCharacters
import com.educalab.redeconomica.ui.art.LugarIlustracion
import com.educalab.redeconomica.ui.components.BarraValleSuperior
import com.educalab.redeconomica.ui.components.ChipRecurso
import com.educalab.redeconomica.ui.components.EtiquetaUrgencia
import com.educalab.redeconomica.ui.components.FichaHabitante
import com.educalab.redeconomica.ui.components.TarjetaValle
import com.educalab.redeconomica.ui.components.TiloDice
import com.educalab.redeconomica.domain.model.ValleyPlace

/**
 * Mercado del Valle: zona libre, sin misión ni puntuación.
 *
 * Aquí el niño mira quién tiene qué y quién necesita qué, elige a dos vecinos
 * y la app le enseña qué tratos serían posibles entre ellos, calculados por el
 * mismo motor que usa el juego.
 */
@Composable
fun PantallaMercado(
    contenedor: AppContainer,
    alVolver: () -> Unit
) {
    val habitantes = SeedCharacters.HABITANTES
    var unoId by remember { mutableStateOf(habitantes.first().id) }
    var otroId by remember { mutableStateOf(habitantes[1].id) }

    val uno = habitantes.first { it.id == unoId }
    val otro = habitantes.first { it.id == otroId }
    val posibles = remember(unoId, otroId) {
        if (unoId == otroId) emptyList()
        else contenedor.motor.intercambio.buscarIntercambios(uno, listOf(otro))
    }
    val catalogo = contenedor.motor.catalogo

    Scaffold(
        topBar = { BarraValleSuperior("Mercado del Valle", alVolver) }
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
                    LugarIlustracion(ValleyPlace.MERCADO, Modifier.size(80.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Aquí no hay retos. Mira quién necesita qué y prueba a imaginar tratos.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                TiloDice("Elige a dos vecinos y te digo si tienen algo que ofrecerse.")
            }

            item {
                Column(Modifier.fillMaxWidth()) {
                    Text("Primer vecino", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        habitantes.forEach { h ->
                            FichaHabitante(
                                h.id, h.nombre, h.oficio,
                                seleccionado = h.id == unoId,
                                onClick = { unoId = h.id }
                            )
                        }
                    }
                }
            }

            item {
                Column(Modifier.fillMaxWidth()) {
                    Text("Segundo vecino", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        habitantes.forEach { h ->
                            FichaHabitante(
                                h.id, h.nombre, h.oficio,
                                seleccionado = h.id == otroId,
                                onClick = { otroId = h.id }
                            )
                        }
                    }
                }
            }

            item { PuestoDeVecino(uno) }
            item { PuestoDeVecino(otro) }

            item {
                TarjetaValle(
                    Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text("Tratos posibles", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    if (unoId == otroId) {
                        Text(
                            "Elige dos vecinos distintos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (posibles.isEmpty()) {
                        Text(
                            "Hoy no hay ningún trato que les venga bien a los dos. " +
                                "Ni ${uno.nombre} necesita lo que tiene ${otro.nombre}, ni al revés. " +
                                "Un intercambio no siempre es posible.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        posibles.take(6).forEach { oferta ->
                            Text(
                                "· ${uno.nombre} da ${oferta.entrega.descripcion(catalogo)} " +
                                    "y recibe ${oferta.pide.descripcion(catalogo)}.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                        if (posibles.size > 6) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Y ${posibles.size - 6} formas más. Casi nunca hay una sola manera " +
                                    "de hacer un buen trato.",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun PuestoDeVecino(vecino: com.educalab.redeconomica.domain.model.EconomicCharacter) {
    TarjetaValle(Modifier.fillMaxWidth()) {
        FichaHabitante(vecino.id, vecino.nombre, vecino.oficio)
        Spacer(Modifier.height(6.dp))
        Text(vecino.presentacion, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(10.dp))
        Text("En su puesto tiene", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            vecino.inventario.contenido.forEach { (id, n) -> ChipRecurso(id, n, tamano = 32) }
        }
        Spacer(Modifier.height(10.dp))
        Text("Y anda buscando", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        vecino.necesidades.forEach { n ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                ChipRecurso(n.recursoId, n.cantidad, tamano = 28)
                Spacer(Modifier.width(8.dp))
                EtiquetaUrgencia(n.urgencia)
            }
        }
    }
}
