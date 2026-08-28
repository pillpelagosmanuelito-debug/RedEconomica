package com.educalab.redeconomica.data.seed

import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.Need
import com.educalab.redeconomica.domain.model.Urgency

/**
 * Atajos para escribir el contenido semilla sin repetir cien veces lo mismo.
 *
 * Cada escenario parte de los habitantes base y solo cambia lo que esa
 * situación necesita: qué tienen hoy y qué les hace falta hoy.
 */

internal fun inv(vararg pares: Pair<String, Int>): Inventory = Inventory.of(*pares)

internal fun nec(recurso: String, cantidad: Int, urgencia: Urgency = Urgency.MEDIA): Need =
    Need(recurso, cantidad, urgencia)

internal val ALTA = Urgency.ALTA
internal val MEDIA = Urgency.MEDIA
internal val BAJA = Urgency.BAJA

/** Un habitante del Valle ajustado a la situación de un escenario. */
internal fun hab(
    id: String,
    tiene: Inventory = Inventory.VACIO,
    necesita: List<Need> = emptyList(),
    produce: Map<String, Int>? = null
): EconomicCharacter {
    val base = SeedCharacters.base(id)
    return base.copy(
        inventario = tiene,
        necesidades = necesita,
        productividad = produce ?: base.productividad
    )
}

/** El jugador dentro de un escenario. */
internal fun yo(
    tiene: Inventory = Inventory.VACIO,
    necesita: List<Need> = emptyList(),
    produce: Map<String, Int> = mapOf("manzana" to 3, "verdura" to 3)
): EconomicCharacter = SeedCharacters.jugador(
    inventario = tiene,
    necesidades = necesita,
    productividad = produce
)
