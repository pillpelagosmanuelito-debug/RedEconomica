package com.educalab.redeconomica.data.seed

import com.educalab.redeconomica.domain.model.Badge
import com.educalab.redeconomica.domain.model.CollectionItem
import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.GlossaryEntry
import com.educalab.redeconomica.domain.model.MissionDef
import com.educalab.redeconomica.domain.model.ResourceDef
import com.educalab.redeconomica.domain.model.Scenario

/**
 * Todo el contenido del Valle, en un solo sitio.
 *
 * Los escenarios se definen como datos (no dentro de los Composables) y se
 * validan con `ScenarioValidator` antes de llegar a la pantalla: las pruebas
 * unitarias recorren esta lista entera y comprueban que cada situación tiene
 * al menos una solución.
 */
object SeedContent {

    val RECURSOS: List<ResourceDef> = SeedResources.TODOS
    val CATALOGO: Map<String, ResourceDef> = SeedResources.PORID

    val HABITANTES: List<EconomicCharacter> = SeedCharacters.HABITANTES
    val GUIA: EconomicCharacter = SeedCharacters.TILO

    val ESCENARIOS: List<Scenario> =
        SeedScenariosA.LISTA + SeedScenariosB.LISTA + SeedScenariosC.LISTA

    val ESCENARIOS_PORID: Map<String, Scenario> = ESCENARIOS.associateBy { it.id }

    val MISIONES: List<MissionDef> = SeedMissions.TODAS

    val INSIGNIAS: List<Badge> = SeedProgression.INSIGNIAS
    val COLECCION: List<CollectionItem> = SeedProgression.COLECCION
    val DICCIONARIO: List<GlossaryEntry> = SeedProgression.DICCIONARIO

    /** Escenarios que pueden salir como «Reto económico del día». */
    val RETOS_DIARIOS: List<Scenario> = ESCENARIOS.filter { it.dificultad <= 4 }

    fun escenario(id: String): Scenario =
        ESCENARIOS_PORID[id] ?: error("Escenario desconocido: $id")

    fun escenariosDe(misionId: String): List<Scenario> {
        val mision = SeedMissions.PORID[misionId] ?: return emptyList()
        return mision.escenarios.map { escenario(it) }
    }

    /** Comprobación de integridad usada por las pruebas y por el sembrado. */
    fun problemasDeIntegridad(): List<String> {
        val fallos = mutableListOf<String>()
        val idsEscenarios = ESCENARIOS.map { it.id }
        if (idsEscenarios.size != idsEscenarios.toSet().size) {
            fallos += "Hay escenarios con el mismo id"
        }
        MISIONES.forEach { m ->
            m.escenarios.forEach { s ->
                if (s !in ESCENARIOS_PORID) fallos += "La misión ${m.id} apunta a un escenario inexistente: $s"
            }
            m.recompensa.objetos.forEach { o ->
                if (o !in SeedProgression.COLECCION_PORID) {
                    fallos += "La misión ${m.id} entrega un objeto inexistente: $o"
                }
            }
            val ins = m.recompensa.insigniaId
            if (ins != null && ins !in SeedProgression.INSIGNIAS_PORID) {
                fallos += "La misión ${m.id} entrega una insignia inexistente: $ins"
            }
            val req = m.requiereMision
            if (req != null && SeedMissions.PORID[req] == null) {
                fallos += "La misión ${m.id} requiere una misión inexistente: $req"
            }
        }
        val usados = MISIONES.flatMap { it.escenarios }.toSet()
        ESCENARIOS.forEach {
            if (it.id !in usados) fallos += "El escenario ${it.id} no lo usa ninguna misión"
        }
        COLECCION.forEach { item ->
            val r = item.recursoId
            if (r != null && r !in CATALOGO) fallos += "El objeto ${item.id} apunta a un recurso inexistente: $r"
        }
        return fallos
    }
}
