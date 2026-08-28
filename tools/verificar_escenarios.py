#!/usr/bin/env python3
"""Comprobación independiente del contenido semilla de RedEconómica.

Reimplementa en Python las reglas del motor económico (las mismas que están en
`domain/engine`) y las aplica a los 42 escenarios para comprobar que:

  - todo intercambio propuesto como solución sería aceptado por el motor;
  - todo escenario de especialización tiene al menos un reparto que cumple;
  - todo trabajo en equipo tiene al menos un reparto que alcanza el objetivo;
  - toda escasez se puede repartir atendiendo a las urgencias altas;
  - toda decisión tiene opciones asequibles y alguna renuncia real;
  - las respuestas esperadas de "¿aceptarías?" coinciden con el motor.

Es una red de seguridad extra sobre las pruebas JUnit, útil sobre todo en
entornos donde no se puede compilar el proyecto Android.

Uso:  python3 tools/verificar_escenarios.py
"""
from itertools import product

VALOR = {
    "manzana": 1, "verdura": 1, "trigo": 1, "harina": 2, "pan": 2, "pescado": 2,
    "leche": 1, "queso": 2, "miel": 2, "madera": 1, "tabla": 2, "mesa": 4,
    "silla": 3, "herramienta": 3, "clavo": 1, "lana": 1, "tela": 2, "manta": 3,
    "semilla": 1, "cesta": 2,
}

TOLERANCIA = 60
ALTA, MEDIA, BAJA = "ALTA", "MEDIA", "BAJA"

fallos = []


def valor(inv):
    return sum(VALOR[k] * v for k, v in inv.items())


def faltante(inv, needs, r):
    for (nr, nc, _u) in needs:
        if nr == r:
            return max(0, nc - inv.get(r, 0))
    return 0


def contiene(inv, otro):
    return all(inv.get(k, 0) >= v for k, v in otro.items())


def acepta(entrega, pide, a_inv, a_needs, b_inv, b_needs, mutuo=True):
    """Devuelve (aceptado, motivo)."""
    if not entrega or not pide:
        return False, "OFERTA_VACIA"
    if not contiene(a_inv, entrega):
        return False, "SIN_RECURSOS_PROPONENTE"
    if not contiene(b_inv, pide):
        return False, "SIN_RECURSOS_RECEPTOR"
    if not any(faltante(b_inv, b_needs, r) > 0 for r in entrega):
        return False, "NO_NECESITA_LO_OFRECIDO"
    b_tras = dict(b_inv)
    for k, v in pide.items():
        b_tras[k] = b_tras.get(k, 0) - v
    for k, v in entrega.items():
        b_tras[k] = b_tras.get(k, 0) + v
    for (nr, nc, nu) in b_needs:
        if nu == ALTA and b_inv.get(nr, 0) >= nc and b_tras.get(nr, 0) < nc:
            return False, "PERDERIA_LO_QUE_NECESITA"
    if valor(entrega) * 100 < valor(pide) * TOLERANCIA:
        return False, "DESEQUILIBRIO"
    if mutuo and not any(faltante(a_inv, a_needs, r) > 0 for r in pide):
        return False, "SIN_BENEFICIO_MUTUO"
    return True, None


def comprobar_intercambio(sid, jug_inv, jug_needs, socios, objetivo, pasos):
    """`pasos` = [(socio, entrega, pide)] que deberían llevar al objetivo."""
    inv = dict(jug_inv)
    socios = {k: (dict(v[0]), list(v[1])) for k, v in socios.items()}
    for (quien, entrega, pide) in pasos:
        b_inv, b_needs = socios[quien]
        ok, motivo = acepta(entrega, pide, inv, jug_needs, b_inv, b_needs)
        if not ok:
            fallos.append(f"{sid}: el trato con {quien} sería rechazado ({motivo})")
            return
        for k, v in entrega.items():
            inv[k] -= v
            b_inv[k] = b_inv.get(k, 0) + v
        for k, v in pide.items():
            inv[k] = inv.get(k, 0) + v
            b_inv[k] -= v
        if any(v < 0 for v in inv.values()) or any(v < 0 for v in b_inv.values()):
            fallos.append(f"{sid}: inventario negativo tras el trato con {quien}")
            return
    if not contiene(inv, objetivo):
        fallos.append(f"{sid}: los tratos no alcanzan el objetivo {objetivo} (queda {inv})")


