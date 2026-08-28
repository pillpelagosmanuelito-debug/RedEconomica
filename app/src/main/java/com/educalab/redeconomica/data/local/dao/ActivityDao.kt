package com.educalab.redeconomica.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.educalab.redeconomica.data.local.entity.AllocationRunEntity
import com.educalab.redeconomica.data.local.entity.ChainRunEntity
import com.educalab.redeconomica.data.local.entity.CooperationRunEntity
import com.educalab.redeconomica.data.local.entity.DecisionRunEntity
import com.educalab.redeconomica.data.local.entity.SpecializationRunEntity
import com.educalab.redeconomica.data.local.entity.TradeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Historial de acciones económicas.
 *
 * Cada intercambio, cada reparto de trabajo y cada decisión se guarda aquí.
 * Los contadores del perfil se calculan con estas consultas: no hay ningún
 * número inventado.
 */
@Dao
interface ActivityDao {

    @Insert
    suspend fun registrarIntercambio(fila: TradeEntity): Long

    @Query("SELECT * FROM trades ORDER BY fechaMillis DESC LIMIT :limite")
    fun ultimosIntercambiosFlow(limite: Int): Flow<List<TradeEntity>>

    @Query("SELECT COUNT(*) FROM trades WHERE aceptado = 1")
    fun intercambiosAceptadosFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trades")
    fun intercambiosPropuestosFlow(): Flow<Int>

    @Insert
    suspend fun registrarEspecializacion(fila: SpecializationRunEntity): Long

    @Query("SELECT COUNT(*) FROM specialization_runs")
    fun especializacionesFlow(): Flow<Int>

    @Query("SELECT * FROM specialization_runs WHERE escenarioId = :escenarioId ORDER BY fechaMillis")
    suspend fun especializacionesDe(escenarioId: String): List<SpecializationRunEntity>

    @Insert
    suspend fun registrarCooperacion(fila: CooperationRunEntity): Long

    @Query("SELECT COUNT(*) FROM cooperation_runs WHERE completado = 1")
    fun cooperacionesCompletadasFlow(): Flow<Int>

    @Insert
    suspend fun registrarReparto(fila: AllocationRunEntity): Long

    @Query("SELECT COUNT(*) FROM allocation_runs WHERE valido = 1")
    fun repartosResueltosFlow(): Flow<Int>

    @Insert
    suspend fun registrarDecision(fila: DecisionRunEntity): Long

    @Query("SELECT COUNT(*) FROM decision_runs WHERE alcanza = 1")
    fun decisionesFlow(): Flow<Int>

    @Insert
    suspend fun registrarCadena(fila: ChainRunEntity): Long

    @Query("SELECT COUNT(*) FROM chain_runs WHERE correcto = 1")
    fun cadenasFlow(): Flow<Int>

    @Query("DELETE FROM trades")
    suspend fun borrarIntercambios()

    @Query("DELETE FROM specialization_runs")
    suspend fun borrarEspecializaciones()

    @Query("DELETE FROM cooperation_runs")
    suspend fun borrarCooperaciones()

    @Query("DELETE FROM allocation_runs")
    suspend fun borrarRepartos()

    @Query("DELETE FROM decision_runs")
    suspend fun borrarDecisiones()

    @Query("DELETE FROM chain_runs")
    suspend fun borrarCadenas()
}
