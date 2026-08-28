package com.educalab.redeconomica.domain.model

/** Urgencia de una necesidad. Se muestra con icono + texto, nunca solo con color. */
enum class Urgency(val etiqueta: String, val peso: Int) {
    BAJA("Puede esperar", 1),
    MEDIA("Le hace falta", 2),
    ALTA("Lo necesita ya", 3)
}

/**
 * Algo que un habitante necesita.
 *
 * Una necesidad no es decoración: el motor de intercambio la consulta para
 * decidir si un personaje acepta o rechaza una propuesta.
 */
data class Need(
    val recursoId: String,
    val cantidad: Int,
    val urgencia: Urgency = Urgency.MEDIA
) {
    init {
        require(cantidad > 0) { "Una necesidad debe pedir al menos 1 unidad" }
    }
}

/** Zonas del Valle Económico. Cada una tiene una función económica distinta. */
enum class ValleyPlace(val etiqueta: String, val funcion: String) {
    GRANJA("Granja", "Produce alimentos del campo"),
    HUERTO("Huerto", "Cultiva verduras y semillas"),
    PANADERIA("Panadería", "Transforma harina en pan"),
    TALLER("Taller", "Fabrica herramientas"),
    CARPINTERIA("Carpintería", "Convierte madera en muebles"),
    PESQUERIA("Pesquería", "Obtiene pescado del río"),
    TELAR("Telar", "Elabora telas y ropa"),
    MERCADO("Mercado del Valle", "Donde todos se encuentran para intercambiar"),
    PLAZA("Plaza central", "El punto de reunión del pueblo"),
    CENTRO_INTERCAMBIO("Centro de Intercambio", "Se estudian los trueques"),
    COOPERATIVA("Cooperativa", "Varios habitantes unen recursos"),
    LABORATORIO("Laboratorio del Valle", "Se prueban ideas sin riesgo"),
    ALMACEN("Almacén del Valle", "Guarda todo lo descubierto")
}

/**
 * Un habitante del Valle.
 *
 * [productividad] indica cuántas unidades produce en UN turno si dedica todo
 * el turno a ese recurso. Es la base de la especialización y del costo de
 * oportunidad.
 */
data class EconomicCharacter(
    val id: String,
    val nombre: String,
    val oficio: String,
    val lugar: ValleyPlace,
    val presentacion: String,
    val productividad: Map<String, Int>,
    val inventario: Inventory = Inventory.VACIO,
    val necesidades: List<Need> = emptyList(),
    val avatarId: String = id
) {
    init {
        require(nombre.isNotBlank()) { "El habitante necesita nombre" }
        productividad.forEach { (r, n) ->
            require(n >= 0) { "Productividad negativa de $nombre en $r" }
        }
    }

    fun puedeProducir(recursoId: String): Boolean = (productividad[recursoId] ?: 0) > 0

    fun produccionPorTurno(recursoId: String): Int = productividad[recursoId] ?: 0

    /** Recursos que este habitante sabe producir, de mayor a menor capacidad. */
    fun oficiosPosibles(): List<String> =
        productividad.filterValues { it > 0 }.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key }

    fun necesidadDe(recursoId: String): Need? = necesidades.firstOrNull { it.recursoId == recursoId }

    /** Cuánto le falta todavía de [recursoId], contando lo que ya tiene. */
    fun faltante(recursoId: String): Int {
        val need = necesidadDe(recursoId) ?: return 0
        return (need.cantidad - inventario.cantidad(recursoId)).coerceAtLeast(0)
    }

    fun necesidadesPendientes(): List<Need> = necesidades.filter { faltante(it.recursoId) > 0 }

    fun conInventario(nuevo: Inventory): EconomicCharacter = copy(inventario = nuevo)
}