def producir(personajes, plan):
    total = {}
    for pid, prod in personajes.items():
        elegido = plan.get(pid, "SIN_TAREA")
        if elegido == "SIN_TAREA":
            continue
        if elegido is None:
            posibles = [r for r, n in prod.items() if n > 0]
            k = len(posibles)
            for r in posibles:
                total[r] = total.get(r, 0) + prod[r] // k
        else:
            total[elegido] = total.get(elegido, 0) + prod.get(elegido, 0)
    return total


def comprobar_especializacion(sid, personajes, objetivo):
    opciones = []
    for pid, prod in personajes.items():
        posibles = [r for r, n in prod.items() if n > 0] + [None]
        opciones.append([(pid, o) for o in posibles])
    encontrados = 0
    for combo in product(*opciones):
        plan = dict(combo)
        total = producir(personajes, plan)
        if contiene(total, objetivo):
            encontrados += 1
    if encontrados == 0:
        fallos.append(f"{sid}: ningún reparto de trabajo alcanza {objetivo}")
    return encontrados


def comprobar_cooperacion(sid, personajes, etapas, objetivo):
    ids = list(etapas.keys())
    for e, rend in etapas.items():
        if not any(v > 0 for v in rend.values()):
            fallos.append(f"{sid}: la etapa {e} no la puede hacer nadie")
    encontrados = 0
    mejor = 0
    for combo in product(ids, repeat=len(personajes)):
        plan = dict(zip(personajes, combo))
        capacidades = []
        for e in ids:
            capacidades.append(sum(etapas[e].get(p, 0) for p, ee in plan.items() if ee == e))
        r = min(capacidades)
        mejor = max(mejor, r)
        if r >= objetivo:
            encontrados += 1
    if encontrados == 0:
        fallos.append(f"{sid}: ningún reparto llega a {objetivo} (máximo posible {mejor})")
    solos = sum(min(etapas[e].get(p, 0) for e in ids) for p in personajes)
    if mejor <= solos:
        fallos.append(
            f"{sid}: cooperar ({mejor}) no mejora el trabajo por separado ({solos})"
        )
    return encontrados


def comprobar_escasez(sid, disponible, demandas):
    urgentes = sum(c for (_p, c, u) in demandas if u == ALTA)
    if urgentes > disponible:
        fallos.append(f"{sid}: no se pueden cubrir las urgencias altas ({urgentes} > {disponible})")
    total = sum(c for (_p, c, _u) in demandas)
    return total > disponible


def comprobar_decision(sid, presupuesto, opciones, max_sel):
    asequibles = []
    for k in range(1, max_sel + 1):
        for combo in product(*[[(o, True), (o, False)] for o in opciones]):
            sel = [o for (o, incluido) in combo if incluido]
            if len(sel) != k:
                continue
            costo = {}
            for o in sel:
                for r, n in opciones[o].items():
                    costo[r] = costo.get(r, 0) + n
            if contiene(presupuesto, costo):
                asequibles.append(tuple(sorted(sel)))
    asequibles = sorted(set(asequibles))
    if not asequibles:
        fallos.append(f"{sid}: con ese presupuesto no se puede elegir nada")
    if any(len(s) == len(opciones) for s in asequibles):
        fallos.append(f"{sid}: alcanza para todo, no hay ninguna renuncia")
    return asequibles


# ---------------------------------------------------------------------------
# Escenarios
# ---------------------------------------------------------------------------

print("== Intercambios ==")

comprobar_intercambio(
    "s04", {"manzana": 5}, [("pan", 2, ALTA)],
    {"tomas": ({"pan": 5}, [("manzana", 3, ALTA)])},
    {"pan": 2}, [("tomas", {"manzana": 3}, {"pan": 2})]
)

comprobar_intercambio(
    "s06", {"verdura": 6}, [("herramienta", 2, ALTA)],
    {"sofia": ({"herramienta": 3}, [("verdura", 3, ALTA)])},
    {"herramienta": 2}, [("sofia", {"verdura": 4}, {"herramienta": 2})]
)

