package com.educalab.redeconomica.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.educalab.redeconomica.AppContainer
import com.educalab.redeconomica.ui.screens.activity.ActivityViewModel
import com.educalab.redeconomica.ui.screens.activity.PantallaActividad
import com.educalab.redeconomica.ui.screens.glossary.PantallaDiccionario
import com.educalab.redeconomica.ui.screens.lab.LabViewModel
import com.educalab.redeconomica.ui.screens.lab.PantallaLaboratorio
import com.educalab.redeconomica.ui.screens.market.PantallaMercado
import com.educalab.redeconomica.ui.screens.mission.PantallaMision
import com.educalab.redeconomica.ui.screens.onboarding.PantallaOnboarding
import com.educalab.redeconomica.ui.screens.profile.PantallaAlmacen
import com.educalab.redeconomica.ui.screens.profile.PantallaInsignias
import com.educalab.redeconomica.ui.screens.profile.PantallaPerfil
import com.educalab.redeconomica.ui.screens.profile.ProfileViewModel
import com.educalab.redeconomica.ui.screens.review.PantallaRepaso
import com.educalab.redeconomica.ui.screens.valley.PantallaValle
import com.educalab.redeconomica.ui.screens.valley.ValleyViewModel

/** Rutas del Valle. */
object Rutas {
    const val ONBOARDING = "onboarding"
    const val VALLE = "valle"
    const val MISION = "mision"
    const val ACTIVIDAD = "actividad"
    const val MERCADO = "mercado"
    const val LABORATORIO = "laboratorio"
    const val DICCIONARIO = "diccionario"
    const val ALMACEN = "almacen"
    const val INSIGNIAS = "insignias"
    const val PERFIL = "perfil"
    const val REPASO = "repaso"

    fun mision(id: String) = "$MISION/$id"
    fun actividad(id: String) = "$ACTIVIDAD/$id"
}

private fun fabricaValle(c: AppContainer) = viewModelFactory {
    initializer { ValleyViewModel(c) }
}

private fun fabricaPerfil(c: AppContainer) = viewModelFactory {
    initializer { ProfileViewModel(c) }
}

private fun fabricaLab(c: AppContainer) = viewModelFactory {
    initializer { LabViewModel(c) }
}

private fun fabricaActividad(c: AppContainer, escenarioId: String): ViewModelProvider.Factory =
    viewModelFactory { initializer { ActivityViewModel(c, escenarioId) } }

@Composable
fun NavegacionValle(
    contenedor: AppContainer,
    onboardingHecho: Boolean,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = if (onboardingHecho) Rutas.VALLE else Rutas.ONBOARDING
    ) {

        composable(Rutas.ONBOARDING) {
            val vm: ProfileViewModel = viewModel(factory = fabricaPerfil(contenedor))
            PantallaOnboarding(
                viewModel = vm,
                alTerminar = {
                    navController.navigate(Rutas.VALLE) {
                        popUpTo(Rutas.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.VALLE) {
            val vm: ValleyViewModel = viewModel(factory = fabricaValle(contenedor))
            PantallaValle(
                viewModel = vm,
                alAbrirMision = { navController.navigate(Rutas.mision(it)) },
                alAbrirActividad = { navController.navigate(Rutas.actividad(it)) },
                alIrA = { navController.navigate(it) }
            )
        }

        composable(
            route = "${Rutas.MISION}/{misionId}",
            arguments = listOf(navArgument("misionId") { type = NavType.StringType })
        ) { entrada ->
            val misionId = entrada.arguments?.getString("misionId").orEmpty()
            PantallaMision(
                contenedor = contenedor,
                misionId = misionId,
                alAbrirActividad = { navController.navigate(Rutas.actividad(it)) },
                alVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Rutas.ACTIVIDAD}/{escenarioId}",
            arguments = listOf(navArgument("escenarioId") { type = NavType.StringType })
        ) { entrada ->
            val escenarioId = entrada.arguments?.getString("escenarioId").orEmpty()
            val vm: ActivityViewModel = viewModel(
                key = "actividad_$escenarioId",
                factory = fabricaActividad(contenedor, escenarioId)
            )
            PantallaActividad(
                viewModel = vm,
                alVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.MERCADO) {
            PantallaMercado(contenedor = contenedor, alVolver = { navController.popBackStack() })
        }

        composable(Rutas.LABORATORIO) {
            val vm: LabViewModel = viewModel(factory = fabricaLab(contenedor))
            PantallaLaboratorio(viewModel = vm, alVolver = { navController.popBackStack() })
        }

        composable(Rutas.DICCIONARIO) {
            val vm: ProfileViewModel = viewModel(factory = fabricaPerfil(contenedor))
            PantallaDiccionario(viewModel = vm, alVolver = { navController.popBackStack() })
        }

        composable(Rutas.ALMACEN) {
            val vm: ProfileViewModel = viewModel(factory = fabricaPerfil(contenedor))
            PantallaAlmacen(viewModel = vm, alVolver = { navController.popBackStack() })
        }

        composable(Rutas.INSIGNIAS) {
            val vm: ProfileViewModel = viewModel(factory = fabricaPerfil(contenedor))
            PantallaInsignias(viewModel = vm, alVolver = { navController.popBackStack() })
        }

        composable(Rutas.PERFIL) {
            val vm: ProfileViewModel = viewModel(factory = fabricaPerfil(contenedor))
            PantallaPerfil(
                viewModel = vm,
                alVolver = { navController.popBackStack() },
                alRepetirOnboarding = {
                    navController.navigate(Rutas.ONBOARDING) {
                        popUpTo(Rutas.VALLE) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.REPASO) {
            PantallaRepaso(
                contenedor = contenedor,
                alAbrirActividad = { navController.navigate(Rutas.actividad(it)) },
                alVolver = { navController.popBackStack() }
            )
        }
    }
}
