# -*- coding: utf-8 -*-
"""Importa os SVGs de grade reais (pasta 'assets/imagens e grades moldes') para o app.

Para cada molde com SVG valido, gera um LAYOUT ESTATICO em config/layouts/
(layout-<codigo>-grade.json) que o diagrama interativo renderiza identico ao SVG
original (moldura, canais, pontos intermediarios, hubs/zonas clicaveis e
cavidades com raio/fonte originais), copia o SVG para assets/desenhos/
<codigo>-grade.svg e registra/atualiza a entrada em config/desenhos.json.

Regras:
  - 168 mantem o layout existente (layout-168-r05, ja validado no projeto).
  - 164 (SVG sem cavidades) e 173 (4 cavidades no SVG vs 8 do molde) sao
    IGNORADOS e reportados (mantem o layout generico atual).
  - Numeracao corrigida em 115/165/192/197/199 (export sem a cavidade 25 e com
    27 duplicado): reaproveita o padrao por grupos de 8 do proprio SVG.
  - DUO-PLUS nao tem molde no projeto: ignorado e reportado.
  - Moldes sem SVG (69, 81, 101, 109, 137, 147B, 161) mantem os layouts atuais.

Uso: python scripts/importar_svgs_grades.py [--aplicar]
Sem --aplicar roda em modo auditoria (nao grava nada).
"""
import io
import json
import math
import os
import re
import shutil
import sys
import xml.etree.ElementTree as ET

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8")

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WEB = os.path.join(RAIZ, "src", "main", "webapp")
BASE_SVGS = os.path.join(WEB, "assets", "imagens e grades moldes")
CFG = os.path.join(WEB, "config")
LAYOUTS = os.path.join(CFG, "layouts")
DEST_SVGS = os.path.join(WEB, "assets", "desenhos")
MAPA_SVGS = os.path.join(os.environ.get("TEMP", "."), "opencode", "svg_mapa.json")

NS = "{http://www.w3.org/2000/svg}"
APLICAR = "--aplicar" in sys.argv

# MOLDES COM NUMERACAO QUEBRADA NO EXPORT (falta 25, 27 duplicado)
CORRIGIR_IDS = {"115", "165", "192", "197", "199"}
# MOLDE DE REFERENCIA: mantem o layout proprio ja validado
MANTER_LAYOUT = {"168"}
# PULOS POR SVG INVALIDO/INCOMPLETO
PULOS = {"164": "SVG sem cavidades (apenas canais/hub)", "173": "4 cavidades no SVG vs 8 do molde"}


def num(v, casas=2):
    """Converte para numero redondo (arquivos menores, visual identico)."""
    f = float(v)
    r = round(f, casas)
    return int(r) if r == int(r) else r


def estilo_px(style):
    m = re.search(r"font:\s*\d+\s+(\d+(?:\.\d+)?)px", style or "")
    return float(m.group(1)) if m else None


def carregar(caminho):
    with open(caminho, encoding="utf-8") as f:
        return json.load(f)


def gravar(caminho, dados):
    txt = json.dumps(dados, ensure_ascii=False, indent=2)
    with open(caminho, "w", encoding="utf-8", newline="\r\n") as f:
        f.write(txt + "\r\n")


def ler_svg(cod, rel):
    """Extrai todos os elementos do SVG exportado do diagrama."""
    caminho = os.path.join(BASE_SVGS, rel)
    root = ET.fromstring(open(caminho, encoding="utf-8").read())
    out = {
        "viewBox": root.get("viewBox"),
        "fundo": [], "conexoes": [], "interms": [], "hubs": [], "cavidades": [],
        "_cav_txt": {}, "_hub_txt": {},
    }
    for el in root.iter():
        tag = el.tag.replace(NS, "")
        cls = (el.get("class") or "").split()
        if tag == "rect" and "moldura-rect" in cls:
            out["fundo"].append({"tag": "rect", "attrs": {
                "x": num(el.get("x")), "y": num(el.get("y")),
                "width": num(el.get("width")), "height": num(el.get("height")),
            }, "classes": ["moldura-rect"]})
        elif tag == "line" and "linha-conexao" in cls:
            out["conexoes"].append({"tag": "line", "attrs": {
                "x1": num(el.get("x1")), "y1": num(el.get("y1")),
                "x2": num(el.get("x2")), "y2": num(el.get("y2")),
            }, "classes": ["linha-conexao"]})
        elif tag == "circle":
            if "interm-circle" in cls:
                out["interms"].append({"x": num(el.get("cx")), "y": num(el.get("cy")),
                                       "r": num(el.get("r"))})
            elif "hub-circle" in cls:
                out["hubs"].append({"x": num(el.get("cx")), "y": num(el.get("cy")),
                                    "r": num(el.get("r"))})
            elif "cav-circle" in cls:
                out["cavidades"].append({"id": el.get("data-cav"), "x": num(el.get("cx")),
                                         "y": num(el.get("cy")), "r": num(el.get("r"))})
        elif tag == "text":
            if "cav-text" in cls and el.get("data-cav"):
                fs = estilo_px(el.get("style"))
                if fs:
                    out["_cav_txt"][el.get("data-cav")] = num(fs)
            elif "hub-text" in cls:
                out["_hub_txt"][(num(el.get("x")), num(el.get("y")))] = {
                    "label": (el.text or "").strip() or None,
                    "fontSize": estilo_px(el.get("style")),
                }
    return out


