package com.educalab.redeconomica.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.educalab.redeconomica.domain.model.ValleyPlace

/**
 * Los edificios del Valle y el paisaje de fondo.
 *
 * El mapa del pueblo no es una lista de botones: es un dibujo con colinas,
 * camino, río y construcciones donde el niño toca el sitio al que quiere ir.
 */

@Composable
fun LugarIlustracion(
    lugar: ValleyPlace,
    modifier: Modifier = Modifier,
    apagado: Boolean = false
) {
    Canvas(
        modifier = modifier.semantics { contentDescription = lugar.etiqueta }
    ) {
        dibujarLugar(lugar, apagado)
    }
}

private fun apagar(color: Color, apagado: Boolean): Color =
    if (!apagado) color else Color(
        red = color.red * 0.45f + 0.35f,
        green = color.green * 0.45f + 0.35f,
        blue = color.blue * 0.45f + 0.35f,
        alpha = color.alpha
    )

fun DrawScope.dibujarLugar(lugar: ValleyPlace, apagado: Boolean = false) = lienzo { l ->
    fun c(color: Color) = apagar(color, apagado)

    // Suelo común
    ovalo(l, c(ArtColors.verdeClaro), 50f, 92f, 46f, 10f)

    when (lugar) {
        ValleyPlace.GRANJA -> {
            caja(l, c(ArtColors.rojoManzana), 22f, 46f, 56f, 42f)
            poligono(l, c(ArtColors.rojoOscuro), 16f to 46f, 50f to 20f, 84f to 46f)
            caja(l, c(ArtColors.crema), 42f, 60f, 16f, 28f, 2f)
            caja(l, c(ArtColors.cremaOscuro), 28f, 54f, 10f, 10f)
            circulo(l, c(ArtColors.amarilloTrigo), 68f, 60f, 6f)
        }
        ValleyPlace.HUERTO -> {
            for (i in 0..3) {
                caja(l, c(ArtColors.marronMadera), 16f + i * 18f, 62f, 12f, 26f, 2f)
                poligono(
                    l, c(ArtColors.verdePrado),
                    (22f + i * 18f) to 62f, (14f + i * 18f) to 42f, (30f + i * 18f) to 42f
                )
            }
            caja(l, c(ArtColors.marronOscuro), 10f, 58f, 80f, 4f)
        }
        ValleyPlace.PANADERIA -> {
            caja(l, c(ArtColors.cremaOscuro), 20f, 44f, 60f, 44f)
            poligono(l, c(ArtColors.naranjaTeja), 14f to 44f, 50f to 18f, 86f to 44f)
            caja(l, c(ArtColors.marronOscuro), 40f, 58f, 20f, 30f, 3f)
            ovalo(l, c(ArtColors.amarilloTrigo), 50f, 66f, 8f, 5f)
            caja(l, c(ArtColors.grisPiedra), 64f, 20f, 10f, 20f)
            circulo(l, c(Color(0x66FFFFFF)), 69f, 14f, 6f)
        }
        ValleyPlace.TALLER -> {
            caja(l, c(ArtColors.grisPiedra), 20f, 46f, 60f, 42f)
            poligono(l, c(ArtColors.grisMetal), 16f to 46f, 50f to 24f, 84f to 46f)
            caja(l, c(ArtColors.marronOscuro), 30f, 60f, 16f, 28f, 2f)
            caja(l, c(ArtColors.naranjaTeja), 56f, 58f, 16f, 14f, 2f)
            trazo(l, c(ArtColors.amarilloTrigo), 3f, false, 58f to 64f, 70f to 64f)
        }
        ValleyPlace.CARPINTERIA -> {
            caja(l, c(ArtColors.marronMadera), 20f, 48f, 60f, 40f)
            poligono(l, c(ArtColors.marronOscuro), 14f to 48f, 50f to 24f, 86f to 48f)
            for (i in 0..2) {
                caja(l, c(ArtColors.cremaOscuro), 26f, 58f + i * 10f, 48f, 6f, 2f)
            }
        }
        ValleyPlace.PESQUERIA -> {
            caja(l, c(ArtColors.azulClaro), 6f, 74f, 88f, 18f)
            caja(l, c(ArtColors.marronMadera), 26f, 46f, 48f, 30f)
            poligono(l, c(ArtColors.azulRio), 20f to 46f, 50f to 26f, 80f to 46f)
            trazo(l, c(ArtColors.marronOscuro), 3f, false, 34f to 76f, 34f to 92f)
            trazo(l, c(ArtColors.marronOscuro), 3f, false, 66f to 76f, 66f to 92f)
            ovalo(l, c(ArtColors.azulRio), 78f, 84f, 9f, 5f)
        }
        ValleyPlace.TELAR -> {
            caja(l, c(ArtColors.moradoUva), 22f, 48f, 56f, 40f)
            poligono(l, c(Color(0xFF5F4278)), 16f to 48f, 50f to 24f, 84f to 48f)
            caja(l, c(ArtColors.crema), 32f, 58f, 36f, 22f, 2f)
            for (i in 0..3) linea(l, c(ArtColors.moradoUva), 34f + i * 9f, 58f, 34f + i * 9f, 80f, 1.6f)
        }
        ValleyPlace.MERCADO -> {
            for (i in 0..1) {
                val x = 12f + i * 42f
                caja(l, c(ArtColors.marronMadera), x, 56f, 34f, 32f, 2f)
                poligono(
                    l, c(if (i == 0) ArtColors.rojoManzana else ArtColors.azulRio),
                    (x - 4f) to 56f, (x + 17f) to 36f, (x + 38f) to 56f
                )
            }
            circulo(l, c(ArtColors.amarilloTrigo), 22f, 68f, 5f)
            circulo(l, c(ArtColors.verdePrado), 34f, 68f, 5f)
            ovalo(l, c(ArtColors.naranjaTeja), 66f, 68f, 7f, 5f)
        }
        ValleyPlace.PLAZA -> {
            ovalo(l, c(ArtColors.cremaOscuro), 50f, 76f, 40f, 18f)
            caja(l, c(ArtColors.grisPiedra), 44f, 44f, 12f, 34f, 3f)
            circulo(l, c(ArtColors.azulClaro), 50f, 40f, 14f)
            circulo(l, c(ArtColors.crema), 50f, 40f, 9f)
            trazo(l, c(ArtColors.negroSuave), 2f, false, 50f to 40f, 50f to 33f)
            trazo(l, c(ArtColors.negroSuave), 2f, false, 50f to 40f, 56f to 43f)
        }
        ValleyPlace.CENTRO_INTERCAMBIO -> {
            caja(l, c(ArtColors.cremaOscuro), 18f, 44f, 64f, 44f)
            poligono(l, c(ArtColors.verdeOscuro), 12f to 44f, 50f to 20f, 88f to 44f)
            circulo(l, c(ArtColors.amarilloTrigo), 36f, 64f, 9f)
            ovalo(l, c(ArtColors.rojoManzana), 64f, 64f, 10f, 7f)
            trazo(l, c(ArtColors.marronOscuro), 3f, false, 44f to 58f, 56f to 58f)
            trazo(l, c(ArtColors.marronOscuro), 3f, false, 44f to 70f, 56f to 70f)
        }
        ValleyPlace.COOPERATIVA -> {
            caja(l, c(ArtColors.verdePrado), 14f, 48f, 72f, 40f)
            poligono(l, c(ArtColors.verdeOscuro), 8f to 48f, 50f to 22f, 92f to 48f)
            circulo(l, c(ArtColors.crema), 34f, 66f, 8f)
            circulo(l, c(ArtColors.crema), 50f, 62f, 8f)
            circulo(l, c(ArtColors.crema), 66f, 66f, 8f)
        }
        ValleyPlace.LABORATORIO -> {
            caja(l, c(ArtColors.azulClaro), 22f, 46f, 56f, 42f)
            poligono(l, c(ArtColors.azulRio), 16f to 46f, 50f to 22f, 84f to 46f)
            poligono(l, c(ArtColors.crema), 44f to 56f, 56f to 56f, 62f to 80f, 38f to 80f)
            caja(l, c(ArtColors.verdePrado), 40f, 70f, 20f, 10f)
            circulo(l, c(ArtColors.amarilloTrigo), 46f, 66f, 3f)
            circulo(l, c(ArtColors.rojoManzana), 55f, 62f, 2.5f)
        }
        ValleyPlace.ALMACEN -> {
            caja(l, c(ArtColors.marronMadera), 16f, 44f, 68f, 44f)
            poligono(l, c(ArtColors.amarilloTrigo), 10f to 44f, 50f to 20f, 90f to 44f)
            caja(l, c(ArtColors.cremaOscuro), 26f, 58f, 20f, 14f, 2f)
            caja(l, c(ArtColors.cremaOscuro), 54f, 58f, 20f, 14f, 2f)
            caja(l, c(ArtColors.cremaOscuro), 40f, 74f, 20f, 14f, 2f)
        }
    }

    if (apagado) {
        // Candado: el estado bloqueado no se expresa solo con color.
        caja(l, ArtColors.grisPiedra, 40f, 40f, 20f, 18f, 3f)
        trazo(l, ArtColors.grisPiedra, 4f, false, 44f to 40f, 44f to 32f, 56f to 32f, 56f to 40f)
        circulo(l, ArtColors.crema, 50f, 49f, 3f)
    }
}

