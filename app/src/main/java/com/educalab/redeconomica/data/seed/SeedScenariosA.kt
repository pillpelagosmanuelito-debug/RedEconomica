package com.educalab.redeconomica.data.seed

import com.educalab.redeconomica.domain.model.ActivityKind
import com.educalab.redeconomica.domain.model.BudgetCase
import com.educalab.redeconomica.domain.model.BuildOption
import com.educalab.redeconomica.domain.model.ConceptId
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.Scenario
import com.educalab.redeconomica.domain.model.ScarcityCase
import com.educalab.redeconomica.domain.model.ScarcityDemand
import com.educalab.redeconomica.domain.model.ScenarioPayload
import com.educalab.redeconomica.domain.model.TradeOffer
import com.educalab.redeconomica.domain.model.TradeRejectReason

/**
 * Escenarios de las misiones 1 a 5: necesidades, recursos, primer intercambio,
 * especialización y escasez.
 *
 * Todo esto son DATOS. Ninguna pantalla decide qué es correcto: lo decide el
 * motor del dominio a partir de estos números.
 */
internal object SeedScenariosA {

    val LISTA: List<Scenario> = listOf(

        // ---------------------------------------------- M1 · La primera cosecha
        Scenario(
            id = "s01",
            tipo = ActivityKind.ESPECIALIZACION,
            titulo = "La primera cosecha",
            situacion = "Lía tiene el frutal lleno y una mañana entera por delante. " +
                "El Valle necesita seis manzanas para empezar la semana.",
            instruccion = "Dile a Lía a qué dedica el turno y pulsa «Trabajar».",
            jugador = yo(),
            participantes = listOf(
                hab("lia", tiene = inv(), produce = mapOf("manzana" to 6, "verdura" to 3))
            ),
            payload = ScenarioPayload.Especializacion(
                objetivo = inv("manzana" to 6),
                comparaConDeTodo = true
            ),
            explicacionFinal = "Lía consigue seis manzanas porque dedicó el turno entero al frutal. " +
                "Cuando reparte el tiempo entre varias tareas, saca menos de cada una.",
            conceptoId = ConceptId.NECESIDADES.name,
            dificultad = 1
        ),

        Scenario(
            id = "s02",
            tipo = ActivityKind.ESCASEZ,
            titulo = "Seis manzanas, tres vecinos",
            situacion = "Tilo ha dejado la cesta de Lía en la plaza. Tomás, Nina y Dani se acercan, " +
                "y cada uno pide una cantidad distinta.",
            instruccion = "Reparte las seis manzanas según lo que pide cada uno.",
            jugador = yo(),
            participantes = listOf(
                hab("tomas", tiene = inv("pan" to 4), necesita = listOf(nec("manzana", 3, ALTA))),
                hab("nina", tiene = inv("madera" to 4), necesita = listOf(nec("manzana", 2, MEDIA))),
                hab("dani", tiene = inv("tela" to 3), necesita = listOf(nec("manzana", 1, BAJA)))
            ),
            payload = ScenarioPayload.Escasez(
                ScarcityCase(
                    recursoId = "manzana",
                    disponible = 6,
                    demandas = listOf(
                        ScarcityDemand("tomas", "Tomás", 3, ALTA, "Sin fruta no puede desayunar antes del horno"),
                        ScarcityDemand("nina", "Nina", 2, MEDIA, "Se lleva la comida al taller"),
                        ScarcityDemand("dani", "Dani", 1, BAJA, "Le apetece una, sin más")
                    )
                )
            ),
            explicacionFinal = "Cada vecino necesitaba una cantidad distinta. Hoy había justo para todos: " +
                "eso no siempre pasa.",
            conceptoId = ConceptId.NECESIDADES.name,
            dificultad = 1
        ),

        Scenario(
            id = "s03",
            tipo = ActivityKind.ESPECIALIZACION,
            titulo = "Dos huertos, dos manos",
            situacion = "Lía y Bruno trabajan la misma mañana. El Valle quiere seis manzanas y seis verduras.",
            instruccion = "Asigna una tarea a cada uno y compara con «que cada uno haga de todo».",
            jugador = yo(),
            participantes = listOf(
                hab("lia", produce = mapOf("manzana" to 6, "verdura" to 3)),
                hab("bruno", produce = mapOf("verdura" to 6, "manzana" to 2))
            ),
            payload = ScenarioPayload.Especializacion(
                objetivo = inv("manzana" to 6, "verdura" to 6),
                comparaConDeTodo = true
            ),
            explicacionFinal = "Lía es más rápida con las manzanas y Bruno con las verduras. " +
                "Si cada uno hace lo suyo, el Valle consigue las dos cosas.",
            conceptoId = ConceptId.RECURSOS.name,
            dificultad = 1
        ),

        // ------------------------------------ M2 · ¿Quién tiene lo que necesito?
        Scenario(
            id = "s04",
            tipo = ActivityKind.INTERCAMBIO,
            titulo = "El primer trueque",
            situacion = "Tienes cinco manzanas y ningún pan. Tomás tiene el horno lleno de pan " +
                "y lleva dos días sin comer fruta.",
            instruccion = "Arrastra lo que ofreces y lo que pides, y propónselo a Tomás.",
            jugador = yo(
                tiene = inv("manzana" to 5),
                necesita = listOf(nec("pan", 2, ALTA))
            ),
            participantes = listOf(
                hab("tomas", tiene = inv("pan" to 5), necesita = listOf(nec("manzana", 3, ALTA)))
            ),
            payload = ScenarioPayload.Intercambio(
                objetivo = inv("pan" to 2),
                pistaCorta = "Tomás necesita manzanas. Tú necesitas pan. Ahí está el trato."
            ),
            explicacionFinal = "Los dos teníais algo que al otro le faltaba. A eso se le llama intercambio: " +
                "nadie pierde, los dos ganan lo que necesitaban.",
            conceptoId = ConceptId.INTERCAMBIO.name,
            dificultad = 1
        ),

        Scenario(
            id = "s05",
            tipo = ActivityKind.EVALUAR_OFERTA,
            titulo = "¿Aceptarías este trato?",
            situacion = "Rita llega con su carreta y te propone algo. Tú tienes manzanas de sobra, " +
                "pero lo que buscas es pan.",
            instruccion = "Mira lo que ofrece y lo que pide. ¿Te interesa?",
            jugador = yo(
                tiene = inv("manzana" to 5),
                necesita = listOf(nec("pan", 2, ALTA))
            ),
            participantes = listOf(
                hab("rita", tiene = inv("leche" to 3), necesita = listOf(nec("manzana", 3, MEDIA)))
            ),
            payload = ScenarioPayload.EvaluarOferta(
                oferta = TradeOffer(
                    proponenteId = "rita",
                    receptorId = TradeOffer.ID_JUGADOR,
                    entrega = inv("leche" to 1),
                    pide = inv("manzana" to 4)
                ),
                aceptarEsLoCorrecto = false,
                motivoEsperado = TradeRejectReason.NO_NECESITA_LO_OFRECIDO
            ),
            explicacionFinal = "La leche está muy bien, pero hoy no te resuelve nada: tú buscabas pan. " +
                "Un intercambio solo merece la pena si te da algo que necesitas.",
            conceptoId = ConceptId.INTERCAMBIO.name,
            dificultad = 2
        ),

        Scenario(
            id = "s06",
            tipo = ActivityKind.INTERCAMBIO,
            titulo = "Herramientas para el huerto",
            situacion = "Tienes seis verduras recién recogidas. Sofía tiene herramientas colgadas " +
                "en la pared del taller y hoy no ha comido nada verde.",
            instruccion = "Propón un intercambio que a Sofía le parezca justo.",
            jugador = yo(
                tiene = inv("verdura" to 6),
                necesita = listOf(nec("herramienta", 2, ALTA))
            ),
            participantes = listOf(
                hab("sofia", tiene = inv("herramienta" to 3), necesita = listOf(nec("verdura", 3, ALTA)))
            ),
            payload = ScenarioPayload.Intercambio(
                objetivo = inv("herramienta" to 2),
                pistaCorta = "Una herramienta lleva muchas horas de taller. Ofrece bastante verdura."
            ),
            explicacionFinal = "Una herramienta cuesta muchas horas de trabajo; una verdura, muchas menos. " +
                "Por eso Sofía pide varias verduras por cada herramienta.",
            conceptoId = ConceptId.INTERCAMBIO.name,
            dificultad = 2
        ),

        // ------------------------------------------- M3 · Cada uno a lo suyo
        Scenario(
            id = "s07",
            tipo = ActivityKind.ESPECIALIZACION,
            titulo = "¿Y si cada uno hace solo una cosa?",
            situacion = "Lía y Tomás han probado a hacer un poco de todo y el resultado ha sido flojo. " +
                "Hoy van a intentarlo de otra manera.",
            instruccion = "Prueba primero «que cada uno haga de todo» y luego especializa. Compara.",
            jugador = yo(),
            participantes = listOf(
                hab("lia", produce = mapOf("manzana" to 6, "verdura" to 3)),
                hab("tomas", produce = mapOf("pan" to 5, "harina" to 4))
            ),
            payload = ScenarioPayload.Especializacion(
                objetivo = inv("manzana" to 6, "pan" to 5),
                comparaConDeTodo = true
            ),
            explicacionFinal = "Repartir el turno entre dos tareas no da la mitad de cada una: da menos. " +
                "Concentrarse en una sola cosa se llama especializarse.",
            conceptoId = ConceptId.ESPECIALIZACION.name,
            dificultad = 2
        ),

        Scenario(
            id = "s08",
            tipo = ActivityKind.ESPECIALIZACION,
            titulo = "El encargo del taller",
            situacion = "Sofía necesita clavos, Nina tablas y en la plaza piden fruta. " +
                "Tres habitantes, un turno.",
            instruccion = "Decide en qué se especializa cada uno para cubrir el encargo.",
            jugador = yo(),
            participantes = listOf(
                hab("lia", produce = mapOf("manzana" to 6, "verdura" to 3)),
                hab("nina", produce = mapOf("madera" to 5, "tabla" to 4)),
                hab("sofia", produce = mapOf("herramienta" to 4, "clavo" to 6))
            ),
            payload = ScenarioPayload.Especializacion(
                objetivo = inv("manzana" to 6, "tabla" to 4, "clavo" to 6),
                comparaConDeTodo = true
            ),
            explicacionFinal = "Cada uno se puso en lo que mejor se le da. El Valle consiguió las tres cosas " +
                "en el mismo turno.",
            conceptoId = ConceptId.ESPECIALIZACION.name,
            dificultad = 2
        ),

        Scenario(
            id = "s09",
            tipo = ActivityKind.ESPECIALIZACION,
            titulo = "El pedido de la escuela",
            situacion = "La escuela del Valle prepara la comida de la semana: verduras, pan y pescado.",
            instruccion = "Asigna a Bruno, Tomás y Emi. Solo hay un turno.",
            jugador = yo(),
            participantes = listOf(
                hab("bruno", produce = mapOf("verdura" to 6, "trigo" to 3)),
                hab("tomas", produce = mapOf("pan" to 5, "harina" to 4)),
                hab("emi", produce = mapOf("pescado" to 5, "cesta" to 3))
            ),
            payload = ScenarioPayload.Especializacion(
                objetivo = inv("verdura" to 6, "pan" to 5, "pescado" to 5),
                comparaConDeTodo = true
            ),
            explicacionFinal = "Nadie sabe hacerlo todo, y no hace falta: entre tres personas especializadas " +
                "sale el pedido completo.",
            conceptoId = ConceptId.ESPECIALIZACION.name,
            dificultad = 3
        ),

        // ------------------------------------- M4 · Intercambio de vecinos
        Scenario(
            id = "s10",
            tipo = ActivityKind.INTERCAMBIO,
            titulo = "Dos tratos en la plaza",
            situacion = "Se acerca la noche y necesitas pan para cenar y una tela para la ventana. " +
                "Tienes seis manzanas y dos vecinos con hambre de fruta.",
            instruccion = "Haz los intercambios que hagan falta hasta conseguir las dos cosas.",
            jugador = yo(
                tiene = inv("manzana" to 6),
                necesita = listOf(nec("pan", 2, ALTA), nec("tela", 1, MEDIA))
            ),
            participantes = listOf(
                hab("tomas", tiene = inv("pan" to 4), necesita = listOf(nec("manzana", 3, ALTA))),
                hab("dani", tiene = inv("tela" to 3), necesita = listOf(nec("manzana", 2, ALTA)))
            ),
            payload = ScenarioPayload.Intercambio(
                objetivo = inv("pan" to 2, "tela" to 1),
                pistaCorta = "No hace falta un solo trato enorme: dos pequeños funcionan mejor."
            ),
            explicacionFinal = "Con las mismas manzanas has resuelto dos necesidades distintas, " +
                "porque cada vecino quería algo diferente.",
            conceptoId = ConceptId.INTERCAMBIO.name,
            dificultad = 3
        ),

        Scenario(
            id = "s11",
            tipo = ActivityKind.EVALUAR_OFERTA,
            titulo = "La propuesta de Nina",
            situacion = "Estás arreglando un banco y te faltan dos tablas. Nina aparece con ellas " +
                "justo cuando tienes el cesto lleno de manzanas.",
            instruccion = "Decide si aceptas la propuesta de Nina.",
            jugador = yo(
                tiene = inv("manzana" to 6),
                necesita = listOf(nec("tabla", 2, ALTA))
            ),
            participantes = listOf(
                hab("nina", tiene = inv("tabla" to 2), necesita = listOf(nec("manzana", 4, ALTA)))
            ),
            payload = ScenarioPayload.EvaluarOferta(
                oferta = TradeOffer(
                    proponenteId = "nina",
                    receptorId = TradeOffer.ID_JUGADOR,
                    entrega = inv("tabla" to 2),
                    pide = inv("manzana" to 4)
                ),
                aceptarEsLoCorrecto = true,
                motivoEsperado = null
            ),
            explicacionFinal = "Este sí: Nina se queda con la fruta que le faltaba y tú con las tablas " +
                "que necesitabas. Los dos salís ganando.",
            conceptoId = ConceptId.INTERCAMBIO.name,
            dificultad = 2
        ),

        Scenario(
            id = "s12",
            tipo = ActivityKind.INTERCAMBIO,
            titulo = "Pídele lo que le sobra",
            situacion = "Vuelves del río con cuatro pescados. Lía tiene dos panes, pero son justo " +
                "los que necesita para mañana. Tomás también tiene pan… y lleva días sin pescado.",
            instruccion = "Consigue dos panes sin dejar a nadie sin lo que necesita.",
            jugador = yo(
                tiene = inv("pescado" to 4),
                necesita = listOf(nec("pan", 2, ALTA))
            ),
            participantes = listOf(
                hab(
                    "lia",
                    tiene = inv("pan" to 2, "manzana" to 4),
                    necesita = listOf(nec("pan", 2, ALTA), nec("pescado", 1, MEDIA))
                ),
                hab("tomas", tiene = inv("pan" to 3), necesita = listOf(nec("pescado", 2, ALTA)))
            ),
            payload = ScenarioPayload.Intercambio(
                objetivo = inv("pan" to 2),
                pistaCorta = "Mira quién tiene pan de sobra y quién lo necesita para sí mismo."
            ),
            explicacionFinal = "A Lía el pan no le sobra: es justo el que necesita. Tomás sí puede " +
                "desprenderse del suyo. Por eso un intercambio depende de lo que cada uno necesita.",
            conceptoId = ConceptId.INTERCAMBIO.name,
            dificultad = 3
        ),

        // ---------------------------------------- M5 · El problema del pan
        Scenario(
            id = "s13",
            tipo = ActivityKind.ESCASEZ,
            titulo = "El problema del pan",
            situacion = "El horno se apagó a media noche y solo han salido tres panes. " +
                "En la plaza hay cinco vecinos esperando.",
            instruccion = "Reparte los tres panes. No hay para todos: piensa a quién le hace más falta.",
            jugador = yo(),
            participantes = listOf(
                hab("lia", necesita = listOf(nec("pan", 2, ALTA))),
                hab("bruno", necesita = listOf(nec("pan", 1, ALTA))),
                hab("nina", necesita = listOf(nec("pan", 2, MEDIA))),
                hab("emi", necesita = listOf(nec("pan", 1, MEDIA))),
                hab("dani", necesita = listOf(nec("pan", 1, BAJA)))
            ),
            payload = ScenarioPayload.Escasez(
                ScarcityCase(
                    recursoId = "pan",
                    disponible = 3,
                    demandas = listOf(
                        ScarcityDemand("lia", "Lía", 2, ALTA, "Sale al frutal al amanecer y no ha cenado"),
                        ScarcityDemand("bruno", "Bruno", 1, ALTA, "Lleva desde ayer en el huerto"),
                        ScarcityDemand("nina", "Nina", 2, MEDIA, "Tiene comida en casa, pero poca"),
                        ScarcityDemand("emi", "Emi", 1, MEDIA, "Puede pescar algo para hoy"),
                        ScarcityDemand("dani", "Dani", 1, BAJA, "Le apetece, aunque tiene queso")
                    )
                )
            ),
            explicacionFinal = "Se pedían siete panes y solo había tres. Cuando hay menos de lo que " +
                "hace falta, decimos que hay escasez, y alguien tiene que quedarse con menos.",
            conceptoId = ConceptId.RECURSOS.name,
            dificultad = 3
        ),

        Scenario(
            id = "s14",
            tipo = ActivityKind.ESCASEZ,
            titulo = "Cuatro herramientas",
            situacion = "Sofía ha terminado cuatro herramientas esta semana. Cuatro vecinos las quieren, " +
                "y entre todos piden seis.",
            instruccion = "Reparte las cuatro herramientas. Hay más de una forma razonable de hacerlo.",
            jugador = yo(),
            participantes = listOf(
                hab("bruno", necesita = listOf(nec("herramienta", 2, ALTA))),
                hab("nina", necesita = listOf(nec("herramienta", 1, MEDIA))),
                hab("emi", necesita = listOf(nec("herramienta", 1, MEDIA))),
                hab("rita", necesita = listOf(nec("herramienta", 2, MEDIA)))
            ),
            payload = ScenarioPayload.Escasez(
                ScarcityCase(
                    recursoId = "herramienta",
                    disponible = 4,
                    demandas = listOf(
                        ScarcityDemand("bruno", "Bruno", 2, ALTA, "Se le rompió la azada y el huerto no espera"),
                        ScarcityDemand("nina", "Nina", 1, MEDIA, "La suya aguanta un poco más"),
                        ScarcityDemand("emi", "Emi", 1, MEDIA, "Para arreglar la barca"),
                        ScarcityDemand("rita", "Rita", 2, MEDIA, "Para la carreta, que cojea")
                    )
                )
            ),
            explicacionFinal = "Aquí no había una única respuesta correcta. Lo importante era atender " +
                "primero a quien no podía esperar y repartir el resto con cabeza.",
            conceptoId = ConceptId.RECURSOS.name,
            dificultad = 3
        ),

        Scenario(
            id = "s15",
            tipo = ActivityKind.DECISION,
            titulo = "Cinco sacos de trigo",
            situacion = "En el granero quedan cinco sacos de trigo. Se puede moler, se puede guardar " +
                "para sembrar, o se puede preparar la fiesta del Valle. No da para todo.",
            instruccion = "Elige como mucho dos cosas y mira a qué renuncias.",
            jugador = yo(tiene = inv("trigo" to 5)),
            participantes = listOf(
                hab("tomas", tiene = inv("harina" to 1)),
                hab("bruno", tiene = inv("semilla" to 1))
            ),
            payload = ScenarioPayload.Decision(
                BudgetCase(
                    titulo = "¿Qué hacemos con el trigo?",
                    presupuesto = inv("trigo" to 5),
                    opciones = listOf(
                        BuildOption(
                            "moler", "Moler harina",
                            "Tomás muele tres sacos y salen tres de harina.",
                            costo = inv("trigo" to 3), obtiene = inv("harina" to 3), utilidad = 3
                        ),
                        BuildOption(
                            "sembrar", "Guardar para sembrar",
                            "Bruno convierte dos sacos en semillas para la próxima cosecha.",
                            costo = inv("trigo" to 2), obtiene = inv("semilla" to 4), utilidad = 4
                        ),
                        BuildOption(
                            "fiesta", "Pan para la fiesta",
                            "Cuatro sacos se van en panes para la plaza.",
                            costo = inv("trigo" to 4), obtiene = inv("pan" to 2), utilidad = 2
                        )
                    ),
                    maxSelecciones = 2
                )
            ),
            explicacionFinal = "Con cinco sacos no cabía todo. Al elegir una cosa dejaste de tener otra: " +
                "eso pasa siempre que los recursos son limitados.",
            conceptoId = ConceptId.ELECCION.name,
            dificultad = 3
        )
    )
}
