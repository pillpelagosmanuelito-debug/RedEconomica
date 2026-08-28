package com.educalab.redeconomica.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.educalab.redeconomica.data.local.entity.BadgeEntity
import com.educalab.redeconomica.data.local.entity.CharacterEntity
import com.educalab.redeconomica.data.local.entity.CollectionItemEntity
import com.educalab.redeconomica.data.local.entity.GlossaryEntity
import com.educalab.redeconomica.data.local.entity.MissionEntity
import com.educalab.redeconomica.data.local.entity.ResourceEntity
import com.educalab.redeconomica.data.local.entity.ScenarioEntity
import kotlinx.coroutines.flow.Flow

/** Lecturas y sembrado del catálogo del Valle. */
@Dao
interface CatalogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRecursos(items: List<ResourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPersonajes(items: List<CharacterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMisiones(items: List<MissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEscenarios(items: List<ScenarioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarInsignias(items: List<BadgeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarObjetos(items: List<CollectionItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDiccionario(items: List<GlossaryEntity>)

    @Query("SELECT COUNT(*) FROM resources")
    suspend fun contarRecursos(): Int

    @Query("SELECT COUNT(*) FROM missions")
    suspend fun contarMisiones(): Int

    @Query("SELECT COUNT(*) FROM scenarios")
    suspend fun contarEscenarios(): Int

    @Query("SELECT * FROM resources ORDER BY id")
    suspend fun recursos(): List<ResourceEntity>

    @Query("SELECT * FROM characters WHERE esGuia = 0 ORDER BY nombre")
    suspend fun habitantes(): List<CharacterEntity>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun personaje(id: String): CharacterEntity?

    @Query("SELECT * FROM missions ORDER BY numero")
    fun misionesFlow(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions ORDER BY numero")
    suspend fun misiones(): List<MissionEntity>

    @Query("SELECT * FROM missions WHERE id = :id")
    suspend fun mision(id: String): MissionEntity?

    @Query("SELECT * FROM scenarios WHERE misionId = :misionId ORDER BY orden")
    suspend fun escenariosDe(misionId: String): List<ScenarioEntity>

    @Query("SELECT * FROM scenarios WHERE id = :id")
    suspend fun escenario(id: String): ScenarioEntity?

    @Query("SELECT * FROM badges ORDER BY meta")
    fun insigniasFlow(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM collection_items ORDER BY id")
    fun objetosFlow(): Flow<List<CollectionItemEntity>>

    @Query("SELECT * FROM glossary ORDER BY termino")
    fun diccionarioFlow(): Flow<List<GlossaryEntity>>

    @Query("SELECT * FROM glossary WHERE id = :id")
    suspend fun entradaDiccionario(id: String): GlossaryEntity?
}
