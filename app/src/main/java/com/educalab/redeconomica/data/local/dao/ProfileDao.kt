package com.educalab.redeconomica.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.educalab.redeconomica.data.local.entity.ProfileEntity
import com.educalab.redeconomica.data.local.entity.WarehouseEntity
import kotlinx.coroutines.flow.Flow

/** Perfil local del niño y su almacén de recursos. Nada de esto sale del móvil. */
@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(perfil: ProfileEntity)

    @Query("SELECT * FROM profile WHERE id = 1")
    fun perfilFlow(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 1")
    suspend fun perfil(): ProfileEntity?

    @Query("UPDATE profile SET alias = :alias, avatarId = :avatarId WHERE id = 1")
    suspend fun actualizarIdentidad(alias: String, avatarId: String)

    @Query("UPDATE profile SET onboardingHecho = :hecho WHERE id = 1")
    suspend fun marcarOnboarding(hecho: Boolean)

    @Query(
        "UPDATE profile SET sonidoActivo = :sonido, vibracionActiva = :vibracion, " +
            "textoGrande = :textoGrande WHERE id = 1"
    )
    suspend fun actualizarAjustes(sonido: Boolean, vibracion: Boolean, textoGrande: Boolean)

    @Query("SELECT * FROM warehouse WHERE cantidad > 0 ORDER BY recursoId")
    fun almacenFlow(): Flow<List<WarehouseEntity>>

    @Query("SELECT * FROM warehouse WHERE recursoId = :recursoId")
    suspend fun enAlmacen(recursoId: String): WarehouseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarEnAlmacen(fila: WarehouseEntity)

    @Query("DELETE FROM warehouse")
    suspend fun vaciarAlmacen()

    @Query("DELETE FROM profile")
    suspend fun borrarPerfil()
}
