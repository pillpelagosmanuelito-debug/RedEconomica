package com.educalab.redeconomica.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.educalab.redeconomica.data.local.entity.DailyChallengeEntity
import com.educalab.redeconomica.data.local.entity.ExperimentEntity
import kotlinx.coroutines.flow.Flow

/** Laboratorio del Valle y reto económico del día. */
@Dao
interface LabDao {

    @Insert
    suspend fun guardarExperimento(fila: ExperimentEntity): Long

    @Query("SELECT * FROM experiments ORDER BY fechaMillis DESC LIMIT :limite")
    fun ultimosExperimentosFlow(limite: Int): Flow<List<ExperimentEntity>>

    @Query("SELECT * FROM experiments ORDER BY fechaMillis DESC LIMIT 2")
    suspend fun dosUltimosExperimentos(): List<ExperimentEntity>

    @Query("SELECT COUNT(*) FROM experiments")
    fun numeroExperimentosFlow(): Flow<Int>

    @Query("DELETE FROM experiments")
    suspend fun borrarExperimentos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarRetoDelDia(fila: DailyChallengeEntity)

    @Query("SELECT * FROM daily_challenge WHERE diaIndice = :dia")
    suspend fun retoDelDia(dia: Long): DailyChallengeEntity?

    @Query("SELECT * FROM daily_challenge WHERE diaIndice = :dia")
    fun retoDelDiaFlow(dia: Long): Flow<DailyChallengeEntity?>

    @Query("UPDATE daily_challenge SET completado = 1 WHERE diaIndice = :dia")
    suspend fun marcarRetoCompletado(dia: Long)

    @Query("SELECT COUNT(*) FROM daily_challenge WHERE completado = 1")
    fun retosCompletadosFlow(): Flow<Int>
}
