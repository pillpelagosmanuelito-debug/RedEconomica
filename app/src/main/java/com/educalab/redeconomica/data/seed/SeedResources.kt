package com.educalab.redeconomica.data.seed

import com.educalab.redeconomica.domain.model.ResourceDef
import com.educalab.redeconomica.domain.model.ResourceType

/**
 * Catálogo de recursos del Valle Económico.
 *
 * `valorBase` (1..4) es un peso pedagógico, no un precio: sirve para que el
 * motor decida si a un habitante le parece razonable lo que recibe. Sube con
 * el trabajo acumulado en el producto (la madera vale menos que la mesa).
 */
object SeedResources {

    val TODOS: List<ResourceDef> = listOf(
        ResourceDef("manzana", "manzana", "manzanas", ResourceType.ALIMENTO, 1,
            "Fruta del huerto de Lía. Se recoge rápido y gusta a todo el mundo."),
        ResourceDef("verdura", "verdura", "verduras", ResourceType.ALIMENTO, 1,
            "Lo que sale de la tierra del Valle: zanahorias, calabazas, judías."),
        ResourceDef("trigo", "saco de trigo", "sacos de trigo", ResourceType.MATERIA_PRIMA, 1,
            "El grano del campo. Todavía no se puede comer así."),
        ResourceDef("harina", "saco de harina", "sacos de harina", ResourceType.ELABORADO, 2,
            "Trigo molido. Es el paso intermedio para el pan."),
        ResourceDef("pan", "pan", "panes", ResourceType.ELABORADO, 2,
            "Sale del horno de Tomás. Todo el Valle lo busca."),
        ResourceDef("pescado", "pescado", "pescados", ResourceType.ALIMENTO, 2,
            "Del río del Valle. Hay que ir temprano."),
        ResourceDef("leche", "jarra de leche", "jarras de leche", ResourceType.ALIMENTO, 1,
            "De las cabras del prado alto."),
        ResourceDef("queso", "queso", "quesos", ResourceType.ELABORADO, 2,
            "Leche que ha esperado unas semanas. Merece la pena."),
        ResourceDef("miel", "tarro de miel", "tarros de miel", ResourceType.ALIMENTO, 2,
            "De las colmenas del bosque. Poca cantidad, mucha demanda."),
        ResourceDef("madera", "tronco", "troncos", ResourceType.MATERIA_PRIMA, 1,
            "Del bosque de arriba. Pesa, pero sirve para casi todo."),
        ResourceDef("tabla", "tabla", "tablas", ResourceType.ELABORADO, 2,
            "Madera ya cortada y lista para construir."),
        ResourceDef("mesa", "mesa", "mesas", ResourceType.ELABORADO, 4,
            "Cuesta mucha madera y mucho trabajo, pero dura años."),
        ResourceDef("silla", "silla", "sillas", ResourceType.ELABORADO, 3,
            "Más pequeña que una mesa, y hacen falta varias."),
        ResourceDef("herramienta", "herramienta", "herramientas", ResourceType.HERRAMIENTA, 3,
            "Martillos, azadas y sierras del taller de Sofía."),
        ResourceDef("clavo", "puñado de clavos", "puñados de clavos", ResourceType.HERRAMIENTA, 1,
            "Pequeños, baratos y siempre faltan justo al final."),
        ResourceDef("lana", "vellón de lana", "vellones de lana", ResourceType.MATERIA_PRIMA, 1,
            "De las ovejas del prado. Hay que lavarla y peinarla."),
        ResourceDef("tela", "tela", "telas", ResourceType.TEXTIL, 2,
            "Lana convertida en algo que ya se puede usar."),
        ResourceDef("manta", "manta", "mantas", ResourceType.TEXTIL, 3,
            "Abriga de verdad. En invierno el Valle no habla de otra cosa."),
        ResourceDef("semilla", "puñado de semillas", "puñados de semillas", ResourceType.MATERIA_PRIMA, 1,
            "Lo que hay que sembrar hoy para tener comida mañana."),
        ResourceDef("cesta", "cesta", "cestas", ResourceType.ELABORADO, 2,
            "Sin cestas, media cosecha se queda en el campo.")
    )

    val PORID: Map<String, ResourceDef> = TODOS.associateBy { it.id }

    fun def(id: String): ResourceDef =
        PORID[id] ?: error("Recurso desconocido en el contenido semilla: $id")
}
