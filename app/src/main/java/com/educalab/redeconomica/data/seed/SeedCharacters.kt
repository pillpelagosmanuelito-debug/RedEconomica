package com.educalab.redeconomica.data.seed

import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.Need
import com.educalab.redeconomica.domain.model.TradeOffer
import com.educalab.redeconomica.domain.model.Urgency
import com.educalab.redeconomica.domain.model.ValleyPlace

/**
 * Habitantes del Valle Económico.
 *
 * `productividad` = unidades que consigue en UN turno si dedica el turno
 * entero a ese producto. Estos números son la base de la especialización, de
 * la ventaja comparativa y del costo de oportunidad: no son decoración.
 *
 * Los inventarios y las necesidades que aparecen aquí son los "de partida";
 * cada escenario los ajusta a su situación.
 */
object SeedCharacters {

    val LIA = EconomicCharacter(
        id = "lia",
        nombre = "Lía",
        oficio = "Fruticultora",
        lugar = ValleyPlace.GRANJA,
        presentacion = "Sube al frutal antes que salga el sol. Recoge manzanas más rápido que nadie, pero el horno se le da fatal.",
        productividad = mapOf("manzana" to 6, "verdura" to 3, "miel" to 2, "semilla" to 2),
        inventario = Inventory.of("manzana" to 6),
        necesidades = listOf(Need("pan", 2, Urgency.ALTA), Need("herramienta", 1, Urgency.MEDIA))
    )

    val TOMAS = EconomicCharacter(
        id = "tomas",
        nombre = "Tomás",
        oficio = "Panadero",
        lugar = ValleyPlace.PANADERIA,
        presentacion = "Enciende el horno de madrugada. Convierte harina en pan como nadie, aunque nunca ha sabido plantar nada.",
        productividad = mapOf("pan" to 5, "harina" to 4, "trigo" to 2),
        inventario = Inventory.of("pan" to 5),
        necesidades = listOf(Need("manzana", 3, Urgency.ALTA), Need("madera", 2, Urgency.MEDIA))
    )

    val NINA = EconomicCharacter(
        id = "nina",
        nombre = "Nina",
        oficio = "Carpintera",
        lugar = ValleyPlace.CARPINTERIA,
        presentacion = "Su taller huele a serrín. Con un tronco y tiempo saca tablas, sillas y hasta mesas.",
        productividad = mapOf("madera" to 5, "tabla" to 4, "silla" to 3, "mesa" to 2),
        inventario = Inventory.of("madera" to 5, "tabla" to 2),
        necesidades = listOf(Need("pan", 2, Urgency.ALTA), Need("tela", 2, Urgency.BAJA))
    )

    val BRUNO = EconomicCharacter(
        id = "bruno",
        nombre = "Bruno",
        oficio = "Hortelano",
        lugar = ValleyPlace.HUERTO,
        presentacion = "Habla con sus tomates. Del huerto salen verduras todo el año, pero sus herramientas están hechas polvo.",
        productividad = mapOf("verdura" to 6, "semilla" to 4, "manzana" to 2, "trigo" to 3),
        inventario = Inventory.of("verdura" to 6, "semilla" to 3),
        necesidades = listOf(Need("herramienta", 2, Urgency.ALTA), Need("pan", 1, Urgency.MEDIA))
    )

    val SOFIA = EconomicCharacter(
        id = "sofia",
        nombre = "Sofía",
        oficio = "Herrera",
        lugar = ValleyPlace.TALLER,
        presentacion = "Golpea el metal con una calma que asusta. Hace herramientas que duran una vida.",
        productividad = mapOf("herramienta" to 4, "clavo" to 6, "tabla" to 2),
        inventario = Inventory.of("herramienta" to 3, "clavo" to 4),
        necesidades = listOf(Need("verdura", 3, Urgency.ALTA), Need("madera", 2, Urgency.MEDIA))
    )

