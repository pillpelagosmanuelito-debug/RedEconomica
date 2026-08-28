package com.educalab.redeconomica.domain

import com.educalab.redeconomica.data.seed.SeedResources
import com.educalab.redeconomica.domain.model.Inventory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El inventario es la pieza que impide estados económicos imposibles.
 * Si esto falla, todo lo demás falla.
 */
class InventoryTest {

    private val catalogo = SeedResources.PORID

    @Test
    fun `un inventario vacio no tiene nada`() {
        assertTrue(Inventory.VACIO.esVacio)
        assertEquals(0, Inventory.VACIO.total)
        assertEquals(0, Inventory.VACIO.cantidad("manzana"))
    }

    @Test
    fun `las entradas en cero se descartan`() {
        val inv = Inventory.of("manzana" to 3, "pan" to 0)
        assertEquals(setOf("manzana"), inv.recursos)
        assertEquals(3, inv.total)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `no se puede crear un inventario con cantidades negativas`() {
        Inventory.of("manzana" to -1)
    }

    @Test
    fun `sumar acumula por recurso`() {
        val a = Inventory.of("manzana" to 3, "pan" to 1)
        val b = Inventory.of("manzana" to 2, "tela" to 4)
        val suma = a.mas(b)
        assertEquals(5, suma.cantidad("manzana"))
        assertEquals(1, suma.cantidad("pan"))
        assertEquals(4, suma.cantidad("tela"))
    }

    @Test
    fun `restar de mas devuelve null en vez de negativos`() {
        val a = Inventory.of("manzana" to 2)
        assertNull(a.menos(Inventory.of("manzana" to 3)))
        assertNull(a.menos(Inventory.of("pan" to 1)))
    }

    @Test
    fun `restar justo deja el inventario vacio`() {
        val a = Inventory.of("manzana" to 2)
        val resto = a.menos(Inventory.of("manzana" to 2))
        assertTrue(resto!!.esVacio)
    }

    @Test
    fun `contiene compara recurso a recurso`() {
        val a = Inventory.of("manzana" to 3, "pan" to 2)
        assertTrue(a.contiene(Inventory.of("manzana" to 3)))
        assertTrue(a.contiene(Inventory.of("manzana" to 1, "pan" to 2)))
        assertFalse(a.contiene(Inventory.of("manzana" to 4)))
        assertFalse(a.contiene(Inventory.of("tela" to 1)))
    }

    @Test
    fun `dos inventarios con el mismo contenido son iguales`() {
        assertEquals(
            Inventory.of("pan" to 2, "manzana" to 1),
            Inventory.of("manzana" to 1, "pan" to 2)
        )
    }

    @Test
    fun `el formato de persistencia va y vuelve sin perder nada`() {
        val original = Inventory.of("manzana" to 4, "pan" to 2, "tela" to 1)
        val texto = Inventory.aTexto(original)
        assertEquals(original, Inventory.desdeTexto(texto))
    }

    @Test
    fun `un texto vacio produce un inventario vacio`() {
        assertTrue(Inventory.desdeTexto("").esVacio)
    }

    @Test
    fun `el valor pedagogico usa el catalogo`() {
        // 3 manzanas (1 cada una) + 2 panes (2 cada uno) = 7
        val inv = Inventory.of("manzana" to 3, "pan" to 2)
        assertEquals(7, inv.valor(catalogo))
    }

    @Test
    fun `la descripcion usa singular y plural`() {
        val uno = Inventory.of("manzana" to 1)
        val varios = Inventory.of("manzana" to 3)
        assertEquals("1 manzana", uno.descripcion(catalogo))
        assertEquals("3 manzanas", varios.descripcion(catalogo))
        assertEquals("nada", Inventory.VACIO.descripcion(catalogo))
    }
}
