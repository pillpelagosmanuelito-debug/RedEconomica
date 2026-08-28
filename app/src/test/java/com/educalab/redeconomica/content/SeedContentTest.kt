package com.educalab.redeconomica.content

import com.educalab.redeconomica.data.seed.SeedContent
import com.educalab.redeconomica.data.seed.SeedResources
import com.educalab.redeconomica.domain.engine.EconomyEngine
import com.educalab.redeconomica.domain.engine.ScenarioValidator
import com.educalab.redeconomica.domain.model.ActivityKind
import com.educalab.redeconomica.domain.model.ScenarioPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Estas pruebas recorren TODO el contenido del Valle.
 *
 * Es la regla más importante del proyecto: nunca se le presenta a un niño una
 * situación imposible. El validador comprueba escenario por escenario que
 * existe al menos una solución.
 */
class SeedContentTest {

    private val motor = EconomyEngine(SeedResources.PORID)
    private val validador = ScenarioValidator(motor)

    @Test
    fun `el contenido no tiene referencias rotas`() {
        val problemas = SeedContent.problemasDeIntegridad()
        assertTrue(problemas.joinToString("\n"), problemas.isEmpty())
    }

    @Test
    fun `todos los escenarios pasan el validador`() {
        val fallos = validador.validarTodos(SeedContent.ESCENARIOS)
        assertTrue(
            fallos.joinToString("\n") { "${it.escenarioId}: ${it.problema}" },
            fallos.isEmpty()
        )
    }

    @Test
    fun `hay contenido suficiente para varias sesiones`() {
        assertTrue(SeedContent.ESCENARIOS.size >= 40)
        assertEquals(14, SeedContent.MISIONES.size)
        assertTrue(SeedContent.RECURSOS.size >= 20)
        assertTrue(SeedContent.HABITANTES.size >= 8)
        assertTrue(SeedContent.INSIGNIAS.size >= 8)
        assertTrue(SeedContent.COLECCION.size >= 20)
        assertTrue(SeedContent.DICCIONARIO.size >= 10)
    }

    @Test
    fun `las mecanicas estan repartidas y no todo es elegir una respuesta`() {
        val porTipo = SeedContent.ESCENARIOS.groupingBy { it.tipo }.eachCount()
        val evaluar = porTipo[ActivityKind.EVALUAR_OFERTA] ?: 0
        assertTrue(
            "Demasiados escenarios de opción simple: $evaluar de ${SeedContent.ESCENARIOS.size}",
            evaluar * 2 <= SeedContent.ESCENARIOS.size
        )
        assertTrue("Faltan mecánicas distintas", porTipo.keys.size >= 7)
    }

    @Test
    fun `la dificultad crece a lo largo de las misiones`() {
        val primeras = SeedContent.MISIONES.take(3)
            .flatMap { SeedContent.escenariosDe(it.id) }
            .map { it.dificultad }
        val ultimas = SeedContent.MISIONES.takeLast(3)
            .flatMap { SeedContent.escenariosDe(it.id) }
            .map { it.dificultad }
        assertTrue(primeras.average() < ultimas.average())
    }

    @Test
    fun `hay escenarios con mas de una solucion valida`() {
        val conVarias = SeedContent.ESCENARIOS.count { validador.numeroDeSoluciones(it) > 1 }
        assertTrue("Se esperaban escenarios con varias soluciones", conVarias >= 5)
    }

    @Test
    fun `cada escenario explica algo al terminar`() {
        SeedContent.ESCENARIOS.forEach {
            assertTrue(
                "El escenario ${it.id} no explica nada",
                it.explicacionFinal.length > 30
            )
        }
    }

    @Test
    fun `los textos son cortos y legibles para 8-12 anos`() {
        SeedContent.ESCENARIOS.forEach {
            assertTrue("Situación demasiado larga en ${it.id}", it.situacion.length <= 220)
            assertTrue("Instrucción demasiado larga en ${it.id}", it.instruccion.length <= 130)
        }
    }

    @Test
    fun `todas las misiones encadenan con la anterior salvo la primera`() {
        SeedContent.MISIONES.forEachIndexed { indice, mision ->
            if (indice == 0) {
                assertTrue(mision.requiereMision == null)
            } else {
                assertEquals(SeedContent.MISIONES[indice - 1].id, mision.requiereMision)
            }
        }
    }

    @Test
    fun `los escenarios de intercambio siempre tienen algun trato posible`() {
        SeedContent.ESCENARIOS
            .filter { it.payload is ScenarioPayload.Intercambio }
            .forEach { escenario ->
                val posibles = motor.intercambio.buscarIntercambios(
                    escenario.jugador, escenario.participantes
                )
                assertTrue("Sin salida en ${escenario.id}", posibles.isNotEmpty())
            }
    }

    @Test
    fun `los retos diarios son alcanzables y no los mas dificiles`() {
        assertTrue(SeedContent.RETOS_DIARIOS.isNotEmpty())
        assertTrue(SeedContent.RETOS_DIARIOS.all { it.dificultad <= 4 })
    }

    @Test
    fun `cada recurso del catalogo tiene nombre en singular y plural`() {
        SeedContent.RECURSOS.forEach {
            assertTrue(it.singular.isNotBlank())
            assertTrue(it.plural.isNotBlank())
            assertTrue(it.descripcion.length > 10)
        }
    }

    @Test
    fun `cada habitante sabe producir algo`() {
        SeedContent.HABITANTES.forEach {
            assertTrue("${it.nombre} no sabe hacer nada", it.oficiosPosibles().isNotEmpty())
        }
    }
}
