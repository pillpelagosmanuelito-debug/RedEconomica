#!/usr/bin/env python3
"""Genera database/sample_data.sql a partir del contenido semilla en Kotlin.

Lee los ficheros de `data/seed`, extrae el catálogo (recursos, habitantes,
misiones, escenarios, insignias, objetos y diccionario) y escribe los INSERT
equivalentes a lo que `DatabaseSeeder` deja en la base de datos la primera vez
que se abre la app. Así el SQL de la memoria no se escribe a mano y no puede
desviarse del código.

Uso:  python3 tools/generar_sample_data.py
"""
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SEED = os.path.join(ROOT, "app", "src", "main", "java", "com", "educalab",
                    "redeconomica", "data", "seed")


def leer(nombre):
    with open(os.path.join(SEED, nombre), encoding="utf-8") as f:
        return f.read()


def unir_cadenas(texto):
    """Convierte 'a" + "b' en 'ab' y limpia saltos de línea."""
    trozos = re.findall(r'"((?:[^"\\]|\\.)*)"', texto)
    return "".join(trozos)


def esc(valor):
    if valor is None:
        return "NULL"
    return "'" + str(valor).replace("'", "''") + "'"


lineas = [
    "-- =====================================================================",
    "-- RedEconómica v1.0.0 — Datos semilla del Valle Económico",
    "-- =====================================================================",
    "-- Generado por tools/generar_sample_data.py a partir de data/seed/*.kt",
    "-- Es exactamente el contenido que DatabaseSeeder escribe en el primer",
    "-- arranque de la app. Al final hay unas filas de ESTADO de ejemplo para",
    "-- ilustrar cómo queda la base de datos tras un rato de juego.",
    "-- =====================================================================",
    "",
]

# ---------------------------------------------------------------- recursos
src = leer("SeedResources.kt")
recursos = re.findall(
    r'ResourceDef\("([^"]+)",\s*"([^"]+)",\s*"([^"]+)",\s*ResourceType\.(\w+),\s*(\d+),\s*\n?\s*((?:"[^"]*"\s*\+?\s*)+)\)',
    src
)
lineas.append("-- ------------------------------ recursos (%d) ------------------------------" % len(recursos))
for rid, sing, plur, tipo, valor, desc in recursos:
    lineas.append(
        "INSERT INTO resources (id, singular, plural, tipo, valorBase, descripcion) VALUES "
        f"({esc(rid)}, {esc(sing)}, {esc(plur)}, {esc(tipo)}, {valor}, {esc(unir_cadenas(desc))});"
    )
lineas.append("")

# ------------------------------------------------------------- habitantes
src = leer("SeedCharacters.kt")
bloques = re.split(r'\n\s*val\s+[A-Z_]+\s*=\s*EconomicCharacter\(', src)[1:]
lineas.append("-- ---------------------------- habitantes -----------------------------")
for bloque in bloques:
    cuerpo = bloque.split("\n    )")[0]
    def campo(nombre):
        m = re.search(nombre + r'\s*=\s*((?:"[^"]*"\s*\+?\s*)+)', cuerpo)
        return unir_cadenas(m.group(1)) if m else ""
    cid = campo("id")
    if not cid:
        continue
    nombre = campo("nombre")
    oficio = campo("oficio")
    lugar_m = re.search(r'lugar\s*=\s*ValleyPlace\.(\w+)', cuerpo)
    lugar = lugar_m.group(1) if lugar_m else "PLAZA"
    presentacion = campo("presentacion")
    prod_m = re.search(r'productividad\s*=\s*(mapOf\((.*?)\)|emptyMap\(\))', cuerpo, re.S)
    productividad = ""
    if prod_m and prod_m.group(2):
        pares = re.findall(r'"(\w+)"\s+to\s+(\d+)', prod_m.group(2))
        productividad = "|".join(f"{k}:{v}" for k, v in sorted(pares))
    inv_m = re.search(r'inventario\s*=\s*(Inventory\.of\((.*?)\)|Inventory\.VACIO)', cuerpo, re.S)
    inventario = ""
    if inv_m and inv_m.group(2):
        pares = re.findall(r'"(\w+)"\s+to\s+(\d+)', inv_m.group(2))
        inventario = "|".join(f"{k}:{v}" for k, v in sorted(pares))
    es_guia = 1 if cid == "tilo" else 0
    avatar = cid
    lineas.append(
        "INSERT INTO characters (id, nombre, oficio, lugar, presentacion, productividad, "
        "inventarioBase, avatarId, esGuia) VALUES "
        f"({esc(cid)}, {esc(nombre)}, {esc(oficio)}, {esc(lugar)}, {esc(presentacion)}, "
        f"{esc(productividad)}, {esc(inventario)}, {esc(avatar)}, {es_guia});"
    )
