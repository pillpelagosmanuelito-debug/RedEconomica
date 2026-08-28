package com.educalab.redeconomica.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tablas de estado: lo que el niño ha hecho de verdad.
 *
 * De aquí, y solo de aquí, salen el progreso, las insignias y las estadísticas.
 * No hay ningún contador guardado "a mano": todo se cuenta con consultas.
 */

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val alias: String,
    val avatarId: String,
    val onboardingHecho: Boolean,
    val sonidoActivo: Boolean,
    val vibracionActiva: Boolean,
    val textoGrande: Boolean,
    val creadoMillis: Long
)

/** El Almacén del Valle del jugador: recursos que ha ido acumulando. */
@Entity(tableName = "warehouse")
data class WarehouseEntity(
    @PrimaryKey val recursoId: String,
    val cantidad: Int
)

@Entity(tableName = "mission_progress")
data class MissionProgressEntity(
    @PrimaryKey val misionId: String,
    val estado: String,
    val intentosTotales: Int,
    val sinFallos: Boolean,
    val actualizadoMillis: Long
)

@Entity(
    tableName = "scenario_attempts",
    indices = [Index("escenarioId"), Index("misionId")]
)
data class ScenarioAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val escenarioId: String,
    val misionId: String,
    val conceptoId: String,
    val logrado: Boolean,
    val numeroDeIntento: Int,
    val fechaMillis: Long
)

@Entity(tableName = "trades", indices = [Index("escenarioId")])
data class TradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val escenarioId: String,
    val proponenteId: String,
    val receptorId: String,
    val entrega: String,
    val pide: String,
    val aceptado: Boolean,
    val motivo: String?,
    val fechaMillis: Long
)

@Entity(tableName = "specialization_runs", indices = [Index("escenarioId")])
data class SpecializationRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val escenarioId: String,
    /** Formato "lia=manzana|tomas=pan|nina=" (vacío = hace de todo) */
    val plan: String,
    val produccionTotal: String,
    val valorTotal: Int,
    val cumplioObjetivo: Boolean,
    val fechaMillis: Long
)

@Entity(tableName = "cooperation_runs", indices = [Index("escenarioId")])
data class CooperationRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val escenarioId: String,
    val plan: String,
    val resultado: Int,
    val objetivo: Int,
    val resultadoSinCooperar: Int,
    val completado: Boolean,
    val fechaMillis: Long
)

@Entity(tableName = "allocation_runs", indices = [Index("escenarioId")])
data class AllocationRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val escenarioId: String,
    val recursoId: String,
    val disponible: Int,
    val reparto: String,
    val valido: Boolean,
    val fechaMillis: Long
)

@Entity(tableName = "decision_runs", indices = [Index("escenarioId")])
data class DecisionRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val escenarioId: String,
    val seleccion: String,
    val renuncias: String,
    val alcanza: Boolean,
    val fechaMillis: Long
)

@Entity(tableName = "chain_runs", indices = [Index("escenarioId")])
data class ChainRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val escenarioId: String,
    val ordenPropuesto: String,
    val correcto: Boolean,
    val aciertosSeguidos: Int,
    val fechaMillis: Long
)

@Entity(tableName = "user_badges")
data class UserBadgeEntity(
    @PrimaryKey val insigniaId: String,
    val fechaMillis: Long
)

@Entity(tableName = "user_collection")
data class UserCollectionEntity(
    @PrimaryKey val objetoId: String,
    val fechaMillis: Long
)

@Entity(tableName = "discovered_concepts")
data class DiscoveredConceptEntity(
    @PrimaryKey val conceptoId: String,
    val fechaMillis: Long
)

@Entity(tableName = "experiments")
data class ExperimentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val etiqueta: String,
    val habitantes: Int,
    val turnos: Int,
    val modo: String,
    val permiteIntercambio: Boolean,
    val ponenEnComun: Boolean,
    val produccionTotal: String,
    val valorTotal: Int,
    val intercambios: Int,
    val necesidadesCubiertas: Int,
    val necesidadesTotales: Int,
    val fechaMillis: Long
)

@Entity(tableName = "daily_challenge")
data class DailyChallengeEntity(
    @PrimaryKey val diaIndice: Long,
    val escenarioId: String,
    val completado: Boolean,
    val fechaMillis: Long
)
