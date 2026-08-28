package com.educalab.redeconomica.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.educalab.redeconomica.data.local.dao.ActivityDao
import com.educalab.redeconomica.data.local.dao.CatalogDao
import com.educalab.redeconomica.data.local.dao.LabDao
import com.educalab.redeconomica.data.local.dao.ProfileDao
import com.educalab.redeconomica.data.local.dao.ProgressDao
import com.educalab.redeconomica.data.local.entity.AllocationRunEntity
import com.educalab.redeconomica.data.local.entity.BadgeEntity
import com.educalab.redeconomica.data.local.entity.ChainRunEntity
import com.educalab.redeconomica.data.local.entity.CharacterEntity
import com.educalab.redeconomica.data.local.entity.CollectionItemEntity
import com.educalab.redeconomica.data.local.entity.CooperationRunEntity
import com.educalab.redeconomica.data.local.entity.DailyChallengeEntity
import com.educalab.redeconomica.data.local.entity.DecisionRunEntity
import com.educalab.redeconomica.data.local.entity.DiscoveredConceptEntity
import com.educalab.redeconomica.data.local.entity.ExperimentEntity
import com.educalab.redeconomica.data.local.entity.GlossaryEntity
import com.educalab.redeconomica.data.local.entity.MissionEntity
import com.educalab.redeconomica.data.local.entity.MissionProgressEntity
import com.educalab.redeconomica.data.local.entity.ProfileEntity
import com.educalab.redeconomica.data.local.entity.ResourceEntity
import com.educalab.redeconomica.data.local.entity.ScenarioAttemptEntity
import com.educalab.redeconomica.data.local.entity.ScenarioEntity
import com.educalab.redeconomica.data.local.entity.SpecializationRunEntity
import com.educalab.redeconomica.data.local.entity.TradeEntity
import com.educalab.redeconomica.data.local.entity.UserBadgeEntity
import com.educalab.redeconomica.data.local.entity.UserCollectionEntity
import com.educalab.redeconomica.data.local.entity.WarehouseEntity

/**
 * Base de datos local de RedEconómica.
 *
 * Todo vive en el dispositivo: no hay servidor, ni cuenta, ni copia en la nube
 * gestionada por la app. El archivo se llama `redeconomica.db`.
 */
@Database(
    entities = [
        // Catálogo
        ResourceEntity::class,
        CharacterEntity::class,
        MissionEntity::class,
        ScenarioEntity::class,
        BadgeEntity::class,
        CollectionItemEntity::class,
        GlossaryEntity::class,
        // Estado
        ProfileEntity::class,
        WarehouseEntity::class,
        MissionProgressEntity::class,
        ScenarioAttemptEntity::class,
        TradeEntity::class,
        SpecializationRunEntity::class,
        CooperationRunEntity::class,
        AllocationRunEntity::class,
        DecisionRunEntity::class,
        ChainRunEntity::class,
        UserBadgeEntity::class,
        UserCollectionEntity::class,
        DiscoveredConceptEntity::class,
        ExperimentEntity::class,
        DailyChallengeEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun catalogDao(): CatalogDao
    abstract fun profileDao(): ProfileDao
    abstract fun progressDao(): ProgressDao
    abstract fun activityDao(): ActivityDao
    abstract fun labDao(): LabDao

    companion object {
        const val NOMBRE = "redeconomica.db"

        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE
                )
                    // El contenido es semilla reproducible: si algún día cambia el
                    // esquema, se vuelve a sembrar en lugar de arrastrar migraciones
                    // complicadas en una app sin datos personales.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instancia = it }
            }
    }
}
