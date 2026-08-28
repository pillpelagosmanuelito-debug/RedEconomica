// Configuración de plugins a nivel raíz. Ninguno se aplica aquí: solo se declaran
// las versiones para que el módulo :app las reutilice sin repetirlas.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