lineas.append("")

# ---------------------------------------------------------------- misiones
src = leer("SeedMissions.kt")
bloques = src.split("MissionDef(")[1:]
lineas.append("-- ----------------------------- misiones ------------------------------")
escenarios_de_mision = {}
for bloque in bloques:
    def campo(nombre):
        m = re.search(nombre + r'\s*=\s*((?:"[^"]*"\s*\+?\s*)+)', bloque)
        return unir_cadenas(m.group(1)) if m else ""
    mid = campo("id")
    numero = re.search(r'numero\s*=\s*(\d+)', bloque).group(1)
    titulo = campo("titulo")
    lugar = re.search(r'lugar\s*=\s*ValleyPlace\.(\w+)', bloque).group(1)
    concepto = re.search(r'concepto\s*=\s*ConceptId\.(\w+)', bloque).group(1)
    ini = campo("narrativaInicio")
    fin = campo("narrativaFinal")
    obj = campo("objetivoVisible")
    req_m = re.search(r'requiereMision\s*=\s*"([^"]+)"', bloque)
    req = req_m.group(1) if req_m else None
    ins_m = re.search(r'insigniaId\s*=\s*"([^"]+)"', bloque)
    ins = ins_m.group(1) if ins_m else None
    zona_m = re.search(r'zonaDesbloqueada\s*=\s*ValleyPlace\.(\w+)', bloque)
    zona = zona_m.group(1) if zona_m else None
    sellos = re.search(r'sellos\s*=\s*(\d+)', bloque).group(1)
    escs = re.findall(r'"(s\d\d)"', re.search(r'escenarios\s*=\s*listOf\(([^)]*)\)', bloque).group(1))
    escenarios_de_mision[mid] = escs
    lineas.append(
        "INSERT INTO missions (id, numero, titulo, lugar, concepto, narrativaInicio, "
        "narrativaFinal, objetivoVisible, requiereMision, insigniaId, zonaDesbloqueada, sellos) "
        f"VALUES ({esc(mid)}, {numero}, {esc(titulo)}, {esc(lugar)}, {esc(concepto)}, "
        f"{esc(ini)}, {esc(fin)}, {esc(obj)}, {esc(req)}, {esc(ins)}, {esc(zona)}, {sellos});"
    )
lineas.append("")

# -------------------------------------------------------------- escenarios
src = ""
for f in ("SeedScenariosA.kt", "SeedScenariosB.kt", "SeedScenariosC.kt"):
    src += leer(f)
bloques = src.split("Scenario(\n")[1:]
mision_de = {}
for mid, escs in escenarios_de_mision.items():
    for i, e in enumerate(escs):
        mision_de[e] = (mid, i)

lineas.append("-- ---------------------------- escenarios -----------------------------")
n_esc = 0
for bruto in bloques:
    bloque = "\n" + bruto
    def campo(nombre):
        m = re.search(r'\n\s+' + nombre + r'\s*=\s*((?:"[^"]*"\s*\+?\s*\n?\s*)+)', bloque)
        return unir_cadenas(m.group(1)) if m else ""
    sid = campo("id")
    if not sid.startswith("s"):
        continue
    tipo = re.search(r'tipo\s*=\s*ActivityKind\.(\w+)', bloque).group(1)
    titulo = campo("titulo")
    situacion = campo("situacion")
    instruccion = campo("instruccion")
    explicacion = campo("explicacionFinal")
    concepto = re.search(r'conceptoId\s*=\s*ConceptId\.(\w+)\.name', bloque).group(1)
    dificultad = re.search(r'dificultad\s*=\s*(\d+)', bloque).group(1)
    mid, orden = mision_de.get(sid, ("", 0))
    n_esc += 1
    lineas.append(
        "INSERT INTO scenarios (id, misionId, orden, tipo, titulo, situacion, instruccion, "
        "explicacionFinal, conceptoId, dificultad) VALUES "
        f"({esc(sid)}, {esc(mid)}, {orden}, {esc(tipo)}, {esc(titulo)}, {esc(situacion)}, "
        f"{esc(instruccion)}, {esc(explicacion)}, {esc(concepto)}, {dificultad});"
    )
