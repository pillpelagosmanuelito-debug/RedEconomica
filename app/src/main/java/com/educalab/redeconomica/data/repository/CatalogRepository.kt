package com.educalab.redeconomica.data.repository

import com.educalab.redeconomica.data.local.Mappers
import com.educalab.redeconomica.data.local.dao.CatalogDao
import com.educalab.redeconomica.data.seed.SeedContent
import com.educalab.redeconomica.domain.model.Badge
import com.educalab.redeconomica.domain.model.CollectionItem
import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.GlossaryEntry
import com.educalab.redeconomica.domain.model.MissionDef
import com.educalab.redeconomica.domain.model.ResourceDef
import com.educalab.redeconomica.domain.model.Scenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Acceso al catálogo del Valle.
 *
 * Las listas (recursos, habitantes, misiones, insignias, objetos, diccionario)
 * se leen de Room, que es donde el sembrado las dejó.
 *
 * Los ESCENARIOS son un caso especial y conviene decirlo claro: en la base de
 * datos está su ficha (id, misión, tipo, título, texto, dificultad) y de ahí
 * salen los listados y el progreso; las REGLAS de cada escenario —inventarios,
 * capacidades, etapas, opciones— viven en el módulo de contenido
 * `data.seed`, porque son estructuras con forma distinta según la mecánica y
 * meterlas en columnas obligaría a serializarlas a mano. Están fuera de la
 * interfaz, se validan con `ScenarioValidator` y las pruebas las recorren
 * enteras. Queda documentado en `docs/BASE_DE_DATOS.md`.
 */
class CatalogRepository(private val dao: CatalogDao) {

    suspend fun recursos(): List<ResourceDef> = dao.recursos().map { Mappers.aDominio(it) }

    suspend fun catalogo(): Map<String, ResourceDef> = recursos().associateBy { it.id }

    suspend fun habitantes(): List<EconomicCharacter> = dao.habitantes().map { Mappers.aDominio(it) }

    suspend fun habitante(id: String): EconomicCharacter? =
        dao.personaje(id)?.let { Mappers.aDominio(it) }

    suspend fun misiones(): List<MissionDef> = dao.misiones().map { fila ->
        Mappers.aDominio(
            fila,
            escenarios = dao.escenariosDe(fila.id).map { it.id },
            objetos = SeedContent.MISIONES.first { it.id == fila.id }.recompensa.objetos
        )
    }

    fun misionesFlow(): Flow<List<MissionDef>> = dao.misionesFlow().map { filas ->
        filas.map { fila ->
            Mappers.aDominio(
                fila,
                escenarios = dao.escenariosDe(fila.id).map { it.id },
                objetos = SeedContent.MISIONES.first { it.id == fila.id }.recompensa.objetos
            )
        }
    }

    suspend fun mision(id: String): MissionDef? {
        val fila = dao.mision(id) ?: return null
        return Mappers.aDominio(
            fila,
            escenarios = dao.escenariosDe(id).map { it.id },
            objetos = SeedContent.MISIONES.first { it.id == id }.recompensa.objetos
        )
    }

    /** Ficha del escenario (Room) + reglas del escenario (módulo de contenido). */
    fun escenario(id: String): Scenario = SeedContent.escenario(id)

    suspend fun escenariosDe(misionId: String): List<Scenario> =
        dao.escenariosDe(misionId).map { SeedContent.escenario(it.id) }

    suspend fun misionDeEscenario(escenarioId: String): String? =
        dao.escenario(escenarioId)?.misionId

    fun insigniasFlow(): Flow<List<Badge>> =
        dao.insigniasFlow().map { filas -> filas.map { Mappers.aDominio(it) } }

    fun objetosFlow(): Flow<List<CollectionItem>> =
        dao.objetosFlow().map { filas -> filas.map { Mappers.aDominio(it) } }

    fun diccionarioFlow(): Flow<List<GlossaryEntry>> =
        dao.diccionarioFlow().map { filas -> filas.map { Mappers.aDominio(it) } }

    suspend fun entradaDiccionario(id: String): GlossaryEntry? =
        dao.entradaDiccionario(id)?.let { Mappers.aDominio(it) }
}
