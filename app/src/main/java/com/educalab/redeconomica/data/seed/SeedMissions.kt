package com.educalab.redeconomica.data.seed

import com.educalab.redeconomica.domain.model.ConceptId
import com.educalab.redeconomica.domain.model.MissionDef
import com.educalab.redeconomica.domain.model.MissionReward
import com.educalab.redeconomica.domain.model.ValleyPlace

/**
 * Las Misiones del Valle.
 *
 * La progresión no es "ejercicio 4 de 20": es una historia pequeña donde cada
 * misión ocurre en un sitio del pueblo y desbloquea una zona nueva.
 */
object SeedMissions {

    val TODAS: List<MissionDef> = listOf(
        MissionDef(
            id = "m01", numero = 1, titulo = "La primera cosecha",
            lugar = ValleyPlace.GRANJA, concepto = ConceptId.NECESIDADES,
            narrativaInicio = "Acabas de llegar al Valle. Tilo, el zorro cartero, te deja una nota: " +
                "«Empieza por la granja de Lía. Verás que aquí nadie necesita lo mismo».",
            narrativaFinal = "Ya sabes lo primero: cada vecino necesita cosas distintas, y para " +
                "conseguirlas alguien tiene que producirlas.",
            objetivoVisible = "Ayuda a Lía a recoger y a repartir la primera cosecha",
            escenarios = listOf("s01", "s02", "s03"),
            recompensa = MissionReward(
                objetos = listOf("col_manzana", "col_verdura"),
                zonaDesbloqueada = ValleyPlace.MERCADO,
                sellos = 3
            )
        ),
        MissionDef(
            id = "m02", numero = 2, titulo = "¿Quién tiene lo que necesito?",
            lugar = ValleyPlace.MERCADO, concepto = ConceptId.INTERCAMBIO,
            narrativaInicio = "«Tienes manzanas y te falta pan», dice Tilo. «En el Valle eso tiene " +
                "arreglo. Baja a la plaza».",
            narrativaFinal = "Has hecho tu primer trueque. No hiciste el pan: lo conseguiste dando " +
                "algo que a otro le faltaba.",
            objetivoVisible = "Consigue pan y herramientas sin producirlos tú",
            escenarios = listOf("s04", "s05", "s06"),
            recompensa = MissionReward(
                objetos = listOf("col_pan", "col_herramienta"),
                insigniaId = "primer_trato",
                zonaDesbloqueada = ValleyPlace.TALLER,
                sellos = 3
            ),
            requiereMision = "m01"
        ),
        MissionDef(
            id = "m03", numero = 3, titulo = "Cada uno a lo suyo",
            lugar = ValleyPlace.TALLER, concepto = ConceptId.ESPECIALIZACION,
            narrativaInicio = "En el Valle todos intentan hacer un poco de todo, y todo sale a medias. " +
                "Sofía cree que hay otra manera.",
            narrativaFinal = "Concentrarse en una sola tarea produce más que repartirse entre varias. " +
                "Eso se llama especializarse.",
            objetivoVisible = "Descubre qué pasa cuando cada uno hace una sola cosa",
            escenarios = listOf("s07", "s08", "s09"),
            recompensa = MissionReward(
                objetos = listOf("col_tabla", "col_clavo"),
                insigniaId = "especialista",
                zonaDesbloqueada = ValleyPlace.PLAZA,
                sellos = 4
            ),
            requiereMision = "m02"
        ),
        MissionDef(
            id = "m04", numero = 4, titulo = "Intercambio de vecinos",
            lugar = ValleyPlace.PLAZA, concepto = ConceptId.INTERCAMBIO,
            narrativaInicio = "La plaza se ha llenado. Hoy no basta con un trato: hacen falta varios, " +
                "y no todo el mundo puede desprenderse de lo suyo.",
            narrativaFinal = "Un intercambio depende de lo que cada persona necesita. Lo que a uno le " +
                "sobra, a otro le hace falta… y al revés.",
            objetivoVisible = "Resuelve varias necesidades en la misma tarde",
            escenarios = listOf("s10", "s11", "s12"),
            recompensa = MissionReward(
                objetos = listOf("col_tela"),
                zonaDesbloqueada = ValleyPlace.PANADERIA,
                sellos = 4
            ),
            requiereMision = "m03"
        ),
        MissionDef(
            id = "m05", numero = 5, titulo = "El problema del pan",
            lugar = ValleyPlace.PANADERIA, concepto = ConceptId.RECURSOS,
            narrativaInicio = "El horno se apagó de madrugada. Solo hay tres panes y en la plaza " +
                "esperan cinco vecinos.",
            narrativaFinal = "Cuando hay menos de lo que hace falta, decimos que hay escasez. " +
                "Y entonces hay que decidir.",
            objetivoVisible = "Reparte lo poco que hay sin dejar a nadie tirado",
            escenarios = listOf("s13", "s14", "s15"),
            recompensa = MissionReward(
                objetos = listOf("col_trigo", "col_harina"),
                insigniaId = "maestro_recursos",
                zonaDesbloqueada = ValleyPlace.CARPINTERIA,
                sellos = 4
            ),
            requiereMision = "m04"
        ),
        MissionDef(
            id = "m06", numero = 6, titulo = "La gran decisión",
            lugar = ValleyPlace.CARPINTERIA, concepto = ConceptId.ELECCION,
            narrativaInicio = "Nina ha traído diez troncos. Con eso sale una mesa grande… o varias " +
                "cosas pequeñas. No las dos.",
            narrativaFinal = "Elegir una cosa es renunciar a otra. A eso se le llama costo de " +
                "oportunidad, y aparece en cada decisión.",
            objetivoVisible = "Aprende a mirar lo que dejas de hacer",
            escenarios = listOf("s16", "s17", "s18"),
            recompensa = MissionReward(
                objetos = listOf("col_mesa", "col_silla"),
                insigniaId = "experto_decisiones",
                zonaDesbloqueada = ValleyPlace.TELAR,
                sellos = 5
            ),
            requiereMision = "m05"
        ),
        MissionDef(
            id = "m07", numero = 7, titulo = "Juntos podemos",
            lugar = ValleyPlace.TELAR, concepto = ConceptId.COOPERACION,
            narrativaInicio = "Se acerca el frío y hacen falta mantas. Nadie puede esquilar, tejer y " +
                "repartir a la vez.",
            narrativaFinal = "Lo que sale al final no es la suma de lo que hace cada uno: es lo que " +
                "permite la etapa más floja.",
            objetivoVisible = "Organiza el trabajo en equipo del Valle",
            escenarios = listOf("s19", "s20", "s21"),
            recompensa = MissionReward(
                objetos = listOf("col_lana", "col_manta"),
                insigniaId = "constructor_equipos",
                zonaDesbloqueada = ValleyPlace.COOPERATIVA,
                sellos = 5
            ),
            requiereMision = "m06"
        ),
        MissionDef(
            id = "m08", numero = 8, titulo = "La cooperativa",
            lugar = ValleyPlace.COOPERATIVA, concepto = ConceptId.COOPERACION,
            narrativaInicio = "Cuatro vecinos han puesto sus recursos en común. Ahora hay que " +
                "organizarse de verdad.",
            narrativaFinal = "Juntos consiguieron algo que ninguno podía hacer solo. Eso es una " +
                "cooperativa.",
            objetivoVisible = "Pon en marcha la cooperativa del Valle",
            escenarios = listOf("s22", "s23", "s24"),
            recompensa = MissionReward(
                objetos = listOf("col_miel", "col_cesta"),
                zonaDesbloqueada = ValleyPlace.CENTRO_INTERCAMBIO,
                sellos = 5
            ),
            requiereMision = "m07"
        ),
        MissionDef(
            id = "m09", numero = 9, titulo = "El mercado del Valle",
            lugar = ValleyPlace.MERCADO, concepto = ConceptId.INTEGRACION,
            narrativaInicio = "Día de mercado. Hay puestos, prisas y alguna propuesta que no es tan " +
                "buena como parece.",
            narrativaFinal = "En el mercado no hay precios escritos: hay personas con necesidades " +
                "distintas poniéndose de acuerdo.",
            objetivoVisible = "Muévete en el mercado sin que te la cuelen",
            escenarios = listOf("s25", "s26", "s27"),
            recompensa = MissionReward(
                objetos = listOf("col_pescado", "col_queso"),
                insigniaId = "gran_negociador",
                zonaDesbloqueada = ValleyPlace.LABORATORIO,
                sellos = 6
            ),
            requiereMision = "m08"
        ),
        MissionDef(
            id = "m10", numero = 10, titulo = "La gran feria",
            lugar = ValleyPlace.PLAZA, concepto = ConceptId.INTEGRACION,
            narrativaInicio = "Una vez al año el Valle entero se junta en la plaza. Hay que producir, " +
                "montar, repartir, decidir e intercambiar. Todo en dos días.",
            narrativaFinal = "Has puesto en marcha una feria entera. Producción, especialización, " +
                "cooperación, escasez y decisiones: todo junto.",
            objetivoVisible = "Prepara la feria del Valle de principio a fin",
            escenarios = listOf("s28", "s29", "s30", "s31", "s32"),
            recompensa = MissionReward(
                objetos = listOf("col_leche", "col_semilla", "col_puesto"),
                insigniaId = "cooperador_valle",
                zonaDesbloqueada = ValleyPlace.HUERTO,
                sellos = 8
            ),
            requiereMision = "m09"
        ),
        MissionDef(
            id = "m11", numero = 11, titulo = "El taller de los oficios",
            lugar = ValleyPlace.TALLER, concepto = ConceptId.ESPECIALIZACION,
            narrativaInicio = "«Lía es mejor que Bruno en todo», dice alguien en el taller. " +
                "Tilo levanta una ceja: «¿Seguro que eso lo decide todo?».",
            narrativaFinal = "No siempre produce una cosa quien mejor la hace, sino quien menos " +
                "pierde al dedicarse a ella.",
            objetivoVisible = "Descubre por qué conviene repartirse el trabajo igualmente",
            escenarios = listOf("s33", "s34", "s35"),
            recompensa = MissionReward(
                objetos = listOf("col_martillo"),
                sellos = 6
            ),
            requiereMision = "m10"
        ),
        MissionDef(
            id = "m12", numero = 12, titulo = "El invierno se acerca",
            lugar = ValleyPlace.HUERTO, concepto = ConceptId.ELECCION,
            narrativaInicio = "Las primeras heladas están al caer. Queda poca lana, poca madera " +
                "y ninguna semana de sobra.",
            narrativaFinal = "El Valle pasó el invierno porque alguien decidió a tiempo qué hacer " +
                "primero.",
            objetivoVisible = "Prepara el Valle para el frío con lo poco que queda",
            escenarios = listOf("s36", "s37", "s38"),
            recompensa = MissionReward(
                objetos = listOf("col_farol"),
                sellos = 6
            ),
            requiereMision = "m11"
        ),
        MissionDef(
            id = "m13", numero = 13, titulo = "Las cadenas del Valle",
            lugar = ValleyPlace.CENTRO_INTERCAMBIO, concepto = ConceptId.COOPERACION,
            narrativaInicio = "En el Centro de Intercambio guardan un mural con todos los caminos " +
                "que recorre un producto. Está desordenado.",
            narrativaFinal = "Detrás de cada cosa hay una cadena de personas, y cada una se ocupa " +
                "de un trozo.",
            objetivoVisible = "Reconstruye las cadenas de producción del Valle",
            escenarios = listOf("s39", "s40"),
            recompensa = MissionReward(
                objetos = listOf("col_mural"),
                sellos = 5
            ),
            requiereMision = "m12"
        ),
        MissionDef(
            id = "m14", numero = 14, titulo = "El Valle entero",
            lugar = ValleyPlace.PLAZA, concepto = ConceptId.INTEGRACION,
            narrativaInicio = "Último encargo de Tilo antes de cerrar el correo del año. " +
                "Un turno, cinco oficios y tres cosas por resolver.",
            narrativaFinal = "Llegaste sin saber qué era un intercambio y te vas organizando una " +
                "economía entera. Ah, y eso que has estado haciendo… se llama economía.",
            objetivoVisible = "Haz funcionar el Valle entero por tu cuenta",
            escenarios = listOf("s41", "s42"),
            recompensa = MissionReward(
                objetos = listOf("col_bandolera"),
                insigniaId = "maestro_economia",
                sellos = 10
            ),
            requiereMision = "m13"
        )
    )

    val PORID: Map<String, MissionDef> = TODAS.associateBy { it.id }

    fun deEscenario(escenarioId: String): MissionDef? =
        TODAS.firstOrNull { escenarioId in it.escenarios }
}
