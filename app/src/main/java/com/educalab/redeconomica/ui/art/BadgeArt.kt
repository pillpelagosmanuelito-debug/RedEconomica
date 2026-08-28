package com.educalab.redeconomica.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Insignias e iconos de conceptos.
 *
 * Una insignia bloqueada no se distingue solo por el color: se dibuja en gris
 * y con la silueta vacía, y además la tarjeta lleva el texto "Todavía no".
 */

@Composable
fun InsigniaIlustracion(
    arteId: String,
    conseguida: Boolean,
    modifier: Modifier = Modifier,
    descripcion: String? = null
) {
    Canvas(
        modifier = modifier.semantics {
            contentDescription = descripcion
                ?: if (conseguida) "Insignia conseguida" else "Insignia todavía no conseguida"
        }
    ) {
        dibujarInsignia(arteId, conseguida)
    }
}

fun DrawScope.dibujarInsignia(arteId: String, conseguida: Boolean) = lienzo { l ->
    val fondo = if (conseguida) ArtColors.amarilloTrigo else Color(0xFFD8D3C8)
    val aro = if (conseguida) ArtColors.naranjaTeja else ArtColors.grisPiedra
    val tinta = if (conseguida) ArtColors.marronOscuro else ArtColors.grisPiedra

    // Cintas
    poligono(l, if (conseguida) ArtColors.rojoManzana else Color(0xFFC4BDB0),
        34f to 68f, 46f to 68f, 46f to 96f, 40f to 88f, 34f to 96f)
    poligono(l, if (conseguida) ArtColors.rojoOscuro else Color(0xFFB4ADA0),
        54f to 68f, 66f to 68f, 66f to 96f, 60f to 88f, 54f to 96f)

    circulo(l, aro, 50f, 44f, 34f)
    circulo(l, fondo, 50f, 44f, 28f)

    when (arteId) {
        "insignia_apreton" -> { // dos manos que se dan
            caja(l, tinta, 30f, 40f, 20f, 8f, 3f)
            caja(l, tinta, 50f, 40f, 20f, 8f, 3f)
            circulo(l, fondo, 50f, 44f, 5f)
            trazo(l, tinta, 3f, false, 34f to 52f, 44f to 56f)
            trazo(l, tinta, 3f, false, 66f to 52f, 56f to 56f)
        }
        "insignia_balanza" -> {
            linea(l, tinta, 50f, 26f, 50f, 60f, 3f)
            linea(l, tinta, 30f, 34f, 70f, 34f, 3f)
            trazo(l, tinta, 2f, false, 30f to 34f, 24f to 46f, 36f to 46f, 30f to 34f)
            trazo(l, tinta, 2f, false, 70f to 34f, 64f to 46f, 76f to 46f, 70f to 34f)
            caja(l, tinta, 40f, 58f, 20f, 4f, 2f)
        }
        "insignia_azada" -> {
            linea(l, tinta, 38f, 62f, 60f, 28f, 4f)
            poligono(l, tinta, 34f to 60f, 46f to 66f, 38f to 70f)
        }
        "insignia_cesta" -> {
            poligono(l, tinta, 32f to 44f, 68f to 44f, 62f to 64f, 38f to 64f)
            trazo(l, tinta, 3f, false, 36f to 44f, 44f to 30f, 56f to 30f, 64f to 44f)
        }
        "insignia_engranaje" -> {
            circulo(l, tinta, 50f, 44f, 16f)
            circulo(l, fondo, 50f, 44f, 8f)
            for (i in 0..5) {
                val ang = Math.toRadians((i * 60).toDouble())
                val cx = 50f + 20f * Math.cos(ang).toFloat()
                val cy = 44f + 20f * Math.sin(ang).toFloat()
                circulo(l, tinta, cx, cy, 4.5f)
            }
        }
        "insignia_bifurcacion" -> {
            trazo(l, tinta, 4f, false, 50f to 64f, 50f to 48f, 32f to 30f)
            trazo(l, tinta, 4f, false, 50f to 48f, 68f to 30f)
            circulo(l, tinta, 32f, 28f, 5f)
            circulo(l, tinta, 68f, 28f, 5f)
        }
        "insignia_manos" -> {
            circulo(l, tinta, 38f, 40f, 8f)
            circulo(l, tinta, 62f, 40f, 8f)
            circulo(l, tinta, 50f, 56f, 8f)
            trazo(l, tinta, 2.5f, false, 38f to 40f, 62f to 40f, 50f to 56f, 38f to 40f)
        }
        "insignia_corona" -> {
            poligono(
                l, tinta,
                30f to 58f, 34f to 30f, 42f to 44f, 50f to 26f,
                58f to 44f, 66f to 30f, 70f to 58f
            )
            caja(l, tinta, 30f, 58f, 40f, 6f, 2f)
        }
        "insignia_libro" -> {
            caja(l, tinta, 28f, 30f, 44f, 30f, 2f)
            caja(l, fondo, 32f, 34f, 16f, 22f, 1f)
            caja(l, fondo, 52f, 34f, 16f, 22f, 1f)
            linea(l, tinta, 50f, 30f, 50f, 60f, 2f)
        }
        "insignia_almacen" -> {
            poligono(l, tinta, 24f to 42f, 50f to 26f, 76f to 42f)
            caja(l, tinta, 30f, 42f, 40f, 22f)
            caja(l, fondo, 38f, 48f, 10f, 10f)
            caja(l, fondo, 52f, 48f, 10f, 10f)
        }
        "insignia_matraz" -> {
            poligono(l, tinta, 44f to 26f, 56f to 26f, 66f to 62f, 34f to 62f)
            caja(l, fondo, 40f, 48f, 20f, 8f)
            circulo(l, tinta, 46f, 54f, 2.5f)
            circulo(l, tinta, 55f, 52f, 2f)
        }
        else -> circulo(l, tinta, 50f, 44f, 12f)
    }
}

