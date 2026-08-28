#!/usr/bin/env python3
"""Genera los PDF de la documentación de RedEconómica.

Convierte cada documento Markdown en HTML autocontenido con pandoc (con el CSS
EMBEBIDO dentro de <style>, nunca suelto) y lo imprime a PDF con wkhtmltopdf.

Uso:  python3 tools/generar_pdfs.py
"""
import os
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOCS = os.path.join(ROOT, "docs")
SALIDA = os.path.join(DOCS, "pdf")

DOCUMENTOS = [
    ("MEMORIA_DESCRIPTIVA.md", "Memoria descriptiva — RedEconómica v1.0.0"),
    ("MANUAL_USUARIO.md", "Manual de usuario — RedEconómica"),
    ("MANUAL_TECNICO.md", "Manual técnico — RedEconómica v1.0.0"),
    ("BASE_DE_DATOS.md", "Base de datos — RedEconómica v1.0.0"),
    ("BUILD_REPORT.md", "Informe de compilación — RedEconómica v1.0.0"),
]

CSS = """
@page { margin: 18mm 16mm; }
body {
  font-family: "DejaVu Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 10.5pt; line-height: 1.5; color: #2E2820; margin: 0;
}
h1 {
  font-size: 20pt; color: #2A6136; border-bottom: 3px solid #E8823A;
  padding-bottom: 6px; margin: 0 0 14px 0;
}
h2 {
  font-size: 14pt; color: #2A6136; margin: 22px 0 8px 0;
  border-left: 5px solid #E2B24F; padding-left: 9px;
}
h3 { font-size: 11.5pt; color: #7A4B2A; margin: 16px 0 6px 0; }
p { margin: 7px 0; text-align: justify; }
ul, ol { margin: 7px 0 7px 20px; padding: 0; }
li { margin: 3px 0; }
code {
  font-family: "DejaVu Sans Mono", monospace; font-size: 9pt;
  background: #F3EEE0; padding: 1px 4px; border-radius: 3px; color: #6B4A22;
}
pre {
  background: #F7F2E4; border-left: 4px solid #4CA05C; padding: 9px 11px;
  border-radius: 4px; overflow-x: auto; page-break-inside: avoid;
}
pre code { background: none; padding: 0; font-size: 8.5pt; color: #33291F; }
blockquote {
  border-left: 5px solid #D64F42; background: #FCEDE9; margin: 12px 0;
  padding: 8px 14px; page-break-inside: avoid;
}
blockquote p { margin: 4px 0; }
blockquote h1 { font-size: 15pt; border: none; color: #A83A30; margin: 4px 0; }
table {
  border-collapse: collapse; width: 100%; margin: 11px 0; font-size: 9.2pt;
  page-break-inside: avoid;
}
th {
  background: #2A6136; color: #FFFFFF; text-align: left;
  padding: 5px 7px; border: 1px solid #24512E;
}
td { padding: 5px 7px; border: 1px solid #D9CFB8; vertical-align: top; }
tr:nth-child(even) td { background: #FBF6EA; }
#title-block-header { display: none; }
hr { border: none; border-top: 1px solid #D9CFB8; margin: 18px 0; }
a { color: #2A6136; text-decoration: none; }
strong { color: #33291F; }
"""


def main() -> int:
    os.makedirs(SALIDA, exist_ok=True)
    generados = []
    for nombre, titulo in DOCUMENTOS:
        origen = os.path.join(DOCS, nombre)
        if not os.path.exists(origen):
            print(f"  ! falta {nombre}")
            continue
        destino = os.path.join(SALIDA, nombre.replace(".md", ".pdf"))

        with tempfile.TemporaryDirectory() as tmp:
            css_path = os.path.join(tmp, "estilo.css")
            with open(css_path, "w", encoding="utf-8") as f:
                f.write(CSS)
            html_path = os.path.join(tmp, "doc.html")
            subprocess.run(
                [
                    "pandoc", origen, "-f", "gfm", "-t", "html5",
                    "--standalone", "--embed-resources",
                    "--css", css_path,
                    "--variable", f"pagetitle={titulo}",
                    "-o", html_path,
                ],
                check=True,
            )
            subprocess.run(
                [
                    "wkhtmltopdf", "--quiet", "--enable-local-file-access",
                    "--encoding", "utf-8",
                    "--margin-top", "16mm", "--margin-bottom", "16mm",
                    "--margin-left", "14mm", "--margin-right", "14mm",
                    "--footer-font-size", "8",
                    "--footer-left", "RedEconómica v1.0.0",
                    "--footer-right", "[page] / [topage]",
                    "--footer-spacing", "6",
                    html_path, destino,
                ],
                check=True,
            )
        generados.append(destino)
        print(f"  ✓ {os.path.basename(destino)}  ({os.path.getsize(destino) // 1024} KB)")

    print(f"\n{len(generados)} PDF generados en docs/pdf/")
    return 0


if __name__ == "__main__":
    sys.exit(main())
