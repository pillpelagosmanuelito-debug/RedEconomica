package com.educalab.redeconomica.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Identidad visual de RedEconómica: verdes de valle, cremas de trigo y
 * naranjas de teja. Nada de gris corporativo ni azul de banca.
 */
object ValleColors {
    val Verde = Color(0xFF3F8F4F)
    val VerdeOscuro = Color(0xFF2A6136)
    val VerdeSuave = Color(0xFFD9EDD9)
    val Crema = Color(0xFFFDF6E7)
    val CremaOscuro = Color(0xFFF1E5C8)
    val Naranja = Color(0xFFE8823A)
    val NaranjaSuave = Color(0xFFFCE3CD)
    val Marron = Color(0xFF7A4B2A)
    val Trigo = Color(0xFFE2B24F)
    val Rojo = Color(0xFFD64F42)
    val Azul = Color(0xFF3E7CA6)
    val AzulSuave = Color(0xFFD6E9F5)
    val Morado = Color(0xFF7E5A9B)
    val Tinta = Color(0xFF33291F)
    val TintaSuave = Color(0xFF6B5B49)

    val NocheFondo = Color(0xFF1D2620)
    val NocheSuperficie = Color(0xFF27332B)
    val NocheTinta = Color(0xFFF0EADC)
}

private val EsquemaClaro = lightColorScheme(
    primary = ValleColors.Verde,
    onPrimary = Color.White,
    primaryContainer = ValleColors.VerdeSuave,
    onPrimaryContainer = ValleColors.VerdeOscuro,
    secondary = ValleColors.Naranja,
    onSecondary = Color.White,
    secondaryContainer = ValleColors.NaranjaSuave,
    onSecondaryContainer = ValleColors.Marron,
    tertiary = ValleColors.Azul,
    onTertiary = Color.White,
    tertiaryContainer = ValleColors.AzulSuave,
    onTertiaryContainer = Color(0xFF16374F),
    background = ValleColors.Crema,
    onBackground = ValleColors.Tinta,
    surface = Color.White,
    onSurface = ValleColors.Tinta,
    surfaceVariant = ValleColors.CremaOscuro,
    onSurfaceVariant = ValleColors.TintaSuave,
    error = ValleColors.Rojo,
    onError = Color.White,
    outline = Color(0xFFBCAE93)
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF7CC08A),
    onPrimary = Color(0xFF10281A),
    primaryContainer = ValleColors.VerdeOscuro,
    onPrimaryContainer = Color(0xFFCDEBD3),
    secondary = Color(0xFFF0A46B),
    onSecondary = Color(0xFF3A2110),
    secondaryContainer = Color(0xFF5C3418),
    onSecondaryContainer = Color(0xFFFCE3CD),
    tertiary = Color(0xFF8FC2DE),
    onTertiary = Color(0xFF12303F),
    background = ValleColors.NocheFondo,
    onBackground = ValleColors.NocheTinta,
    surface = ValleColors.NocheSuperficie,
    onSurface = ValleColors.NocheTinta,
    surfaceVariant = Color(0xFF3A473D),
    onSurfaceVariant = Color(0xFFD5CDBD),
    error = Color(0xFFF08A7E),
    onError = Color(0xFF3D110C),
    outline = Color(0xFF7C8A7F)
)

private fun tipografia(escala: Float) = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = (30 * escala).sp,
        lineHeight = (36 * escala).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = (24 * escala).sp,
        lineHeight = (30 * escala).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = (20 * escala).sp,
        lineHeight = (26 * escala).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = (18 * escala).sp,
        lineHeight = (24 * escala).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = (16 * escala).sp,
        lineHeight = (22 * escala).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = (16 * escala).sp,
        lineHeight = (23 * escala).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = (14 * escala).sp,
        lineHeight = (20 * escala).sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = (15 * escala).sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = (13 * escala).sp
    )
)

private val FormasValle = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun RedEconomicaTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    textoGrande: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
        typography = tipografia(if (textoGrande) 1.18f else 1f),
        shapes = FormasValle,
        content = content
    )
}