def corrigir_numeracao(cavs, esperados):
    """Repara o export quebrado: grupos de 8 no padrao [n, n+7, n+1..n+6]."""
    ids = [c["id"] for c in cavs]
    faltando = sorted(set(esperados) - set(ids), key=int)
    dup = sorted({i for i in ids if ids.count(i) > 1}, key=int)
    if not faltando or len(dup) != 1 or len(faltando) != 1:
        return False
    dup_id, falta_id = dup[0], faltando[0]
    # cada grupo de 8: elementos 2..7 definem a base inequivoca (base = min-1)
    for gi in range(0, len(cavs), 8):
        grupo = cavs[gi:gi + 8]
        if len(grupo) < 8 or dup_id not in [c["id"] for c in grupo]:
            continue
        bases = [int(c["id"]) for c in grupo[2:]]
        base = min(bases) - 1
        # valida o padrao do grupo antes de mexer
        if sorted(bases) != list(range(base + 1, base + 7)):
            return False
        if int(grupo[1]["id"]) != base + 7:
            return False
        if grupo[0]["id"] == dup_id:
            grupo[0]["id"] = falta_id
            return True
    return False


def montar_zonas(dados):
    """Atribui cavidades a cada hub via grafo das linhas (hub -> interms -> cavs)."""
    anchors = []  # (x, y, tipo, ref)
    for h in dados["hubs"]:
        anchors.append((h["x"], h["y"], "hub", h))
    for im in dados["interms"]:
        anchors.append((im["x"], im["y"], "interm", im))
    for c in dados["cavidades"]:
        anchors.append((c["x"], c["y"], "cav", c))

    def achar(x, y):
        melhor, dmin = None, 2.0  # tolerancia 2 unidades do viewBox
        for a in anchors:
            d = math.hypot(a[0] - x, a[1] - y)
            if d < dmin:
                melhor, dmin = a, d
        return melhor

    adj = {}
    for lz in dados["conexoes"]:
        at = lz["attrs"]
        a, b = achar(at["x1"], at["y1"]), achar(at["x2"], at["y2"])
        if not a or not b or a is b:
            continue
        adj.setdefault(id(a), []).append(b)
        adj.setdefault(id(b), []).append(a)

    for h in dados["hubs"]:
        vistas, fila, cavs = {id(h)}, [h], []
        while fila:
            no = fila.pop()
            for viz in adj.get(id(no), []):
                if id(viz) in vistas or viz[2] == "hub":
                    continue
                vistas.add(id(viz))
                if viz[2] == "cav":
                    cavs.append(viz[3]["id"])
                else:
                    fila.append(viz[3] if viz[2] == "interm" else viz)
        h["cavidades"] = sorted(set(cavs), key=int)


