package com.educalab.redeconomica.domain

import com.educalab.redeconomica.data.seed.SeedResources
import com.educalab.redeconomica.domain.engine.TradeEngine
import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.Need
import com.educalab.redeconomica.domain.model.TradeEvaluation
import com.educalab.redeconomica.domain.model.TradeOffer
import com.educalab.redeconomica.domain.model.TradeRejectReason
import com.educalab.redeconomica.domain.model.Urgency
import com.educalab.redeconomica.domain.model.ValleyPlace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** El motor de intercambio decide si un vecino acepta. Aquí se comprueban sus reglas. */
class TradeEngineTest {

    private val motor = TradeEngine(SeedResources.PORID)

    private fun personaje(
        id: String,
        tiene: Inventory = Inventory.VACIO,
        necesita: List<Need> = emptyList(),
        produce: Map<String, Int> = mapOf("manzana" to 3)
    ) = EconomicCharacter(
        id = id, nombre = id.replaceFirstChar { it.uppercase() }, oficio = "vecino",
        lugar = ValleyPlace.PLAZA, presentacion = "-",
        productividad = produce, inventario = tiene, necesidades = necesita
    )

    private val jugador = personaje(
        "jugador",
        tiene = Inventory.of("manzana" to 5),
        necesita = listOf(Need("pan", 2, Urgency.ALTA))
    )
    private val tomas = personaje(
        "tomas",
        tiene = Inventory.of("pan" to 5),
        necesita = listOf(Need("manzana", 3, Urgency.ALTA))
    )

    private fun oferta(entrega: Inventory, pide: Inventory) =
        TradeOffer("jugador", "tomas", entrega, pide)

    @Test
    fun `un trato que resuelve a los dos se acepta`() {
        val res = motor.evaluar(
            oferta(Inventory.of("manzana" to 3), Inventory.of("pan" to 2)),
            jugador, tomas
        )
        assertTrue(res is TradeEvaluation.Aceptado)
        val aceptado = res as TradeEvaluation.Aceptado
        assertEquals(2, aceptado.proponenteActualizado.inventario.cantidad("manzana"))
        assertEquals(2, aceptado.proponenteActualizado.inventario.cantidad("pan"))
        assertEquals(3, aceptado.receptorActualizado.inventario.cantidad("pan"))
        assertEquals(3, aceptado.receptorActualizado.inventario.cantidad("manzana"))
    }

    @Test
    fun `una oferta vacia no se evalua como trato`() {
        val res = motor.evaluar(oferta(Inventory.VACIO, Inventory.of("pan" to 1)), jugador, tomas)
        assertEquals(
            TradeRejectReason.OFERTA_VACIA,
            (res as TradeEvaluation.Rechazado).motivo
        )
    }

    @Test
    fun `no se puede entregar lo que no se tiene`() {
        val res = motor.evaluar(
            oferta(Inventory.of("manzana" to 9), Inventory.of("pan" to 1)), jugador, tomas
        )
        assertEquals(
            TradeRejectReason.SIN_RECURSOS_PROPONENTE,
            (res as TradeEvaluation.Rechazado).motivo
        )
    }

    @Test
    fun `no se puede pedir lo que el vecino no tiene`() {
        val res = motor.evaluar(
            oferta(Inventory.of("manzana" to 3), Inventory.of("tela" to 1)), jugador, tomas
        )
        assertEquals(
            TradeRejectReason.SIN_RECURSOS_RECEPTOR,
            (res as TradeEvaluation.Rechazado).motivo
        )
    }

    @Test
    fun `si al vecino no le hace falta lo ofrecido dice que no`() {
        val dani = personaje(
            "dani",
            tiene = Inventory.of("pan" to 3),
            necesita = listOf(Need("tela", 2, Urgency.ALTA))
        )
        val res = motor.evaluar(
            TradeOffer("jugador", "dani", Inventory.of("manzana" to 3), Inventory.of("pan" to 2)),
            jugador, dani
        )
        assertEquals(
            TradeRejectReason.NO_NECESITA_LO_OFRECIDO,
            (res as TradeEvaluation.Rechazado).motivo
        )
    }

