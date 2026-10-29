# -*- coding: utf-8 -*-
"""
Extrai, das planilhas de grade do repositorio 'pastas' (github.com/lucasavila153/pastas):

  1. o DESENHO DA PECA INJETADA  (screenshot do SolidWorks embutido na planilha) e
  2. o DESENHO DA GRADE DO MOLDE (layout dos canais/cavidades), convertido para SVG
     IDENTICO ao original (a imagem raster vai embutida em base64 dentro do SVG,
     portanto os pixels sao exatamente os mesmos da planilha).

Saidas (dentro do projeto):
  src/main/webapp/assets/images/pecas/<codigo>.<ext>        -> peca injetada
  src/main/webapp/assets/desenhos/originais/<codigo>-grade.<ext>  -> raster de origem
  src/main/webapp/assets/desenhos/<codigo>-grade.svg        -> grade em SVG (identica)

Atualizacoes de configuracao:
  config/desenhos.json   -> entradas "desenho-<codigo>-peca" e "desenho-<codigo>-grade"
  config/moldes.json     -> molde.desenhoId aponta para a peca quando ainda estiver vazio
  docs/desenhos-extraidos.md -> relatorio do que foi extraido/nao encontrado

Uso:
  python scripts/extrair_desenhos.py <pasta_do_repositorio_pastas> [opcoes]
    --so-molde 189          processa apenas um molde
    --sem-config            nao mexe em desenhos.json / moldes.json
    --substituir            regrava arquivos que ja existem

Dependencias: pip install pillow olefile
"""

import argparse
import base64
import collections
import hashlib
import io
import json
import os
import re
import subprocess
import sys
import zipfile

try:
    from PIL import Image
except ImportError:  # pragma: no cover
    sys.exit("Falta o Pillow:  pip install pillow")

try:
    import olefile
except ImportError:  # pragma: no cover
    sys.exit("Falta o olefile:  pip install olefile")


# --------------------------------------------------------------------------- paths
AQUI = os.path.dirname(os.path.abspath(__file__))
PROJETO = os.path.dirname(AQUI)
WEBAPP = os.path.join(PROJETO, "src", "main", "webapp")
CONFIG = os.path.join(WEBAPP, "config")
ASSETS = os.path.join(WEBAPP, "assets")
DIR_PECAS = os.path.join(ASSETS, "images", "pecas")
DIR_DESENHOS = os.path.join(ASSETS, "desenhos")
DIR_ORIGINAIS = os.path.join(DIR_DESENHOS, "originais")
MOLDES_JSON = os.path.join(CONFIG, "moldes.json")
DESENHOS_JSON = os.path.join(CONFIG, "desenhos.json")
RELATORIO = os.path.join(PROJETO, "docs", "desenhos-extraidos.md")


# ------------------------------------------------------------------- leitura .xls
MSO_DRAWING_GROUP, MSO_DRAWING, MSO_CONTINUE = 0xEB, 0xEC, 0x3C
LOGO_COMPACTOR = (323, 75)          # logo repetido em todo cabecalho de planilha


def _blobs_desenho_xls(workbook):
    """Reconstroi os blobs Escher: registros MSODRAWINGGROUP/MSODRAWING + CONTINUE.
    (Em .xls a imagem e fragmentada em varios registros; sem isso o PNG sai corrompido.)"""
    n, i = len(workbook), 0
    blobs, atual = [], None
    while i + 4 <= n:
        tipo = int.from_bytes(workbook[i:i + 2], "little")
        comp = int.from_bytes(workbook[i + 2:i + 4], "little")
        if comp > n - i - 4:          # desalinhamento: avanca um byte e tenta de novo
            i += 1
            continue
        dados = workbook[i + 4:i + 4 + comp]
        if tipo in (MSO_DRAWING_GROUP, MSO_DRAWING):
            if atual is not None:
                blobs.append(atual)
            atual = bytes(dados)
        elif tipo == MSO_CONTINUE and atual is not None:
            atual += dados
        else:
            if atual is not None:
                blobs.append(atual)
                atual = None
        i += 4 + comp
    if atual is not None:
        blobs.append(atual)
    return blobs


