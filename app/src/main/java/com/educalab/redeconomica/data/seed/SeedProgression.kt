package com.educalab.redeconomica.data.seed

import com.educalab.redeconomica.domain.model.Badge
import com.educalab.redeconomica.domain.model.BadgeRule
import com.educalab.redeconomica.domain.model.CollectionItem
import com.educalab.redeconomica.domain.model.ConceptId
import com.educalab.redeconomica.domain.model.GlossaryEntry
import com.educalab.redeconomica.domain.model.ResourceType

/** Insignias, Almacén del Valle y Diccionario del Valle. */
object SeedProgression {

    /**
     * Insignias.
     *
     * Ninguna dice que el niño sea listo: todas describen algo que hizo.
     * Y todas se desbloquean con contadores reales guardados en Room.
     */
    val INSIGNIAS: List<Badge> = listOf(
        Badge("primer_trato", "Primer trato",
            "Cerraste tu primer intercambio en la plaza.",
            BadgeRule.PRIMER_INTERCAMBIO, 1, "insignia_apreton"),
        Badge("gran_negociador", "Buen negociador",
            "Diez intercambios aceptados por tus vecinos.",
            BadgeRule.INTERCAMBIOS_ACEPTADOS, 10, "insignia_balanza"),
        Badge("especialista", "Especialista del Valle",
            "Probaste seis repartos de trabajo distintos.",
            BadgeRule.ESPECIALIZACIONES_PROBADAS, 6, "insignia_azada"),
        Badge("maestro_recursos", "Maestro de recursos",
            "Resolviste cinco repartos cuando no había para todos.",
            BadgeRule.REPARTOS_RESUELTOS, 5, "insignia_cesta"),
        Badge("constructor_equipos", "Constructor de equipos",
            "Completaste cinco trabajos en equipo.",
            BadgeRule.COOPERACIONES_COMPLETADAS, 5, "insignia_engranaje"),
        Badge("experto_decisiones", "Experto en decisiones",
            "Tomaste cinco decisiones con recursos limitados.",
            BadgeRule.DECISIONES_TOMADAS, 5, "insignia_bifurcacion"),
        Badge("cooperador_valle", "Cooperador del Valle",
            "Terminaste diez misiones ayudando al pueblo.",
            BadgeRule.MISIONES_COMPLETADAS, 10, "insignia_manos"),
        Badge("maestro_economia", "Maestro de la economía",
            "Completaste las catorce misiones del Valle.",
            BadgeRule.MISIONES_COMPLETADAS, 14, "insignia_corona"),
        Badge("explorador_palabras", "Explorador de palabras",
            "Descubriste los siete conceptos del Diccionario.",
            BadgeRule.CONCEPTOS_DESCUBIERTOS, 7, "insignia_libro"),
        Badge("almacenista", "Almacenista",
            "Guardaste doce objetos en el Almacén del Valle.",
            BadgeRule.OBJETOS_COLECCIONADOS, 12, "insignia_almacen"),
        Badge("cientifico_valle", "Curioso del laboratorio",
            "Hiciste cinco experimentos en el Laboratorio del Valle.",
            BadgeRule.EXPERIMENTOS_REALIZADOS, 5, "insignia_matraz")
    )

