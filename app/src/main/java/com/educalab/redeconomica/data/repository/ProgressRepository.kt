package com.educalab.redeconomica.data.repository

import com.educalab.redeconomica.data.local.Mappers
import com.educalab.redeconomica.data.local.dao.ActivityDao
import com.educalab.redeconomica.data.local.dao.LabDao
import com.educalab.redeconomica.data.local.dao.ProfileDao
import com.educalab.redeconomica.data.local.dao.ProgressDao
import com.educalab.redeconomica.data.local.entity.AllocationRunEntity
import com.educalab.redeconomica.data.local.entity.ChainRunEntity
import com.educalab.redeconomica.data.local.entity.CooperationRunEntity
import com.educalab.redeconomica.data.local.entity.DecisionRunEntity
import com.educalab.redeconomica.data.local.entity.DiscoveredConceptEntity
import com.educalab.redeconomica.data.local.entity.MissionProgressEntity
import com.educalab.redeconomica.data.local.entity.ScenarioAttemptEntity
import com.educalab.redeconomica.data.local.entity.SpecializationRunEntity
import com.educalab.redeconomica.data.local.entity.TradeEntity
import com.educalab.redeconomica.data.local.entity.UserBadgeEntity
import com.educalab.redeconomica.data.local.entity.UserCollectionEntity
import com.educalab.redeconomica.data.local.entity.WarehouseEntity
import com.educalab.redeconomica.domain.engine.ProgressEngine
import com.educalab.redeconomica.domain.model.ActionCounters
import com.educalab.redeconomica.domain.model.Allocation
import com.educalab.redeconomica.domain.model.AttemptResult
import com.educalab.redeconomica.domain.model.Badge
import com.educalab.redeconomica.domain.model.CollectionItem
import com.educalab.redeconomica.domain.model.CooperationOutcome
import com.educalab.redeconomica.domain.model.CooperationPlan
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.MissionDef
import com.educalab.redeconomica.domain.model.ModuleState
import com.educalab.redeconomica.domain.model.ProductionOutcome
import com.educalab.redeconomica.domain.model.ProgressSummary
import com.educalab.redeconomica.domain.model.Scenario
import com.educalab.redeconomica.domain.model.SpecializationPlan
import com.educalab.redeconomica.domain.model.TradeOffer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Estado visible de una misión en el mapa del Valle. */
data class MissionStatus(
    val mision: MissionDef,
    val estado: ModuleState,
    val completados: Int,
    val total: Int
) {
    val porcentaje: Int get() = if (total == 0) 0 else completados * 100 / total
}

/** Lo que se ha desbloqueado justo ahora, para poder celebrarlo en pantalla. */
data class ProgressUpdate(
    val misionCompletada: MissionDef?,
    val nuevasInsignias: List<Badge>,
    val nuevosObjetos: List<CollectionItem>,
    val conceptoDescubierto: String?
) {
    val hayAlgoQueCelebrar: Boolean
        get() = misionCompletada != null || nuevasInsignias.isNotEmpty() || nuevosObjetos.isNotEmpty()
}

/**
 * Progreso del niño en el Valle.
 *
 * Todos los números salen de contar filas reales: intercambios registrados,
 * repartos guardados, misiones terminadas. No existe ningún campo "puntuación"
 * que se pueda inflar.
 */