def _carve(bloco):
    """Localiza PNG/JPEG/EMF dentro de um blob Escher e devolve (bytes, formato, tamanho)."""
    achados = []

    i = 0
    while True:
        i = bloco.find(b"\x89PNG\r\n\x1a\n", i)
        if i < 0:
            break
        fim = bloco.find(b"IEND\xae\x42\x60\x82", i)
        if fim < 0:
            break
        achados.append((bloco[i:fim + 8], "PNG"))
        i = fim + 8

    i = 0
    while True:
        i = bloco.find(b"\xff\xd8\xff", i)
        if i < 0:
            break
        fim = bloco.find(b"\xff\xd9", i + 3)
        if fim < 0:
            break
        achados.append((bloco[i:fim + 2], "JPEG"))
        i = fim + 2

    i = 0
    while True:                                   # EMF: assinatura "FMEL" no offset 40
        i = bloco.find(b"FMEL", i)
        if i < 0:
            break
        ini = i - 40
        if ini >= 0:
            tam = int.from_bytes(bloco[ini:ini + 4], "little")
            if 2048 < tam < 20_000_000 and ini + tam <= len(bloco):
                achados.append((bloco[ini:ini + tam], "EMF"))
        i += 4

    saida, visto = [], set()
    for dados, fmt in achados:
        h = hashlib.md5(dados).hexdigest()
        if h in visto:
            continue
        visto.add(h)
        if fmt == "EMF":
            if int.from_bytes(dados[40:44], "little") == 0x464D454C:
                saida.append((dados, fmt, None))
            continue
        try:
            im = Image.open(io.BytesIO(dados))
            im.load()
            saida.append((dados, fmt, im.size))
        except Exception:
            pass                                  # fragmento invalido: ignora
    return saida


def _imagens_xls(caminho):
    try:
        ole = olefile.OleFileIO(caminho)
        stream = None
        for nome in ole.listdir():
            if len(nome) == 1 and nome[0] in ("Workbook", "Book"):
                stream = ole.openstream(nome[0]).read()
                break
        ole.close()
    except Exception:
        return []
    if not stream:
        return []
    res = []
    for blob in _blobs_desenho_xls(stream):
        res += _carve(blob)
    return res


# ------------------------------------------------------------------ leitura .xlsx
def _imagens_xlsx(caminho):
    res = []
    try:
        z = zipfile.ZipFile(caminho)
    except Exception:
        return res
    for nome in z.namelist():
        if "/media/" not in nome:
            continue
        dados = z.read(nome)
        try:
            im = Image.open(io.BytesIO(dados))
            im.load()
        except Exception:
            continue
        fmt = "PNG" if dados[:8] == b"\x89PNG\r\n\x1a\n" else \
              "JPEG" if dados[:2] == b"\xff\xd8" else "OUTRO"
        res.append((dados, fmt, im.size))
    z.close()
    return res


# ------------------------------------------------------------------- EMF -> PNG
def _emf_para_png(dados_emf, destino):
    """Converte EMF para PNG usando GDI+ (Windows). Devolve True se deu certo."""
    pasta_tmp = os.path.join(os.environ.get("TEMP", "."), "emf_tmp")
    os.makedirs(pasta_tmp, exist_ok=True)
    emf = os.path.join(pasta_tmp, hashlib.md5(dados_emf).hexdigest()[:16] + ".emf")
    with open(emf, "wb") as f:
        f.write(dados_emf)
    ps = (
        "Add-Type -AssemblyName System.Drawing; "
        "$img=[System.Drawing.Image]::FromFile('%s'); "
        "$w=[Math]::Min($img.Width,4000); $h=[Math]::Min($img.Height,4000); "
        "$bmp=New-Object System.Drawing.Bitmap($w,$h); "
        "$g=[System.Drawing.Graphics]::FromImage($bmp); "
        "$g.Clear([System.Drawing.Color]::White); "
        "$g.DrawImage($img,0,0,$w,$h); "
        "$bmp.Save('%s',[System.Drawing.Imaging.ImageFormat]::Png); "
        "$g.Dispose(); $bmp.Dispose(); $img.Dispose()"
    ) % (emf, destino)
    try:
        subprocess.run(["powershell", "-NoProfile", "-Command", ps],
                       check=True, capture_output=True, timeout=60)
        return os.path.exists(destino)
    except Exception:
        return False


