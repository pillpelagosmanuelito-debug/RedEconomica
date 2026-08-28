package com.educalab.redeconomica.domain

import com.educalab.redeconomica.domain.engine.ProductionChainEngine
import com.educalab.redeconomica.domain.model.ChainDef
import com.educalab.redeconomica.domain.model.ChainStage
import com.educalab.redeconomica.domain.model.ChainStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Ordenar una cadena de producción y explicar dónde se rompe. */
class ProductionChainEngineTest {

    private val motor = ProductionChainEngine()

    private val cadena = ChainDef(
        id = "pan", titulo = "El pan", introduccion = "-",
        pasos = listOf(
            ChainStep("c1", "Sembrar", "-", ChainStage.MATERIA_PRIMA, "bruno"),
            ChainStep("c2", "Moler", "-", ChainStage.TRANSFORMACION, "tomas"),
            ChainStep("c3", "Hornear", "-", ChainStage.TRANSFORMACION, "tomas"),
            ChainStep("c4", "Transportar", "-", ChainStage.TRANSPORTE, "rita")
        ),
        moraleja = "Cada uno hace una parte."
    )

    @Test
    fun `el orden correcto se acepta`() {
        val res = motor.evaluar(cadena, listOf("c1", "c2", "c3", "c4"))
        assertTrue(res.correcto)
        assertEquals(4, res.aciertosSeguidos)
        assertEquals(cadena.moraleja, res.explicacion)
    }

    @Test
    fun `el motor dice en que paso se rompe la cadena`() {
        val res = motor.evaluar(cadena, listOf("c1", "c3", "c2", "c4"))
        assertFalse(res.correcto)
        assertEquals(1, res.primerErrorEn)
        assertEquals(1, res.aciertosSeguidos)
        assertTrue(res.explicacion.contains("Moler"))
    }

    @Test
    fun `una cadena incompleta se detecta`() {
        val res = motor.evaluar(cadena, listOf("c1", "c2"))
        assertFalse(res.correcto)
        assertEquals(-1, res.primerErrorEn)
        assertTrue(res.mensaje.contains("Falta"))
    }

    @Test
    fun `un paso repetido tambien se detecta`() {
        val res = motor.evaluar(cadena, listOf("c1", "c1", "c2", "c3"))
        assertFalse(res.correcto)
    }

    @Test
    fun `barajar no entrega la cadena ya resuelta`() {
        repeat(20) { semilla ->
            val barajada = motor.barajar(cadena, semilla.toLong())
            assertEquals(cadena.pasos.size, barajada.size)
            assertNotEquals(cadena.ordenCorrecto, barajada.map { it.id })
        }
    }

    @Test
    fun `barajar es reproducible con la misma semilla`() {
        val a = motor.barajar(cadena, 42L).map { it.id }
        val b = motor.barajar(cadena, 42L).map { it.id }
        assertEquals(a, b)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `una cadena de dos pasos no es una cadena`() {
        ChainDef(
            "corta", "-", "-",
            listOf(
                ChainStep("x", "-", "-", ChainStage.MATERIA_PRIMA, null),
                ChainStep("y", "-", "-", ChainStage.INTERCAMBIO, null)
            ),
            "-"
        )
    }
}
