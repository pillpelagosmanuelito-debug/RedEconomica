package com.educalab.redeconomica.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Utilidades de dibujo del Valle.
 *
 * Todas las ilustraciones de RedEconómica se dibujan aquí con Canvas: no hay
 * ni una sola imagen descargada de Internet, así que la app se ve igual de
 * bien en un avión que en casa.
 *
 * Cada ilustración se define sobre un lienzo cuadrado imaginario de 100x100 y
 * [Lienzo] la escala al tamaño real que le toque.
 */
object ArtColors {
    val verdePrado = Color(0xFF4CA05C)
    val verdeOscuro = Color(0xFF2A6136)
    val verdeClaro = Color(0xFF8CCB97)
    val crema = Color(0xFFFDF6E7)
    val cremaOscuro = Color(0xFFEFE0BF)
    val naranjaTeja = Color(0xFFE8823A)
    val naranjaClaro = Color(0xFFF6B26B)
    val marronMadera = Color(0xFF8A5A32)
    val marronOscuro = Color(0xFF5E3A1E)
    val amarilloTrigo = Color(0xFFE2B24F)
    val rojoManzana = Color(0xFFD64F42)
    val rojoOscuro = Color(0xFFA83A30)
    val azulRio = Color(0xFF3E7CA6)
    val azulClaro = Color(0xFF9CC9E3)
    val moradoUva = Color(0xFF7E5A9B)
    val grisPiedra = Color(0xFF8A8078)
    val grisMetal = Color(0xFFB9BEC4)
    val blancoLana = Color(0xFFF3F0E6)
    val negroSuave = Color(0xFF3B342C)
    val pielClara = Color(0xFFF2C9A0)
    val pielMedia = Color(0xFFD9A273)
    val pielOscura = Color(0xFF9C6B45)
}

/** Sistema de coordenadas 0..100 centrado en el área de dibujo. */
class Lienzo(private val lado: Float, private val dx: Float, private val dy: Float) {
    fun x(v: Float): Float = dx + v / 100f * lado
    fun y(v: Float): Float = dy + v / 100f * lado
    fun d(v: Float): Float = v / 100f * lado
    fun p(px: Float, py: Float): Offset = Offset(x(px), y(py))
    fun tam(w: Float, h: Float): Size = Size(d(w), d(h))
    fun rect(l: Float, t: Float, r: Float, b: Float): Rect =
        Rect(x(l), y(t), x(r), y(b))
}

/** Ejecuta un bloque de dibujo en coordenadas 0..100. */
inline fun DrawScope.lienzo(bloque: DrawScope.(Lienzo) -> Unit) {
    val lado = minOf(size.width, size.height)
    val dx = (size.width - lado) / 2f
    val dy = (size.height - lado) / 2f
    bloque(Lienzo(lado, dx, dy))
}

/** Polígono cerrado a partir de pares (x, y) en coordenadas 0..100. */
fun DrawScope.poligono(l: Lienzo, color: Color, vararg puntos: Pair<Float, Float>) {
    if (puntos.size < 3) return
    val camino = Path().apply {
        moveTo(l.x(puntos[0].first), l.y(puntos[0].second))
        for (i in 1 until puntos.size) lineTo(l.x(puntos[i].first), l.y(puntos[i].second))
        close()
    }
    drawPath(camino, color)
}

/** Trazo continuo (no relleno). */
fun DrawScope.trazo(
    l: Lienzo,
    color: Color,
    grosor: Float,
    cerrado: Boolean = false,
    vararg puntos: Pair<Float, Float>
) {
    if (puntos.size < 2) return
    val camino = Path().apply {
        moveTo(l.x(puntos[0].first), l.y(puntos[0].second))
        for (i in 1 until puntos.size) lineTo(l.x(puntos[i].first), l.y(puntos[i].second))
        if (cerrado) close()
    }
    drawPath(camino, color, style = Stroke(width = l.d(grosor)))
}

fun DrawScope.caja(
    l: Lienzo,
    color: Color,
    izq: Float,
    arriba: Float,
    ancho: Float,
    alto: Float,
    radio: Float = 0f
) {
    if (radio > 0f) {
        drawRoundRect(
            color = color,
            topLeft = l.p(izq, arriba),
            size = l.tam(ancho, alto),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(l.d(radio), l.d(radio))
        )
    } else {
        drawRect(color = color, topLeft = l.p(izq, arriba), size = l.tam(ancho, alto))
    }
}

fun DrawScope.circulo(l: Lienzo, color: Color, cx: Float, cy: Float, radio: Float) {
    drawCircle(color, radius = l.d(radio), center = l.p(cx, cy))
}

fun DrawScope.ovalo(l: Lienzo, color: Color, cx: Float, cy: Float, rx: Float, ry: Float) {
    drawOval(color, topLeft = l.p(cx - rx, cy - ry), size = l.tam(rx * 2, ry * 2))
}

fun DrawScope.linea(
    l: Lienzo,
    color: Color,
    x1: Float, y1: Float, x2: Float, y2: Float,
    grosor: Float = 2f
) {
    drawLine(color, l.p(x1, y1), l.p(x2, y2), strokeWidth = l.d(grosor))
}