    /**
     * Almacén del Valle: la colección.
     *
     * Cada pieza se desbloquea completando la misión donde aparece de verdad,
     * no comprándola ni por suerte.
     */
    val COLECCION: List<CollectionItem> = listOf(
        CollectionItem("col_manzana", "Manzana del frutal", ResourceType.ALIMENTO,
            "La primera cosecha de Lía.", "Misión 1 · La primera cosecha", "manzana"),
        CollectionItem("col_verdura", "Verdura del huerto", ResourceType.ALIMENTO,
            "Lo que sale de la tierra de Bruno.", "Misión 1 · La primera cosecha", "verdura"),
        CollectionItem("col_pan", "Pan de Tomás", ResourceType.ELABORADO,
            "El primer pan que conseguiste sin hornearlo tú.", "Misión 2 · ¿Quién tiene lo que necesito?", "pan"),
        CollectionItem("col_herramienta", "Herramienta de Sofía", ResourceType.HERRAMIENTA,
            "Horas de taller convertidas en metal.", "Misión 2 · ¿Quién tiene lo que necesito?", "herramienta"),
        CollectionItem("col_tabla", "Tabla recta", ResourceType.ELABORADO,
            "Un tronco al que Nina le vio la forma.", "Misión 3 · Cada uno a lo suyo", "tabla"),
        CollectionItem("col_clavo", "Puñado de clavos", ResourceType.HERRAMIENTA,
            "Pequeños, y siempre faltan al final.", "Misión 3 · Cada uno a lo suyo", "clavo"),
        CollectionItem("col_tela", "Tela del telar", ResourceType.TEXTIL,
            "Lana que ya sirve para algo.", "Misión 4 · Intercambio de vecinos", "tela"),
        CollectionItem("col_trigo", "Saco de trigo", ResourceType.MATERIA_PRIMA,
            "Todavía no se puede comer.", "Misión 5 · El problema del pan", "trigo"),
        CollectionItem("col_harina", "Saco de harina", ResourceType.ELABORADO,
            "El paso intermedio que casi nadie ve.", "Misión 5 · El problema del pan", "harina"),
        CollectionItem("col_mesa", "Mesa del Valle", ResourceType.ELABORADO,
            "Ocho troncos y un día entero.", "Misión 6 · La gran decisión", "mesa"),
        CollectionItem("col_silla", "Silla de la plaza", ResourceType.ELABORADO,
            "Más pequeña, y hacen falta varias.", "Misión 6 · La gran decisión", "silla"),
        CollectionItem("col_lana", "Vellón de lana", ResourceType.MATERIA_PRIMA,
            "Recién esquilado, aún huele a prado.", "Misión 7 · Juntos podemos", "lana"),
        CollectionItem("col_manta", "Manta de invierno", ResourceType.TEXTIL,
            "Cinco pasos y tres personas dentro.", "Misión 7 · Juntos podemos", "manta"),
        CollectionItem("col_miel", "Tarro de miel", ResourceType.ALIMENTO,
            "Poca cantidad, mucha gente pidiéndola.", "Misión 8 · La cooperativa", "miel"),
        CollectionItem("col_cesta", "Cesta de mimbre", ResourceType.ELABORADO,
            "Sin ella, media cosecha se queda en el campo.", "Misión 8 · La cooperativa", "cesta"),
        CollectionItem("col_pescado", "Pescado del río", ResourceType.ALIMENTO,
            "Emi conoce cada recodo.", "Misión 9 · El mercado del Valle", "pescado"),
        CollectionItem("col_queso", "Queso curado", ResourceType.ELABORADO,
            "Leche que supo esperar.", "Misión 9 · El mercado del Valle", "queso"),
        CollectionItem("col_leche", "Jarra de leche", ResourceType.ALIMENTO,
            "De las cabras del prado alto.", "Misión 10 · La gran feria", "leche"),
        CollectionItem("col_semilla", "Puñado de semillas", ResourceType.MATERIA_PRIMA,
            "La comida del año que viene.", "Misión 10 · La gran feria", "semilla"),
        CollectionItem("col_puesto", "Puesto de la feria", ResourceType.ELABORADO,
            "Montado entre cuatro en una mañana.", "Misión 10 · La gran feria", null),
        CollectionItem("col_martillo", "Martillo de Sofía", ResourceType.HERRAMIENTA,
            "El que usa cuando la cosa va en serio.", "Misión 11 · El taller de los oficios", null),
        CollectionItem("col_farol", "Farol del invierno", ResourceType.ELABORADO,
            "Alumbra el granero cuando cae la nieve.", "Misión 12 · El invierno se acerca", null),
        CollectionItem("col_mural", "Mural de las cadenas", ResourceType.ELABORADO,
            "El mapa de todo lo que pasa antes de que algo llegue.", "Misión 13 · Las cadenas del Valle", null),
        CollectionItem("col_bandolera", "Bandolera de Tilo", ResourceType.ELABORADO,
            "Te la deja al marcharse. Ahora el correo es cosa tuya.", "Misión 14 · El Valle entero", null)
    )

