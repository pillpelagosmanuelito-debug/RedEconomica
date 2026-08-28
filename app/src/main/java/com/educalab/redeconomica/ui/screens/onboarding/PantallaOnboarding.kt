package com.educalab.redeconomica.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.educalab.redeconomica.data.seed.SeedCharacters
import com.educalab.redeconomica.ui.art.FondoValle
import com.educalab.redeconomica.ui.art.LugarIlustracion
import com.educalab.redeconomica.ui.art.PersonajeRetrato
import com.educalab.redeconomica.ui.components.TiloDice
import com.educalab.redeconomica.ui.screens.profile.ProfileViewModel
import com.educalab.redeconomica.domain.model.ValleyPlace

/**
 * Onboarding en cuatro pantallas: el mundo, el guía, cómo se avanza y el
 * perfil (alias + avatar). No se repite nunca más salvo que se pida desde
 * Ajustes.
 */
@Composable
fun PantallaOnboarding(
    viewModel: ProfileViewModel,
    alTerminar: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    var paso by remember { mutableIntStateOf(0) }
    var alias by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf(estado.perfil.avatarId) }

    Box(Modifier.fillMaxSize()) {
        FondoValle(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(Modifier.height(20.dp))
                when (paso) {
                    0 -> PasoBienvenida()
                    1 -> PasoGuia()
                    2 -> PasoComoAvanzar()
                    else -> PasoPerfil(
                        alias = alias,
                        avatarSeleccionado = avatar,
                        onAlias = { alias = it },
                        onAvatar = { avatar = it }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { i ->
                        Box(
                            Modifier
                                .size(if (i == paso) 12.dp else 9.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == paso) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (paso < 3) {
                            paso++
                        } else {
                            viewModel.guardarIdentidad(alias, avatar)
                            viewModel.terminarOnboarding()
                            alTerminar()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (paso < 3) "Seguir" else "Entrar al Valle")
                }
                if (paso < 3) {
                    TextButton(onClick = { paso = 3 }) { Text("Saltar presentación") }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PasoBienvenida() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "RedEconómica",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "El Pueblo de los Intercambios",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LugarIlustracion(ValleyPlace.GRANJA, Modifier.size(84.dp))
            LugarIlustracion(ValleyPlace.MERCADO, Modifier.size(84.dp))
            LugarIlustracion(ValleyPlace.TALLER, Modifier.size(84.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "Bienvenido al Valle Económico. Aquí viven ocho vecinos y cada uno " +
                "sabe hacer cosas distintas… y necesita cosas distintas.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PasoGuia() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PersonajeRetrato("tilo", Modifier.size(150.dp), "Tilo, el cartero del Valle")
        Spacer(Modifier.height(12.dp))
        Text("Este es Tilo", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(10.dp))
        TiloDice(
            "Soy el cartero del Valle. Yo te traigo los encargos, " +
                "pero las decisiones las tomas tú."
        )
    }
}

@Composable
private fun PasoComoAvanzar() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Cómo se avanza", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(14.dp))
        listOf(
            "Produces" to "Cada vecino saca cosas distintas en un turno.",
            "Intercambias" to "Das lo que te sobra por lo que te falta.",
            "Te organizas" to "Decides quién hace qué y repartís el trabajo.",
            "Descubres" to "Al final le pones nombre a lo que ya has hecho."
        ).forEach { (titulo, texto) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(titulo, style = MaterialTheme.typography.titleMedium)
                    Text(
                        texto,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "El progreso se guarda solo. Puedes salir cuando quieras y seguir después.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PasoPerfil(
    alias: String,
    avatarSeleccionado: String,
    onAlias: (String) -> Unit,
    onAvatar: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("¿Cómo te llamamos?", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Pon un mote, no tu nombre real. RedEconómica no pide ningún dato " +
                "personal y funciona sin Internet.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = alias,
            onValueChange = { if (it.length <= 16) onAlias(it) },
            label = { Text("Mote (opcional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Text("Elige tu avatar", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(SeedCharacters.AVATARES.size) { indice ->
                val (id, descripcion) = SeedCharacters.AVATARES[indice]
                val elegido = id == avatarSeleccionado
                Box(
                    Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            if (elegido) 3.dp else 1.dp,
                            if (elegido) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                        .clickable { onAvatar(id) }
                ) {
                    PersonajeRetrato(id, Modifier.size(66.dp), descripcion)
                }
            }
        }
    }
}
