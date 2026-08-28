#!/usr/bin/env python3
"""Genera los iconos de lanzador PNG de RedEconómica.

El icono representa dos cestas intercambiando productos (una manzana y un pan)
sobre un fondo verde de valle. Todo se dibuja localmente con Pillow: no se
descarga ninguna imagen.

Uso:  python3 tools/generate_launcher_icons.py
"""
import math
import os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")

DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

VERDE = (63, 143, 79)
VERDE_OSC = (42, 97, 54)
CREMA = (253, 246, 231)
NARANJA = (232, 130, 58)
MARRON = (122, 75, 42)
ROJO = (214, 79, 66)
TRIGO = (226, 178, 79)


def draw_icon(size: int, rounded: bool) -> Image.Image:
    s = 512  # lienzo de trabajo, se reduce al final
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Fondo
    if rounded:
        d.ellipse([0, 0, s - 1, s - 1], fill=VERDE)
    else:
        d.rounded_rectangle([0, 0, s - 1, s - 1], radius=int(s * 0.22), fill=VERDE)

    # Colinas del valle
    d.pieslice([-int(s * 0.25), int(s * 0.52), int(s * 0.55), int(s * 1.05)],
               180, 360, fill=VERDE_OSC)
    d.pieslice([int(s * 0.45), int(s * 0.58), int(s * 1.2), int(s * 1.1)],
               180, 360, fill=VERDE_OSC)

    # Camino
    d.polygon([(int(s * 0.42), s), (int(s * 0.58), s),
               (int(s * 0.53), int(s * 0.62)), (int(s * 0.47), int(s * 0.62))],
              fill=CREMA)

    # Flechas de intercambio (dos arcos opuestos)
    cx, cy, r = s // 2, int(s * 0.44), int(s * 0.24)
    d.arc([cx - r, cy - r, cx + r, cy + r], 200, 340, fill=CREMA, width=int(s * 0.045))
    d.arc([cx - r, cy - r + int(s * 0.10), cx + r, cy + r + int(s * 0.10)],
          20, 160, fill=CREMA, width=int(s * 0.045))

    def arrow(px, py, ang):
        ln = int(s * 0.075)
        pts = []
        for a in (ang - 140, ang, ang + 140):
            rad = math.radians(a)
            pts.append((px + ln * math.cos(rad), py + ln * math.sin(rad)))
        d.polygon(pts, fill=CREMA)

    arrow(cx + r * math.cos(math.radians(340)), cy + r * math.sin(math.radians(340)), 60)
    arrow(cx - r * math.cos(math.radians(340)),
          cy + int(s * 0.10) + r * math.sin(math.radians(160)), 240)

    # Manzana (izquierda)
    ax, ay, ar = int(s * 0.30), int(s * 0.47), int(s * 0.085)
    d.ellipse([ax - ar, ay - ar, ax + ar, ay + ar], fill=ROJO)
    d.line([ax, ay - ar, ax + int(s * 0.02), ay - ar - int(s * 0.045)],
           fill=MARRON, width=int(s * 0.018))

    # Pan (derecha)
    bx, by = int(s * 0.70), int(s * 0.47)
    d.ellipse([bx - int(s * 0.10), by - int(s * 0.062),
               bx + int(s * 0.10), by + int(s * 0.062)], fill=TRIGO)
    for k in (-1, 0, 1):
        d.line([bx + k * int(s * 0.045) - int(s * 0.018), by - int(s * 0.028),
                bx + k * int(s * 0.045) + int(s * 0.018), by + int(s * 0.028)],
               fill=NARANJA, width=int(s * 0.014))

    return img.resize((size, size), Image.LANCZOS)


def main() -> None:
    for folder, size in DENSITIES.items():
        out = os.path.join(RES, folder)
        os.makedirs(out, exist_ok=True)
        draw_icon(size, False).save(os.path.join(out, "ic_launcher.png"))
        draw_icon(size, True).save(os.path.join(out, "ic_launcher_round.png"))
        print("escrito", folder, size)


if __name__ == "__main__":
    main()
