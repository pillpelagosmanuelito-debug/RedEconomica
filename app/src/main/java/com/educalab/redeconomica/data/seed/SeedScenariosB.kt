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
import com.educalab.redeconomica.domain.model.TradeRejectReason

/**
 * Escenarios de las misiones 6 a 10: elección y costo de oportunidad,
 * cooperación, cadena de producción, cooperativa, mercado y la gran feria.
 */
internal object SeedScenariosB {

    val LISTA: List<Scenario> = listOf(

        // ------------------------------------------- M6 · La gran decisión
        Scenario(
            id = "s16",
            tipo = ActivityKind.DECISION,
            titulo = "Diez troncos",
            situacion = "Nina ha traído diez troncos del bosque. Con eso puede salir una mesa grande, " +
                "o unas cuantas cosas más pequeñas. No las dos.",
            instruccion = "Elige como mucho dos cosas y observa a qué renuncias.",
            jugador = yo(tiene = inv("madera" to 10)),
            participantes = listOf(hab("nina", tiene = inv("tabla" to 2))),
            payload = ScenarioPayload.Decision(
                BudgetCase(
                    titulo = "¿Qué construimos con diez troncos?",
                    presupuesto = inv("madera" to 10),
                    opciones = listOf(
                        BuildOption(
                            "mesa", "Una mesa grande",
                            "Ocho troncos y todo el día de trabajo. Dura años.",
                            costo = inv("madera" to 8), obtiene = inv("mesa" to 1), utilidad = 5
                        ),
                        BuildOption(
                            "silla", "Una silla",
                            "Cinco troncos. Más fácil de mover.",
                            costo = inv("madera" to 5), obtiene = inv("silla" to 1), utilidad = 3
                        ),
                        BuildOption(
                            "cesta", "Tres cestas",
                            "Tres troncos. Sin cestas se pierde media cosecha.",
                            costo = inv("madera" to 3), obtiene = inv("cesta" to 3), utilidad = 3
                        )
                    ),
                    maxSelecciones = 2
                )
            ),
            explicacionFinal = "Los troncos eran limitados. Cada cosa que elegiste te dejó sin otra: " +
                "a eso lo llamamos elegir, y siempre tiene un precio.",
            conceptoId = ConceptId.ELECCION.name,
            dificultad = 3
        ),

        Scenario(
            id = "s17",
            tipo = ActivityKind.COSTO_OPORTUNIDAD,
            titulo = "Lo que Lía deja de hacer",
            situacion = "En un turno Lía puede recoger seis manzanas o cuidar tres verduras. " +
                "Hoy se ha ido al frutal con la cesta grande.",
            instruccion = "Si dedica el turno entero a las manzanas, ¿cuántas verduras deja de conseguir?",
            jugador = yo(),
            participantes = listOf(
                hab("lia", produce = mapOf("manzana" to 6, "verdura" to 3))
            ),
            payload = ScenarioPayload.CostoOportunidad(
                personajeId = "lia",
                recursoElegido = "manzana",
                recursoRenunciado = "verdura",
                cantidadElegida = 6
            ),
            explicacionFinal = "El turno es el mismo para todo. Al elegir las manzanas, Lía renunció " +
                "a tres verduras. Eso que dejas de hacer se llama costo de oportunidad.",
            conceptoId = ConceptId.ELECCION.name,
            dificultad = 3
        ),

        Scenario(
            id = "s18",
            tipo = ActivityKind.COSTO_OPORTUNIDAD,
            titulo = "Tablas o mesas",
            situacion = "Nina puede sacar cuatro tablas en un turno, o montar dos mesas. " +
                "Le han pedido dos tablas para arreglar un tejado.",
            instruccion = "Si hace dos tablas, ¿a cuántas mesas está renunciando?",
            jugador = yo(),
            participantes = listOf(
                hab("nina", produce = mapOf("tabla" to 4, "mesa" to 2))
            ),
            payload = ScenarioPayload.CostoOportunidad(
                personajeId = "nina",
                recursoElegido = "tabla",
                recursoRenunciado = "mesa",
                cantidadElegida = 2
            ),
            explicacionFinal = "La mitad del turno en tablas es media mesa menos… y como aquí hizo " +
                "dos tablas, la renuncia fue una mesa entera.",
            conceptoId = ConceptId.ELECCION.name,
            dificultad = 4
        ),

        // ----------------------------------------------- M7 · Juntos podemos
        Scenario(
            id = "s19",
            tipo = ActivityKind.COOPERACION,
            titulo = "Tres mantas para el invierno",
            situacion = "Hacen falta tres mantas antes de que lleguen las heladas. Nadie puede " +
                "esquilar, tejer y repartir a la vez.",
            instruccion = "Coloca a cada habitante en una etapa y pulsa «Empezar el trabajo».",
            jugador = yo(),
            participantes = listOf(hab("lia"), hab("dani"), hab("rita")),
            payload = ScenarioPayload.Cooperacion(
                etapas = listOf(
                    CooperationStage(
                        "esquilar", "Esquilar las ovejas", 1,
                        "Sacar la lana del prado alto",
                        mapOf("lia" to 3, "dani" to 4, "rita" to 2)
                    ),
                    CooperationStage(
                        "tejer", "Tejer las mantas", 2,
                        "Convertir la lana en manta",
                        mapOf("lia" to 0, "dani" to 4, "rita" to 1)
                    ),
                    CooperationStage(
                        "repartir", "Llevarlas a las casas", 3,
                        "Que lleguen antes del frío",
                        mapOf("lia" to 2, "dani" to 1, "rita" to 4)
                    )
                ),
                objetivo = 3
            ),
            explicacionFinal = "Cada uno hizo una parte y salieron las tres mantas. Por separado, " +
                "haciendo los tres pasos solos, habrían salido muchas menos.",
            conceptoId = ConceptId.COOPERACION.name,
            dificultad = 3
        ),

        Scenario(
            id = "s20",
            tipo = ActivityKind.COOPERACION,
            titulo = "Nadie lleva la carreta",
            situacion = "El pan sale del horno, pero se queda en la puerta: no hay quien lo lleve al " +
                "mercado. Cuatro vecinos, tres etapas.",
            instruccion = "Reparte las tareas. Fíjate en qué etapa se queda corta.",
            jugador = yo(),
            participantes = listOf(hab("bruno"), hab("tomas"), hab("rita"), hab("emi")),
            payload = ScenarioPayload.Cooperacion(
                etapas = listOf(
                    CooperationStage(
                        "cultivar", "Cultivar el trigo", 1,
                        "Sembrar, regar y segar",
                        mapOf("bruno" to 5, "tomas" to 1, "rita" to 1, "emi" to 2)
                    ),
                    CooperationStage(
                        "hornear", "Hornear el pan", 2,
                        "Moler la harina y encender el horno",
                        mapOf("tomas" to 5, "bruno" to 1, "rita" to 0, "emi" to 1)
                    ),
                    CooperationStage(
                        "llevar", "Llevarlo al mercado", 3,
                        "Cargar la carreta y cruzar el Valle",
                        mapOf("rita" to 5, "emi" to 3, "bruno" to 1, "tomas" to 1)
                    )
                ),
                objetivo = 5
            ),
            explicacionFinal = "El resultado no es la suma de lo que hace cada uno: es lo que permite " +
                "la etapa más floja. Si nadie transporta, da igual cuánto pan salga del horno.",
            conceptoId = ConceptId.COOPERACION.name,
            dificultad = 4
        ),

        Scenario(
            id = "s21",
            tipo = ActivityKind.CADENA,
            titulo = "Del campo a la mesa",
            situacion = "Tilo ha traído los pasos del pan revueltos en la bandolera.",
            instruccion = "Arrastra los pasos hasta ponerlos en el orden correcto.",
            jugador = yo(),
            participantes = listOf(hab("bruno"), hab("tomas"), hab("rita"), hab("lia")),
            payload = ScenarioPayload.Cadena(
                ChainDef(
                    id = "cadena_pan",
                    titulo = "La cadena del pan",
                    introduccion = "Antes de que alguien muerda un pan, han trabajado cuatro personas.",
                    pasos = listOf(
                        ChainStep("c1", "Bruno siembra y siega el trigo",
                            "El grano sale del campo", ChainStage.MATERIA_PRIMA, "bruno"),
                        ChainStep("c2", "Tomás muele el trigo y saca harina",
                            "El grano se convierte en harina", ChainStage.TRANSFORMACION, "tomas"),
                        ChainStep("c3", "Tomás hornea los panes",
                            "La harina se convierte en pan", ChainStage.TRANSFORMACION, "tomas"),
                        ChainStep("c4", "Rita lleva el pan al mercado",
                            "El pan viaja hasta la plaza", ChainStage.TRANSPORTE, "rita"),
                        ChainStep("c5", "Lía cambia manzanas por pan",
                            "El pan llega a quien lo necesita", ChainStage.INTERCAMBIO, "lia")
                    ),
                    moraleja = "Cada persona se ocupa de una parte. Ninguna hace el pan entera, " +
                        "y sin embargo el pan llega."
                )
            ),
            explicacionFinal = "Un producto pasa por muchas manos. Cada una se especializa en un paso, " +
                "y por eso el Valle come pan todos los días.",
            conceptoId = ConceptId.COOPERACION.name,
            dificultad = 2
        ),

        // ------------------------------------------------ M8 · La cooperativa
        Scenario(
            id = "s22",
            tipo = ActivityKind.COOPERACION,
            titulo = "La cooperativa se pone en marcha",
            situacion = "Cuatro vecinos han decidido unir recursos para construir cuatro bancos " +
                "para la plaza. Hay cuatro etapas y cuatro personas.",
            instruccion = "Coloca a cada uno donde más aporte.",
            jugador = yo(),
            participantes = listOf(hab("nina"), hab("sofia"), hab("emi"), hab("rita")),
            payload = ScenarioPayload.Cooperacion(
                etapas = listOf(
                    CooperationStage(
                        "traer", "Traer la madera", 1, "Bajar los troncos del bosque",
                        mapOf("emi" to 4, "nina" to 4, "rita" to 2, "sofia" to 1)
                    ),
                    CooperationStage(
                        "cortar", "Cortar las tablas", 2, "Sacar tablas rectas del tronco",
                        mapOf("nina" to 5, "sofia" to 4, "emi" to 1, "rita" to 0)
                    ),
                    CooperationStage(
                        "montar", "Clavar y montar", 3, "Unir las piezas con clavos",
                        mapOf("sofia" to 5, "nina" to 2, "rita" to 1, "emi" to 1)
                    ),
                    CooperationStage(
                        "repartir", "Colocarlos en la plaza", 4, "Llevarlos y dejarlos en su sitio",
                        mapOf("rita" to 5, "emi" to 4, "nina" to 1, "sofia" to 1)
                    )
                ),
                objetivo = 4
            ),
            explicacionFinal = "Juntos consiguieron algo que ninguno podía hacer solo. Eso es una " +
                "cooperativa: cada uno aporta lo suyo y el resultado es de todos.",
            conceptoId = ConceptId.COOPERACION.name,
            dificultad = 4
        ),

        Scenario(
            id = "s23",
            tipo = ActivityKind.ESPECIALIZACION,
            titulo = "El almacén de la cooperativa",
            situacion = "La cooperativa quiere llenar el almacén antes del invierno: fruta, pan, " +
                "tela y pescado.",
            instruccion = "Cuatro habitantes, un turno. Decide en qué se especializa cada uno.",
            jugador = yo(),
            participantes = listOf(
                hab("lia", produce = mapOf("manzana" to 6, "miel" to 2)),
                hab("tomas", produce = mapOf("pan" to 5, "harina" to 4)),
                hab("dani", produce = mapOf("lana" to 5, "tela" to 4)),
                hab("emi", produce = mapOf("pescado" to 5, "cesta" to 3))
            ),
            payload = ScenarioPayload.Especializacion(
                objetivo = inv("manzana" to 6, "pan" to 5, "tela" to 4, "pescado" to 5),
                comparaConDeTodo = true
            ),
            explicacionFinal = "Fíjate en Dani: produce más lana que tela, pero lo que hacía falta " +
                "era tela. Especializarse es elegir bien, no elegir siempre el número más grande.",
            conceptoId = ConceptId.ESPECIALIZACION.name,
            dificultad = 4
        ),

        Scenario(
            id = "s24",
            tipo = ActivityKind.INTERCAMBIO,
            titulo = "Repartir lo de la cooperativa",
            situacion = "Te han dado cuatro tarros de miel por tu trabajo. Necesitas una manta y pan.",
            instruccion = "Consigue las dos cosas intercambiando la miel.",
            jugador = yo(
                tiene = inv("miel" to 4),
                necesita = listOf(nec("manta", 1, ALTA), nec("pan", 2, MEDIA))
            ),
            participantes = listOf(
                hab("dani", tiene = inv("manta" to 2, "tela" to 3), necesita = listOf(nec("miel", 2, ALTA))),
                hab("tomas", tiene = inv("pan" to 4), necesita = listOf(nec("miel", 2, ALTA)))
            ),
            payload = ScenarioPayload.Intercambio(
                objetivo = inv("manta" to 1, "pan" to 2),
                pistaCorta = "Los dos quieren miel. Reparte la tuya entre ellos."
            ),
            explicacionFinal = "Con un solo producto has cubierto dos necesidades distintas, " +
                "porque cada vecino valoraba la miel por sus propios motivos.",
            conceptoId = ConceptId.INTERCAMBIO.name,
            dificultad = 3
        ),

        // -------------------------------------------- M9 · El mercado del Valle
        Scenario(
            id = "s25",
            tipo = ActivityKind.INTERCAMBIO,
            titulo = "Día de mercado",
            situacion = "Traes cinco pescados del río. En la plaza hay tres puestos y todos " +
                "miran tu cesta.",
            instruccion = "Consigue dos telas y dos puñados de clavos.",
            jugador = yo(
                tiene = inv("pescado" to 5),
                necesita = listOf(nec("tela", 2, ALTA), nec("clavo", 2, MEDIA))
            ),
            participantes = listOf(
                hab("dani", tiene = inv("tela" to 4), necesita = listOf(nec("pescado", 2, ALTA))),
                hab("sofia", tiene = inv("clavo" to 5), necesita = listOf(nec("pescado", 2, ALTA))),
                hab("rita", tiene = inv("leche" to 3), necesita = listOf(nec("pescado", 1, MEDIA)))
            ),
            payload = ScenarioPayload.Intercambio(
                objetivo = inv("tela" to 2, "clavo" to 2),
                pistaCorta = "No tienes que cambiar todo con la misma persona."
            ),
            explicacionFinal = "En el mercado no hay un precio escrito: hay gente con necesidades " +
                "distintas. Por eso el mismo pescado vale cosas diferentes según con quién hables.",
            conceptoId = ConceptId.INTEGRACION.name,
            dificultad = 4
        ),

        Scenario(
            id = "s26",
            tipo = ActivityKind.EVALUAR_OFERTA,
            titulo = "Una manta por un clavo",
            situacion = "Sofía tiene prisa y te propone un cambio rápido. Tú necesitas clavos, " +
                "eso es cierto… pero mira bien qué te pide.",
            instruccion = "Decide si aceptas.",
            jugador = yo(
                tiene = inv("manta" to 2),
                necesita = listOf(nec("clavo", 2, ALTA))
            ),
            participantes = listOf(
                hab("sofia", tiene = inv("clavo" to 4), necesita = listOf(nec("manta", 1, MEDIA)))
            ),
            payload = ScenarioPayload.EvaluarOferta(
                oferta = TradeOffer(
                    proponenteId = "sofia",
                    receptorId = TradeOffer.ID_JUGADOR,
                    entrega = inv("clavo" to 1),
                    pide = inv("manta" to 2)
                ),
                aceptarEsLoCorrecto = false,
                motivoEsperado = TradeRejectReason.DESEQUILIBRIO
            ),
            explicacionFinal = "Necesitar algo no significa aceptar cualquier trato. Dos mantas son " +
                "muchísimo trabajo; un puñado de clavos, bastante menos. Se puede pedir otra cosa.",
            conceptoId = ConceptId.INTEGRACION.name,
            dificultad = 4
        ),

        Scenario(
            id = "s27",
            tipo = ActivityKind.ESCASEZ,
            titulo = "Tres mantas y cinco casas",
            situacion = "Solo han salido tres mantas del telar y el frío ya está en el Valle.",
            instruccion = "Reparte las tres mantas. Piensa quién no puede pasar la noche sin una.",
            jugador = yo(),
            participantes = listOf(
                hab("lia", necesita = listOf(nec("manta", 1, ALTA))),
                hab("nina", necesita = listOf(nec("manta", 1, ALTA))),
                hab("emi", necesita = listOf(nec("manta", 1, MEDIA))),
                hab("bruno", necesita = listOf(nec("manta", 2, MEDIA))),
                hab("rita", necesita = listOf(nec("manta", 1, BAJA)))
            ),
            payload = ScenarioPayload.Escasez(
                ScarcityCase(
                    recursoId = "manta",
                    disponible = 3,
                    demandas = listOf(
                        ScarcityDemand("lia", "Lía", 1, ALTA, "Su casa está en lo alto, donde más hiela"),
                        ScarcityDemand("nina", "Nina", 1, ALTA, "El taller no tiene chimenea"),
                        ScarcityDemand("emi", "Emi", 1, MEDIA, "Duerme cerca del río, pero tiene fuego"),
                        ScarcityDemand("bruno", "Bruno", 2, MEDIA, "Vive con su abuela"),
                        ScarcityDemand("rita", "Rita", 1, BAJA, "Guarda dos pieles en la carreta")
                    )
                )
            ),
            explicacionFinal = "Había varias formas correctas de repartir, pero todas empezaban por " +
                "quien peor lo iba a pasar. Repartir bien no es dar lo mismo a todos.",
            conceptoId = ConceptId.INTEGRACION.name,
            dificultad = 4
        ),

        // ------------------------------------------------ M10 · La gran feria
        Scenario(
            id = "s28",
            tipo = ActivityKind.ESPECIALIZACION,
            titulo = "Preparar la feria",
            situacion = "Mañana es la feria del Valle. Hay que llenar los puestos: fruta, verdura, " +
                "pan y tela.",
            instruccion = "Cuatro habitantes, un turno. Que no falte nada.",
            jugador = yo(),
            participantes = listOf(
                hab("lia", produce = mapOf("manzana" to 6, "miel" to 2)),
                hab("bruno", produce = mapOf("verdura" to 6, "semilla" to 4)),
                hab("tomas", produce = mapOf("pan" to 5, "harina" to 4)),
                hab("dani", produce = mapOf("tela" to 4, "lana" to 5))
            ),
            payload = ScenarioPayload.Especializacion(
                objetivo = inv("manzana" to 6, "verdura" to 6, "pan" to 5, "tela" to 4),
                comparaConDeTodo = true
            ),
            explicacionFinal = "Cuatro personas especializadas llenaron cuatro puestos en un solo turno. " +
                "Haciendo cada uno de todo no habría llegado ni para dos.",
            conceptoId = ConceptId.INTEGRACION.name,
            dificultad = 4
        ),

        Scenario(
            id = "s29",
            tipo = ActivityKind.COOPERACION,
            titulo = "Montar los puestos",
            situacion = "Los puestos de la feria no se montan solos: hay que traer tablas, armarlos " +
                "y colocar la mercancía.",
            instruccion = "Coloca a los cuatro donde más aporten. Buscamos cinco puestos.",
            jugador = yo(),
            participantes = listOf(hab("nina"), hab("sofia"), hab("emi"), hab("rita")),
            payload = ScenarioPayload.Cooperacion(
                etapas = listOf(
                    CooperationStage(
                        "tablas", "Traer las tablas", 1, "Del taller a la plaza",
                        mapOf("nina" to 4, "rita" to 3, "emi" to 2, "sofia" to 0)
                    ),
                    CooperationStage(
                        "armar", "Armar los puestos", 2, "Clavar el tablero y las patas",
                        mapOf("sofia" to 5, "nina" to 3, "emi" to 0, "rita" to 0)
                    ),
                    CooperationStage(
                        "colocar", "Colocar la mercancía", 3, "Que todo se vea bonito",
                        mapOf("emi" to 5, "rita" to 4, "nina" to 0, "sofia" to 0)
                    )
                ),
                objetivo = 5
            ),
            explicacionFinal = "Ninguno de los cuatro podía montar un puesto entero solo. " +
                "Coordinados salieron cinco.",
            conceptoId = ConceptId.INTEGRACION.name,
            dificultad = 5
        ),

        Scenario(
            id = "s30",
            tipo = ActivityKind.INTERCAMBIO,
            titulo = "Tu puesto en la feria",
            situacion = "Has tejido cuatro cestas para vender. Quieres llevarte miel y queso a casa.",
            instruccion = "Consigue dos tarros de miel y un queso.",
            jugador = yo(
                tiene = inv("cesta" to 4),
                necesita = listOf(nec("miel", 2, MEDIA), nec("queso", 1, MEDIA))
            ),
            participantes = listOf(
                hab("lia", tiene = inv("miel" to 3), necesita = listOf(nec("cesta", 2, ALTA))),
                hab("rita", tiene = inv("queso" to 2, "leche" to 3), necesita = listOf(nec("cesta", 2, ALTA)))
            ),
            payload = ScenarioPayload.Intercambio(
                objetivo = inv("miel" to 2, "queso" to 1),
                pistaCorta = "Las dos necesitan cestas para la feria. Tienes justo cuatro."
            ),
            explicacionFinal = "Produjiste algo que a otros les hacía falta, y con eso conseguiste " +
                "lo que te faltaba a ti. Así funciona un mercado.",
            conceptoId = ConceptId.INTEGRACION.name,
            dificultad = 4
        ),

        Scenario(
            id = "s31",
            tipo = ActivityKind.DECISION,
            titulo = "El presupuesto del puesto",
            situacion = "Para tu puesto tienes diez troncos y cuatro puñados de clavos. Ni uno más.",
            instruccion = "Elige como mucho dos cosas.",
            jugador = yo(tiene = inv("madera" to 10, "clavo" to 4)),
            participantes = listOf(hab("nina"), hab("sofia")),
            payload = ScenarioPayload.Decision(
                BudgetCase(
                    titulo = "¿Cómo montamos el puesto?",
                    presupuesto = inv("madera" to 10, "clavo" to 4),
                    opciones = listOf(
                        BuildOption(
                            "grande", "Mostrador grande",
                            "Se ve desde toda la plaza. Ocho troncos y tres clavos.",
                            costo = inv("madera" to 8, "clavo" to 3),
                            obtiene = inv("mesa" to 1), utilidad = 5
                        ),
                        BuildOption(
                            "bancos", "Dos bancos para la gente",
                            "Seis troncos y dos clavos. La gente se queda más rato.",
                            costo = inv("madera" to 6, "clavo" to 2),
                            obtiene = inv("silla" to 2), utilidad = 4
                        ),
                        BuildOption(
                            "cartel", "Un cartel bien visible",
                            "Dos troncos y un clavo.",
                            costo = inv("madera" to 2, "clavo" to 1),
                            obtiene = inv("tabla" to 2), utilidad = 2
                        )
                    ),
                    maxSelecciones = 2
                )
            ),
            explicacionFinal = "El mostrador grande y los bancos juntos no cabían en el presupuesto. " +
                "Elegir uno significaba quedarse sin el otro.",
            conceptoId = ConceptId.ELECCION.name,
            dificultad = 4
        ),

        Scenario(
            id = "s32",
            tipo = ActivityKind.ESCASEZ,
            titulo = "Faltan cestas en la feria",
            situacion = "Han quedado cinco cestas libres y cuatro puestos las piden. Entre todos " +
                "necesitarían siete.",
            instruccion = "Reparte las cinco cestas.",
            jugador = yo(),
            participantes = listOf(
                hab("lia", necesita = listOf(nec("cesta", 2, ALTA))),
                hab("bruno", necesita = listOf(nec("cesta", 2, ALTA))),
                hab("emi", necesita = listOf(nec("cesta", 1, MEDIA))),
                hab("rita", necesita = listOf(nec("cesta", 2, MEDIA)))
            ),
            payload = ScenarioPayload.Escasez(
                ScarcityCase(
                    recursoId = "cesta",
                    disponible = 5,
                    demandas = listOf(
                        ScarcityDemand("lia", "Lía", 2, ALTA, "Sin cestas la fruta se machaca"),
                        ScarcityDemand("bruno", "Bruno", 2, ALTA, "Las verduras no aguantan sueltas"),
                        ScarcityDemand("emi", "Emi", 1, MEDIA, "El pescado va sobre hielo"),
                        ScarcityDemand("rita", "Rita", 2, MEDIA, "Para cargar la carreta")
                    )
                )
            ),
            explicacionFinal = "Cinco cestas para siete peticiones. Otra vez escasez, y otra vez " +
                "más de una manera razonable de repartir.",
            conceptoId = ConceptId.INTEGRACION.name,
            dificultad = 4
        )
    )
}
