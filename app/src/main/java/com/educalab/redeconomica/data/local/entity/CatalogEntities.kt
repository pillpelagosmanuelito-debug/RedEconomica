package com.educalab.redeconomica.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tablas de catálogo: el contenido del Valle sembrado en la base de datos la
 * primera vez que se abre la app.
 *
 * Los enumerados se guardan como texto (`name`) para que el esquema sea legible
 * desde cualquier visor de SQLite y para no depender de conversores.
 */

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey val id: String,
    val singular: String,
    val plural: String,
    val tipo: String,
    val valorBase: Int,
    val descripcion: String
)

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val oficio: String,
    val lugar: String,
    val presentacion: String,
    /** Formato "manzana:6|verdura:3" */
    val productividad: String,
    val inventarioBase: String,
    val avatarId: String,
    val esGuia: Boolean
)

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val id: String,
    val numero: Int,
    val titulo: String,
    val lugar: String,
    val concepto: String,
    val narrativaInicio: String,
    val narrativaFinal: String,
    val objetivoVisible: String,
    val requiereMision: String?,
    val insigniaId: String?,
    val zonaDesbloqueada: String?,
    val sellos: Int
)

@Entity(
    tableName = "scenarios",
    indices = [Index("misionId")]
)
data class ScenarioEntity(
    @PrimaryKey val id: String,
    val misionId: String,
    val orden: Int,
    val tipo: String,
    val titulo: String,
    val situacion: String,
    val instruccion: String,
    val explicacionFinal: String,
    val conceptoId: String,
    val dificultad: Int
)

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val descripcion: String,
    val regla: String,
    val meta: Int,
    val arteId: String
)

@Entity(tableName = "collection_items")
data class CollectionItemEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val familia: String,
    val descripcion: String,
    val comoSeConsigue: String,
    val recursoId: String?
)

@Entity(tableName = "glossary")
data class GlossaryEntity(
    @PrimaryKey val id: String,
    val termino: String,
    val definicionInfantil: String,
    val ejemplo: String,
    val conceptoId: String,
    val arteId: String,
    val miniActividad: String?
)
