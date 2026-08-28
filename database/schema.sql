-- =====================================================================
-- RedEconómica v1.0.0 — Esquema de la base de datos local
-- =====================================================================
-- Motor: SQLite (a través de Room 2.6.1)
-- Archivo en el dispositivo: redeconomica.db
-- Versión del esquema Room: 1
--
-- Este archivo reproduce, en SQL legible, el esquema que Room genera a
-- partir de las entidades anotadas con @Entity. Sirve como documentación y
-- para poder inspeccionar la base de datos con cualquier visor de SQLite.
--
-- Dos bloques:
--   1. CATÁLOGO  → el contenido del Valle, sembrado al primer arranque.
--   2. ESTADO    → lo que el niño ha hecho de verdad.
--
-- Nota de diseño: los enumerados se guardan como TEXTO con el nombre de la
-- constante Kotlin (por ejemplo 'ALIMENTO', 'COMPLETADO'), y los mapas
-- pequeños (productividad, inventarios, planes) se guardan en un formato
-- compacto y legible: "manzana:6|verdura:3" y "lia=manzana|tomas=pan".
-- La alternativa —una tabla por cada par clave-valor— habría multiplicado
-- las tablas sin aportar ninguna consulta útil a la aplicación.
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. CATÁLOGO
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `resources` (
    `id`          TEXT    NOT NULL,
    `singular`    TEXT    NOT NULL,
    `plural`      TEXT    NOT NULL,
    `tipo`        TEXT    NOT NULL,   -- ALIMENTO | MATERIA_PRIMA | ELABORADO | HERRAMIENTA | TEXTIL
    `valorBase`   INTEGER NOT NULL,   -- peso pedagógico 1..4 (NO es un precio)
    `descripcion` TEXT    NOT NULL,
    PRIMARY KEY(`id`)
);

CREATE TABLE IF NOT EXISTS `characters` (
    `id`             TEXT    NOT NULL,
    `nombre`         TEXT    NOT NULL,
    `oficio`         TEXT    NOT NULL,
    `lugar`          TEXT    NOT NULL,   -- ValleyPlace
    `presentacion`   TEXT    NOT NULL,
    `productividad`  TEXT    NOT NULL,   -- "manzana:6|verdura:3"
    `inventarioBase` TEXT    NOT NULL,   -- "manzana:6"
    `avatarId`       TEXT    NOT NULL,
    `esGuia`         INTEGER NOT NULL,   -- 1 solo para Tilo
    PRIMARY KEY(`id`)
);

CREATE TABLE IF NOT EXISTS `missions` (
    `id`               TEXT    NOT NULL,
    `numero`           INTEGER NOT NULL,
    `titulo`           TEXT    NOT NULL,
    `lugar`            TEXT    NOT NULL,
    `concepto`         TEXT    NOT NULL,   -- ConceptId
    `narrativaInicio`  TEXT    NOT NULL,
    `narrativaFinal`   TEXT    NOT NULL,
    `objetivoVisible`  TEXT    NOT NULL,
    `requiereMision`   TEXT,
    `insigniaId`       TEXT,
    `zonaDesbloqueada` TEXT,
    `sellos`           INTEGER NOT NULL,
    PRIMARY KEY(`id`)
);

CREATE TABLE IF NOT EXISTS `scenarios` (
    `id`               TEXT    NOT NULL,
    `misionId`         TEXT    NOT NULL,
    `orden`            INTEGER NOT NULL,
    `tipo`             TEXT    NOT NULL,   -- ActivityKind
    `titulo`           TEXT    NOT NULL,
    `situacion`        TEXT    NOT NULL,
    `instruccion`      TEXT    NOT NULL,
    `explicacionFinal` TEXT    NOT NULL,
    `conceptoId`       TEXT    NOT NULL,
    `dificultad`       INTEGER NOT NULL,   -- 1..5
    PRIMARY KEY(`id`)
);
CREATE INDEX IF NOT EXISTS `index_scenarios_misionId` ON `scenarios` (`misionId`);

CREATE TABLE IF NOT EXISTS `badges` (
    `id`          TEXT    NOT NULL,
    `nombre`      TEXT    NOT NULL,
    `descripcion` TEXT    NOT NULL,
    `regla`       TEXT    NOT NULL,   -- BadgeRule
    `meta`        INTEGER NOT NULL,
    `arteId`      TEXT    NOT NULL,
    PRIMARY KEY(`id`)
);

CREATE TABLE IF NOT EXISTS `collection_items` (
    `id`             TEXT NOT NULL,
    `nombre`         TEXT NOT NULL,
    `familia`        TEXT NOT NULL,   -- ResourceType
    `descripcion`    TEXT NOT NULL,
    `comoSeConsigue` TEXT NOT NULL,
    `recursoId`      TEXT,
    PRIMARY KEY(`id`)
);

