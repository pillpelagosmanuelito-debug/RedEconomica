package com.educalab.redeconomica.data.seed

import com.educalab.redeconomica.domain.model.ActivityKind
import com.educalab.redeconomica.domain.model.BudgetCase
import com.educalab.redeconomica.domain.model.BuildOption
import com.educalab.redeconomica.domain.model.ChainDef
import com.educalab.redeconomica.domain.model.ChainStage
import com.educalab.redeconomica.domain.model.ChainStep
import com.educalab.redeconomica.domain.model.ConceptId
import com.educalab.redeconomica.domain.model.CooperationStage
import com.educalab.redeconomica.domain.model.Scenario
import com.educalab.redeconomica.domain.model.ScarcityCase
import com.educalab.redeconomica.domain.model.ScarcityDemand
import com.educalab.redeconomica.domain.model.ScenarioPayload
import com.educalab.redeconomica.domain.model.TradeOffer

/**
 * Escenarios de las misiones 11 a 14: ventaja comparativa, el invierno,
 * las cadenas de producción y el desafío final del Valle.
 */
internal object SeedScenariosC {

    val LISTA: List<Scenario> = listOf(

        // ------------------------------------------ M11 · El taller de oficios
        Scenario(
            id = "s33",
            tipo = ActivityKind.COSTO_OPORTUNIDAD,
            titulo = "Bruno tiene que elegir",
            situacion = "En un turno Bruno puede sacar seis verduras del huerto o tres sacos de trigo. " +
                "Hoy le han pedido cuatro verduras.",
            instruccion = "Si dedica el turno a cuatro verduras, ¿a cuántos sacos de trigo renuncia?",
            jugador = yo(),
            participantes = listOf(
                hab("bruno", produce = mapOf("verdura" to 6, "trigo" to 3))
            ),
            payload = ScenarioPayload.CostoOportunidad(
                personajeId = "bruno",
                recursoElegido = "verdura",
                recursoRenunciado = "trigo",
                cantidadElegida = 4
            ),
            explicacionFinal = "Cuatro verduras son dos tercios del turno, y en ese tiempo habrían " +
                "salido dos sacos de trigo. Esa es la parte que Bruno deja de hacer.",
            conceptoId = ConceptId.ELECCION.name,
            dificultad = 4
        ),

        Scenario(
            id = "s34",
            tipo = ActivityKind.ESPECIALIZACION,
            titulo = "Lía es mejor en todo… ¿y qué?",
            situacion = "Lía recoge más manzanas Y más verduras que Bruno. Aun así, el Valle sale " +
                "ganando si se reparten el trabajo.",
            instruccion = "Consigue seis manzanas y tres verduras entre los dos.",
            jugador = yo(),
            participantes = listOf(
                hab("lia", produce = mapOf("manzana" to 6, "verdura" to 4)),
                hab("bruno", produce = mapOf("manzana" to 3, "verdura" to 3))
            ),
            payload = ScenarioPayload.Especializacion(
                objetivo = inv("manzana" to 6, "verdura" to 3),
                comparaConDeTodo = true
            ),
            explicacionFinal = "Aunque Lía es mejor en las dos cosas, cuando recoge verduras pierde " +
                "muchas manzanas. A Bruno le cuesta menos dejar las manzanas. Por eso conviene que " +
                "Lía vaya al frutal y Bruno al huerto.",
            conceptoId = ConceptId.ESPECIALIZACION.name,
            dificultad = 5
        ),

        Scenario(
            id = "s35",
            tipo = ActivityKind.EVALUAR_OFERTA,
            titulo = "Trigo por harina",
            situacion = "Tienes cuatro sacos de trigo, pero en casa nadie sabe molerlos. " +
                "Tomás te propone un cambio.",
            instruccion = "Piensa en el trabajo que hay detrás de cada cosa y decide.",
            jugador = yo(
                tiene = inv("trigo" to 4),
                necesita = listOf(nec("harina", 2, ALTA))
            ),
            participantes = listOf(
                hab("tomas", tiene = inv("harina" to 3), necesita = listOf(nec("trigo", 3, ALTA)))
            ),
            payload = ScenarioPayload.EvaluarOferta(
                oferta = TradeOffer(
                    proponenteId = "tomas",
                    receptorId = TradeOffer.ID_JUGADOR,
                    entrega = inv("harina" to 2),
                    pide = inv("trigo" to 4)
                ),
                aceptarEsLoCorrecto = true,
                motivoEsperado = null
            ),
            explicacionFinal = "El trigo sin moler no te sirve de nada. Tomás pone el molino y el " +
                "trabajo; tú pones el grano. Los dos acabáis con algo que sí podéis usar.",
            conceptoId = ConceptId.INTERCAMBIO.name,
            dificultad = 4
        ),

        // -------------------------------------------- M12 · El invierno llega
        Scenario(
            id = "s36",
            tipo = ActivityKind.DECISION,
            titulo = "Antes de la primera nevada",
            situacion = "Quedan ocho vellones de lana y seis troncos. El invierno no espera y " +
                "no da tiempo a todo.",
            instruccion = "Elige como mucho dos cosas para el Valle.",
            jugador = yo(tiene = inv("lana" to 8, "madera" to 6)),
            participantes = listOf(hab("dani"), hab("nina")),
            payload = ScenarioPayload.Decision(
                BudgetCase(
                    titulo = "¿Qué preparamos para el invierno?",
                    presupuesto = inv("lana" to 8, "madera" to 6),
                    opciones = listOf(
                        BuildOption(
                            "mantas", "Mantas para las casas frías",
                            "Seis vellones. Abrigan de verdad.",
                            costo = inv("lana" to 6), obtiene = inv("manta" to 2), utilidad = 5
                        ),
                        BuildOption(
                            "ropa", "Ropa de abrigo para trabajar fuera",
                            "Cuatro vellones y dos troncos para el bastidor.",
                            costo = inv("lana" to 4, "madera" to 2),
                            obtiene = inv("tela" to 3), utilidad = 3
                        ),
                        BuildOption(
                            "techo", "Arreglar el techo del granero",
                            "Cinco troncos. Si entra nieve, se pierde la comida.",
                            costo = inv("madera" to 5), obtiene = inv("tabla" to 3), utilidad = 4
                        )
                    ),
                    maxSelecciones = 2
                )
            ),
            explicacionFinal = "No cabía todo. Elegir dos cosas significó dejar la tercera para " +
                "más adelante, y eso también es una decisión económica.",
            conceptoId = ConceptId.ELECCION.name,
            dificultad = 4
        ),

        Scenario(
            id = "s37",
            tipo = ActivityKind.ESCASEZ,
            titulo = "Cuatro jarras de leche",
            situacion = "Las cabras dan menos con el frío. Hoy solo hay cuatro jarras y se piden seis.",
            instruccion = "Reparte las cuatro jarras.",
            jugador = yo(),
            participantes = listOf(
                hab("lia", necesita = listOf(nec("leche", 1, ALTA))),
                hab("bruno", necesita = listOf(nec("leche", 1, ALTA))),
                hab("nina", necesita = listOf(nec("leche", 2, MEDIA))),
                hab("emi", necesita = listOf(nec("leche", 2, MEDIA)))
            ),
            payload = ScenarioPayload.Escasez(
                ScarcityCase(
                    recursoId = "leche",
                    disponible = 4,
                    demandas = listOf(
                        ScarcityDemand("lia", "Lía", 1, ALTA, "Su hermano pequeño está malo"),
                        ScarcityDemand("bruno", "Bruno", 1, ALTA, "Su abuela solo toma leche por la mañana"),
                        ScarcityDemand("nina", "Nina", 2, MEDIA, "Para el desayuno del taller"),
                        ScarcityDemand("emi", "Emi", 2, MEDIA, "Quiere hacer queso para guardar")
                    )
                )
            ),
            explicacionFinal = "Con menos de lo que hace falta, lo primero es mirar quién no puede " +
                "esperar. Lo demás se reparte como mejor parezca.",
            conceptoId = ConceptId.RECURSOS.name,
            dificultad = 4
        ),

        Scenario(
            id = "s38",
            tipo = ActivityKind.COOPERACION,
            titulo = "Cerrar el granero",
            situacion = "Si entra nieve en el granero, se pierde la comida del invierno. " +
                "Hay que cerrarlo hoy: cortar, clavar y subir las tablas al tejado.",
            instruccion = "Reparte las tres tareas entre Nina, Sofía y Rita.",
            jugador = yo(),
            participantes = listOf(hab("nina"), hab("sofia"), hab("rita")),
            payload = ScenarioPayload.Cooperacion(
                etapas = listOf(
                    CooperationStage(
                        "cortar", "Cortar las tablas", 1, "Sacar tablas del tronco",
                        mapOf("nina" to 5, "sofia" to 3, "rita" to 0)
                    ),
                    CooperationStage(
                        "clavar", "Clavarlas", 2, "Unir las tablas del tejado",
                        mapOf("sofia" to 5, "nina" to 2, "rita" to 0)
                    ),
                    CooperationStage(
                        "subir", "Subirlas al tejado", 3, "Cargar y trepar con cuidado",
                        mapOf("rita" to 4, "nina" to 1, "sofia" to 0)
                    )
                ),
                objetivo = 4
            ),
            explicacionFinal = "Ninguno de los tres podía cerrar el granero solo. Repartidos, salió " +
                "en una tarde.",
            conceptoId = ConceptId.COOPERACION.name,
            dificultad = 4
        ),

        // ----------------------------------------- M13 · Las cadenas del Valle
        Scenario(
            id = "s39",
            tipo = ActivityKind.CADENA,
            titulo = "La cadena de la tela",
            situacion = "Una manta no aparece de la nada: empieza en el prado, con una oveja.",
            instruccion = "Pon los cinco pasos en orden.",
            jugador = yo(),
            participantes = listOf(hab("dani"), hab("rita"), hab("nina")),
            payload = ScenarioPayload.Cadena(
                ChainDef(
                    id = "cadena_tela",
                    titulo = "De la oveja a la manta",
                    introduccion = "Cinco pasos, tres personas, y un montón de trabajo que casi " +
                        "nunca se ve.",
                    pasos = listOf(
                        ChainStep("t1", "Dani esquila las ovejas",
                            "La lana sale del prado", ChainStage.MATERIA_PRIMA, "dani"),
                        ChainStep("t2", "Dani lava y peina la lana",
                            "La lana queda lista para el telar", ChainStage.TRANSFORMACION, "dani"),
                        ChainStep("t3", "Dani teje la tela",
                            "La lana se convierte en tela", ChainStage.TRANSFORMACION, "dani"),
                        ChainStep("t4", "Rita la lleva al mercado",
                            "La tela viaja hasta la plaza", ChainStage.TRANSPORTE, "rita"),
                        ChainStep("t5", "Nina cambia tablas por tela",
                            "La tela llega a quien la necesitaba", ChainStage.INTERCAMBIO, "nina")
                    ),
                    moraleja = "Detrás de una manta hay lana, trabajo, un viaje y un intercambio."
                )
            ),
            explicacionFinal = "Cada paso añade algo. Por eso una manta vale más que un vellón de lana: " +
                "lleva mucho más trabajo dentro.",
            conceptoId = ConceptId.COOPERACION.name,
            dificultad = 3
        ),

        Scenario(
            id = "s40",
            tipo = ActivityKind.CADENA,
            titulo = "La mesa de la escuela",
            situacion = "La escuela del Valle necesita una mesa nueva. En el bosque todavía es un árbol.",
            instruccion = "Ordena los pasos hasta que la mesa esté en la escuela.",
            jugador = yo(),
            participantes = listOf(hab("emi"), hab("nina"), hab("sofia"), hab("rita")),
            payload = ScenarioPayload.Cadena(
                ChainDef(
                    id = "cadena_mesa",
                    titulo = "Del bosque a la escuela",
                    introduccion = "Cuatro personas y una mesa que aún no existe.",
                    pasos = listOf(
                        ChainStep("m1", "Emi y Nina bajan los troncos del bosque",
                            "La madera sale del bosque", ChainStage.MATERIA_PRIMA, "emi"),
                        ChainStep("m2", "Nina corta las tablas",
                            "El tronco se convierte en tablas", ChainStage.TRANSFORMACION, "nina"),
                        ChainStep("m3", "Sofía clava y monta la mesa",
                            "Las tablas se convierten en mesa", ChainStage.TRANSFORMACION, "sofia"),
                        ChainStep("m4", "Rita la lleva a la escuela",
                            "La mesa cruza el Valle", ChainStage.TRANSPORTE, "rita"),
                        ChainStep("m5", "La escuela paga con verduras del huerto",
                            "El trabajo se cambia por comida", ChainStage.INTERCAMBIO, null)
                    ),
                    moraleja = "Nadie hizo la mesa entera. Entre cuatro, la escuela tiene mesa."
                )
            ),
            explicacionFinal = "Cuando cada persona se ocupa de un paso, el Valle consigue cosas que " +
                "una sola persona tardaría semanas en terminar.",
            conceptoId = ConceptId.INTEGRACION.name,
            dificultad = 3
        ),

        // ---------------------------------------------- M14 · El Valle entero
        Scenario(
            id = "s41",
            tipo = ActivityKind.ESPECIALIZACION,
            titulo = "Un turno para todo el Valle",
            situacion = "Última tarea antes de que Tilo cierre el correo del año: el Valle necesita " +
                "cinco cosas distintas en un solo turno.",
            instruccion = "Cinco habitantes, cinco oficios. Piénsalo bien.",
            jugador = yo(),
            participantes = listOf(
                hab("lia", produce = mapOf("manzana" to 6, "miel" to 2)),
                hab("bruno", produce = mapOf("verdura" to 6, "trigo" to 3)),
                hab("tomas", produce = mapOf("pan" to 5, "harina" to 4)),
                hab("dani", produce = mapOf("tela" to 4, "lana" to 5)),
                hab("sofia", produce = mapOf("herramienta" to 4, "clavo" to 6))
            ),
            payload = ScenarioPayload.Especializacion(
                objetivo = inv(
                    "manzana" to 6, "verdura" to 6, "pan" to 5,
                    "tela" to 4, "herramienta" to 4
                ),
                comparaConDeTodo = true
            ),
            explicacionFinal = "Cinco personas, cinco tareas, un turno. Eso es una economía pequeña " +
                "funcionando: cada uno hace lo suyo y entre todos sale lo que nadie podría solo.",
            conceptoId = ConceptId.INTEGRACION.name,
            dificultad = 5
        ),

        Scenario(
            id = "s42",
            tipo = ActivityKind.INTERCAMBIO,
            titulo = "La última cesta",
            situacion = "Terminas el año con ocho manzanas y tres cosas por resolver: pan para la " +
                "cena, una tela para la ventana y clavos para la puerta.",
            instruccion = "Consigue las tres cosas. Las manzanas justo dan.",
            jugador = yo(
                tiene = inv("manzana" to 8),
                necesita = listOf(
                    nec("pan", 2, ALTA),
                    nec("tela", 1, MEDIA),
                    nec("clavo", 2, MEDIA)
                )
            ),
            participantes = listOf(
                hab("tomas", tiene = inv("pan" to 4), necesita = listOf(nec("manzana", 3, ALTA))),
                hab("dani", tiene = inv("tela" to 3), necesita = listOf(nec("manzana", 2, ALTA))),
                hab("sofia", tiene = inv("clavo" to 5), necesita = listOf(nec("manzana", 3, ALTA)))
            ),
            payload = ScenarioPayload.Intercambio(
                objetivo = inv("pan" to 2, "tela" to 1, "clavo" to 2),
                pistaCorta = "Tres vecinos, tres necesidades y ocho manzanas. Repártelas con cabeza."
            ),
            explicacionFinal = "Empezaste el Valle sin saber qué era un intercambio y acabas " +
                "resolviendo tres a la vez. Eso es economía: gente que necesita cosas distintas " +
                "poniéndose de acuerdo.",
            conceptoId = ConceptId.INTEGRACION.name,
            dificultad = 5
        )
    )
}
