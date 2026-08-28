package com.educalab.redeconomica

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba instrumentada de humo: la app arranca y pinta algo.
 *
 * Requiere un dispositivo o emulador; el flujo de GitHub Actions incluido
 * ejecuta solo las pruebas JVM.
 */
@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val regla = createAndroidComposeRule<MainActivity>()

    @Test
    fun la_app_arranca_sin_romperse() {
        regla.waitForIdle()
        regla.onRoot().assertIsDisplayed()
    }
}