    val EMI = EconomicCharacter(
        id = "emi",
        nombre = "Emi",
        oficio = "Pescadora",
        lugar = ValleyPlace.PESQUERIA,
        presentacion = "Conoce cada recodo del río. Vuelve con la cesta llena y con historias que nadie se cree.",
        productividad = mapOf("pescado" to 5, "cesta" to 3, "madera" to 2),
        inventario = Inventory.of("pescado" to 5, "cesta" to 2),
        necesidades = listOf(Need("pan", 2, Urgency.MEDIA), Need("tela", 1, Urgency.BAJA))
    )

    val DANI = EconomicCharacter(
        id = "dani",
        nombre = "Dani",
        oficio = "Tejedor",
        lugar = ValleyPlace.TELAR,
        presentacion = "Su telar suena todo el día. De la lana saca telas, y de las telas mantas para el invierno.",
        productividad = mapOf("lana" to 5, "tela" to 4, "manta" to 2),
        inventario = Inventory.of("tela" to 4, "lana" to 3),
        necesidades = listOf(Need("pescado", 2, Urgency.ALTA), Need("manzana", 2, Urgency.BAJA))
    )

    val RITA = EconomicCharacter(
        id = "rita",
        nombre = "Rita",
        oficio = "Transportista",
        lugar = ValleyPlace.MERCADO,
        presentacion = "Con su carreta cruza el Valle dos veces al día. No produce casi nada, pero sin ella nada llega.",
        productividad = mapOf("cesta" to 4, "leche" to 3, "queso" to 2),
        inventario = Inventory.of("leche" to 4, "queso" to 2, "cesta" to 2),
        necesidades = listOf(Need("verdura", 2, Urgency.MEDIA), Need("clavo", 2, Urgency.ALTA))
    )

    /** Personaje guía: aparece con misiones y explicaciones cortas, no molesta. */
    val TILO = EconomicCharacter(
        id = "tilo",
        nombre = "Tilo",
        oficio = "Cartero del Valle",
        lugar = ValleyPlace.PLAZA,
        presentacion = "Un zorro con bandolera que lleva y trae recados. Sabe quién necesita qué antes que nadie.",
        productividad = emptyMap(),
        inventario = Inventory.VACIO,
        necesidades = emptyList()
    )

    /** El niño dentro del Valle. Sin nombre real: solo un alias que él elige. */
    fun jugador(
        alias: String = "Vecino",
        avatarId: String = "avatar_1",
        inventario: Inventory = Inventory.VACIO,
        necesidades: List<Need> = emptyList(),
        productividad: Map<String, Int> = mapOf("manzana" to 3, "verdura" to 3)
    ) = EconomicCharacter(
        id = TradeOffer.ID_JUGADOR,
        nombre = alias,
        oficio = "Vecino nuevo del Valle",
        lugar = ValleyPlace.PLAZA,
        presentacion = "Acabas de llegar al Valle. Todavía no sabes hacer de todo, pero eso tiene arreglo.",
        productividad = productividad,
        inventario = inventario,
        necesidades = necesidades,
        avatarId = avatarId
    )

    val HABITANTES: List<EconomicCharacter> = listOf(LIA, TOMAS, NINA, BRUNO, SOFIA, EMI, DANI, RITA)

    val PORID: Map<String, EconomicCharacter> = (HABITANTES + TILO).associateBy { it.id }

    fun base(id: String): EconomicCharacter =
        PORID[id] ?: error("Habitante desconocido en el contenido semilla: $id")

    /** Ocho avatares locales, elegidos con imágenes, nunca escribiendo nada. */
    val AVATARES: List<Pair<String, String>> = listOf(
        "avatar_1" to "Sombrero de paja",
        "avatar_2" to "Gorro de lana",
        "avatar_3" to "Pañuelo del mercado",
        "avatar_4" to "Gorra de carpintería",
        "avatar_5" to "Trenza con cinta",
        "avatar_6" to "Capucha de viaje",
        "avatar_7" to "Delantal de taller",
        "avatar_8" to "Sombrero de pescar"
    )
}
