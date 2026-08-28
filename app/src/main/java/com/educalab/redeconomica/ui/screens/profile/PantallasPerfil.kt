package com.educalab.redeconomica.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as itemsDeRejilla
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.data.seed.SeedCharacters
import com.educalab.redeconomica.ui.art.InsigniaIlustracion
import com.educalab.redeconomica.ui.art.PersonajeRetrato
import com.educalab.redeconomica.ui.art.RecursoIcono
import com.educalab.redeconomica.ui.components.BarraValle
import com.educalab.redeconomica.ui.components.BarraValleSuperior
import com.educalab.redeconomica.ui.components.ChipRecurso
import com.educalab.redeconomica.ui.components.TarjetaValle

/**
 * Almacén del Valle: la colección.
 *
 * Lo que aún no está se ve en silueta y con el texto de cómo conseguirlo: el
 * niño sabe siempre qué le falta y por qué.
 */
@Composable
fun PantallaAlmacen(viewModel: ProfileViewModel, alVolver: () -> Unit) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(topBar = { BarraValleSuperior("Almacén del Valle", alVolver) }) { relleno ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(14.dp)
        ) {
            Text(
                "Tienes ${estado.objetosDesbloqueados.size} de ${estado.objetos.size} objetos",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            BarraValle(
                progreso = if (estado.objetos.isEmpty()) 0f
                else estado.objetosDesbloqueados.size.toFloat() / estado.objetos.size,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            if (!estado.almacen.esVacio) {
                Text("Recursos guardados", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    estado.almacen.contenido.forEach { (id, n) ->
                        ChipRecurso(id, n, tamano = 30)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsDeRejilla(estado.objetos, key = { it.id }) { objeto ->
                    val tengo = objeto.id in estado.objetosDesbloqueados
                    TarjetaValle(
                        Modifier.fillMaxWidth(),
                        color = if (tengo) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (objeto.recursoId != null) {
                                RecursoIcono(objeto.recursoId, Modifier.size(64.dp))
                            } else {
                                Text("★", style = MaterialTheme.typography.displaySmall)
                            }
                            if (!tengo) {
                                Box(
                                    Modifier
                                        .size(70.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⊘", style = MaterialTheme.typography.headlineMedium)
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (tengo) objeto.nombre else "Sin descubrir",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (tengo) objeto.descripcion else objeto.comoSeConsigue,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** Insignias, con su avance real ("3 de 5") además del dibujo. */
@Composable
fun PantallaInsignias(viewModel: ProfileViewModel, alVolver: () -> Unit) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(topBar = { BarraValleSuperior("Insignias del Valle", alVolver) }) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Conseguidas: ${estado.insigniasGanadas.size} de ${estado.insignias.size}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(estado.insignias, key = { it.id }) { insignia ->
                val tengo = insignia.id in estado.insigniasGanadas
                val avance = estado.avanceInsignias[insignia.id] ?: 0
                TarjetaValle(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InsigniaIlustracion(insignia.arteId, tengo, Modifier.size(66.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(insignia.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(
                                insignia.descripcion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            BarraValle(
                                progreso = avance.toFloat() / insignia.meta,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (tengo) "✓ Conseguida" else "$avance de ${insignia.meta} · Todavía no",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/** Perfil y ajustes. Sin datos personales, y con todo explicado. */
@Composable
fun PantallaPerfil(
    viewModel: ProfileViewModel,
    alVolver: () -> Unit,
    alRepetirOnboarding: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    var alias by remember(estado.perfil.alias) { mutableStateOf(estado.perfil.alias) }
    var confirmarReinicio by remember { mutableStateOf(false) }

    Scaffold(topBar = { BarraValleSuperior("Tu perfil", alVolver) }) { relleno ->
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
                        PersonajeRetrato(estado.perfil.avatarId, Modifier.size(76.dp), "Tu avatar")
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(estado.perfil.alias, style = MaterialTheme.typography.headlineSmall)
                            estado.resumen?.let {
                                Text(
                                    "Nivel ${it.nivel} · ${it.nivelTitulo}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { if (it.length <= 16) alias = it },
                        label = { Text("Tu mote") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Avatar", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SeedCharacters.AVATARES.forEach { (id, descripcion) ->
                            val elegido = id == estado.perfil.avatarId
                            Box(
                                Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        if (elegido) 3.dp else 1.dp,
                                        if (elegido) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                        CircleShape
                                    )
                                    .clickable { viewModel.guardarIdentidad(alias, id) }
                            ) {
                                PersonajeRetrato(id, Modifier.size(58.dp), descripcion)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { viewModel.guardarIdentidad(alias, estado.perfil.avatarId) }) {
                        Text("Guardar")
                    }
                }
            }

            item {
                val r = estado.resumen
                TarjetaValle(Modifier.fillMaxWidth()) {
                    Text("Lo que has hecho en el Valle", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "Misiones completadas" to "${r?.misionesCompletadas ?: 0}",
                        "Intercambios aceptados" to "${r?.intercambiosAceptados ?: 0}",
                        "Repartos de trabajo probados" to "${r?.especializacionesProbadas ?: 0}",
                        "Trabajos en equipo terminados" to "${r?.cooperacionesCompletadas ?: 0}",
                        "Repartos de lo escaso resueltos" to "${r?.repartosResueltos ?: 0}",
                        "Decisiones tomadas" to "${r?.decisionesTomadas ?: 0}",
                        "Experimentos del laboratorio" to "${r?.experimentosRealizados ?: 0}",
                        "Sellos del Valle" to "${r?.sellos ?: 0}"
                    ).forEach { (etiqueta, valor) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Text(etiqueta, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(valor, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            item {
                TarjetaValle(Modifier.fillMaxWidth()) {
                    Text("Ajustes", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    FilaAjuste(
                        "Sonidos cortos", estado.perfil.sonidoActivo
                    ) {
                        viewModel.guardarAjustes(
                            it, estado.perfil.vibracionActiva, estado.perfil.textoGrande
                        )
                    }
                    FilaAjuste(
                        "Vibración suave", estado.perfil.vibracionActiva
                    ) {
                        viewModel.guardarAjustes(
                            estado.perfil.sonidoActivo, it, estado.perfil.textoGrande
                        )
                    }
                    FilaAjuste(
                        "Texto más grande", estado.perfil.textoGrande
                    ) {
                        viewModel.guardarAjustes(
                            estado.perfil.sonidoActivo, estado.perfil.vibracionActiva, it
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = {
                        viewModel.repetirOnboarding()
                        alRepetirOnboarding()
                    }) { Text("Ver otra vez la presentación") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { confirmarReinicio = true }) {
                        Text("Empezar el Valle de cero")
                    }
                }
            }

            item {
                TarjetaValle(
                    Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text("Privacidad", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "RedEconómica funciona sin Internet y no pide ningún permiso. " +
                            "No guarda nombre real, ni correo, ni edad, ni ubicación. " +
                            "Todo lo que juegas se queda en este dispositivo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (confirmarReinicio) {
        AlertDialog(
            onDismissRequest = { confirmarReinicio = false },
            title = { Text("¿Empezar de cero?") },
            text = {
                Text(
                    "Se borrará tu progreso: misiones, insignias, objetos y experimentos. " +
                        "Los vecinos y el Valle siguen igual.",
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reiniciarProgreso()
                    confirmarReinicio = false
                }) { Text("Sí, borrar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarReinicio = false }) { Text("No") }
            }
        )
    }
}

@Composable
private fun FilaAjuste(etiqueta: String, valor: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = valor, onCheckedChange = onCambio)
    }
}