    @Test
    fun `nadie entrega lo que necesita con urgencia`() {
        val lia = personaje(
            "lia",
            tiene = Inventory.of("pan" to 2),
            necesita = listOf(Need("pan", 2, Urgency.ALTA), Need("manzana", 1, Urgency.MEDIA))
        )
        val res = motor.evaluar(
            TradeOffer("jugador", "lia", Inventory.of("manzana" to 3), Inventory.of("pan" to 2)),
            jugador, lia
        )
        assertEquals(
            TradeRejectReason.PERDERIA_LO_QUE_NECESITA,
            (res as TradeEvaluation.Rechazado).motivo
        )
    }

    @Test
    fun `un trato muy desigual se rechaza`() {
        // 1 manzana (valor 1) por 3 panes (valor 6): 100 < 360
        val res = motor.evaluar(
            oferta(Inventory.of("manzana" to 1), Inventory.of("pan" to 3)), jugador, tomas
        )
        assertEquals(
            TradeRejectReason.DESEQUILIBRIO,
            (res as TradeEvaluation.Rechazado).motivo
        )
    }

    @Test
    fun `sin beneficio propio el motor lo advierte`() {
        val sinNecesidad = jugador.copy(necesidades = emptyList())
        val res = motor.evaluar(
            oferta(Inventory.of("manzana" to 3), Inventory.of("pan" to 2)),
            sinNecesidad, tomas
        )
        assertEquals(
            TradeRejectReason.SIN_BENEFICIO_MUTUO,
            (res as TradeEvaluation.Rechazado).motivo
        )
    }

    @Test
    fun `el beneficio mutuo se puede relajar cuando el escenario lo pide`() {
        val sinNecesidad = jugador.copy(necesidades = emptyList())
        val res = motor.evaluar(
            oferta(Inventory.of("manzana" to 3), Inventory.of("pan" to 2)),
            sinNecesidad, tomas, exigirBeneficioMutuo = false
        )
        assertTrue(res is TradeEvaluation.Aceptado)
    }

    @Test
    fun `un intercambio no puede hacerse consigo mismo`() {
        var fallo = false
        try {
            motor.evaluar(
                TradeOffer("jugador", "jugador", Inventory.of("manzana" to 1), Inventory.of("pan" to 1)),
                jugador, jugador
            )
        } catch (e: IllegalArgumentException) {
            fallo = true
        }
        assertTrue(fallo)
    }

    @Test
    fun `buscar intercambios encuentra al menos una salida`() {
        val posibles = motor.buscarIntercambios(jugador, listOf(tomas))
        assertTrue(posibles.isNotEmpty())
        assertTrue(posibles.all { it.receptorId == "tomas" })
    }

    @Test
    fun `buscar intercambios no inventa tratos cuando no hay coincidencia`() {
        val ajeno = personaje(
            "ajeno",
            tiene = Inventory.of("tela" to 3),
            necesita = listOf(Need("madera", 2, Urgency.ALTA))
        )
        assertTrue(motor.buscarIntercambios(jugador, listOf(ajeno)).isEmpty())
    }

    @Test
    fun `un escenario puede admitir varias soluciones distintas`() {
        val generoso = personaje(
            "generoso",
            tiene = Inventory.of("pan" to 6),
            necesita = listOf(Need("manzana", 4, Urgency.MEDIA))
        )
        val soluciones = motor.buscarIntercambios(jugador, listOf(generoso))
        assertTrue("Se esperaban varias soluciones válidas", soluciones.size > 1)
    }

    @Test
    fun `dos intercambios seguidos actualizan bien los inventarios`() {
        val primero = motor.evaluar(
            oferta(Inventory.of("manzana" to 3), Inventory.of("pan" to 2)),
            jugador, tomas
        ) as TradeEvaluation.Aceptado
        val jugador2 = primero.proponenteActualizado
        val tomas2 = primero.receptorActualizado
        val segundo = motor.evaluar(
            TradeOffer("jugador", "tomas", Inventory.of("manzana" to 2), Inventory.of("pan" to 1)),
            jugador2.copy(necesidades = listOf(Need("pan", 4, Urgency.ALTA))),
            tomas2.copy(necesidades = listOf(Need("manzana", 6, Urgency.MEDIA)))
        )
        assertTrue(segundo is TradeEvaluation.Aceptado)
        val final = (segundo as TradeEvaluation.Aceptado).proponenteActualizado
        assertEquals(0, final.inventario.cantidad("manzana"))
        assertEquals(3, final.inventario.cantidad("pan"))
    }
}
