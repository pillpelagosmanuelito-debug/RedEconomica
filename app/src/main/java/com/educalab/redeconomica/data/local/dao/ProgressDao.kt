package com.educalab.redeconomica.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.educalab.redeconomica.data.local.entity.DiscoveredConceptEntity
import com.educalab.redeconomica.data.local.entity.MissionProgressEntity
import com.educalab.redeconomica.data.local.entity.ScenarioAttemptEntity
import com.educalab.redeconomica.data.local.entity.UserBadgeEntity
import com.educalab.redeconomica.data.local.entity.UserCollectionEntity
import kotlinx.coroutines.flow.Flow

/** Progreso: misiones, intentos, insignias, objetos y conceptos descubiertos. */
@Dao
interface ProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarProgresoMision(fila: MissionProgressEntity)

    @Query("SELECT * FROM mission_progress")
    fun progresoMisionesFlow(): Flow<List<MissionProgressEntity>>

    @Query("SELECT * FROM mission_progress WHERE misionId = :misionId")
    suspend fun progresoMision(misionId: String): MissionProgressEntity?

    @Query("SELECT COUNT(*) FROM mission_progress WHERE estado IN ('COMPLETADO','DOMINADO')")
    fun misionesCompletadasFlow(): Flow<Int>

    @Insert
    suspend fun registrarIntento(fila: ScenarioAttemptEntity): Long

    @Query("SELECT * FROM scenario_attempts WHERE escenarioId = :escenarioId ORDER BY fechaMillis")
    suspend fun intentosDe(escenarioId: String): List<ScenarioAttemptEntity>

    @Query("SELECT COUNT(*) FROM scenario_attempts WHERE escenarioId = :escenarioId")
    suspend fun numeroDeIntentos(escenarioId: String): Int

    @Query(
        "SELECT DISTINCT escenarioId FROM scenario_attempts " +
            "WHERE misionId = :misionId AND logrado = 1"
    )
    suspend fun escenariosLogrados(misionId: String): List<String>

    @Query("SELECT DISTINCT escenarioId FROM scenario_attempts WHERE logrado = 1")
    fun escenariosLogradosFlow(): Flow<List<String>>

    /** Escenarios donde el niño falló al menos una vez y que puede repasar. */
    @Query(
        "SELECT DISTINCT escenarioId FROM scenario_attempts WHERE logrado = 0 " +
            "AND escenarioId NOT IN (SELECT escenarioId FROM scenario_attempts WHERE logrado = 1)"
    )
    fun escenariosParaRepasarFlow(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun darInsignia(fila: UserBadgeEntity)

    @Query("SELECT * FROM user_badges")
    fun insigniasGanadasFlow(): Flow<List<UserBadgeEntity>>

    @Query("SELECT insigniaId FROM user_badges")
    suspend fun idsInsigniasGanadas(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun desbloquearObjeto(fila: UserCollectionEntity)

    @Query("SELECT * FROM user_collection")
    fun objetosDesbloqueadosFlow(): Flow<List<UserCollectionEntity>>

    @Query("SELECT COUNT(*) FROM user_collection")
    fun numeroObjetosFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun descubrirConcepto(fila: DiscoveredConceptEntity)

    @Query("SELECT * FROM discovered_concepts")
    fun conceptosFlow(): Flow<List<DiscoveredConceptEntity>>

    @Query("SELECT COUNT(*) FROM discovered_concepts")
    fun numeroConceptosFlow(): Flow<Int>

    @Query("DELETE FROM mission_progress")
    suspend fun borrarProgresoMisiones()

    @Query("DELETE FROM scenario_attempts")
    suspend fun borrarIntentos()

    @Query("DELETE FROM user_badges")
    suspend fun borrarInsignias()

    @Query("DELETE FROM user_collection")
    suspend fun borrarColeccion()

    @Query("DELETE FROM discovered_concepts")
    suspend fun borrarConceptos()
}
