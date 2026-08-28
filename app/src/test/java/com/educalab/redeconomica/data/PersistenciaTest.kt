package com.educalab.redeconomica.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.educalab.redeconomica.data.local.AppDatabase
import com.educalab.redeconomica.data.local.DatabaseSeeder
import com.educalab.redeconomica.data.repository.CatalogRepository
import com.educalab.redeconomica.data.repository.ProfileRepository
import com.educalab.redeconomica.data.repository.ProgressRepository
import com.educalab.redeconomica.data.seed.SeedContent
import com.educalab.redeconomica.domain.model.AttemptResult
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.TradeOffer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Persistencia real con Room (en memoria, con Robolectric).
 *
 * Comprueba que el Valle se siembra bien, que el progreso se guarda y que los
 * contadores del perfil salen de contar filas, no de un número guardado a mano.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PersistenciaTest {

    private lateinit var db: AppDatabase
    private lateinit var sembrador: DatabaseSeeder
    private lateinit var catalogo: CatalogRepository
    private lateinit var progreso: ProgressRepository
    private lateinit var perfil: ProfileRepository

    @Before
    fun preparar(): Unit = runBlocking {
        val contexto: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sembrador = DatabaseSeeder(db)
        catalogo = CatalogRepository(db.catalogDao())
        perfil = ProfileRepository(db.profileDao())
        progreso = ProgressRepository(
            progressDao = db.progressDao(),
            activityDao = db.activityDao(),
            labDao = db.labDao(),
            profileDao = db.profileDao(),
            catalogo = catalogo
        )
        sembrador.sembrarSiHaceFalta()
    }

    @After
    fun cerrar() {
        db.close()
    }

    @Test
    fun `la base de datos nueva se siembra con todo el Valle`() = runBlocking {
        assertEquals(SeedContent.RECURSOS.size, catalogo.recursos().size)
        assertEquals(SeedContent.HABITANTES.size, catalogo.habitantes().size)
        assertEquals(SeedContent.MISIONES.size, catalogo.misiones().size)
        assertEquals(SeedContent.ESCENARIOS.size, db.catalogDao().contarEscenarios())
    }

    @Test
    fun `sembrar dos veces no duplica nada`() = runBlocking {
        val sembroDeNuevo = sembrador.sembrarSiHaceFalta()
        assertFalse(sembroDeNuevo)
        assertEquals(SeedContent.RECURSOS.size, catalogo.recursos().size)
    }

    @Test
    fun `los datos del habitante viajan enteros a la base de datos`() = runBlocking {
        val lia = catalogo.habitante("lia")
        assertNotNull(lia)
        assertEquals("Lía", lia!!.nombre)
        assertEquals(6, lia.produccionPorTurno("manzana"))
    }

    @Test
    fun `una mision recupera sus escenarios en orden`() = runBlocking {
        val mision = catalogo.mision("m01")
        assertNotNull(mision)
        assertEquals(listOf("s01", "s02", "s03"), mision!!.escenarios)
    }

    @Test
    fun `el perfil por defecto existe y no guarda datos personales`() = runBlocking {
        val p = perfil.perfil()
        assertEquals("Vecino", p.alias)
        assertFalse(p.onboardingHecho)
    }

    @Test
    fun `guardar el alias y el avatar funciona`() = runBlocking {
        perfil.guardarIdentidad("Tuli", "avatar_5")
        val p = perfil.perfil()
        assertEquals("Tuli", p.alias)
        assertEquals("avatar_5", p.avatarId)
    }

    @Test
    fun `un alias vacio no borra el perfil`() = runBlocking {
        perfil.guardarIdentidad("   ", "avatar_2")
        assertEquals("Vecino", perfil.perfil().alias)
    }

    @Test
    fun `un intento logrado queda registrado y descubre el concepto`() = runBlocking {
        val escenario = SeedContent.escenario("s01")
        val actualizacion = progreso.registrarIntento(
            escenario,
            AttemptResult("s01", true, "ok", "explicación", 1, escenario.conceptoId)
        )
        assertTrue(progreso.escenariosLogradosFlow().first().contains("s01"))
        assertEquals(escenario.conceptoId, actualizacion.conceptoDescubierto)
    }

    @Test
    fun `un intento fallido aparece en la lista de repaso`() = runBlocking {
        val escenario = SeedContent.escenario("s02")
        progreso.registrarIntento(
            escenario,
            AttemptResult("s02", false, "todavía no", "prueba otra vez", 1, escenario.conceptoId)
        )
        assertTrue(progreso.escenariosParaRepasarFlow().first().contains("s02"))
    }

    @Test
    fun `al acertar despues, el escenario sale de la lista de repaso`() = runBlocking {
        val escenario = SeedContent.escenario("s02")
        progreso.registrarIntento(
            escenario, AttemptResult("s02", false, "-", "-", 1, escenario.conceptoId)
        )
        progreso.registrarIntento(
            escenario, AttemptResult("s02", true, "-", "-", 2, escenario.conceptoId)
        )
        assertFalse(progreso.escenariosParaRepasarFlow().first().contains("s02"))
    }

    @Test
    fun `completar una mision entrega sus objetos y desbloquea la insignia`() = runBlocking {
        val mision = SeedContent.MISIONES.first { it.id == "m02" }
        var ultima = progreso.registrarIntento(
            SeedContent.escenario("s04"),
            AttemptResult("s04", true, "-", "-", 1, "INTERCAMBIO")
        )
        progreso.registrarIntento(
            SeedContent.escenario("s05"),
            AttemptResult("s05", true, "-", "-", 1, "INTERCAMBIO")
        )
        ultima = progreso.registrarIntento(
            SeedContent.escenario("s06"),
            AttemptResult("s06", true, "-", "-", 1, "INTERCAMBIO")
        )
        assertEquals("m02", ultima.misionCompletada?.id)
        val objetos = progreso.objetosDesbloqueadosFlow().first()
        assertTrue(objetos.containsAll(mision.recompensa.objetos))
    }

    @Test
    fun `los contadores se calculan contando filas reales`() = runBlocking {
        val oferta = TradeOffer(
            "jugador", "tomas",
            Inventory.of("manzana" to 3), Inventory.of("pan" to 2)
        )
        progreso.registrarIntercambio("s04", oferta, aceptado = true, motivo = null)
        progreso.registrarIntercambio("s04", oferta, aceptado = false, motivo = "DESEQUILIBRIO")
        val contadores = progreso.contadoresAhora()
        assertEquals(1, contadores.intercambiosAceptados)
        assertEquals(2, contadores.intercambiosPropuestos)
    }

    @Test
    fun `la primera insignia se entrega tras el primer intercambio aceptado`() = runBlocking {
        val oferta = TradeOffer(
            "jugador", "tomas",
            Inventory.of("manzana" to 3), Inventory.of("pan" to 2)
        )
        progreso.registrarIntercambio("s04", oferta, aceptado = true, motivo = null)
        val ganadas = progreso.insigniasGanadasFlow().first()
        assertTrue(ganadas.contains("primer_trato"))
    }

    @Test
    fun `reiniciar el progreso deja el catalogo intacto`() = runBlocking {
        progreso.registrarIntento(
            SeedContent.escenario("s01"),
            AttemptResult("s01", true, "-", "-", 1, "NECESIDADES")
        )
        sembrador.reiniciarProgreso()
        assertTrue(progreso.escenariosLogradosFlow().first().isEmpty())
        assertEquals(SeedContent.RECURSOS.size, catalogo.recursos().size)
        assertEquals(0, progreso.contadoresAhora().intercambiosAceptados)
    }

    @Test
    fun `el almacen guarda los recursos de los objetos conseguidos`() = runBlocking {
        progreso.registrarIntento(
            SeedContent.escenario("s01"),
            AttemptResult("s01", true, "-", "-", 1, "NECESIDADES")
        )
        progreso.registrarIntento(
            SeedContent.escenario("s02"),
            AttemptResult("s02", true, "-", "-", 1, "NECESIDADES")
        )
        progreso.registrarIntento(
            SeedContent.escenario("s03"),
            AttemptResult("s03", true, "-", "-", 1, "RECURSOS")
        )
        val almacen = perfil.almacenFlow.first()
        assertTrue(almacen.cantidad("manzana") >= 1)
        assertTrue(almacen.cantidad("verdura") >= 1)
    }

    @Test
    fun `el reto del dia se guarda y se puede marcar completado`() = runBlocking {
        val laboratorio = com.educalab.redeconomica.data.repository.LabRepository(db.labDao())
        val reto = laboratorio.retoDeHoy(1_700_000_000_000L)
        assertNotNull(reto)
        assertFalse(laboratorio.estaCompletadoElRetoDeHoy(1_700_000_000_000L))
        laboratorio.marcarRetoCompletado(1_700_000_000_000L)
        assertTrue(laboratorio.estaCompletadoElRetoDeHoy(1_700_000_000_000L))
    }

    @Test
    fun `una base de datos recien creada no tiene progreso`() = runBlocking {
        val contadores = progreso.contadoresAhora()
        assertEquals(0, contadores.misionesCompletadas)
        assertEquals(0, contadores.objetosDesbloqueados)
        assertTrue(progreso.insigniasGanadasFlow().first().isEmpty())
    }
}