    /** Diccionario del Valle: la palabra llega DESPUÉS de haberlo vivido. */
    val DICCIONARIO: List<GlossaryEntry> = listOf(
        GlossaryEntry("g_necesidad", "Necesidad",
            "Algo que te hace falta y ahora mismo no tienes.",
            "Lía tiene manzanas de sobra, pero necesita pan para desayunar.",
            ConceptId.NECESIDADES, "arte_necesidad",
            "Mira la lista de un vecino y di qué le falta."),
        GlossaryEntry("g_recurso", "Recurso",
            "Cualquier cosa que sirve para producir o para vivir: madera, trigo, tiempo, manos.",
            "Con diez troncos Nina puede hacer una mesa. Los troncos son un recurso.",
            ConceptId.RECURSOS, "arte_recurso",
            "Cuenta cuántos recursos distintos hay hoy en tu cesta."),
        GlossaryEntry("g_escasez", "Escasez",
            "Cuando hay menos de una cosa de la que la gente necesita.",
            "Tres panes y cinco vecinos con hambre: eso es escasez.",
            ConceptId.RECURSOS, "arte_escasez",
            "Reparte tres panes entre cinco y mira a quién le toca esperar."),
        GlossaryEntry("g_produccion", "Producción",
            "Hacer que exista algo que antes no estaba.",
            "Tomás no encuentra el pan: lo produce con harina, agua y un horno.",
            ConceptId.NECESIDADES, "arte_produccion",
            "Elige un oficio y mira cuánto sale en un turno."),
        GlossaryEntry("g_intercambio", "Intercambio",
            "Dar algo que tienes a cambio de algo que necesitas.",
            "Tú das tres manzanas, Tomás te da dos panes. Los dos ganáis.",
            ConceptId.INTERCAMBIO, "arte_intercambio",
            "Busca en la plaza a alguien que necesite lo que te sobra."),
        GlossaryEntry("g_beneficio", "Beneficio mutuo",
            "Cuando las dos personas de un trato acaban mejor que antes.",
            "Nina quería fruta y tú tablas. Después del trueque, los dos tenéis lo que buscabais.",
            ConceptId.INTERCAMBIO, "arte_beneficio",
            "Mira una propuesta y di qué gana cada uno."),
        GlossaryEntry("g_especializacion", "Especialización",
            "Dedicarse a una sola tarea en lugar de hacer un poco de todo.",
            "Lía solo recoge manzanas y Tomás solo hornea. Entre los dos sale más.",
            ConceptId.ESPECIALIZACION, "arte_especializacion",
            "Prueba el mismo turno con y sin especializar y compara."),
        GlossaryEntry("g_ventaja", "Quién debería hacer qué",
            "No siempre produce algo quien mejor lo hace, sino quien menos deja de hacer al dedicarse a ello.",
            "Lía es mejor en todo, pero si va al huerto pierde muchas manzanas. Mejor que vaya al frutal.",
            ConceptId.ESPECIALIZACION, "arte_ventaja",
            "Compara cuánto pierde cada uno al cambiar de tarea."),
        GlossaryEntry("g_cooperacion", "Cooperación",
            "Repartirse las partes de un trabajo para llegar donde solos no llegaríamos.",
            "Uno esquila, otro teje y otro reparte. Así salen las tres mantas.",
            ConceptId.COOPERACION, "arte_cooperacion",
            "Coloca a cada habitante en una etapa y mira el resultado."),
        GlossaryEntry("g_cadena", "Cadena de producción",
            "Los pasos por los que pasa algo antes de llegar a quien lo necesita.",
            "Trigo → harina → pan → carreta → mercado.",
            ConceptId.COOPERACION, "arte_cadena",
            "Ordena los pasos del pan."),
        GlossaryEntry("g_eleccion", "Elección",
            "Quedarse con una opción cuando no se pueden tener todas.",
            "Con diez troncos: una mesa, o una silla y tres cestas. No las dos cosas.",
            ConceptId.ELECCION, "arte_eleccion",
            "Gasta diez troncos y mira qué te queda."),
        GlossaryEntry("g_costo", "Costo de oportunidad",
            "Lo que dejas de hacer cuando eliges otra cosa.",
            "Si Lía pasa el turno recogiendo seis manzanas, deja de cuidar tres verduras.",
            ConceptId.ELECCION, "arte_costo",
            "Mueve el reparto del turno y mira los dos números a la vez."),
        GlossaryEntry("g_mercado", "Mercado",
            "El sitio donde la gente que necesita cosas distintas se pone de acuerdo.",
            "En la plaza del Valle nadie manda: cada uno propone y decide.",
            ConceptId.INTEGRACION, "arte_mercado",
            "Da una vuelta por el mercado y mira quién necesita qué.")
    )

    val INSIGNIAS_PORID: Map<String, Badge> = INSIGNIAS.associateBy { it.id }
    val COLECCION_PORID: Map<String, CollectionItem> = COLECCION.associateBy { it.id }
}
