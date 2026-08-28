package com.educalab.redeconomica.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Ilustración de cada recurso del Valle, dibujada con Canvas.
 *
 * Nada de emojis como sustituto del dibujo: los emojis dependen del móvil y
 * cambian de estilo. Estas formas se ven igual en todos.
 */
@Composable
fun RecursoIcono(
    recursoId: String,
    modifier: Modifier = Modifier,
    descripcion: String? = null
) {
    Canvas(
        modifier = modifier.semantics {
            contentDescription = descripcion ?: nombreLegible(recursoId)
        }
    ) {
        dibujarRecurso(recursoId)
    }
}

fun nombreLegible(recursoId: String): String = when (recursoId) {
    "manzana" -> "Manzanas"
    "verdura" -> "Verduras"
    "trigo" -> "Trigo"
    "harina" -> "Harina"
    "pan" -> "Pan"
    "pescado" -> "Pescado"
    "leche" -> "Leche"
    "queso" -> "Queso"
    "miel" -> "Miel"
    "madera" -> "Madera"
    "tabla" -> "Tablas"
    "mesa" -> "Mesa"
    "silla" -> "Silla"
    "herramienta" -> "Herramienta"
    "clavo" -> "Clavos"
    "lana" -> "Lana"
    "tela" -> "Tela"
    "manta" -> "Manta"
    "semilla" -> "Semillas"
    "cesta" -> "Cesta"
    else -> recursoId
}