/** Iconos del Diccionario del Valle. */
@Composable
fun ConceptoIlustracion(arteId: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.semantics { contentDescription = "Ilustración del concepto" }) {
        dibujarConcepto(arteId)
    }
}

fun DrawScope.dibujarConcepto(arteId: String) = lienzo { l ->
    circulo(l, ArtColors.cremaOscuro, 50f, 50f, 46f)
    val tinta = ArtColors.marronOscuro
    when (arteId) {
        "arte_necesidad" -> {
            circulo(l, ArtColors.pielClara, 36f, 40f, 12f)
            poligono(l, ArtColors.rojoManzana, 24f to 78f, 30f to 54f, 44f to 54f, 50f to 78f)
            circulo(l, ArtColors.crema, 70f, 40f, 14f)
            trazo(l, tinta, 3f, false, 65f to 36f, 70f to 32f, 74f to 38f, 70f to 44f)
            circulo(l, tinta, 70f, 50f, 2f)
        }
        "arte_recurso" -> {
            caja(l, ArtColors.marronMadera, 22f, 52f, 24f, 26f, 3f)
            poligono(l, ArtColors.verdePrado, 58f to 76f, 54f to 44f, 78f to 44f, 74f to 76f)
            circulo(l, ArtColors.amarilloTrigo, 50f, 32f, 10f)
        }
        "arte_escasez" -> {
            ovalo(l, ArtColors.amarilloTrigo, 34f, 46f, 14f, 9f)
            for (i in 0..3) circulo(l, ArtColors.pielMedia, 24f + i * 17f, 74f, 8f)
            trazo(l, ArtColors.rojoManzana, 4f, false, 62f to 36f, 84f to 56f)
            trazo(l, ArtColors.rojoManzana, 4f, false, 84f to 36f, 62f to 56f)
        }
        "arte_produccion" -> {
            caja(l, ArtColors.grisPiedra, 22f, 54f, 26f, 24f, 2f)
            trazo(l, tinta, 4f, false, 52f to 66f, 70f to 48f)
            poligono(l, ArtColors.amarilloTrigo, 62f to 40f, 82f to 40f, 72f to 24f)
        }
        "arte_intercambio" -> {
            circulo(l, ArtColors.rojoManzana, 28f, 60f, 12f)
            ovalo(l, ArtColors.amarilloTrigo, 72f, 60f, 14f, 9f)
            trazo(l, tinta, 4f, false, 36f to 40f, 50f to 30f, 64f to 40f)
            poligono(l, tinta, 64f to 34f, 74f to 42f, 60f to 46f)
        }
        "arte_beneficio" -> {
            circulo(l, ArtColors.verdePrado, 34f, 52f, 18f)
            circulo(l, ArtColors.azulRio, 66f, 52f, 18f)
            circulo(l, ArtColors.amarilloTrigo, 50f, 52f, 10f)
        }
        "arte_especializacion" -> {
            for (i in 0..2) {
                circulo(l, ArtColors.pielClara, 26f + i * 24f, 40f, 9f)
                caja(l, listOf(ArtColors.rojoManzana, ArtColors.azulRio, ArtColors.verdePrado)[i],
                    18f + i * 24f, 52f, 16f, 26f, 3f)
            }
        }
        "arte_ventaja" -> {
            trazo(l, tinta, 3f, false, 20f to 78f, 20f to 26f)
            trazo(l, tinta, 3f, false, 20f to 78f, 84f to 78f)
            trazo(l, ArtColors.rojoManzana, 4f, false, 20f to 34f, 80f to 70f)
            circulo(l, ArtColors.azulRio, 48f, 52f, 5f)
        }
        "arte_cooperacion" -> {
            circulo(l, ArtColors.rojoManzana, 32f, 38f, 10f)
            circulo(l, ArtColors.azulRio, 68f, 38f, 10f)
            circulo(l, ArtColors.verdePrado, 50f, 68f, 10f)
            trazo(l, tinta, 3f, true, 32f to 38f, 68f to 38f, 50f to 68f)
        }
        "arte_cadena" -> {
            for (i in 0..3) {
                caja(
                    l,
                    listOf(ArtColors.verdePrado, ArtColors.amarilloTrigo, ArtColors.naranjaTeja, ArtColors.azulRio)[i],
                    14f + i * 20f, 44f, 14f, 14f, 3f
                )
                if (i < 3) trazo(l, tinta, 2.5f, false, (28f + i * 20f) to 51f, (34f + i * 20f) to 51f)
            }
        }
        "arte_eleccion" -> {
            trazo(l, tinta, 4f, false, 50f to 82f, 50f to 54f)
            trazo(l, tinta, 4f, false, 50f to 54f, 26f to 30f)
            trazo(l, tinta, 4f, false, 50f to 54f, 74f to 30f)
            circulo(l, ArtColors.verdePrado, 26f, 26f, 9f)
            circulo(l, ArtColors.grisPiedra, 74f, 26f, 9f)
        }
        "arte_costo" -> {
            caja(l, ArtColors.verdePrado, 20f, 40f, 24f, 38f, 3f)
            caja(l, Color(0x66888888), 56f, 56f, 24f, 22f, 3f)
            trazo(l, ArtColors.rojoManzana, 3f, false, 56f to 56f, 80f to 78f)
        }
        "arte_mercado" -> {
            caja(l, ArtColors.marronMadera, 18f, 52f, 30f, 26f, 2f)
            caja(l, ArtColors.marronMadera, 54f, 52f, 30f, 26f, 2f)
            poligono(l, ArtColors.rojoManzana, 14f to 52f, 33f to 34f, 52f to 52f)
            poligono(l, ArtColors.azulRio, 50f to 52f, 69f to 34f, 88f to 52f)
        }
        else -> circulo(l, ArtColors.verdePrado, 50f, 50f, 20f)
    }
}