class ProgressRepository(
    private val progressDao: ProgressDao,
    private val activityDao: ActivityDao,
    private val labDao: LabDao,
    private val profileDao: ProfileDao,
    private val catalogo: CatalogRepository,
    private val motor: ProgressEngine = ProgressEngine()
) {

    // ------------------------------------------------------------- contadores

    val contadoresFlow: Flow<ActionCounters> = combine(
        combine(
            activityDao.intercambiosAceptadosFlow(),
            activityDao.intercambiosPropuestosFlow(),
            activityDao.especializacionesFlow(),
            activityDao.cooperacionesCompletadasFlow(),
            activityDao.repartosResueltosFlow()
        ) { a, b, c, d, e -> listOf(a, b, c, d, e) },
        combine(
            activityDao.decisionesFlow(),
            activityDao.cadenasFlow(),
            labDao.numeroExperimentosFlow(),
            progressDao.misionesCompletadasFlow(),
            progressDao.numeroConceptosFlow()
        ) { a, b, c, d, e -> listOf(a, b, c, d, e) },
        progressDao.numeroObjetosFlow()
    ) { primeros, segundos, objetos ->
        ActionCounters(
            intercambiosAceptados = primeros[0],
            intercambiosPropuestos = primeros[1],
            especializacionesProbadas = primeros[2],
            cooperacionesCompletadas = primeros[3],
            repartosResueltos = primeros[4],
            decisionesTomadas = segundos[0],
            cadenasOrdenadas = segundos[1],
            experimentosRealizados = segundos[2],
            misionesCompletadas = segundos[3],
            conceptosDescubiertos = segundos[4],
            objetosDesbloqueados = objetos
        )
    }

    val resumenFlow: Flow<ProgressSummary> = combine(
        contadoresFlow,
        progressDao.insigniasGanadasFlow()
    ) { contadores, insignias ->
        motor.resumen(
            contadores = contadores,
            misionesTotales = TOTAL_MISIONES,
            insigniasConseguidas = insignias.size,
            insigniasTotales = TOTAL_INSIGNIAS
        )
    }

    suspend fun contadoresAhora(): ActionCounters = contadoresFlow.first()

    // -------------------------------------------------------------- misiones

    fun estadosMisionesFlow(): Flow<List<MissionStatus>> = combine(
        catalogo.misionesFlow(),
        progressDao.progresoMisionesFlow(),
        progressDao.escenariosLogradosFlow()
    ) { misiones, progresos, logrados ->
        val completadas = progresos
            .filter { it.estado == ModuleState.COMPLETADO.name || it.estado == ModuleState.DOMINADO.name }
            .map { it.misionId }.toSet()
        val logradosSet = logrados.toSet()
        misiones.map { mision ->
            val fila = progresos.firstOrNull { it.misionId == mision.id }
            val hechos = mision.escenarios.count { it in logradosSet }
            val progresoDominio = fila?.let {
                Mappers.aDominio(it, mision.escenarios.filter { s -> s in logradosSet }.toSet())
            }
            MissionStatus(
                mision = mision,
                estado = motor.estadoDeMision(mision, progresoDominio, completadas),
                completados = hechos,
                total = mision.escenarios.size
            )
        }
    }

    fun escenariosParaRepasarFlow(): Flow<List<String>> = progressDao.escenariosParaRepasarFlow()

    fun escenariosLogradosFlow(): Flow<Set<String>> =
        progressDao.escenariosLogradosFlow().map { it.toSet() }

    // ------------------------------------------------------------- registrar

    /**
     * Guarda un intento y, si tocaba, desbloquea recompensas.
     *
     * Devuelve lo que acaba de conseguirse para que la pantalla pueda mostrarlo
     * sin volver a preguntar a la base de datos.
     */
    suspend fun registrarIntento(escenario: Scenario, resultado: AttemptResult): ProgressUpdate {
        val misionId = catalogo.misionDeEscenario(escenario.id) ?: ""
        val previos = progressDao.numeroDeIntentos(escenario.id)
        progressDao.registrarIntento(
            ScenarioAttemptEntity(
                escenarioId = escenario.id,
                misionId = misionId,
                conceptoId = escenario.conceptoId,
                logrado = resultado.logrado,
                numeroDeIntento = previos + 1,
                fechaMillis = System.currentTimeMillis()
            )
        )

        var conceptoNuevo: String? = null
        if (resultado.logrado) {
            val yaConocidos = progressDao.conceptosFlow().first().map { it.conceptoId }.toSet()
            if (escenario.conceptoId !in yaConocidos) {
                progressDao.descubrirConcepto(
                    DiscoveredConceptEntity(escenario.conceptoId, System.currentTimeMillis())
                )
                conceptoNuevo = escenario.conceptoId
            }
        }

        var misionCompletada: MissionDef? = null
        val nuevosObjetos = mutableListOf<CollectionItem>()

        if (misionId.isNotBlank()) {
            val mision = catalogo.mision(misionId)
            if (mision != null) {
                val logrados = progressDao.escenariosLogrados(misionId).toSet()
                val todosHechos = logrados.containsAll(mision.escenarios)
                val sinFallos = mision.escenarios.all { esc ->
                    progressDao.intentosDe(esc).none { !it.logrado }
                }
                val estado = when {
                    todosHechos && sinFallos -> ModuleState.DOMINADO
                    todosHechos -> ModuleState.COMPLETADO
                    logrados.isNotEmpty() -> ModuleState.INICIADO
                    else -> ModuleState.DISPONIBLE
                }
                val yaEstaba = progressDao.progresoMision(misionId)
                val estabaCompletada = yaEstaba?.estado == ModuleState.COMPLETADO.name ||
                    yaEstaba?.estado == ModuleState.DOMINADO.name

                progressDao.guardarProgresoMision(
                    MissionProgressEntity(
                        misionId = misionId,
                        estado = estado.name,
                        intentosTotales = (yaEstaba?.intentosTotales ?: 0) + 1,
                        sinFallos = sinFallos,
                        actualizadoMillis = System.currentTimeMillis()
                    )
                )

                if (todosHechos && !estabaCompletada) {
                    misionCompletada = mision
                    nuevosObjetos += desbloquearRecompensas(mision)
                }
            }
        }

        val nuevasInsignias = revisarInsignias()

        return ProgressUpdate(misionCompletada, nuevasInsignias, nuevosObjetos, conceptoNuevo)
    }

    private suspend fun desbloquearRecompensas(mision: MissionDef): List<CollectionItem> {
        val ahora = System.currentTimeMillis()
        val yaTiene = progressDao.objetosDesbloqueadosFlow().first().map { it.objetoId }.toSet()
        val catalogoObjetos = catalogo.objetosFlow().first().associateBy { it.id }
        val nuevos = mutableListOf<CollectionItem>()
        mision.recompensa.objetos.forEach { id ->
            if (id !in yaTiene) {
                progressDao.desbloquearObjeto(UserCollectionEntity(id, ahora))
                catalogoObjetos[id]?.let { item ->
                    nuevos += item
                    item.recursoId?.let { recurso -> sumarAlAlmacen(recurso, 1) }
                }
            }
        }
        return nuevos
    }

    private suspend fun sumarAlAlmacen(recursoId: String, cantidad: Int) {
        val actual = profileDao.enAlmacen(recursoId)?.cantidad ?: 0
        profileDao.guardarEnAlmacen(WarehouseEntity(recursoId, actual + cantidad))
    }

    /** Comprueba todas las insignias contra los contadores reales. */
    suspend fun revisarInsignias(): List<Badge> {
        val contadores = contadoresAhora()
        val definidas = catalogo.insigniasFlow().first()
        val yaGanadas = progressDao.idsInsigniasGanadas().toSet()
        val merecidas = motor.insigniasGanadas(definidas, contadores)
        val ahora = System.currentTimeMillis()
        val nuevas = merecidas.filter { it.id !in yaGanadas }
        nuevas.forEach { progressDao.darInsignia(UserBadgeEntity(it.id, ahora)) }
        return nuevas
    }

    // ------------------------------------------------- historial de acciones

    suspend fun registrarIntercambio(
        escenarioId: String,
        oferta: TradeOffer,
        aceptado: Boolean,
        motivo: String?
    ) {
        activityDao.registrarIntercambio(
            TradeEntity(
                escenarioId = escenarioId,
                proponenteId = oferta.proponenteId,
                receptorId = oferta.receptorId,
                entrega = Inventory.aTexto(oferta.entrega),
                pide = Inventory.aTexto(oferta.pide),
                aceptado = aceptado,
                motivo = motivo,
                fechaMillis = System.currentTimeMillis()
            )
        )
        if (aceptado) revisarInsignias()
    }

    suspend fun registrarEspecializacion(
        escenarioId: String,
        plan: SpecializationPlan,
        resultado: ProductionOutcome,
        valor: Int,
        cumplio: Boolean
    ) {
        activityDao.registrarEspecializacion(
            SpecializationRunEntity(
                escenarioId = escenarioId,
                plan = Mappers.planATexto(plan.asignaciones),
                produccionTotal = Inventory.aTexto(resultado.total),
                valorTotal = valor,
                cumplioObjetivo = cumplio,
                fechaMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun registrarCooperacion(
        escenarioId: String,
        plan: CooperationPlan,
        resultado: CooperationOutcome
    ) {
        activityDao.registrarCooperacion(
            CooperationRunEntity(
                escenarioId = escenarioId,
                plan = Mappers.planATexto(plan.asignaciones),
                resultado = resultado.resultado,
                objetivo = resultado.objetivo,
                resultadoSinCooperar = resultado.resultadoSinCooperar,
                completado = resultado.completado,
                fechaMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun registrarReparto(
        escenarioId: String,
        recursoId: String,
        disponible: Int,
        reparto: Allocation,
        valido: Boolean
    ) {
        activityDao.registrarReparto(
            AllocationRunEntity(
                escenarioId = escenarioId,
                recursoId = recursoId,
                disponible = disponible,
                reparto = Mappers.mapaATexto(reparto.porPersonaje),
                valido = valido,
                fechaMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun registrarDecision(
        escenarioId: String,
        seleccion: List<String>,
        renuncias: List<String>,
        alcanza: Boolean
    ) {
        activityDao.registrarDecision(
            DecisionRunEntity(
                escenarioId = escenarioId,
                seleccion = seleccion.joinToString("|"),
                renuncias = renuncias.joinToString("|"),
                alcanza = alcanza,
                fechaMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun registrarCadena(
        escenarioId: String,
        orden: List<String>,
        correcto: Boolean,
        aciertos: Int
    ) {
        activityDao.registrarCadena(
            ChainRunEntity(
                escenarioId = escenarioId,
                ordenPropuesto = orden.joinToString("|"),
                correcto = correcto,
                aciertosSeguidos = aciertos,
                fechaMillis = System.currentTimeMillis()
            )
        )
    }

    // ---------------------------------------------------------------- varios

    fun insigniasGanadasFlow(): Flow<Set<String>> =
        progressDao.insigniasGanadasFlow().map { filas -> filas.map { it.insigniaId }.toSet() }

    fun objetosDesbloqueadosFlow(): Flow<Set<String>> =
        progressDao.objetosDesbloqueadosFlow().map { filas -> filas.map { it.objetoId }.toSet() }

    fun conceptosDescubiertosFlow(): Flow<Set<String>> =
        progressDao.conceptosFlow().map { filas -> filas.map { it.conceptoId }.toSet() }

    fun ultimosIntercambiosFlow() = activityDao.ultimosIntercambiosFlow(20)

    companion object {
        const val TOTAL_MISIONES = 14
        const val TOTAL_INSIGNIAS = 11
    }
}