/** Paisaje del Valle: cielo, colinas, río, camino y arbolitos. */
@Composable
fun FondoValle(modifier: Modifier = Modifier, oscuro: Boolean = isSystemInDarkTheme()) {
    Canvas(modifier = modifier) {
        val cieloAlto = if (oscuro) Color(0xFF23334A) else Color(0xFFBFE3F5)
        val cieloBajo = if (oscuro) Color(0xFF33465E) else Color(0xFFE9F6DE)
        drawRect(
            brush = Brush.verticalGradient(listOf(cieloAlto, cieloBajo)),
            size = size
        )
        val w = size.width
        val h = size.height

        fun colina(color: Color, baseY: Float, altura: Float, centro: Float, ancho: Float) {
            val camino = Path().apply {
                moveTo(centro - ancho, baseY)
                quadraticBezierTo(centro, baseY - altura, centro + ancho, baseY)
                lineTo(centro + ancho, h)
                lineTo(centro - ancho, h)
                close()
            }
            drawPath(camino, color)
        }

        val verdeLejos = if (oscuro) Color(0xFF2C4A38) else Color(0xFF9AD3A3)
        val verdeCerca = if (oscuro) Color(0xFF1F3A2A) else Color(0xFF6FBF80)
        colina(verdeLejos, h * 0.72f, h * 0.28f, w * 0.22f, w * 0.45f)
        colina(verdeLejos, h * 0.74f, h * 0.24f, w * 0.78f, w * 0.42f)
        colina(verdeCerca, h * 0.86f, h * 0.22f, w * 0.50f, w * 0.70f)

        // Río
        val rio = Path().apply {
            moveTo(0f, h * 0.93f)
            cubicTo(w * 0.3f, h * 0.86f, w * 0.6f, h * 1.0f, w, h * 0.90f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(rio, if (oscuro) Color(0xFF2B4A63) else Color(0xFF8FC9E8))

        // Nubes
        if (!oscuro) {
            val nube = Color(0xCCFFFFFF)
            drawCircle(nube, radius = h * 0.05f, center = Offset(w * 0.18f, h * 0.16f))
            drawCircle(nube, radius = h * 0.07f, center = Offset(w * 0.25f, h * 0.15f))
            drawCircle(nube, radius = h * 0.05f, center = Offset(w * 0.32f, h * 0.17f))
            drawCircle(nube, radius = h * 0.045f, center = Offset(w * 0.72f, h * 0.12f))
            drawCircle(nube, radius = h * 0.06f, center = Offset(w * 0.79f, h * 0.11f))
        }
    }
}

/** Arbolito decorativo reutilizable. */
fun DrawScope.dibujarArbol(l: Lienzo, x: Float, y: Float, escala: Float) {
    caja(l, ArtColors.marronMadera, x - 2f * escala, y, 4f * escala, 14f * escala, 1f)
    circulo(l, ArtColors.verdeOscuro, x, y - 4f * escala, 9f * escala)
    circulo(l, ArtColors.verdePrado, x - 4f * escala, y, 7f * escala)
    circulo(l, ArtColors.verdePrado, x + 4f * escala, y, 7f * escala)
}
