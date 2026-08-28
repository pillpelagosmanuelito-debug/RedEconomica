package com.educalab.redeconomica.domain.model

/**
 * Familias de recursos del Valle Económico.
 *
 * La familia sirve para dos cosas: agrupar visualmente el Almacén del Valle y
 * permitir que los escenarios pidan "algo de comer" sin nombrar un producto
 * concreto.
 */
enum class ResourceType(val etiqueta: String) {
    ALIMENTO("Alimento"),
    MATERIA_PRIMA("Materia prima"),
    ELABORADO("Producto elaborado"),
    HERRAMIENTA("Herramienta"),
    TEXTIL("Textil")
}

/**
 * Definición de un recurso del Valle.
 *
 * [valorBase] no es un precio de mercado: es un peso pedagógico pequeño
 * (1 a 4) que el motor usa para decidir si un intercambio le resulta
 * razonable a quien lo recibe. Nunca se muestra como "precio" al niño.
 */
data class ResourceDef(
    val id: String,
    val singular: String,
    val plural: String,
    val tipo: ResourceType,
    val valorBase: Int,
    val descripcion: String
) {
    init {
        require(id.isNotBlank()) { "El recurso necesita un id" }
        require(valorBase in 1..4) { "valorBase de $id fuera de rango (1..4)" }
    }

    /** "1 manzana" / "3 manzanas" */
    fun etiquetaCantidad(cantidad: Int): String =
        if (cantidad == 1) "1 $singular" else "$cantidad $plural"
}

/**
 * Inventario inmutable de recursos.
 *
 * Invariantes garantizadas por construcción:
 *  - nunca contiene cantidades negativas;
 *  - nunca contiene entradas en cero (se descartan);
 *  - dos inventarios con el mismo contenido son iguales.
 *
 * Toda modificación devuelve un inventario nuevo. La interfaz jamás modifica
 * inventarios directamente: siempre pasa por los motores del dominio.
 */
class Inventory private constructor(val contenido: Map<String, Int>) {

    val total: Int get() = contenido.values.sum()
    val esVacio: Boolean get() = contenido.isEmpty()
    val recursos: Set<String> get() = contenido.keys

    fun cantidad(recursoId: String): Int = contenido[recursoId] ?: 0

    fun contiene(otro: Inventory): Boolean =
        otro.contenido.all { (id, n) -> cantidad(id) >= n }

    fun mas(otro: Inventory): Inventory {
        val nuevo = contenido.toMutableMap()
        otro.contenido.forEach { (id, n) -> nuevo[id] = (nuevo[id] ?: 0) + n }
        return of(nuevo)
    }

    fun mas(recursoId: String, cantidad: Int): Inventory =
        mas(of(recursoId to cantidad))

    /** Resta [otro]. Devuelve null si no hay suficiente: nunca genera negativos. */
    fun menos(otro: Inventory): Inventory? {
        if (!contiene(otro)) return null
        val nuevo = contenido.toMutableMap()
        otro.contenido.forEach { (id, n) -> nuevo[id] = (nuevo[id] ?: 0) - n }
        return of(nuevo)
    }

    /** Valor pedagógico total del inventario según [catalogo]. */
    fun valor(catalogo: Map<String, ResourceDef>): Int =
        contenido.entries.sumOf { (id, n) -> (catalogo[id]?.valorBase ?: 1) * n }

    fun descripcion(catalogo: Map<String, ResourceDef>): String =
        if (esVacio) "nada" else contenido.entries.joinToString(", ") { (id, n) ->
            catalogo[id]?.etiquetaCantidad(n) ?: "$n $id"
        }

    override fun equals(other: Any?): Boolean =
        other is Inventory && other.contenido == contenido

    override fun hashCode(): Int = contenido.hashCode()

    override fun toString(): String =
        contenido.entries.joinToString(", ") { "${it.key}x${it.value}" }

    companion object {
        val VACIO = Inventory(emptyMap())

        fun of(mapa: Map<String, Int>): Inventory {
            mapa.forEach { (id, n) ->
                require(n >= 0) { "Cantidad negativa para '$id': $n" }
            }
            val limpio = mapa.filterValues { it > 0 }
            if (limpio.isEmpty()) return VACIO
            return Inventory(limpio.toSortedMap().toMap())
        }

        fun of(vararg pares: Pair<String, Int>): Inventory = of(pares.toMap())

        /** Formato de persistencia: "manzana:4|pan:2". Estable y legible. */
        fun desdeTexto(texto: String): Inventory {
            if (texto.isBlank()) return VACIO
            val mapa = texto.split('|')
                .filter { it.isNotBlank() }
                .associate { parte ->
                    val trozos = parte.split(':')
                    require(trozos.size == 2) { "Entrada de inventario inválida: '$parte'" }
                    trozos[0].trim() to (trozos[1].trim().toIntOrNull()
                        ?: error("Cantidad no numérica en '$parte'"))
                }
            return of(mapa)
        }

        fun aTexto(inventario: Inventory): String =
            inventario.contenido.entries.joinToString("|") { "${it.key}:${it.value}" }
    }
}
