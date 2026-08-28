package com.educalab.redeconomica.domain

import com.educalab.redeconomica.data.seed.SeedMissions
import com.educalab.redeconomica.data.seed.SeedProgression
import com.educalab.redeconomica.domain.engine.ProgressEngine
import com.educalab.redeconomica.domain.model.ActionCounters
import com.educalab.redeconomica.domain.model.MissionProgress
import com.educalab.redeconomica.domain.model.ModuleState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Progreso, niveles e insignias: todo derivado de acciones reales. */
class ProgressEngineTest {

    private val motor = ProgressEngine()

    @Test
    fun `sin hacer nada no hay sellos ni nivel alto`() {
        val cero = ActionCounters()
        assertEquals(0, motor.sellos(cero))
        assertEquals(1, motor.nivel(0))
        assertEquals("Vecino nuevo", motor.tituloNivel(1))
    }

    @Test
    fun `cada accion suma sellos`() {
        val c = ActionCounters(
            intercambiosAceptados = 2,      // 4
            especializacionesProbadas = 1,  // 2
            cooperacionesCompletadas = 1,   // 3
            repartosResueltos = 1,          // 2
            decisionesTomadas = 1,          // 2
            cadenasOrdenadas = 1,           // 2
            experimentosRealizados = 3,     // 3
            misionesCompletadas = 2,        // 10
            conceptosDescubiertos = 1       // 3
        )
        assertEquals(31, motor.sellos(c))
    }

    @Test
    fun `el nivel sube por tramos`() {
        assertEquals(1, motor.nivel(9))
        assertEquals(2, motor.nivel(10))
        assertEquals(3, motor.nivel(25))
        assertEquals(7, motor.nivel(500))
    }

    @Test
    fun `el motor dice cuantos sellos faltan para el siguiente nivel`() {
        assertEquals(4, motor.sellosParaSiguienteNivel(6))
        assertEquals(0, motor.sellosParaSiguienteNivel(300))
    }

    @Test
    fun `una mision con requisito sin cumplir esta bloqueada`() {
        val mision = SeedMissions.PORID["m03"]!!
        val estado = motor.estadoDeMision(mision, null, completadas = emptySet())
        assertEquals(ModuleState.BLOQUEADO, estado)
    }

    @Test
    fun `la primera mision esta disponible desde el principio`() {
        val mision = SeedMissions.PORID["m01"]!!
        assertEquals(
            ModuleState.DISPONIBLE,
            motor.estadoDeMision(mision, null, completadas = emptySet())
        )
    }

    @Test
    fun `una mision a medias aparece como empezada`() {
        val mision = SeedMissions.PORID["m01"]!!
        val progreso = MissionProgress(
            misionId = "m01",
            estado = ModuleState.INICIADO,
            escenariosCompletados = setOf(mision.escenarios.first()),
            intentosTotales = 2,
            sinFallos = false
        )
        assertEquals(
            ModuleState.INICIADO,
            motor.estadoDeMision(mision, progreso, completadas = emptySet())
        )
    }

    @Test
    fun `terminar sin fallos deja la mision dominada`() {
        val mision = SeedMissions.PORID["m01"]!!
        val progreso = MissionProgress(
            misionId = "m01",
            estado = ModuleState.COMPLETADO,
            escenariosCompletados = mision.escenarios.toSet(),
            intentosTotales = 3,
            sinFallos = true
        )
        assertEquals(
            ModuleState.DOMINADO,
            motor.estadoDeMision(mision, progreso, completadas = emptySet())
        )
    }

    @Test
    fun `terminar con algun fallo la deja completada, no dominada`() {
        val mision = SeedMissions.PORID["m01"]!!
        val progreso = MissionProgress(
            misionId = "m01",
            estado = ModuleState.COMPLETADO,
            escenariosCompletados = mision.escenarios.toSet(),
            intentosTotales = 6,
            sinFallos = false
        )
        assertEquals(
            ModuleState.COMPLETADO,
            motor.estadoDeMision(mision, progreso, completadas = emptySet())
        )
    }

    @Test
    fun `la primera insignia llega con el primer intercambio`() {
        val ganadas = motor.insigniasGanadas(
            SeedProgression.INSIGNIAS,
            ActionCounters(intercambiosAceptados = 1)
        )
        assertTrue(ganadas.any { it.id == "primer_trato" })
        assertFalse(ganadas.any { it.id == "gran_negociador" })
    }

    @Test
    fun `el avance de una insignia se ve antes de conseguirla`() {
        val insignia = SeedProgression.INSIGNIAS_PORID["gran_negociador"]!!
        val avance = motor.avanceDe(insignia, ActionCounters(intercambiosAceptados = 4))
        assertEquals(4, avance)
    }

    @Test
    fun `el avance nunca pasa de la meta`() {
        val insignia = SeedProgression.INSIGNIAS_PORID["primer_trato"]!!
        assertEquals(1, motor.avanceDe(insignia, ActionCounters(intercambiosAceptados = 30)))
    }

    @Test
    fun `el resumen no habla de inteligencia sino de lo que se ha hecho`() {
        val resumen = motor.resumen(
            ActionCounters(intercambiosAceptados = 3, misionesCompletadas = 1),
            misionesTotales = 14,
            insigniasConseguidas = 1,
            insigniasTotales = 11
        )
        assertEquals(3, resumen.intercambiosAceptados)
        assertEquals(1, resumen.misionesCompletadas)
        assertEquals(14, resumen.misionesTotales)
        assertTrue(resumen.sellos > 0)
    }
}