comprobar_intercambio(
    "s10", {"manzana": 6}, [("pan", 2, ALTA), ("tela", 1, MEDIA)],
    {
        "tomas": ({"pan": 4}, [("manzana", 3, ALTA)]),
        "dani": ({"tela": 3}, [("manzana", 2, ALTA)]),
    },
    {"pan": 2, "tela": 1},
    [("tomas", {"manzana": 3}, {"pan": 2}), ("dani", {"manzana": 2}, {"tela": 1})]
)

comprobar_intercambio(
    "s12", {"pescado": 4}, [("pan", 2, ALTA)],
    {
        "lia": ({"pan": 2, "manzana": 4}, [("pan", 2, ALTA), ("pescado", 1, MEDIA)]),
        "tomas": ({"pan": 3}, [("pescado", 2, ALTA)]),
    },
    {"pan": 2}, [("tomas", {"pescado": 2}, {"pan": 2})]
)

# El trato con Lía DEBE fallar: es la lección del escenario.
ok, motivo = acepta(
    {"pescado": 2}, {"pan": 2},
    {"pescado": 4}, [("pan", 2, ALTA)],
    {"pan": 2, "manzana": 4}, [("pan", 2, ALTA), ("pescado", 1, MEDIA)]
)
if ok or motivo != "PERDERIA_LO_QUE_NECESITA":
    fallos.append(f"s12: Lía debería negarse por quedarse sin pan, y da {ok}/{motivo}")

comprobar_intercambio(
    "s24", {"miel": 4}, [("manta", 1, ALTA), ("pan", 2, MEDIA)],
    {
        "dani": ({"manta": 2, "tela": 3}, [("miel", 2, ALTA)]),
        "tomas": ({"pan": 4}, [("miel", 2, ALTA)]),
    },
    {"manta": 1, "pan": 2},
    [("dani", {"miel": 2}, {"manta": 1}), ("tomas", {"miel": 2}, {"pan": 2})]
)

comprobar_intercambio(
    "s25", {"pescado": 5}, [("tela", 2, ALTA), ("clavo", 2, MEDIA)],
    {
        "dani": ({"tela": 4}, [("pescado", 2, ALTA)]),
        "sofia": ({"clavo": 5}, [("pescado", 2, ALTA)]),
        "rita": ({"leche": 3}, [("pescado", 1, MEDIA)]),
    },
    {"tela": 2, "clavo": 2},
    [("dani", {"pescado": 2}, {"tela": 2}), ("sofia", {"pescado": 2}, {"clavo": 2})]
)

comprobar_intercambio(
    "s30", {"cesta": 4}, [("miel", 2, MEDIA), ("queso", 1, MEDIA)],
    {
        "lia": ({"miel": 3}, [("cesta", 2, ALTA)]),
        "rita": ({"queso": 2, "leche": 3}, [("cesta", 2, ALTA)]),
    },
    {"miel": 2, "queso": 1},
    [("lia", {"cesta": 2}, {"miel": 2}), ("rita", {"cesta": 2}, {"queso": 1})]
)

comprobar_intercambio(
    "s42", {"manzana": 8},
    [("pan", 2, ALTA), ("tela", 1, MEDIA), ("clavo", 2, MEDIA)],
    {
        "tomas": ({"pan": 4}, [("manzana", 3, ALTA)]),
        "dani": ({"tela": 3}, [("manzana", 2, ALTA)]),
        "sofia": ({"clavo": 5}, [("manzana", 3, ALTA)]),
    },
    {"pan": 2, "tela": 1, "clavo": 2},
    [
        ("tomas", {"manzana": 3}, {"pan": 2}),
        ("dani", {"manzana": 2}, {"tela": 1}),
        ("sofia", {"manzana": 3}, {"clavo": 2}),
    ]
)

print("== ¿Aceptarías este trato? ==")

