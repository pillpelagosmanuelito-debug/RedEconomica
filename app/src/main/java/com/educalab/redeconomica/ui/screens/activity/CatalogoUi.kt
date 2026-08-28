package com.educalab.redeconomica.ui.screens.activity

import com.educalab.redeconomica.data.seed.SeedResources
import com.educalab.redeconomica.domain.model.ResourceDef

/**
 * Catálogo de recursos accesible sin suspender.
 *
 * Las pantallas solo lo usan para escribir textos ("3 manzanas y 2 panes").
 * Todos los cálculos económicos siguen haciéndose en el dominio con el
 * catálogo que le pasa el contenedor de dependencias.
 */
internal object SeedCatalogoLocal {
    val catalogo: Map<String, ResourceDef> = SeedResources.PORID
}
