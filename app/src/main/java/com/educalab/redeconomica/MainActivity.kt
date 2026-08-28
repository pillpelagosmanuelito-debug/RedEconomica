package com.educalab.redeconomica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.educalab.redeconomica.data.repository.ProfileRepository
import com.educalab.redeconomica.ui.navigation.NavegacionValle
import com.educalab.redeconomica.ui.theme.RedEconomicaTheme

/**
 * Única Activity de la app.
 *
 * Todo el interfaz es Compose y toda la navegación es Navigation Compose:
 * no hay fragments ni actividades sueltas.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val contenedor = (application as RedEconomicaApp).container
        setContent {
            RaizValle(contenedor)
        }
    }
}

@Composable
private fun RaizValle(contenedor: AppContainer) {
    val perfil by contenedor.perfil.perfilFlow
        .collectAsState(initial = ProfileRepository.PERFIL_POR_DEFECTO)

    RedEconomicaTheme(textoGrande = perfil.textoGrande) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavegacionValle(
                contenedor = contenedor,
                onboardingHecho = perfil.onboardingHecho
            )
        }
    }
}