pruebas_oferta = [
    # sid, entrega, pide, prop_inv, prop_needs, rec_inv, rec_needs, esperado
    ("s05", {"leche": 1}, {"manzana": 4}, {"leche": 3}, [("manzana", 3, MEDIA)],
     {"manzana": 5}, [("pan", 2, ALTA)], False),
    ("s11", {"tabla": 2}, {"manzana": 4}, {"tabla": 2}, [("manzana", 4, ALTA)],
     {"manzana": 6}, [("tabla", 2, ALTA)], True),
    ("s26", {"clavo": 1}, {"manta": 2}, {"clavo": 4}, [("manta", 1, MEDIA)],
     {"manta": 2}, [("clavo", 2, ALTA)], False),
    ("s35", {"harina": 2}, {"trigo": 4}, {"harina": 3}, [("trigo", 3, ALTA)],
     {"trigo": 4}, [("harina", 2, ALTA)], True),
]
for (sid, entrega, pide, pi, pn, ri, rn, esperado) in pruebas_oferta:
    ok, motivo = acepta(entrega, pide, pi, pn, ri, rn, mutuo=False)
    if ok != esperado:
        fallos.append(f"{sid}: se esperaba aceptar={esperado} y el motor da {ok} ({motivo})")

print("== Especialización ==")

espec = {
    "s01": ({"lia": {"manzana": 6, "verdura": 3}}, {"manzana": 6}),
    "s03": ({"lia": {"manzana": 6, "verdura": 3}, "bruno": {"verdura": 6, "manzana": 2}},
            {"manzana": 6, "verdura": 6}),
    "s07": ({"lia": {"manzana": 6, "verdura": 3}, "tomas": {"pan": 5, "harina": 4}},
            {"manzana": 6, "pan": 5}),
    "s08": ({"lia": {"manzana": 6, "verdura": 3}, "nina": {"madera": 5, "tabla": 4},
             "sofia": {"herramienta": 4, "clavo": 6}},
            {"manzana": 6, "tabla": 4, "clavo": 6}),
    "s09": ({"bruno": {"verdura": 6, "trigo": 3}, "tomas": {"pan": 5, "harina": 4},
             "emi": {"pescado": 5, "cesta": 3}},
            {"verdura": 6, "pan": 5, "pescado": 5}),
    "s23": ({"lia": {"manzana": 6, "miel": 2}, "tomas": {"pan": 5, "harina": 4},
             "dani": {"lana": 5, "tela": 4}, "emi": {"pescado": 5, "cesta": 3}},
            {"manzana": 6, "pan": 5, "tela": 4, "pescado": 5}),
    "s28": ({"lia": {"manzana": 6, "miel": 2}, "bruno": {"verdura": 6, "semilla": 4},
             "tomas": {"pan": 5, "harina": 4}, "dani": {"tela": 4, "lana": 5}},
            {"manzana": 6, "verdura": 6, "pan": 5, "tela": 4}),
    "s34": ({"lia": {"manzana": 6, "verdura": 4}, "bruno": {"manzana": 3, "verdura": 3}},
            {"manzana": 6, "verdura": 3}),
    "s41": ({"lia": {"manzana": 6, "miel": 2}, "bruno": {"verdura": 6, "trigo": 3},
             "tomas": {"pan": 5, "harina": 4}, "dani": {"tela": 4, "lana": 5},
             "sofia": {"herramienta": 4, "clavo": 6}},
            {"manzana": 6, "verdura": 6, "pan": 5, "tela": 4, "herramienta": 4}),
}
for sid, (pers, obj) in espec.items():
    n = comprobar_especializacion(sid, pers, obj)
    print(f"  {sid}: {n} repartos válidos")

print("== Cooperación ==")

coop = {
    "s19": (["lia", "dani", "rita"], {
        "esquilar": {"lia": 3, "dani": 4, "rita": 2},
        "tejer": {"lia": 0, "dani": 4, "rita": 1},
        "repartir": {"lia": 2, "dani": 1, "rita": 4},
    }, 3),
    "s20": (["bruno", "tomas", "rita", "emi"], {
        "cultivar": {"bruno": 5, "tomas": 1, "rita": 1, "emi": 2},
        "hornear": {"tomas": 5, "bruno": 1, "rita": 0, "emi": 1},
        "llevar": {"rita": 5, "emi": 3, "bruno": 1, "tomas": 1},
    }, 5),
    "s22": (["nina", "sofia", "emi", "rita"], {
        "traer": {"emi": 4, "nina": 4, "rita": 2, "sofia": 1},
        "cortar": {"nina": 5, "sofia": 4, "emi": 1, "rita": 0},
        "montar": {"sofia": 5, "nina": 2, "rita": 1, "emi": 1},
        "repartir": {"rita": 5, "emi": 4, "nina": 1, "sofia": 1},
    }, 4),
    "s29": (["nina", "sofia", "emi", "rita"], {
        "tablas": {"nina": 4, "rita": 3, "emi": 2, "sofia": 0},
        "armar": {"sofia": 5, "nina": 3, "emi": 0, "rita": 0},
        "colocar": {"emi": 5, "rita": 4, "nina": 0, "sofia": 0},
    }, 5),
    "s38": (["nina", "sofia", "rita"], {
        "cortar": {"nina": 5, "sofia": 3, "rita": 0},
        "clavar": {"sofia": 5, "nina": 2, "rita": 0},
        "subir": {"rita": 4, "nina": 1, "sofia": 0},
    }, 4),
}
for sid, (pers, etapas, obj) in coop.items():
    n = comprobar_cooperacion(sid, pers, etapas, obj)
    print(f"  {sid}: {n} repartos válidos")

