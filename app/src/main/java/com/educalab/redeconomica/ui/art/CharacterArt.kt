package com.educalab.redeconomica.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Retratos de los habitantes del Valle y de los avatares del jugador.
 *
 * Ocho vecinos, un zorro cartero y ocho avatares, todos con la misma base y
 * detalles distintos (gorro, pelo, color de ropa y un objeto de su oficio)
 * para que se distingan de un vistazo sin depender del color.
 */

private data class Aspecto(
    val piel: Color,
    val pelo: Color,
    val ropa: Color,
    val tocado: Int,
    val accesorio: Int
)

private fun aspectoDe(id: String): Aspecto = when (id) {
    "lia" -> Aspecto(ArtColors.pielClara, Color(0xFF7A4B2A), ArtColors.rojoManzana, 1, 1)
    "tomas" -> Aspecto(ArtColors.pielMedia, Color(0xFF3B342C), ArtColors.crema, 2, 2)
    "nina" -> Aspecto(ArtColors.pielOscura, Color(0xFF2A2320), ArtColors.marronMadera, 3, 3)
    "bruno" -> Aspecto(ArtColors.pielMedia, Color(0xFF6B4A22), ArtColors.verdePrado, 1, 4)
    "sofia" -> Aspecto(ArtColors.pielClara, Color(0xFFB35B2A), ArtColors.grisPiedra, 4, 5)
    "emi" -> Aspecto(ArtColors.pielOscura, Color(0xFF1F1B18), ArtColors.azulRio, 5, 6)
    "dani" -> Aspecto(ArtColors.pielClara, Color(0xFF9A7B4F), ArtColors.moradoUva, 6, 7)
    "rita" -> Aspecto(ArtColors.pielMedia, Color(0xFF4A3A2A), ArtColors.naranjaTeja, 5, 8)
    "avatar_1" -> Aspecto(ArtColors.pielClara, Color(0xFF7A4B2A), ArtColors.verdePrado, 1, 0)
    "avatar_2" -> Aspecto(ArtColors.pielMedia, Color(0xFF3B342C), ArtColors.azulRio, 6, 0)
    "avatar_3" -> Aspecto(ArtColors.pielOscura, Color(0xFF241E1A), ArtColors.rojoManzana, 5, 0)
    "avatar_4" -> Aspecto(ArtColors.pielClara, Color(0xFFB35B2A), ArtColors.marronMadera, 3, 0)
    "avatar_5" -> Aspecto(ArtColors.pielMedia, Color(0xFF5A3E24), ArtColors.moradoUva, 7, 0)
    "avatar_6" -> Aspecto(ArtColors.pielOscura, Color(0xFF1F1B18), ArtColors.verdeOscuro, 4, 0)
    "avatar_7" -> Aspecto(ArtColors.pielClara, Color(0xFF9A7B4F), ArtColors.grisPiedra, 2, 0)
    "avatar_8" -> Aspecto(ArtColors.pielMedia, Color(0xFF2A2320), ArtColors.amarilloTrigo, 5, 0)
    else -> Aspecto(ArtColors.pielClara, Color(0xFF5A3E24), ArtColors.verdePrado, 0, 0)
}

@Composable
fun PersonajeRetrato(
    personajeId: String,
    modifier: Modifier = Modifier,
    descripcion: String? = null
) {
    Canvas(
        modifier = modifier.semantics {
            contentDescription = descripcion ?: "Retrato de $personajeId"
        }
    ) {
        if (personajeId == "tilo") dibujarTilo() else dibujarPersonaje(personajeId)
    }
}