def main():
    if not os.path.exists(MAPA_SVGS):
        sys.exit("Mapeamento nao encontrado: " + MAPA_SVGS)
    mapa = carregar(MAPA_SVGS)
    catalogo = carregar(os.path.join(CFG, "moldes.json"))
    desenhos = carregar(os.path.join(CFG, "desenhos.json"))
    porcodigo = {m["codigo"]: m for m in catalogo["moldes"]}

    criados, atualizados, pulados, ignorados = [], [], [], []

    for cod in sorted(mapa, key=lambda x: (re.sub(r"\D", "", x).zfill(4), x)):
        if cod in PULOS:
            pulados.append("%s: %s (mantem layout atual)" % (cod, PULOS[cod]))
            continue
        if cod not in porcodigo:
            ignorados.append("%s: sem molde no projeto" % cod)
            continue
        molde = porcodigo[cod]
        esperados = [str(c) for c in molde["cavidades"]]
        dados = ler_svg(cod, mapa[cod][0])

        ids = [c["id"] for c in dados["cavidades"]]
        if sorted(ids, key=int) != sorted(esperados, key=int):
            if cod in CORRIGIR_IDS and corrigir_numeracao(dados["cavidades"], esperados):
                ids = [c["id"] for c in dados["cavidades"]]
            if sorted(ids, key=int) != sorted(esperados, key=int):
                pulados.append("%s: ids do SVG (%d cavs) nao batem com o molde (%d)" %
                               (cod, len(ids), len(esperados)))
                continue

        # rótulos com fonte original
        for c in dados["cavidades"]:
            fs = dados["_cav_txt"].get(c["id"])
            if fs:
                c["fontSize"] = fs
        for h in dados["hubs"]:
            info = dados["_hub_txt"].get((h["x"], h["y"]))
            if info:
                h["label"] = info["label"]
                if info["fontSize"]:
                    h["fontSize"] = info["fontSize"]
            h.setdefault("label", None)

        montar_zonas(dados)

        # preserva tabela/impressao do layout atual do molde (folha de impressao)
        tabela, impressao = {"blocos": 2}, None
        if molde.get("layoutId"):
            atual_path = os.path.join(LAYOUTS, molde["layoutId"] + ".json")
            if os.path.exists(atual_path):
                antigo = carregar(atual_path)
                tabela = antigo.get("tabela") or tabela
                impressao = antigo.get("impressao")

        layout_id = "layout-%s-grade" % cod
        layout = {
            "id": layout_id,
            "moldeId": molde["id"],
            "revisao": "1",
            "tipo": "estatico",
            "viewBox": dados["viewBox"],
            "formaPadrao": "forma-circulo-28",
            "observacao": ("Layout estatico gerado do SVG da grade real do molde "
                           "(%s). Molde %s com %d cavidades." %
                           (mapa[cod][0].replace("\\", "/"), cod, len(esperados))),
            "fundo": dados["fundo"],
            "conexoes": dados["conexoes"],
            "interms": dados["interms"],
            "hubs": dados["hubs"],
            "cavidades": dados["cavidades"],
            "tabela": tabela,
        }
        if impressao:
            layout["impressao"] = impressao

        if cod in MANTER_LAYOUT:
            ignorados.append("%s: mantem %s (ja validado); layout novo nao gerado" %
                             (cod, molde.get("layoutId")))
            continue

        if APLICAR:
            gravar(os.path.join(LAYOUTS, layout_id + ".json"), layout)
            shutil.copyfile(os.path.join(BASE_SVGS, mapa[cod][0]),
                            os.path.join(DEST_SVGS, "%s-grade.svg" % cod))

        # catlogo de desenhos (papel grade)
        entrada_id = "desenho-%s-grade" % cod
        entrada = next((d for d in desenhos["desenhos"]
                        if d.get("papel") == "grade" and d.get("moldeId") == molde["id"]), None)
        novo = entrada is None
        if entrada is None:
            entrada = {"id": entrada_id, "moldeId": molde["id"], "papel": "grade"}
            desenhos["desenhos"].append(entrada)
        entrada["imagem"] = "assets/desenhos/%s-grade.svg" % cod
        entrada["documento"] = entrada.get("documento")  # mantem raster original se existir
        entrada["origem"] = mapa[cod][0].replace("\\", "/")
        entrada["observacao"] = ("Grade do molde (SVG real do diagrama) integrada ao app "
                                 "como layout interativo %s." % layout_id)
        entrada.pop("id", None)
        entrada["id"] = entrada_id
        if novo:
            atualizados.append("%s: desenhos.json + layout %s" % (cod, layout_id))
        if APLICAR and molde.get("layoutId") != layout_id:
            molde["layoutId"] = layout_id
            atualizados.append("%s: layoutId -> %s" % (cod, layout_id))
        criados.append(cod)

    print("Moldes integrados (%d): %s" % (len(criados), ", ".join(criados)))
    print()
    if atualizados:
        print("Atualizacoes (%d):" % len(atualizados))
        for a in atualizados:
            print("  -", a)
        print()
    if pulados:
        print("PULADOS (%d):" % len(pulados))
        for p in pulados:
            print("  -", p)
        print()
    if ignorados:
        print("IGNORADOS/NOTAS (%d):" % len(ignorados))
        for i in ignorados:
            print("  -", i)
        print()
    print("Modo:", "APLICADO (arquivos gravados)" if APLICAR else "auditoria (nada gravado, use --aplicar)")
    if APLICAR:
        gravar(os.path.join(CFG, "moldes.json"), catalogo)
        gravar(os.path.join(CFG, "desenhos.json"), desenhos)
        print("moldes.json e desenhos.json gravados.")


if __name__ == "__main__":
    main()
