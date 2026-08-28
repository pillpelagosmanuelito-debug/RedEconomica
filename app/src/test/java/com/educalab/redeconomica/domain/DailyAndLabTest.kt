package com.educalab.redeconomica.domain

import com.educalab.redeconomica.data.seed.SeedCharacters
import com.educalab.redeconomica.data.seed.SeedContent
import com.educalab.redeconomica.data.seed.SeedResources
import com.educalab.redeconomica.domain.engine.DailyChallengeGenerator
import com.educalab.redeconomica.domain.engine.LabConfig
import com.educalab.redeconomica.domain.engine.LabEngine
import com.educalab.redeconomica.domain.engine.LabMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reto del día y Laboratorio del Valle. */
class DailyAndLabTest {

    private val generador = DailyChallengeGenerator()
    private val laboratorio = LabEngine(SeedResources.PORID)

    @Test
    fun `el reto del dia es el mismo si se pregunta dos veces`() {
        val a = generador.retoDe(20000L, SeedContent.RETOS_DIARIOS)
        val b = generador.retoDe(20000L, SeedContent.RETOS_DIARIOS)
        assertNotNull(a)
        assertEquals(a!!.escenarioId, b!!.escenarioId)
    }

    @Test
    fun `dias distintos proponen retos distintos a lo largo de una semana`() {
        val ids = (0L..6L).map { generador.retoDe(20000L + it, SeedContent.RETOS_DIARIOS)!!.escenarioId }
        assertTrue("La semana entera propone el mismo reto", ids.toSet().size > 1)
    }

    @Test
    fun `sin escenarios no hay reto y no revienta`() {
        assertNull(generador.retoDe(1L, emptyList()))
    }

    @Test
    fun `el indice de dia avanza una vez al dia`() {
        val unDia = 86_400_000L
        assertEquals(0L, generador.indiceDeDia(0L))
        assertEquals(1L, generador.indiceDeDia(unDia))
        assertEquals(1L, generador.indiceDeDia(unDia + 3_600_000L))
    }

    @Test
    fun `especializarse produce mas que hacer de todo`() {
        val base = SeedCharacters.HABITANTES
        val deTodo = laboratorio.ejecutar(
            LabConfig(habitantes = 4, turnos = 2, modo = LabMode.TODOS_DE_TODO), base
        )
        val cadaUno = laboratorio.ejecutar(
            LabConfig(habitantes = 4, turnos = 2, modo = LabMode.CADA_UNO_LO_SUYO), base
        )
        assertTrue(
            "Especializar debería producir más valor",
            cadaUno.valorTotal > deTodo.valorTotal
        )
    }

    @Test
    fun `mas turnos producen mas`() {
        val base = SeedCharacters.HABITANTES
        val uno = laboratorio.ejecutar(LabConfig(habitantes = 3, turnos = 1), base)
        val tres = laboratorio.ejecutar(LabConfig(habitantes = 3, turnos = 3), base)
        assertTrue(tres.produccionTotal.total > uno.produccionTotal.total)
    }

    @Test
    fun `poner los recursos en comun no empeora las necesidades cubiertas`() {
        val base = SeedCharacters.HABITANTES
        val separados = laboratorio.ejecutar(
            LabConfig(habitantes = 4, turnos = 3, modo = LabMode.CADA_UNO_LO_SUYO), base
        )
        val enComun = laboratorio.ejecutar(
            LabConfig(
                habitantes = 4, turnos = 3,
                modo = LabMode.CADA_UNO_LO_SUYO, ponenEnComun = true
            ),
            base
        )
        assertTrue(enComun.necesidadesCubiertas >= separados.necesidadesCubiertas)
    }

    @Test
    fun `el experimento deja constancia de lo que ocurrio`() {
        val run = laboratorio.ejecutar(LabConfig(habitantes = 3, turnos = 2), SeedCharacters.HABITANTES)
        assertTrue(run.detalle.isNotEmpty())
        assertTrue(run.resumen.contains("habitantes"))
        assertEquals(3, run.config.habitantes)
    }

    @Test
    fun `comparar dos experimentos da una conclusion`() {
        val base = SeedCharacters.HABITANTES
        val a = laboratorio.ejecutar(LabConfig(habitantes = 3, turnos = 1), base)
        val b = laboratorio.ejecutar(LabConfig(habitantes = 3, turnos = 3), base)
        val comparacion = laboratorio.comparar(a, b)
        assertTrue(comparacion.diferenciaValor > 0)
        assertTrue(comparacion.conclusion.isNotBlank())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `el laboratorio no admite un solo habitante`() {
        LabConfig(habitantes = 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `el laboratorio no admite cero turnos`() {
        LabConfig(turnos = 0)
    }

    @Test
    fun `permitir intercambios no rompe los inventarios`() {
        val run = laboratorio.ejecutar(
            LabConfig(habitantes = 5, turnos = 3, permiteIntercambio = true),
            SeedCharacters.HABITANTES
        )
        assertTrue(run.intercambiosRealizados >= 0)
        assertTrue(run.produccionTotal.total > 0)
    }
}
