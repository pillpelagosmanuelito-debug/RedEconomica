package com.educalab.redeconomica.data.repository

import com.educalab.redeconomica.data.local.dao.LabDao
import com.educalab.redeconomica.data.local.entity.DailyChallengeEntity
import com.educalab.redeconomica.data.local.entity.ExperimentEntity
import com.educalab.redeconomica.data.seed.SeedContent
import com.educalab.redeconomica.domain.engine.DailyChallenge
import com.educalab.redeconomica.domain.engine.DailyChallengeGenerator
import com.educalab.redeconomica.domain.engine.LabConfig
import com.educalab.redeconomica.domain.engine.LabMode
import com.educalab.redeconomica.domain.engine.LabRun
import com.educalab.redeconomica.domain.model.Inventory
import kotlinx.coroutines.flow.Flow

/**
 * Laboratorio del Valle y reto del día.
 *
 * Los experimentos se guardan de verdad: por eso se pueden comparar dos
 * ejecuciones separadas por días.
 */
class LabRepository(
    private val dao: LabDao,
    private val generador: DailyChallengeGenerator = DailyChallengeGenerator()
) {

    fun ultimosExperimentosFlow(limite: Int = 10): Flow<List<ExperimentEntity>> =
        dao.ultimosExperimentosFlow(limite)

    fun numeroExperimentosFlow(): Flow<Int> = dao.numeroExperimentosFlow()

    suspend fun guardarExperimento(etiqueta: String, run: LabRun): Long =
        dao.guardarExperimento(
            ExperimentEntity(
                etiqueta = etiqueta,
                habitantes = run.config.habitantes,
                turnos = run.config.turnos,
                modo = run.config.modo.name,
                permiteIntercambio = run.config.permiteIntercambio,
                ponenEnComun = run.config.ponenEnComun,
                produccionTotal = Inventory.aTexto(run.produccionTotal),
                valorTotal = run.valorTotal,
                intercambios = run.intercambiosRealizados,
                necesidadesCubiertas = run.necesidadesCubiertas,
                necesidadesTotales = run.necesidadesTotales,
                fechaMillis = System.currentTimeMillis()
            )
        )

    suspend fun dosUltimos(): List<ExperimentEntity> = dao.dosUltimosExperimentos()

    fun configuracionDe(fila: ExperimentEntity): LabConfig = LabConfig(
        habitantes = fila.habitantes,
        turnos = fila.turnos,
        modo = LabMode.valueOf(fila.modo),
        permiteIntercambio = fila.permiteIntercambio,
        ponenEnComun = fila.ponenEnComun
    )

    // ------------------------------------------------------- reto del día

    suspend fun retoDeHoy(ahoraMillis: Long = System.currentTimeMillis()): DailyChallenge? {
        val dia = generador.indiceDeDia(ahoraMillis)
        val guardado = dao.retoDelDia(dia)
        if (guardado != null) {
            val esc = SeedContent.ESCENARIOS_PORID[guardado.escenarioId] ?: return null
            return DailyChallenge(
                diaIndice = dia,
                escenarioId = esc.id,
                tipoEtiqueta = esc.tipo.etiqueta,
                titulo = esc.titulo,
                invitacion = "Sigue con el reto de hoy."
            )
        }
        val reto = generador.retoDe(dia, SeedContent.RETOS_DIARIOS) ?: return null
        dao.guardarRetoDelDia(
            DailyChallengeEntity(
                diaIndice = dia,
                escenarioId = reto.escenarioId,
                completado = false,
                fechaMillis = ahoraMillis
            )
        )
        return reto
    }

    suspend fun estaCompletadoElRetoDeHoy(ahoraMillis: Long = System.currentTimeMillis()): Boolean =
        dao.retoDelDia(generador.indiceDeDia(ahoraMillis))?.completado == true

    suspend fun marcarRetoCompletado(ahoraMillis: Long = System.currentTimeMillis()) {
        dao.marcarRetoCompletado(generador.indiceDeDia(ahoraMillis))
    }

    fun retosCompletadosFlow(): Flow<Int> = dao.retosCompletadosFlow()
}
