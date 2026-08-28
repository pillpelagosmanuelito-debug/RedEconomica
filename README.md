# RedEconómica v1.0.0

**El Pueblo de los Intercambios** — aplicación Android educativa para que niños
y niñas de 8 a 12 años aprendan economía **haciendo economía**.

No es un cuestionario con dibujos: es una pequeña comunidad viva (el *Valle
Económico*) donde el niño produce, intercambia, se especializa, coopera,
reparte lo que escasea y decide con recursos limitados. Los conceptos —
intercambio, escasez, especialización, cooperación, costo de oportunidad —
aparecen **después** de haberlos vivido.

```
TENGO → NECESITO → PRODUZCO → INTERCAMBIO → ME ESPECIALIZO → COOPERO → DECIDO
```

---

## Estado de la compilación

> **COMPILACIÓN NO VERIFICADA.**
> El entorno donde se generó este proyecto no tiene Android SDK ni acceso de
> red a `dl.google.com`, `services.gradle.org` ni Maven Central, así que **no
> se pudo ejecutar `./gradlew assembleDebug` y no hay APK en este paquete**.
> El detalle completo, con lo que sí se verificó, está en
> [`docs/BUILD_REPORT.md`](docs/BUILD_REPORT.md).
>
> Para obtener el APK: abre el proyecto en Android Studio, o sube el
> repositorio a GitHub y deja que se ejecute el flujo incluido en
> `.github/workflows/build.yml` (Actions → *Compilar RedEconómica* → artefacto
> `RedEconomica-v1.0.0-apk`).

---

## De un vistazo

| | |
|---|---|
| Paquete | `com.educalab.redeconomica` |
| Versión | 1.0.0 (versionCode 1) |
| minSdk / targetSdk | 24 / 34 |
| Lenguaje | Kotlin 2.0.21 |
| Interfaz | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Repository, dominio sin dependencias de Android |
| Persistencia | Room 2.6.1 (22 tablas) |
| Permisos | **ninguno** (ni siquiera INTERNET) |
| Contenido | 20 recursos, 9 personajes, 14 misiones, 42 desafíos, 11 insignias, 24 objetos, 13 entradas de diccionario |
| Pruebas | 136 pruebas JVM + 1 instrumentada |
| Ilustraciones | 100 % dibujadas con Compose Canvas, sin ninguna imagen descargada |

---

## Cómo compilar

```bash
./gradlew clean
./gradlew testDebugUnitTest     # 136 pruebas JVM
./gradlew lintDebug
./gradlew assembleDebug         # app/build/outputs/apk/debug/app-debug.apk
```

Requisitos: **JDK 17** y Android SDK con la plataforma 34.

---

## Qué hay dentro

```
app/          código de la aplicación (Kotlin + Compose)
database/     schema.sql y sample_data.sql (el Valle completo en SQL)
docs/         memoria, manuales y informe de compilación (+ PDFs)
tools/        scripts de apoyo (iconos, verificación de contenido, SQL)
.github/      flujo de GitHub Actions que genera el APK
```

### El código, por capas

```
domain/     modelos y MOTORES económicos. Kotlin puro, sin Android.
            EconomyEngine · TradeEngine · SpecializationEngine ·
            CooperationEngine · ScarcityEngine · OpportunityCostEngine ·
            ProductionChainEngine · LabEngine · ProgressEngine ·
            ScenarioValidator · DailyChallengeGenerator
data/       Room (entidades, DAOs, sembrado) + repositorios + contenido semilla
ui/         Compose: tema, ilustraciones (Canvas), componentes, pantallas y
            ViewModels. Ni una regla económica vive aquí.
```

### Las pantallas

Mapa del Valle · Misión · Actividad (ocho mecánicas distintas) · Mercado del
Valle · Laboratorio del Valle · Diccionario del Valle · Almacén del Valle ·
Insignias · Perfil y ajustes · Practicar otra vez · Onboarding.

---

## Privacidad

RedEconómica **no pide ningún permiso** y **no tiene acceso a Internet**. No
guarda nombre real, correo, edad ni ubicación: solo un mote que el niño elige
y un avatar dibujado en la propia app. Todo se queda en el dispositivo, en
`redeconomica.db`. No hay cuentas, ni analítica, ni anuncios, ni rankings, ni
chat.

---

## Herramientas de apoyo

```bash
python3 tools/verificar_escenarios.py   # comprueba que los 42 desafíos tienen solución
python3 tools/generar_sample_data.py    # regenera database/sample_data.sql desde el código
python3 tools/generate_launcher_icons.py# regenera los iconos PNG del lanzador
```

---

## Documentación

- [`docs/MEMORIA_DESCRIPTIVA.md`](docs/MEMORIA_DESCRIPTIVA.md) — qué es, por qué así, qué se decidió
- [`docs/MANUAL_USUARIO.md`](docs/MANUAL_USUARIO.md) — para el niño y para quien le acompaña
- [`docs/MANUAL_TECNICO.md`](docs/MANUAL_TECNICO.md) — arquitectura, motores y reglas
- [`docs/BASE_DE_DATOS.md`](docs/BASE_DE_DATOS.md) — las 22 tablas y de dónde sale cada número
- [`docs/BUILD_REPORT.md`](docs/BUILD_REPORT.md) — qué se verificó y qué no

Los mismos documentos en PDF están en `docs/pdf/`.
