# Base de datos — RedEconómica v1.0.0

Motor: **SQLite** a través de **Room 2.6.1**.
Archivo en el dispositivo: `redeconomica.db`. Versión de esquema: **1**.
22 tablas. Todo local: no hay servidor, ni sincronización, ni copia gestionada
por la aplicación.

El SQL equivalente está en [`database/schema.sql`](../database/schema.sql) y el
contenido sembrado en [`database/sample_data.sql`](../database/sample_data.sql)
(generado desde el código por `tools/generar_sample_data.py`).

---

## 1. Diagrama

```
                         ┌──────────────┐
                         │  resources   │◄──────────────┐
                         └──────┬───────┘               │ recursoId
                                │ (por id, en texto)    │
   ┌──────────────┐      ┌──────┴───────┐        ┌──────┴───────────┐
   │  characters  │      │  warehouse   │        │ collection_items │
   └──────────────┘      └──────────────┘        └──────────────────┘
                                                          ▲ objetoId
   ┌──────────────┐  requiereMision                       │
   │   missions   │──┐                             ┌──────┴───────┐
   └──────┬───────┘  │ (auto)                      │user_collection│
          │ misionId └──────┘                      └──────────────┘
   ┌──────┴───────┐
   │  scenarios   │◄──────────┬──────────┬──────────┬──────────┬────────┐
   └──────┬───────┘           │          │          │          │        │
          │ escenarioId       │          │          │          │        │
   ┌──────┴────────────┐ ┌────┴────┐ ┌───┴──────┐ ┌─┴────────┐ ┌┴──────┐ ┌────────┐
   │ scenario_attempts │ │ trades  │ │special.  │ │coopera-  │ │alloca-│ │decision│
   └──────┬────────────┘ └─────────┘ │_runs     │ │tion_runs │ │tion_r.│ │_runs   │
          │ misionId                 └──────────┘ └──────────┘ └───────┘ └────────┘
   ┌──────┴───────────┐                                        ┌────────────┐
   │ mission_progress │                                        │ chain_runs │
   └──────────────────┘                                        └────────────┘

   ┌──────────┐  ┌─────────────┐  ┌────────────────────┐  ┌─────────────┐
   │  badges  │◄─┤ user_badges │  │ discovered_concepts│  │  glossary   │
   └──────────┘  └─────────────┘  └────────────────────┘  └─────────────┘

   ┌──────────┐  ┌─────────────┐  ┌────────────────┐
   │ profile  │  │ experiments │  │ daily_challenge│
   └──────────┘  └─────────────┘  └────────────────┘
```

Room no declara claves foráneas: las relaciones se mantienen por id desde los
repositorios y se verifican en pruebas (`SeedContent.problemasDeIntegridad()`).
Se prefirió así para que el sembrado sea idempotente y el borrado de progreso
no arrastre el catálogo.

---

## 2. Tablas de catálogo

Se siembran una sola vez, en el primer arranque, desde `data/seed` mediante
`DatabaseSeeder`. Volver a arrancar la app no duplica nada.

| Tabla | Filas sembradas | Qué guarda |
|---|---|---|
| `resources` | 20 | Recursos del Valle: id, singular, plural, familia, `valorBase`, descripción |
| `characters` | 9 | 8 vecinos + Tilo: oficio, zona, productividad, inventario base, avatar |
| `missions` | 14 | Las misiones: número, zona, concepto, narrativa, requisito, recompensa |
| `scenarios` | 42 | Ficha de cada desafío: misión, orden, tipo, textos, concepto, dificultad |
| `badges` | 11 | Insignias: regla que las desbloquea y meta |
| `collection_items` | 24 | Objetos del Almacén del Valle |
| `glossary` | 13 | Diccionario del Valle |

### Nota de diseño: dónde viven las reglas de un escenario

En `scenarios` está la **ficha** de cada desafío, y de ella salen los listados,
el orden dentro de la misión y el progreso. Las **reglas** concretas —qué tiene
cada personaje, cuántas etapas hay, qué opciones de presupuesto existen— viven
en el módulo de contenido `data/seed`, **fuera de la interfaz**, porque tienen
forma distinta según la mecánica (una escasez y una cadena de producción no
comparten columnas). Guardarlas en tablas habría exigido escribir un
serializador propio sin ganar ninguna consulta útil.

Es una simplificación consciente y está declarada también en la memoria
descriptiva. Lo que **no** se hizo es meter esas reglas dentro de los
Composables: son datos, se validan con `ScenarioValidator` y las pruebas los
recorren enteros.

---

## 3. Tablas de estado