# -------------------------------------------------------------------- classificacao
def _e_logo(size):
    return size == LOGO_COMPACTOR


def _e_grade(size):
    w, h = size
    if w < 300 or h < 300:
        return False
    return abs(w - h) / float(max(w, h)) < 0.40      # aproximadamente quadrado


def _e_peca(size):
    w, h = size
    return min(w, h) >= 200                          # foto/desenho de peca


def classificar(imagens):
    """imagens: lista de (dados, fmt, size). Devolve (imagem_peca, imagem_grade)."""
    validas = [x for x in imagens if x[2] and not _e_logo(x[2])]
    grades = [x for x in validas if _e_grade(x[2])]
    pecas = [x for x in validas if not _e_grade(x[2]) and _e_peca(x[2])]
    grade = max(grades, key=lambda x: x[2][0] * x[2][1]) if grades else None
    peca = max(pecas, key=lambda x: x[2][0] * x[2][1]) if pecas else None
    # se so existe uma imagem quadrada grande e nenhuma de peca, ela vira a grade;
    # se so existe uma imagem grande nao-quadrada, ela vira a peca (comportamento acima)
    return peca, grade


# ------------------------------------------------------------------------ SVG
def svg_identico(dados, fmt, size, destino):
    """Grava um SVG cujo conteudo e exatamente a imagem original (em base64)."""
    mime = {"PNG": "image/png", "JPEG": "image/jpeg", "GIF": "image/gif"}.get(fmt, "image/png")
    b64 = base64.b64encode(dados).decode("ascii")
    w, h = size
    svg = (
        '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"\n'
        '     width="{w}" height="{h}" viewBox="0 0 {w} {h}">\n'
        '  <!-- Gerado por scripts/extrair_desenhos.py: desenho da grade do molde,\n'
        '       copia identica (mesmos pixels) da imagem embutida na planilha. -->\n'
        '  <image x="0" y="0" width="{w}" height="{h}" xlink:href="data:{mime};base64,{b64}"/>\n'
        '</svg>\n'
    ).format(w=w, h=h, mime=mime, b64=b64)
    with open(destino, "w", encoding="ascii") as f:
        f.write(svg)


# ----------------------------------------------------------------------- principal
def codigo_de(relatorio, nome):
    for seg in relatorio.split(os.sep):
        m = re.search(r"(?i)(?:molde|mol\.)\s*([0-9]+\s*[A-Za-z]?)", seg)
        if m:
            return re.sub(r"\s+", "", m.group(1)).upper()
    m = re.search(r"(?i)(?:molde|mol\.)\s*([0-9]+\s*[A-Za-z]?)", nome)
    return re.sub(r"\s+", "", m.group(1)).upper() if m else ""


