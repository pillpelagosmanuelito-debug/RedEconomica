package com.educalab.redeconomica.domain.engine

import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.Need
import com.educalab.redeconomica.domain.model.ResourceDef
import com.educalab.redeconomica.domain.model.TradeEvaluation
import com.educalab.redeconomica.domain.model.TradeOffer
import com.educalab.redeconomica.domain.model.TradeRejectReason
import com.educalab.redeconomica.domain.model.Urgency

/**
 * Motor de intercambio.
 *
 * Reglas, en orden, para que un habitante acepte una propuesta:
 *
 *  1. La propuesta debe estar completa (algo por algo).
 *  2. Quien propone debe TENER lo que entrega.
 *  3. Quien recibe debe TENER lo que se le pide.
 *  4. Lo que se le ofrece debe servirle para alguna necesidad suya.
 *  5. No debe quedarse sin algo que necesita con urgencia alta.
 *  6. No debe entregar mucho más valor del que recibe.
 *  7. (Opcional) Quien propone también debe salir ganando algo.
 *
 * El motor NUNCA toca la base de datos ni la interfaz: recibe personajes,
 * devuelve personajes nuevos. Toda la aritmética de inventarios pasa por
 * [Inventory], que impide cantidades negativas.
 */
class TradeEngine(
    private val catalogo: Map<String, ResourceDef>,
    /** Tolerancia: se acepta si recibe al menos el 60 % del valor que entrega. */
    private val toleranciaPorcentaje: Int = 60
) {

    init {
        require(toleranciaPorcentaje in 1..100) { "Tolerancia fuera de rango" }
    }

    fun evaluar(
        oferta: TradeOffer,
        proponente: EconomicCharacter,
        receptor: EconomicCharacter,
        exigirBeneficioMutuo: Boolean = true
    ): TradeEvaluation {

        require(proponente.id == oferta.proponenteId) { "El proponente no coincide con la oferta" }
        require(receptor.id == oferta.receptorId) { "El receptor no coincide con la oferta" }
        require(proponente.id != receptor.id) { "Nadie puede intercambiar consigo mismo" }

        if (oferta.esVacia) {
            return rechazo(
                oferta, TradeRejectReason.OFERTA_VACIA,
                "Todavía falta algo en la propuesta.",
                "Elige qué entregas y qué pides a cambio."
            )
        }

        if (!proponente.inventario.contiene(oferta.entrega)) {
            return rechazo(
                oferta, TradeRejectReason.SIN_RECURSOS_PROPONENTE,
                "No tienes todo lo que quieres entregar.",
                "Revisa tu cesta: ofreces ${desc(oferta.entrega)}."
            )
        }

        if (!receptor.inventario.contiene(oferta.pide)) {
            return rechazo(
                oferta, TradeRejectReason.SIN_RECURSOS_RECEPTOR,
                "${receptor.nombre} no tiene ${desc(oferta.pide)}.",
                "Mira qué tiene realmente ${receptor.nombre} antes de pedir."
            )
        }

        val leSirve = oferta.entrega.recursos.any { receptor.faltante(it) > 0 }
        if (!leSirve) {
            val pendiente = receptor.necesidadesPendientes().firstOrNull()
            return rechazo(
                oferta, TradeRejectReason.NO_NECESITA_LO_OFRECIDO,
                "${receptor.nombre} no necesita ${desc(oferta.entrega)}.",
                if (pendiente != null)
                    "Fíjate en lo que sí le hace falta: ${etiqueta(pendiente)}."
                else
                    "Ahora mismo ${receptor.nombre} lo tiene todo cubierto."
            )
        }

        val receptorTras = receptor.inventario.menos(oferta.pide)!!.mas(oferta.entrega)
        val urgenteRota = receptor.necesidades.firstOrNull { need ->
            need.urgencia == Urgency.ALTA &&
                receptor.inventario.cantidad(need.recursoId) >= need.cantidad &&
                receptorTras.cantidad(need.recursoId) < need.cantidad
        }
        if (urgenteRota != null) {
            return rechazo(
                oferta, TradeRejectReason.PERDERIA_LO_QUE_NECESITA,
                "${receptor.nombre} se quedaría sin ${nombreRecurso(urgenteRota.recursoId)}, " +
                    "y es justo lo que más necesita.",
                "Pídele algo que le sobre, no aquello que necesita."
            )
        }

        val valorEntrega = oferta.entrega.valor(catalogo)
        val valorPedido = oferta.pide.valor(catalogo)
        if (valorEntrega * 100 < valorPedido * toleranciaPorcentaje) {
            return rechazo(
                oferta, TradeRejectReason.DESEQUILIBRIO,
                "${receptor.nombre} cree que entrega demasiado a cambio de tan poco.",
                "Prueba a ofrecer un poco más, o a pedir un poco menos."
            )
        }

        if (exigirBeneficioMutuo) {
            val teSirve = oferta.pide.recursos.any { proponente.faltante(it) > 0 }
            if (!teSirve) {
                return rechazo(
                    oferta, TradeRejectReason.SIN_BENEFICIO_MUTUO,
                    "Ese intercambio no te resuelve nada a ti.",
                    "Un intercambio funciona cuando las dos partes consiguen algo que les hace falta."
                )
            }
        }

        val proponenteTras = proponente.conInventario(
            proponente.inventario.menos(oferta.entrega)!!.mas(oferta.pide)
        )
        val receptorFinal = receptor.conInventario(receptorTras)

        return TradeEvaluation.Aceptado(
            oferta = oferta,
            proponenteActualizado = proponenteTras,
            receptorActualizado = receptorFinal,
            mensaje = "¡Trato hecho! ${receptor.nombre} acepta el intercambio.",
            loQueGanaCadaUno = "Tú consigues ${desc(oferta.pide)}. " +
                "${receptor.nombre} consigue ${desc(oferta.entrega)}, que es lo que le faltaba."
        )
    }

    /** Aplica una evaluación aceptada y devuelve los dos personajes actualizados. */
    fun aplicar(evaluacion: TradeEvaluation.Aceptado): Pair<EconomicCharacter, EconomicCharacter> =
        evaluacion.proponenteActualizado to evaluacion.receptorActualizado

    /**
     * Busca propuestas viables entre [quien] y cada uno de [otros].
     *
     * Se usa para tres cosas: validar que un escenario tiene solución, dar
     * pistas en el Mercado, y comprobar en las pruebas que existen escenarios
     * con MÁS DE UNA solución válida.
     */
    fun buscarIntercambios(
        quien: EconomicCharacter,
        otros: List<EconomicCharacter>,
        maxUnidades: Int = 6,
        exigirBeneficioMutuo: Boolean = true
    ): List<TradeOffer> {
        val encontrados = mutableListOf<TradeOffer>()
        for (otro in otros) {
            if (otro.id == quien.id) continue
            val loQueQuiero = otro.inventario.recursos.filter { quien.faltante(it) > 0 }
            val loQueOfrezco = quien.inventario.recursos.filter { otro.faltante(it) > 0 }
            for (pedido in loQueQuiero.sorted()) {
                val maxPedido = minOf(
                    otro.inventario.cantidad(pedido),
                    quien.faltante(pedido),
                    maxUnidades
                )
                for (qPide in 1..maxPedido) {
                    for (entregado in loQueOfrezco.sorted()) {
                        if (entregado == pedido) continue
                        val maxEntrega = minOf(
                            quien.inventario.cantidad(entregado),
                            maxUnidades
                        )
                        for (qDa in 1..maxEntrega) {
                            val oferta = TradeOffer(
                                quien.id, otro.id,
                                Inventory.of(entregado to qDa),
                                Inventory.of(pedido to qPide)
                            )
                            val res = evaluar(oferta, quien, otro, exigirBeneficioMutuo)
                            if (res is TradeEvaluation.Aceptado) encontrados += oferta
                        }
                    }
                }
            }
        }
        return encontrados
    }

    private fun rechazo(
        oferta: TradeOffer,
        motivo: TradeRejectReason,
        mensaje: String,
        pista: String
    ) = TradeEvaluation.Rechazado(oferta, motivo, mensaje, pista)

    private fun desc(inv: Inventory) = inv.descripcion(catalogo)

    private fun nombreRecurso(id: String) = catalogo[id]?.plural ?: id

    private fun etiqueta(need: Need): String {
        val def = catalogo[need.recursoId]
        return def?.etiquetaCantidad(need.cantidad) ?: "${need.cantidad} ${need.recursoId}"
    }
}
