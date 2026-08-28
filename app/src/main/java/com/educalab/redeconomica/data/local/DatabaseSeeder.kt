package com.educalab.redeconomica.data.local

import com.educalab.redeconomica.data.local.entity.ProfileEntity
import com.educalab.redeconomica.data.seed.SeedCharacters
import com.educalab.redeconomica.data.seed.SeedContent
import com.educalab.redeconomica.data.seed.SeedMissions

/**
 * Siembra el Valle en la base de datos la primera vez que se abre la app.
 *
 * Es idempotente: si el catálogo ya está, no vuelve a escribir nada, así que
 * arrancar la app cien veces no duplica ni un recurso.
 */
class DatabaseSeeder(private val db: AppDatabase) {

    suspend fun sembrarSiHaceFalta(): Boolean {
        val dao = db.catalogDao()
        if (dao.contarRecursos() > 0 && dao.contarEscenarios() > 0) return false
        sembrar()
        return true
    }

    suspend fun sembrar() {
        val dao = db.catalogDao()

        dao.insertarRecursos(SeedContent.RECURSOS.map { Mappers.aEntidad(it) })

        dao.insertarPersonajes(
            SeedContent.HABITANTES.map { Mappers.aEntidad(it, esGuia = false) } +
                Mappers.aEntidad(SeedCharacters.TILO, esGuia = true)
        )

        dao.insertarMisiones(SeedMissions.TODAS.map { Mappers.aEntidad(it) })

        val filasEscenario = SeedMissions.TODAS.flatMap { mision ->
            mision.escenarios.mapIndexed { indice, escenarioId ->
                Mappers.aEntidad(SeedContent.escenario(escenarioId), mision.id, indice)
            }
        }
        dao.insertarEscenarios(filasEscenario)

        dao.insertarInsignias(SeedContent.INSIGNIAS.map { Mappers.aEntidad(it) })
        dao.insertarObjetos(SeedContent.COLECCION.map { Mappers.aEntidad(it) })
        dao.insertarDiccionario(SeedContent.DICCIONARIO.map { Mappers.aEntidad(it) })

        if (db.profileDao().perfil() == null) {
            db.profileDao().guardar(
                ProfileEntity(
                    id = 1,
                    alias = "Vecino",
                    avatarId = "avatar_1",
                    onboardingHecho = false,
                    sonidoActivo = true,
                    vibracionActiva = true,
                    textoGrande = false,
                    creadoMillis = System.currentTimeMillis()
                )
            )
        }
    }

    /** Borra todo el progreso pero deja el catálogo. Se usa desde Ajustes. */
    suspend fun reiniciarProgreso() {
        db.progressDao().borrarProgresoMisiones()
        db.progressDao().borrarIntentos()
        db.progressDao().borrarInsignias()
        db.progressDao().borrarColeccion()
        db.progressDao().borrarConceptos()
        db.activityDao().borrarIntercambios()
        db.activityDao().borrarEspecializaciones()
        db.activityDao().borrarCooperaciones()
        db.activityDao().borrarRepartos()
        db.activityDao().borrarDecisiones()
        db.activityDao().borrarCadenas()
        db.labDao().borrarExperimentos()
        db.profileDao().vaciarAlmacen()
    }
}
