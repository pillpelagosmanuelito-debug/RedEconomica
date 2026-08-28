package com.educalab.redeconomica

import android.app.Application
import android.content.Context
import com.educalab.redeconomica.data.local.AppDatabase
import com.educalab.redeconomica.data.local.DatabaseSeeder
import com.educalab.redeconomica.data.repository.CatalogRepository
import com.educalab.redeconomica.data.repository.LabRepository
import com.educalab.redeconomica.data.repository.ProfileRepository
import com.educalab.redeconomica.data.repository.ProgressRepository
import com.educalab.redeconomica.data.seed.SeedResources
import com.educalab.redeconomica.domain.engine.EconomyEngine
import com.educalab.redeconomica.domain.engine.LabEngine
import com.educalab.redeconomica.domain.engine.ScenarioValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Contenedor de dependencias hecho a mano.
 *
 * RedEconómica no usa Hilt ni Dagger: la app es pequeña, el grafo es plano y
 * un contenedor explícito se lee mejor y compila más rápido.
 */
class AppContainer(context: Context) {

    val baseDeDatos: AppDatabase = AppDatabase.obtener(context)
    val sembrador: DatabaseSeeder = DatabaseSeeder(baseDeDatos)

    val catalogo: CatalogRepository = CatalogRepository(baseDeDatos.catalogDao())
    val perfil: ProfileRepository = ProfileRepository(baseDeDatos.profileDao())
    val laboratorio: LabRepository = LabRepository(baseDeDatos.labDao())
    val progreso: ProgressRepository = ProgressRepository(
        progressDao = baseDeDatos.progressDao(),
        activityDao = baseDeDatos.activityDao(),
        labDao = baseDeDatos.labDao(),
        profileDao = baseDeDatos.profileDao(),
        catalogo = catalogo
    )

    val motor: EconomyEngine = EconomyEngine(SeedResources.PORID)
    val motorLaboratorio: LabEngine = LabEngine(SeedResources.PORID)
    val validador: ScenarioValidator = ScenarioValidator(motor)
}

class RedEconomicaApp : Application() {

    lateinit var container: AppContainer
        private set

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        alcance.launch {
            container.sembrador.sembrarSiHaceFalta()
            container.perfil.asegurarPerfil()
        }
    }
}