fun DrawScope.dibujarPersonaje(id: String) = lienzo { l ->
    val a = aspectoDe(id)

    // Cuerpo
    poligono(l, a.ropa, 26f to 100f, 34f to 62f, 66f to 62f, 74f to 100f)
    poligono(l, a.piel, 22f to 92f, 30f to 68f, 36f to 70f, 30f to 94f)
    poligono(l, a.piel, 78f to 92f, 70f to 68f, 64f to 70f, 70f to 94f)

    // Cuello y cabeza
    caja(l, a.piel, 45f, 52f, 10f, 12f)
    circulo(l, a.piel, 50f, 40f, 22f)

    // Pelo base
    when (a.tocado) {
        1 -> { // sombrero de paja
            poligono(l, a.pelo, 30f to 34f, 70f to 34f, 66f to 22f, 34f to 22f)
            ovalo(l, ArtColors.amarilloTrigo, 50f, 24f, 34f, 7f)
            ovalo(l, Color(0xFFCE9134), 50f, 18f, 18f, 10f)
        }
        2 -> { // gorro de panadero
            ovalo(l, ArtColors.crema, 50f, 16f, 24f, 14f)
            caja(l, ArtColors.cremaOscuro, 30f, 22f, 40f, 8f, 3f)
        }
        3 -> { // gorra de carpintería
            ovalo(l, a.pelo, 50f, 26f, 23f, 16f)
            caja(l, ArtColors.marronOscuro, 27f, 22f, 46f, 8f, 3f)
            poligono(l, ArtColors.marronOscuro, 27f to 28f, 12f to 32f, 27f to 34f)
        }
        4 -> { // pañuelo de taller
            ovalo(l, a.pelo, 50f, 26f, 23f, 16f)
            poligono(l, ArtColors.rojoManzana, 28f to 26f, 72f to 26f, 70f to 16f, 30f to 16f)
        }
        5 -> { // pelo corto + cinta
            ovalo(l, a.pelo, 50f, 26f, 24f, 17f)
            caja(l, ArtColors.crema, 28f, 26f, 44f, 5f, 2f)
        }
        6 -> { // capucha
            ovalo(l, a.ropa, 50f, 30f, 27f, 24f)
            circulo(l, a.piel, 50f, 40f, 20f)
            ovalo(l, a.pelo, 50f, 26f, 20f, 12f)
        }
        7 -> { // trenza
            ovalo(l, a.pelo, 50f, 26f, 24f, 17f)
            ovalo(l, a.pelo, 74f, 52f, 7f, 18f)
            circulo(l, ArtColors.rojoManzana, 74f, 68f, 4f)
        }
        else -> ovalo(l, a.pelo, 50f, 26f, 24f, 16f)
    }

    // Cara
    circulo(l, ArtColors.negroSuave, 42f, 42f, 2.6f)
    circulo(l, ArtColors.negroSuave, 58f, 42f, 2.6f)
    trazo(l, ArtColors.negroSuave, 2f, false, 44f to 50f, 50f to 53f, 56f to 50f)

    // Objeto del oficio
    when (a.accesorio) {
        1 -> circulo(l, ArtColors.rojoManzana, 22f, 78f, 8f)             // manzana
        2 -> ovalo(l, ArtColors.amarilloTrigo, 22f, 78f, 10f, 6f)        // pan
        3 -> caja(l, ArtColors.cremaOscuro, 12f, 74f, 22f, 7f, 2f)       // tabla
        4 -> poligono(l, ArtColors.naranjaTeja, 16f to 84f, 28f to 84f, 22f to 68f) // zanahoria
        5 -> {                                                            // martillo
            caja(l, ArtColors.marronMadera, 20f, 70f, 5f, 20f)
            caja(l, ArtColors.grisMetal, 12f, 66f, 22f, 8f, 2f)
        }
        6 -> ovalo(l, ArtColors.azulRio, 20f, 78f, 12f, 7f)              // pescado
        7 -> poligono(l, ArtColors.moradoUva, 10f to 72f, 34f to 68f, 34f to 84f, 10f to 88f) // tela
        8 -> {                                                            // carreta
            caja(l, ArtColors.marronMadera, 10f, 72f, 24f, 10f, 2f)
            circulo(l, ArtColors.marronOscuro, 16f, 86f, 5f)
            circulo(l, ArtColors.marronOscuro, 28f, 86f, 5f)
        }
    }
}

/** Tilo, el zorro cartero que trae las misiones. */
fun DrawScope.dibujarTilo() = lienzo { l ->
    // Cuerpo
    poligono(l, ArtColors.naranjaTeja, 30f to 100f, 36f to 60f, 64f to 60f, 70f to 100f)
    caja(l, ArtColors.crema, 42f, 66f, 16f, 28f, 4f)
    // Bandolera
    poligono(l, ArtColors.marronMadera, 34f to 62f, 40f to 60f, 70f to 92f, 64f to 96f)
    caja(l, ArtColors.marronOscuro, 62f, 84f, 16f, 14f, 3f)
    // Cabeza
    circulo(l, ArtColors.naranjaTeja, 50f, 38f, 22f)
    poligono(l, ArtColors.naranjaTeja, 32f to 26f, 26f to 6f, 44f to 20f)
    poligono(l, ArtColors.naranjaTeja, 68f to 26f, 74f to 6f, 56f to 20f)
    poligono(l, ArtColors.crema, 34f to 24f, 31f to 12f, 41f to 21f)
    poligono(l, ArtColors.crema, 66f to 24f, 69f to 12f, 59f to 21f)
    // Hocico
    ovalo(l, ArtColors.crema, 50f, 48f, 14f, 10f)
    circulo(l, ArtColors.negroSuave, 50f, 44f, 3.5f)
    circulo(l, ArtColors.negroSuave, 42f, 34f, 3f)
    circulo(l, ArtColors.negroSuave, 58f, 34f, 3f)
    trazo(l, ArtColors.negroSuave, 1.6f, false, 44f to 52f, 50f to 55f, 56f to 52f)
}
