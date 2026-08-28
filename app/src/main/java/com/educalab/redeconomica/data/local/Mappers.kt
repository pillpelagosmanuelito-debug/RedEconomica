package com.educalab.redeconomica.data.local

import com.educalab.redeconomica.data.local.entity.BadgeEntity
import com.educalab.redeconomica.data.local.entity.CharacterEntity
import com.educalab.redeconomica.data.local.entity.CollectionItemEntity
import com.educalab.redeconomica.data.local.entity.GlossaryEntity
import com.educalab.redeconomica.data.local.entity.MissionEntity
import com.educalab.redeconomica.data.local.entity.MissionProgressEntity
import com.educalab.redeconomica.data.local.entity.ProfileEntity
import com.educalab.redeconomica.data.local.entity.ResourceEntity
import com.educalab.redeconomica.data.local.entity.ScenarioEntity
import com.educalab.redeconomica.domain.model.Badge
import com.educalab.redeconomica.domain.model.BadgeRule
import com.educalab.redeconomica.domain.model.CollectionItem
import com.educalab.redeconomica.domain.model.ConceptId
import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.GlossaryEntry
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.MissionDef
import com.educalab.redeconomica.domain.model.MissionProgress
import com.educalab.redeconomica.domain.model.MissionReward
import com.educalab.redeconomica.domain.model.ModuleState
import com.educalab.redeconomica.domain.model.ResourceDef
import com.educalab.redeconomica.domain.model.ResourceType
import com.educalab.redeconomica.domain.model.Scenario
import com.educalab.redeconomica.domain.model.UserProfile
import com.educalab.redeconomica.domain.model.ValleyPlace

/**
 * Conversión entre las filas de Room y los modelos del dominio.
 *
 * Los mapas de productividad y los inventarios se guardan en texto compacto
 * ("manzana:6|verdura:3") para que la base de datos siga siendo legible sin
 * inventar tablas artificiales por cada par clave-valor.
 */
object Mappers {

    fun mapaATexto(mapa: Map<String, Int>): String =
        mapa.entries.sortedBy { it.key }.joinToString("|") { "${it.key}:${it.value}" }

    fun textoAMapa(texto: String): Map<String, Int> {
        if (texto.isBlank()) return emptyMap()
        return texto.split('|').filter { it.isNotBlank() }.associate { parte ->
            val trozos = parte.split(':')
            trozos[0] to (trozos.getOrNull(1)?.trim()?.toIntOrNull() ?: 0)
        }
    }

    fun planATexto(plan: Map<String, String?>): String =
        plan.entries.sortedBy { it.key }.joinToString("|") { "${it.key}=${it.value ?: ""}" }

    fun textoAPlan(texto: String): Map<String, String?> {
        if (texto.isBlank()) return emptyMap()
        return texto.split('|').filter { it.isNotBlank() }.associate { parte ->
            val trozos = parte.split('=')
            trozos[0] to trozos.getOrNull(1)?.takeIf { it.isNotBlank() }
        }
    }

    // ------------------------------------------------------------- catálogo

    fun aEntidad(r: ResourceDef) = ResourceEntity(
        id = r.id, singular = r.singular, plural = r.plural,
        tipo = r.tipo.name, valorBase = r.valorBase, descripcion = r.descripcion
    )

    fun aDominio(e: ResourceEntity) = ResourceDef(
        id = e.id, singular = e.singular, plural = e.plural,
        tipo = ResourceType.valueOf(e.tipo), valorBase = e.valorBase,
        descripcion = e.descripcion
    )

    fun aEntidad(c: EconomicCharacter, esGuia: Boolean = false) = CharacterEntity(
        id = c.id, nombre = c.nombre, oficio = c.oficio, lugar = c.lugar.name,
        presentacion = c.presentacion,
        productividad = mapaATexto(c.productividad),
        inventarioBase = Inventory.aTexto(c.inventario),
        avatarId = c.avatarId, esGuia = esGuia
    )

    fun aDominio(e: CharacterEntity) = EconomicCharacter(
        id = e.id, nombre = e.nombre, oficio = e.oficio,
        lugar = ValleyPlace.valueOf(e.lugar), presentacion = e.presentacion,
        productividad = textoAMapa(e.productividad),
        inventario = Inventory.desdeTexto(e.inventarioBase),
        necesidades = emptyList(),
        avatarId = e.avatarId
    )