lineas.append("")

# ------------------------------------------- insignias, objetos, glosario
src = leer("SeedProgression.kt")

lineas.append("-- ----------------------------- insignias -----------------------------")
for m in re.finditer(
    r'Badge\("([^"]+)",\s*"([^"]+)",\s*\n\s*((?:"[^"]*"\s*\+?\s*)+),\s*\n\s*BadgeRule\.(\w+),\s*(\d+),\s*"([^"]+)"\)',
    src
):
    lineas.append(
        "INSERT INTO badges (id, nombre, descripcion, regla, meta, arteId) VALUES "
        f"({esc(m.group(1))}, {esc(m.group(2))}, {esc(unir_cadenas(m.group(3)))}, "
        f"{esc(m.group(4))}, {m.group(5)}, {esc(m.group(6))});"
    )
lineas.append("")

lineas.append("-- ------------------------- objetos del almacén ------------------------")
for m in re.finditer(
    r'CollectionItem\("([^"]+)",\s*"([^"]+)",\s*ResourceType\.(\w+),\s*\n\s*"([^"]*)",\s*"([^"]*)",\s*(?:"([^"]*)"|null)\)',
    src
):
    lineas.append(
        "INSERT INTO collection_items (id, nombre, familia, descripcion, comoSeConsigue, recursoId) "
        f"VALUES ({esc(m.group(1))}, {esc(m.group(2))}, {esc(m.group(3))}, {esc(m.group(4))}, "
        f"{esc(m.group(5))}, {esc(m.group(6)) if m.group(6) else 'NULL'});"
    )
lineas.append("")

lineas.append("-- ------------------------ diccionario del valle -----------------------")
for m in re.finditer(
    r'GlossaryEntry\("([^"]+)",\s*"([^"]+)",\s*\n\s*"([^"]*)",\s*\n\s*"([^"]*)",\s*\n\s*ConceptId\.(\w+),\s*"([^"]+)",\s*\n\s*(?:"([^"]*)"|null)\)',
    src
):
    lineas.append(
        "INSERT INTO glossary (id, termino, definicionInfantil, ejemplo, conceptoId, arteId, "
        f"miniActividad) VALUES ({esc(m.group(1))}, {esc(m.group(2))}, {esc(m.group(3))}, "
        f"{esc(m.group(4))}, {esc(m.group(5))}, {esc(m.group(6))}, "
        f"{esc(m.group(7)) if m.group(7) else 'NULL'});"
    )
lineas.append("")

