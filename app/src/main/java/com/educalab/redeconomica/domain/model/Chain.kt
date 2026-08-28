package com.educalab.redeconomica.domain.model

/** Momentos por los que pasa un producto antes de llegar a quien lo necesita. */
enum class ChainStage(val orden: Int, val etiqueta: String, val explicacion: String) {
    MATERIA_PRIMA(1, "De dónde sale", "Alguien obtiene el material del campo, del río o del bosque"),
    TRANSFORMACION(2, "Cómo se transforma", "Alguien lo convierte en otra cosa"),
    TRANSPORTE(3, "Cómo llega", "Alguien lo lleva hasta donde hace falta"),
    INTERCAMBIO(4, "Quién lo recibe", "Alguien lo cambia por lo que necesita")
}

/** Un paso concreto de una cadena de producción. */
data class ChainStep(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val etapa: ChainStage,
    val personajeId: String?
)

/** Una cadena completa, guardada ya en el orden correcto. */
data class ChainDef(
    val id: String,
    val titulo: String,
    val introduccion: String,
    val pasos: List<ChainStep>,
    val moraleja: String
) {
    init {
        require(pasos.size >= 3) { "Una cadena necesita al menos 3 pasos" }
    }

    val ordenCorrecto: List<String> get() = pasos.map { it.id }
}

/** Resultado de ordenar una cadena. */
data class ChainResult(
    val correcto: Boolean,
    val primerErrorEn: Int,
    val aciertosSeguidos: Int,
    val mensaje: String,
    val explicacion: String
)