CREATE TABLE IF NOT EXISTS `glossary` (
    `id`                TEXT NOT NULL,
    `termino`           TEXT NOT NULL,
    `definicionInfantil` TEXT NOT NULL,
    `ejemplo`           TEXT NOT NULL,
    `conceptoId`        TEXT NOT NULL,
    `arteId`            TEXT NOT NULL,
    `miniActividad`     TEXT,
    PRIMARY KEY(`id`)
);


-- ---------------------------------------------------------------------
-- 2. ESTADO
-- ---------------------------------------------------------------------

-- Perfil local. Sin nombre real, sin correo, sin edad, sin ubicación.
CREATE TABLE IF NOT EXISTS `profile` (
    `id`               INTEGER NOT NULL,   -- siempre 1
    `alias`            TEXT    NOT NULL,
    `avatarId`         TEXT    NOT NULL,
    `onboardingHecho`  INTEGER NOT NULL,
    `sonidoActivo`     INTEGER NOT NULL,
    `vibracionActiva`  INTEGER NOT NULL,
    `textoGrande`      INTEGER NOT NULL,
    `creadoMillis`     INTEGER NOT NULL,
    PRIMARY KEY(`id`)
);

CREATE TABLE IF NOT EXISTS `warehouse` (
    `recursoId` TEXT    NOT NULL,
    `cantidad`  INTEGER NOT NULL,
    PRIMARY KEY(`recursoId`)
);

CREATE TABLE IF NOT EXISTS `mission_progress` (
    `misionId`          TEXT    NOT NULL,
    `estado`            TEXT    NOT NULL,   -- ModuleState
    `intentosTotales`   INTEGER NOT NULL,
    `sinFallos`         INTEGER NOT NULL,
    `actualizadoMillis` INTEGER NOT NULL,
    PRIMARY KEY(`misionId`)
);

CREATE TABLE IF NOT EXISTS `scenario_attempts` (
    `id`              INTEGER NOT NULL,
    `escenarioId`     TEXT    NOT NULL,
    `misionId`        TEXT    NOT NULL,
    `conceptoId`      TEXT    NOT NULL,
    `logrado`         INTEGER NOT NULL,
    `numeroDeIntento` INTEGER NOT NULL,
    `fechaMillis`     INTEGER NOT NULL,
    PRIMARY KEY(`id` AUTOINCREMENT)
);
CREATE INDEX IF NOT EXISTS `index_scenario_attempts_escenarioId` ON `scenario_attempts` (`escenarioId`);
CREATE INDEX IF NOT EXISTS `index_scenario_attempts_misionId`   ON `scenario_attempts` (`misionId`);

CREATE TABLE IF NOT EXISTS `trades` (
    `id`           INTEGER NOT NULL,
    `escenarioId`  TEXT    NOT NULL,
    `proponenteId` TEXT    NOT NULL,
    `receptorId`   TEXT    NOT NULL,
    `entrega`      TEXT    NOT NULL,   -- "manzana:3"
    `pide`         TEXT    NOT NULL,   -- "pan:2"
    `aceptado`     INTEGER NOT NULL,
    `motivo`       TEXT,               -- TradeRejectReason cuando se rechaza
    `fechaMillis`  INTEGER NOT NULL,
    PRIMARY KEY(`id` AUTOINCREMENT)
);
CREATE INDEX IF NOT EXISTS `index_trades_escenarioId` ON `trades` (`escenarioId`);

CREATE TABLE IF NOT EXISTS `specialization_runs` (
    `id`              INTEGER NOT NULL,
    `escenarioId`     TEXT    NOT NULL,
    `plan`            TEXT    NOT NULL,   -- "lia=manzana|tomas=pan|nina="
    `produccionTotal` TEXT    NOT NULL,
    `valorTotal`      INTEGER NOT NULL,
    `cumplioObjetivo` INTEGER NOT NULL,
    `fechaMillis`     INTEGER NOT NULL,
    PRIMARY KEY(`id` AUTOINCREMENT)
);
CREATE INDEX IF NOT EXISTS `index_specialization_runs_escenarioId` ON `specialization_runs` (`escenarioId`);

CREATE TABLE IF NOT EXISTS `cooperation_runs` (
    `id`                    INTEGER NOT NULL,
    `escenarioId`           TEXT    NOT NULL,
    `plan`                  TEXT    NOT NULL,
    `resultado`             INTEGER NOT NULL,
    `objetivo`              INTEGER NOT NULL,
    `resultadoSinCooperar`  INTEGER NOT NULL,
    `completado`            INTEGER NOT NULL,
    `fechaMillis`           INTEGER NOT NULL,
    PRIMARY KEY(`id` AUTOINCREMENT)
);
CREATE INDEX IF NOT EXISTS `index_cooperation_runs_escenarioId` ON `cooperation_runs` (`escenarioId`);