print("== Escasez ==")

escasez = {
    "s02": (6, [("tomas", 3, ALTA), ("nina", 2, MEDIA), ("dani", 1, BAJA)]),
    "s13": (3, [("lia", 2, ALTA), ("bruno", 1, ALTA), ("nina", 2, MEDIA),
                ("emi", 1, MEDIA), ("dani", 1, BAJA)]),
    "s14": (4, [("bruno", 2, ALTA), ("nina", 1, MEDIA), ("emi", 1, MEDIA), ("rita", 2, MEDIA)]),
    "s27": (3, [("lia", 1, ALTA), ("nina", 1, ALTA), ("emi", 1, MEDIA),
                ("bruno", 2, MEDIA), ("rita", 1, BAJA)]),
    "s32": (5, [("lia", 2, ALTA), ("bruno", 2, ALTA), ("emi", 1, MEDIA), ("rita", 2, MEDIA)]),
    "s37": (4, [("lia", 1, ALTA), ("bruno", 1, ALTA), ("nina", 2, MEDIA), ("emi", 2, MEDIA)]),
}
for sid, (disp, dem) in escasez.items():
    hay = comprobar_escasez(sid, disp, dem)
    print(f"  {sid}: escasez real = {hay}")

print("== Decisiones ==")

decisiones = {
    "s15": ({"trigo": 5}, {
        "moler": {"trigo": 3}, "sembrar": {"trigo": 2}, "fiesta": {"trigo": 4}}, 2),
    "s16": ({"madera": 10}, {
        "mesa": {"madera": 8}, "silla": {"madera": 5}, "cesta": {"madera": 3}}, 2),
    "s31": ({"madera": 10, "clavo": 4}, {
        "grande": {"madera": 8, "clavo": 3},
        "bancos": {"madera": 6, "clavo": 2},
        "cartel": {"madera": 2, "clavo": 1}}, 2),
    "s36": ({"lana": 8, "madera": 6}, {
        "mantas": {"lana": 6},
        "ropa": {"lana": 4, "madera": 2},
        "techo": {"madera": 5}}, 2),
}
for sid, (pres, ops, maxsel) in decisiones.items():
    combos = comprobar_decision(sid, pres, ops, maxsel)
    print(f"  {sid}: {len(combos)} combinaciones asequibles")

print("== Costo de oportunidad ==")

costos = [
    ("s17", 6, 3, 6, 3),    # Lía: 6 manzanas o 3 verduras, elige 6 -> renuncia 3
    ("s18", 4, 2, 2, 1),    # Nina: 4 tablas o 2 mesas, elige 2 -> renuncia 1
    ("s33", 6, 3, 4, 2),    # Bruno: 6 verduras o 3 trigos, elige 4 -> renuncia 2
]
for (sid, pa, pb, elegida, esperado) in costos:
    calculado = elegida * pb / pa
    if abs(calculado - esperado) > 1e-9:
        fallos.append(f"{sid}: renuncia calculada {calculado}, esperada {esperado}")
    if not (1 <= elegida <= pa):
        fallos.append(f"{sid}: la cantidad elegida no cabe en un turno")
    print(f"  {sid}: renuncia = {calculado}")

print()
if fallos:
    print(f"{len(fallos)} PROBLEMAS ENCONTRADOS:")
    for f in fallos:
        print("  -", f)
    raise SystemExit(1)
print("Todos los escenarios comprobados tienen solución y son coherentes.")