| Tabla | Clave | Qué guarda |
|---|---|---|
| `profile` | `id = 1` | Alias, avatar, onboarding, ajustes. **Ningún dato personal.** |
| `warehouse` | `recursoId` | Recursos acumulados por el jugador |
| `mission_progress` | `misionId` | Estado de la misión, intentos, si fue sin fallos |
| `scenario_attempts` | auto | Un registro por intento: escenario, misión, concepto, si se logró |
| `trades` | auto | Cada propuesta de intercambio, aceptada o no, con su motivo |
| `specialization_runs` | auto | Cada reparto de trabajo ejecutado, con su producción y valor |
| `cooperation_runs` | auto | Cada trabajo en equipo, con resultado, objetivo y lo que saldría sin cooperar |
| `allocation_runs` | auto | Cada reparto de un recurso escaso |
| `decision_runs` | auto | Cada decisión con presupuesto, con sus renuncias |
| `chain_runs` | auto | Cada intento de ordenar una cadena |
| `user_badges` | `insigniaId` | Insignias conseguidas |
| `user_collection` | `objetoId` | Objetos desbloqueados |
| `discovered_concepts` | `conceptoId` | Conceptos ya vividos |
| `experiments` | auto | Cada experimento del Laboratorio, con su configuración y resultado |
| `daily_challenge` | `diaIndice` | Reto del día y si se completó |

---

## 4. Formatos compactos

Tres tipos de dato se guardan como texto legible en lugar de crear tablas
auxiliares:

| Formato | Ejemplo | Dónde |
|---|---|---|
| Inventario / productividad | `manzana:6\|verdura:3` | `characters.productividad`, `trades.entrega`, `warehouse` (no), `experiments.produccionTotal` |
| Plan de trabajo | `lia=manzana\|tomas=pan\|nina=` (vacío = «hace de todo») | `specialization_runs.plan`, `cooperation_runs.plan` |
| Lista simple | `silla\|cesta` | `decision_runs.seleccion`, `chain_runs.ordenPropuesto` |

Se eligió este formato porque es **legible desde cualquier visor de SQLite**,
va y vuelve sin pérdida (probado en `InventoryTest`) y la aplicación nunca
necesita consultar «dame todos los intercambios que incluyan manzanas»: siempre
lee la fila entera.

Los enumerados se guardan con el nombre de la constante Kotlin (`ALIMENTO`,
`COMPLETADO`, `DESEQUILIBRIO`), no con números, para que el volcado se entienda
sin abrir el código.

---

## 5. De dónde sale cada número del perfil

**Ningún contador se guarda.** Todo se calcula contando filas:

| Lo que ve el niño | Consulta |
|---|---|
| Intercambios aceptados | `SELECT COUNT(*) FROM trades WHERE aceptado = 1` |
| Intercambios propuestos | `SELECT COUNT(*) FROM trades` |
| Repartos de trabajo probados | `SELECT COUNT(*) FROM specialization_runs` |
| Trabajos en equipo terminados | `SELECT COUNT(*) FROM cooperation_runs WHERE completado = 1` |
| Repartos de lo escaso resueltos | `SELECT COUNT(*) FROM allocation_runs WHERE valido = 1` |
| Decisiones tomadas | `SELECT COUNT(*) FROM decision_runs WHERE alcanza = 1` |
| Cadenas ordenadas | `SELECT COUNT(*) FROM chain_runs WHERE correcto = 1` |
| Experimentos | `SELECT COUNT(*) FROM experiments` |
| Misiones completadas | `SELECT COUNT(*) FROM mission_progress WHERE estado IN ('COMPLETADO','DOMINADO')` |
| Conceptos descubiertos | `SELECT COUNT(*) FROM discovered_concepts` |
| Objetos del Almacén | `SELECT COUNT(*) FROM user_collection` |

Los **sellos del Valle** y el nivel se derivan de esos contadores en
`ProgressEngine`, no de una columna. Las insignias se recalculan contra los
contadores cada vez que ocurre algo relevante, así que no pueden quedar
desincronizadas.

Los desafíos pendientes de repaso también son una consulta:

```sql
SELECT DISTINCT escenarioId FROM scenario_attempts
WHERE logrado = 0
  AND escenarioId NOT IN (SELECT escenarioId FROM scenario_attempts WHERE logrado = 1);
```

---

## 6. Migraciones y borrado

`AppDatabase` está configurada con `fallbackToDestructiveMigration()`. Al no
haber datos personales y ser el catálogo reproducible desde el código, un cambio
de esquema vuelve a sembrar en lugar de arrastrar migraciones. Si en el futuro
se quisiera conservar el progreso entre versiones, habría que escribir
migraciones reales y subir `version`.

Desde Ajustes, «Empezar el Valle de cero» ejecuta
`DatabaseSeeder.reiniciarProgreso()`, que vacía **solo** las tablas de estado y
deja intacto el catálogo.

---

## 7. Comprobado de verdad

`database/schema.sql` y `database/sample_data.sql` se cargaron en un SQLite
real durante la preparación de la entrega y las consultas del apartado 5 se
ejecutaron con éxito sobre ellos. Recuentos verificados: 20 recursos, 9
personajes, 14 misiones, 42 escenarios, 11 insignias, 24 objetos y 13 entradas
de diccionario.