CREATE TABLE IF NOT EXISTS `allocation_runs` (
    `id`          INTEGER NOT NULL,
    `escenarioId` TEXT    NOT NULL,
    `recursoId`   TEXT    NOT NULL,
    `disponible`  INTEGER NOT NULL,
    `reparto`     TEXT    NOT NULL,   -- "lia:2|bruno:1"
    `valido`      INTEGER NOT NULL,
    `fechaMillis` INTEGER NOT NULL,
    PRIMARY KEY(`id` AUTOINCREMENT)
);
CREATE INDEX IF NOT EXISTS `index_allocation_runs_escenarioId` ON `allocation_runs` (`escenarioId`);

CREATE TABLE IF NOT EXISTS `decision_runs` (
    `id`          INTEGER NOT NULL,
    `escenarioId` TEXT    NOT NULL,
    `seleccion`   TEXT    NOT NULL,   -- "silla|cesta"
    `renuncias`   TEXT    NOT NULL,
    `alcanza`     INTEGER NOT NULL,
    `fechaMillis` INTEGER NOT NULL,
    PRIMARY KEY(`id` AUTOINCREMENT)
);
CREATE INDEX IF NOT EXISTS `index_decision_runs_escenarioId` ON `decision_runs` (`escenarioId`);

CREATE TABLE IF NOT EXISTS `chain_runs` (
    `id`              INTEGER NOT NULL,
    `escenarioId`     TEXT    NOT NULL,
    `ordenPropuesto`  TEXT    NOT NULL,   -- "c1|c2|c3|c4"
    `correcto`        INTEGER NOT NULL,
    `aciertosSeguidos` INTEGER NOT NULL,
    `fechaMillis`     INTEGER NOT NULL,
    PRIMARY KEY(`id` AUTOINCREMENT)
);
CREATE INDEX IF NOT EXISTS `index_chain_runs_escenarioId` ON `chain_runs` (`escenarioId`);

CREATE TABLE IF NOT EXISTS `user_badges` (
    `insigniaId`  TEXT    NOT NULL,
    `fechaMillis` INTEGER NOT NULL,
    PRIMARY KEY(`insigniaId`)
);

CREATE TABLE IF NOT EXISTS `user_collection` (
    `objetoId`    TEXT    NOT NULL,
    `fechaMillis` INTEGER NOT NULL,
    PRIMARY KEY(`objetoId`)
);

CREATE TABLE IF NOT EXISTS `discovered_concepts` (
    `conceptoId`  TEXT    NOT NULL,
    `fechaMillis` INTEGER NOT NULL,
    PRIMARY KEY(`conceptoId`)
);

CREATE TABLE IF NOT EXISTS `experiments` (
    `id`                  INTEGER NOT NULL,
    `etiqueta`            TEXT    NOT NULL,
    `habitantes`          INTEGER NOT NULL,
    `turnos`              INTEGER NOT NULL,
    `modo`                TEXT    NOT NULL,   -- LabMode
    `permiteIntercambio`  INTEGER NOT NULL,
    `ponenEnComun`        INTEGER NOT NULL,
    `produccionTotal`     TEXT    NOT NULL,
    `valorTotal`          INTEGER NOT NULL,
    `intercambios`        INTEGER NOT NULL,
    `necesidadesCubiertas` INTEGER NOT NULL,
    `necesidadesTotales`  INTEGER NOT NULL,
    `fechaMillis`         INTEGER NOT NULL,
    PRIMARY KEY(`id` AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS `daily_challenge` (
    `diaIndice`   INTEGER NOT NULL,   -- milisegundos / 86400000
    `escenarioId` TEXT    NOT NULL,
    `completado`  INTEGER NOT NULL,
    `fechaMillis` INTEGER NOT NULL,
    PRIMARY KEY(`diaIndice`)
);


-- ---------------------------------------------------------------------
-- Consultas de las que sale el progreso (todas cuentan filas reales)
-- ---------------------------------------------------------------------
-- Intercambios aceptados:
--   SELECT COUNT(*) FROM trades WHERE aceptado = 1;
-- Trabajos en equipo terminados:
--   SELECT COUNT(*) FROM cooperation_runs WHERE completado = 1;
-- Repartos de lo escaso resueltos:
--   SELECT COUNT(*) FROM allocation_runs WHERE valido = 1;
-- Desafíos pendientes de repaso:
--   SELECT DISTINCT escenarioId FROM scenario_attempts WHERE logrado = 0
--     AND escenarioId NOT IN (SELECT escenarioId FROM scenario_attempts WHERE logrado = 1);