    fun aEntidad(m: MissionDef) = MissionEntity(
        id = m.id, numero = m.numero, titulo = m.titulo, lugar = m.lugar.name,
        concepto = m.concepto.name, narrativaInicio = m.narrativaInicio,
        narrativaFinal = m.narrativaFinal, objetivoVisible = m.objetivoVisible,
        requiereMision = m.requiereMision, insigniaId = m.recompensa.insigniaId,
        zonaDesbloqueada = m.recompensa.zonaDesbloqueada?.name,
        sellos = m.recompensa.sellos
    )

    fun aDominio(e: MissionEntity, escenarios: List<String>, objetos: List<String>) = MissionDef(
        id = e.id, numero = e.numero, titulo = e.titulo,
        lugar = ValleyPlace.valueOf(e.lugar), concepto = ConceptId.valueOf(e.concepto),
        narrativaInicio = e.narrativaInicio, narrativaFinal = e.narrativaFinal,
        objetivoVisible = e.objetivoVisible, escenarios = escenarios,
        recompensa = MissionReward(
            objetos = objetos,
            insigniaId = e.insigniaId,
            zonaDesbloqueada = e.zonaDesbloqueada?.let { ValleyPlace.valueOf(it) },
            sellos = e.sellos
        ),
        requiereMision = e.requiereMision
    )

    fun aEntidad(s: Scenario, misionId: String, orden: Int) = ScenarioEntity(
        id = s.id, misionId = misionId, orden = orden, tipo = s.tipo.name,
        titulo = s.titulo, situacion = s.situacion, instruccion = s.instruccion,
        explicacionFinal = s.explicacionFinal, conceptoId = s.conceptoId,
        dificultad = s.dificultad
    )

    fun aEntidad(b: Badge) = BadgeEntity(
        id = b.id, nombre = b.nombre, descripcion = b.descripcion,
        regla = b.regla.name, meta = b.meta, arteId = b.arteId
    )

    fun aDominio(e: BadgeEntity) = Badge(
        id = e.id, nombre = e.nombre, descripcion = e.descripcion,
        regla = BadgeRule.valueOf(e.regla), meta = e.meta, arteId = e.arteId
    )

    fun aEntidad(c: CollectionItem) = CollectionItemEntity(
        id = c.id, nombre = c.nombre, familia = c.familia.name,
        descripcion = c.descripcion, comoSeConsigue = c.comoSeConsigue,
        recursoId = c.recursoId
    )

    fun aDominio(e: CollectionItemEntity) = CollectionItem(
        id = e.id, nombre = e.nombre, familia = ResourceType.valueOf(e.familia),
        descripcion = e.descripcion, comoSeConsigue = e.comoSeConsigue,
        recursoId = e.recursoId
    )

    fun aEntidad(g: GlossaryEntry) = GlossaryEntity(
        id = g.id, termino = g.termino, definicionInfantil = g.definicionInfantil,
        ejemplo = g.ejemplo, conceptoId = g.conceptoId.name, arteId = g.arteId,
        miniActividad = g.miniActividad
    )

    fun aDominio(e: GlossaryEntity) = GlossaryEntry(
        id = e.id, termino = e.termino, definicionInfantil = e.definicionInfantil,
        ejemplo = e.ejemplo, conceptoId = ConceptId.valueOf(e.conceptoId),
        arteId = e.arteId, miniActividad = e.miniActividad
    )

    // ---------------------------------------------------------------- estado

    fun aDominio(e: ProfileEntity) = UserProfile(
        alias = e.alias, avatarId = e.avatarId, onboardingHecho = e.onboardingHecho,
        sonidoActivo = e.sonidoActivo, vibracionActiva = e.vibracionActiva,
        textoGrande = e.textoGrande
    )

    fun aDominio(e: MissionProgressEntity, escenariosLogrados: Set<String>) = MissionProgress(
        misionId = e.misionId,
        estado = ModuleState.valueOf(e.estado),
        escenariosCompletados = escenariosLogrados,
        intentosTotales = e.intentosTotales,
        sinFallos = e.sinFallos
    )
}