# ------------------------------------------------------- estado de ejemplo
lineas += [
    "-- =====================================================================",
    "-- ESTADO DE EJEMPLO (así queda la base tras un rato jugando)",
    "-- =====================================================================",
    "INSERT INTO profile (id, alias, avatarId, onboardingHecho, sonidoActivo, vibracionActiva, "
    "textoGrande, creadoMillis) VALUES (1, 'Tuli', 'avatar_3', 1, 1, 1, 0, 1735689600000);",
    "",
    "INSERT INTO scenario_attempts (escenarioId, misionId, conceptoId, logrado, numeroDeIntento, fechaMillis) "
    "VALUES ('s01', 'm01', 'NECESIDADES', 1, 1, 1735689700000);",
    "INSERT INTO scenario_attempts (escenarioId, misionId, conceptoId, logrado, numeroDeIntento, fechaMillis) "
    "VALUES ('s02', 'm01', 'NECESIDADES', 0, 1, 1735689800000);",
    "INSERT INTO scenario_attempts (escenarioId, misionId, conceptoId, logrado, numeroDeIntento, fechaMillis) "
    "VALUES ('s02', 'm01', 'NECESIDADES', 1, 2, 1735689900000);",
    "INSERT INTO scenario_attempts (escenarioId, misionId, conceptoId, logrado, numeroDeIntento, fechaMillis) "
    "VALUES ('s03', 'm01', 'RECURSOS', 1, 1, 1735690000000);",
    "",
    "INSERT INTO mission_progress (misionId, estado, intentosTotales, sinFallos, actualizadoMillis) "
    "VALUES ('m01', 'COMPLETADO', 4, 0, 1735690000000);",
    "",
    "INSERT INTO trades (escenarioId, proponenteId, receptorId, entrega, pide, aceptado, motivo, fechaMillis) "
    "VALUES ('s04', 'jugador', 'tomas', 'manzana:1', 'pan:2', 0, 'DESEQUILIBRIO', 1735690100000);",
    "INSERT INTO trades (escenarioId, proponenteId, receptorId, entrega, pide, aceptado, motivo, fechaMillis) "
    "VALUES ('s04', 'jugador', 'tomas', 'manzana:3', 'pan:2', 1, NULL, 1735690200000);",
    "",
    "INSERT INTO specialization_runs (escenarioId, plan, produccionTotal, valorTotal, cumplioObjetivo, fechaMillis) "
    "VALUES ('s03', 'bruno=verdura|lia=manzana', 'manzana:6|verdura:6', 12, 1, 1735690300000);",
    "",
    "INSERT INTO cooperation_runs (escenarioId, plan, resultado, objetivo, resultadoSinCooperar, completado, fechaMillis) "
    "VALUES ('s19', 'dani=tejer|lia=esquilar|rita=repartir', 3, 3, 2, 1, 1735690400000);",
    "",
    "INSERT INTO allocation_runs (escenarioId, recursoId, disponible, reparto, valido, fechaMillis) "
    "VALUES ('s13', 'pan', 3, 'bruno:1|lia:2', 1, 1735690500000);",
    "",
    "INSERT INTO decision_runs (escenarioId, seleccion, renuncias, alcanza, fechaMillis) "
    "VALUES ('s16', 'silla|cesta', 'Una mesa grande', 1, 1735690600000);",
    "",
    "INSERT INTO user_collection (objetoId, fechaMillis) VALUES ('col_manzana', 1735690000000);",
    "INSERT INTO user_collection (objetoId, fechaMillis) VALUES ('col_verdura', 1735690000000);",
    "INSERT INTO user_badges (insigniaId, fechaMillis) VALUES ('primer_trato', 1735690200000);",
    "INSERT INTO discovered_concepts (conceptoId, fechaMillis) VALUES ('NECESIDADES', 1735689700000);",
    "INSERT INTO discovered_concepts (conceptoId, fechaMillis) VALUES ('RECURSOS', 1735690000000);",
    "INSERT INTO warehouse (recursoId, cantidad) VALUES ('manzana', 1);",
    "INSERT INTO warehouse (recursoId, cantidad) VALUES ('verdura', 1);",
    "",
    "INSERT INTO experiments (etiqueta, habitantes, turnos, modo, permiteIntercambio, ponenEnComun, "
    "produccionTotal, valorTotal, intercambios, necesidadesCubiertas, necesidadesTotales, fechaMillis) "
    "VALUES ('experimento', 4, 2, 'TODOS_DE_TODO', 0, 0, 'harina:2|madera:2|manzana:2|pan:2|tabla:2|verdura:2', "
    "20, 0, 0, 8, 1735690700000);",
    "INSERT INTO experiments (etiqueta, habitantes, turnos, modo, permiteIntercambio, ponenEnComun, "
    "produccionTotal, valorTotal, intercambios, necesidadesCubiertas, necesidadesTotales, fechaMillis) "
    "VALUES ('experimento', 4, 2, 'CADA_UNO_LO_SUYO', 0, 0, 'manzana:12|pan:10|silla:6|verdura:12', "
    "62, 0, 0, 8, 1735690800000);",
    "",
    "INSERT INTO daily_challenge (diaIndice, escenarioId, completado, fechaMillis) "
    "VALUES (20089, 's10', 1, 1735690900000);",
    "",
]

destino = os.path.join(ROOT, "database", "sample_data.sql")
with open(destino, "w", encoding="utf-8") as f:
    f.write("\n".join(lineas) + "\n")

print(f"Escrito {destino}")
print(f"  recursos: {len(recursos)}  escenarios: {n_esc}  misiones: {len(escenarios_de_mision)}")
