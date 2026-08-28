package com.educalab.redeconomica.ui.screens.glossary

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.ui.art.ConceptoIlustracion
import com.educalab.redeconomica.ui.components.BarraValleSuperior
import com.educalab.redeconomica.ui.components.TarjetaValle
import com.educalab.redeconomica.ui.components.TiloDice
import com.educalab.redeconomica.ui.screens.profile.ProfileViewModel

/**
 * Diccionario del Valle.
 *
 * Las palabras llegan después de haberlas vivido: cada entrada marca si el
 * niño ya se ha encontrado ese concepto jugando.
 */
@Composable
fun PantallaDiccionario(
    viewModel: ProfileViewModel,
    alVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    var abierta by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { BarraValleSuperior("Diccionario del Valle", alVolver) }
    ) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                TiloDice(
                    "Aquí están las palabras del Valle. Casi todas las habrás usado " +
                        "antes de leerlas."
                )
            }

            items(estado.diccionario, key = { it.id }) { entrada ->
                val vivido = entrada.conceptoId.name in estado.conceptosDescubiertos
                val desplegada = abierta == entrada.id
                TarjetaValle(
                    Modifier
                        .fillMaxWidth()
                        .clickable { abierta = if (desplegada) null else entrada.id }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ConceptoIlustracion(entrada.arteId, Modifier.size(54.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entrada.termino, style = MaterialTheme.typography.titleLarge)
                            Text(
                                entrada.definicionInfantil,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (desplegada) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text("Un ejemplo", style = MaterialTheme.typography.labelLarge)
                                Text(entrada.ejemplo, style = MaterialTheme.typography.bodyMedium)
                                entrada.miniActividad?.let {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Pruébalo", style = MaterialTheme.typography.labelLarge)
                                    Text(it, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (vivido) "✓ Ya te has encontrado esto jugando"
                        else "· Todavía no te ha pasado en el Valle",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