def main():
    ap = argparse.ArgumentParser(description="Extrai desenho de peca e grade (SVG) das planilhas.")
    ap.add_argument("repo", help="pasta clonada do repositorio lucasavila153/pastas")
    ap.add_argument("--so-molde", help="processa apenas este codigo de molde")
    ap.add_argument("--sem-config", action="store_true", help="nao altera desenhos.json/moldes.json")
    ap.add_argument("--substituir", action="store_true", help="regrava arquivos existentes")
    ap.add_argument("--limite-planilhas", type=int, default=15,
                    help="quantas planilhas (mais recentes) examinar por molde (padrao 15)")
    args = ap.parse_args()

    repo = os.path.abspath(args.repo)
    if not os.path.isdir(repo):
        sys.exit("Pasta do repositorio nao encontrada: " + repo)

    moldes_doc = json.load(open(MOLDES_JSON, encoding="utf-8"))
    moldes = moldes_doc["moldes"]
    if args.so_molde:
        alvo = args.so_molde.upper()
        moldes = [m for m in moldes if m["codigo"].upper() == alvo]
        if not moldes:
            sys.exit("Molde nao encontrado no config/moldes.json: " + args.so_molde)

    # indice unico: codigo -> planilhas (mais recentes primeiro)
    indice = collections.defaultdict(list)
    for raiz, _, arqs in os.walk(repo):
        for nome in arqs:
            if not nome.lower().endswith((".xls", ".xlsx")) or nome.startswith("~$"):
                continue
            caminho = os.path.join(raiz, nome)
            cod = codigo_de(os.path.relpath(caminho, repo), nome)
            if cod:
                indice[cod].append(caminho)
    for cod in indice:
        indice[cod].sort(reverse=True)

    for d in (DIR_PECAS, DIR_DESENHOS, DIR_ORIGINAIS):
        os.makedirs(d, exist_ok=True)

    desenhos_doc = json.load(open(DESENHOS_JSON, encoding="utf-8"))
    por_id = {d["id"]: d for d in desenhos_doc["desenhos"]}
    linhas_rel = ["# Desenhos extraídos das planilhas", "",
                  f"Repositório: `{repo}`", "",
                  "| Molde | Peça injetada | Desenho da grade | Origem |",
                  "|---|---|---|---|"]
    novos_desenhos = []
    alterados_moldes = 0

    for molde in moldes:
        codigo = molde["codigo"]
        mid = molde["id"]
        planilhas = indice.get(codigo.upper(), [])
        if not planilhas:
            linhas_rel.append(f"| **{codigo}** | – | – | sem planilhas no repositório |")
            continue

        melhor_peca = melhor_grade = None
        origem_peca = origem_grade = ""
        for caminho in planilhas[:args.limite_planilhas]:
            imgs = _imagens_xlsx(caminho) if caminho.lower().endswith(".xlsx") else _imagens_xls(caminho)
            peca, grade = classificar(imgs)
            if peca and (melhor_peca is None or peca[2][0] * peca[2][1] > melhor_peca[2][0] * melhor_peca[2][1]):
                melhor_peca, origem_peca = peca, os.path.relpath(caminho, repo)
            if grade and (melhor_grade is None or grade[2][0] * grade[2][1] > melhor_grade[2][0] * melhor_grade[2][1]):
                melhor_grade, origem_grade = grade, os.path.relpath(caminho, repo)
            if melhor_peca and melhor_grade:
                break

        celula_peca = "–"
        celula_grade = "–"
        arq_peca = arq_grade = None
        origem = origem_peca or origem_grade or planilhas[0]

        # ---------------- peça injetada
        if melhor_peca:
            dados, fmt, size = melhor_peca
            ext = {"PNG": "png", "JPEG": "jpg", "GIF": "gif"}.get(fmt, "png").lower()
            arq_peca = f"{codigo}.{ext}"
            destino = os.path.join(DIR_PECAS, arq_peca)
            if args.substituir or not os.path.exists(destino):
                open(destino, "wb").write(dados)
            celula_peca = f"assets/images/pecas/{arq_peca} ({size[0]}×{size[1]})"

        # ---------------- desenho da grade -> SVG identico
        if melhor_grade:
            dados, fmt, size = melhor_grade
            if fmt == "EMF":
                png_tmp = os.path.join(DIR_ORIGINAIS, f"{codigo}-grade.png")
                if _emf_para_png(dados, png_tmp):
                    dados = open(png_tmp, "rb").read()
                    fmt, size = "PNG", Image.open(io.BytesIO(dados)).size
                else:
                    celula_grade = "EMF não convertido"
                    melhor_grade = None
            if melhor_grade:
                ext = {"PNG": "png", "JPEG": "jpg"}.get(fmt, "png").lower()
                arq_grade = f"{codigo}-grade.{ext}"
                orig = os.path.join(DIR_ORIGINAIS, arq_grade)
                if args.substituir or not os.path.exists(orig):
                    open(orig, "wb").write(dados)
                svg = os.path.join(DIR_DESENHOS, f"{codigo}-grade.svg")
                if args.substituir or not os.path.exists(svg):
                    svg_identico(dados, fmt, size, svg)
                celula_grade = f"assets/desenhos/{codigo}-grade.svg ({size[0]}×{size[1]})"

        # ---------------- configuracao
        if not args.sem_config:
            if arq_peca:
                pid = f"desenho-{codigo}-peca"
                if pid not in por_id:
                    entrada = {
                        "id": pid,
                        "moldeId": mid,
                        "papel": "peca",
                        "imagem": "assets/images/pecas/" + arq_peca,
                        "documento": None,
                        "origem": origem_peca,
                        "observacao": "Desenho da peca injetada extraido da planilha do repositorio 'pastas'."
                    }
                    novos_desenhos.append(entrada)
                    por_id[pid] = entrada
            if arq_grade and melhor_grade:
                gid = f"desenho-{codigo}-grade"
                if gid not in por_id:
                    entrada = {
                        "id": gid,
                        "moldeId": mid,
                        "papel": "grade",
                        "imagem": f"assets/desenhos/{codigo}-grade.svg",
                        "documento": "assets/desenhos/originais/" + arq_grade,
                        "origem": origem_grade,
                        "observacao": "Desenho da grade do molde (identico ao da planilha) convertido para SVG."
                    }
                    novos_desenhos.append(entrada)
                    por_id[gid] = entrada
            if arq_peca and not molde.get("desenhoId"):
                molde["desenhoId"] = f"desenho-{codigo}-peca"
                alterados_moldes += 1

        linhas_rel.append(f"| **{codigo}** | {celula_peca} | {celula_grade} | `{origem}` |")
        print(f"molde {codigo:<5} peca={'sim' if melhor_peca else 'nao':<3} "
              f"grade={'sim' if melhor_grade else 'nao':<3} (de {len(planilhas)} planilhas)")

    if not args.sem_config:
        if novos_desenhos:
            desenhos_doc["desenhos"] = list(por_id.values())
            desenhos_doc["observacao"] = (
                desenhos_doc.get("observacao", "") +
                " Entradas com 'papel' vieram da extracao automatica das planilhas do repositorio 'pastas'."
            )
        with open(DESENHOS_JSON, "w", encoding="utf-8") as f:
            json.dump(desenhos_doc, f, ensure_ascii=False, indent=2)
            f.write("\n")
        if alterados_moldes:
            # usa o documento ja lido no inicio (nao reabre o arquivo durante a escrita)
            saida_moldes = {"versao": moldes_doc.get("versao", 1),
                            "observacao": moldes_doc.get("observacao", ""),
                            "moldes": moldes_doc["moldes"]}
            with open(MOLDES_JSON, "w", encoding="utf-8") as f:
                json.dump(saida_moldes, f, ensure_ascii=False, indent=2)
                f.write("\n")

    os.makedirs(os.path.dirname(RELATORIO), exist_ok=True)
    with open(RELATORIO, "w", encoding="utf-8") as f:
        f.write("\n".join(linhas_rel) + "\n")
    print("\nrelatorio:", RELATORIO)


if __name__ == "__main__":
    main()