fun DrawScope.dibujarRecurso(recursoId: String) = lienzo { l ->
    when (recursoId) {
        "manzana" -> {
            circulo(l, ArtColors.rojoManzana, 44f, 58f, 26f)
            circulo(l, ArtColors.rojoManzana, 58f, 58f, 24f)
            trazo(l, ArtColors.marronOscuro, 5f, false, 52f to 34f, 55f to 18f)
            poligono(l, ArtColors.verdePrado, 55f to 22f, 76f to 12f, 70f to 30f)
            ovalo(l, Color(0x55FFFFFF), 38f, 48f, 7f, 10f)
        }
        "verdura" -> {
            poligono(l, ArtColors.naranjaTeja, 38f to 40f, 62f to 40f, 50f to 88f)
            trazo(l, ArtColors.naranjaClaro, 2.5f, false, 43f to 52f, 57f to 52f)
            trazo(l, ArtColors.naranjaClaro, 2.5f, false, 45f to 64f, 55f to 64f)
            poligono(l, ArtColors.verdeOscuro, 50f to 40f, 30f to 16f, 44f to 34f)
            poligono(l, ArtColors.verdePrado, 50f to 40f, 52f to 10f, 60f to 34f)
            poligono(l, ArtColors.verdeOscuro, 50f to 40f, 72f to 18f, 62f to 36f)
        }
        "trigo" -> {
            trazo(l, ArtColors.verdeOscuro, 4f, false, 50f to 92f, 50f to 30f)
            for (i in 0..3) {
                val y = 30f + i * 13f
                poligono(l, ArtColors.amarilloTrigo, 50f to y, 30f to (y + 6f), 48f to (y + 12f))
                poligono(l, ArtColors.amarilloTrigo, 50f to y, 70f to (y + 6f), 52f to (y + 12f))
            }
            circulo(l, ArtColors.amarilloTrigo, 50f, 24f, 8f)
        }
        "harina" -> {
            poligono(
                l, ArtColors.cremaOscuro,
                26f to 88f, 34f to 34f, 66f to 34f, 74f to 88f
            )
            caja(l, ArtColors.crema, 32f, 24f, 36f, 14f, 4f)
            trazo(l, ArtColors.marronMadera, 3f, false, 34f to 30f, 66f to 30f)
            circulo(l, ArtColors.crema, 44f, 58f, 6f)
            circulo(l, ArtColors.crema, 58f, 66f, 5f)
        }
        "pan" -> {
            ovalo(l, ArtColors.amarilloTrigo, 50f, 56f, 34f, 20f)
            ovalo(l, Color(0xFFCE9134), 50f, 62f, 34f, 14f)
            for (i in -1..1) {
                trazo(
                    l, ArtColors.naranjaTeja, 3.5f, false,
                    (46f + i * 15f) to 44f, (54f + i * 15f) to 62f
                )
            }
        }
        "pescado" -> {
            ovalo(l, ArtColors.azulRio, 46f, 52f, 30f, 18f)
            poligono(l, ArtColors.azulClaro, 76f to 52f, 94f to 36f, 94f to 68f)
            circulo(l, ArtColors.crema, 30f, 46f, 5f)
            circulo(l, ArtColors.negroSuave, 30f, 46f, 2.5f)
            poligono(l, ArtColors.azulClaro, 44f to 36f, 58f to 22f, 60f to 38f)
        }
        "leche" -> {
            caja(l, ArtColors.blancoLana, 32f, 34f, 36f, 54f, 6f)
            caja(l, ArtColors.azulClaro, 32f, 60f, 36f, 28f, 6f)
            caja(l, ArtColors.grisMetal, 38f, 22f, 24f, 14f, 4f)
            trazo(l, ArtColors.grisPiedra, 3f, false, 68f to 46f, 80f to 56f, 68f to 66f)
        }
        "queso" -> {
            poligono(l, ArtColors.amarilloTrigo, 18f to 72f, 82f to 72f, 82f to 44f)
            poligono(l, Color(0xFFF3D488), 18f to 72f, 82f to 44f, 30f to 44f)
            circulo(l, Color(0xFFCE9134), 56f, 62f, 6f)
            circulo(l, Color(0xFFCE9134), 70f, 56f, 4f)
        }
        "miel" -> {
            caja(l, Color(0xFFE9A63A), 30f, 38f, 40f, 50f, 8f)
            caja(l, ArtColors.marronMadera, 34f, 26f, 32f, 14f, 4f)
            caja(l, ArtColors.crema, 36f, 56f, 28f, 16f, 2f)
            poligono(l, Color(0xFFCE9134), 44f to 60f, 50f to 56f, 56f to 60f, 56f to 68f, 50f to 72f, 44f to 68f)
        }
        "madera" -> {
            ovalo(l, ArtColors.marronMadera, 30f, 50f, 14f, 26f)
            caja(l, ArtColors.marronMadera, 30f, 24f, 46f, 52f)
            ovalo(l, ArtColors.cremaOscuro, 76f, 50f, 14f, 26f)
            circulo(l, ArtColors.marronOscuro, 76f, 50f, 8f)
            circulo(l, ArtColors.cremaOscuro, 76f, 50f, 4f)
        }
        "tabla" -> {
            caja(l, ArtColors.cremaOscuro, 12f, 38f, 76f, 16f, 3f)
            caja(l, Color(0xFFDCC79B), 12f, 58f, 76f, 14f, 3f)
            trazo(l, ArtColors.marronMadera, 1.5f, false, 24f to 40f, 30f to 52f)
            trazo(l, ArtColors.marronMadera, 1.5f, false, 60f to 60f, 66f to 70f)
        }
        "mesa" -> {
            caja(l, ArtColors.marronMadera, 12f, 34f, 76f, 12f, 3f)
            caja(l, ArtColors.marronOscuro, 20f, 46f, 8f, 40f)
            caja(l, ArtColors.marronOscuro, 72f, 46f, 8f, 40f)
            caja(l, ArtColors.marronOscuro, 24f, 52f, 52f, 6f)
        }
        "silla" -> {
            caja(l, ArtColors.marronMadera, 30f, 20f, 10f, 46f, 3f)
            caja(l, ArtColors.marronMadera, 30f, 52f, 44f, 10f, 3f)
            caja(l, ArtColors.marronOscuro, 32f, 62f, 7f, 24f)
            caja(l, ArtColors.marronOscuro, 66f, 62f, 7f, 24f)
            caja(l, ArtColors.naranjaClaro, 32f, 28f, 8f, 6f)
        }
        "herramienta" -> {
            caja(l, ArtColors.marronMadera, 46f, 40f, 9f, 48f, 3f)
            caja(l, ArtColors.grisMetal, 24f, 24f, 52f, 18f, 4f)
            caja(l, ArtColors.grisPiedra, 24f, 34f, 52f, 8f, 2f)
        }
        "clavo" -> {
            for (i in 0..2) {
                val x = 30f + i * 20f
                caja(l, ArtColors.grisMetal, x - 2f, 34f, 5f, 44f)
                poligono(l, ArtColors.grisMetal, (x - 2.5f) to 78f, (x + 2.5f) to 78f, x to 88f)
                caja(l, ArtColors.grisPiedra, x - 8f, 28f, 16f, 7f, 2f)
            }
        }
        "lana" -> {
            circulo(l, ArtColors.blancoLana, 40f, 52f, 20f)
            circulo(l, ArtColors.blancoLana, 60f, 48f, 18f)
            circulo(l, ArtColors.blancoLana, 52f, 66f, 16f)
            trazo(l, ArtColors.cremaOscuro, 2f, false, 32f to 50f, 44f to 58f, 36f to 66f)
            trazo(l, ArtColors.cremaOscuro, 2f, false, 58f to 42f, 66f to 54f, 58f to 60f)
        }
        "tela" -> {
            poligono(
                l, ArtColors.moradoUva,
                16f to 34f, 84f to 26f, 84f to 62f, 16f to 70f
            )
            poligono(
                l, Color(0xFF9C79B8),
                16f to 70f, 84f to 62f, 84f to 76f, 16f to 84f
            )
            trazo(l, ArtColors.crema, 2f, false, 20f to 44f, 80f to 36f)
            trazo(l, ArtColors.crema, 2f, false, 20f to 56f, 80f to 48f)
        }
        "manta" -> {
            caja(l, ArtColors.rojoManzana, 14f, 28f, 72f, 48f, 6f)
            caja(l, ArtColors.crema, 14f, 40f, 72f, 8f)
            caja(l, ArtColors.amarilloTrigo, 14f, 56f, 72f, 8f)
            for (i in 0..5) {
                trazo(
                    l, ArtColors.rojoOscuro, 2f, false,
                    (18f + i * 13f) to 76f, (18f + i * 13f) to 86f
                )
            }
        }
        "semilla" -> {
            ovalo(l, ArtColors.marronMadera, 38f, 46f, 8f, 12f)
            ovalo(l, ArtColors.marronOscuro, 58f, 40f, 7f, 11f)
            ovalo(l, ArtColors.marronMadera, 50f, 66f, 9f, 13f)
            ovalo(l, ArtColors.cremaOscuro, 36f, 42f, 3f, 4f)
            ovalo(l, ArtColors.cremaOscuro, 48f, 62f, 3f, 4f)
        }
        "cesta" -> {
            poligono(
                l, ArtColors.marronMadera,
                20f to 46f, 80f to 46f, 72f to 84f, 28f to 84f
            )
            for (i in 0..3) {
                trazo(
                    l, ArtColors.cremaOscuro, 2f, false,
                    (24f + i * 2f) to (54f + i * 8f), (76f - i * 2f) to (54f + i * 8f)
                )
            }
            trazo(l, ArtColors.marronOscuro, 4f, false, 26f to 46f, 40f to 24f, 60f to 24f, 74f to 46f)
        }
        else -> {
            circulo(l, ArtColors.grisMetal, 50f, 50f, 28f)
            circulo(l, ArtColors.crema, 50f, 50f, 20f)
        }
    }
}
