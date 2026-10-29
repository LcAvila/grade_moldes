# -*- coding: utf-8 -*-
"""Auditoria dos desenhos de peca ('desenho - molde N.png') nas pastas do usuario.

Cruza os PNGs com config/moldes.json e config/desenhos.json, compara com as
pecas ja existentes em assets/images/pecas (md5) e mostra o plano de importacao.
Sem gravar nada.
"""
import io
import os
import re
import sys
import json
import hashlib

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8")

WEB = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                   "src", "main", "webapp")
BASE = os.path.join(WEB, "assets", "imagens e grades moldes")
PECAS = os.path.join(WEB, "assets", "images", "pecas")

RE_DESENHO = re.compile(r"(?i)^desenho\s*-\s*molde\s*(.+?)\s*\.png$")


def md5(p):
    return hashlib.md5(open(p, "rb").read()).hexdigest()


def md5_bytes(b):
    return hashlib.md5(b).hexdigest()


moldes = json.load(open(os.path.join(WEB, "config", "moldes.json"), encoding="utf-8"))["moldes"]
codigos = {m["codigo"]: m for m in moldes}
desenhos = json.load(open(os.path.join(WEB, "config", "desenhos.json"), encoding="utf-8"))["desenhos"]
por_molde = {}
for d in desenhos:
    if d.get("papel") == "peca":
        por_molde[d["moldeId"]] = d

# varre as pastas
achados = {}   # cod -> [rel paths]
for raiz, dirs, arqs in os.walk(BASE):
    for n in arqs:
        m = RE_DESENHO.match(n)
        if not m:
            continue
        cod = m.group(1).strip().upper()
        rel = os.path.relpath(os.path.join(raiz, n), BASE)
        achados.setdefault(cod, []).append(rel)

pecas_existentes = {}
if os.path.isdir(PECAS):
    for n in os.listdir(PECAS):
        if n.lower().endswith(".png"):
            pecas_existentes[re.match(r"(?i)^([0-9A-Z]+)", n).group(1).upper()] = n

print("desenhos de peca encontrados: %d arquivos para %d codigos" %
      (sum(len(v) for v in achados.values()), len(achados)))
print()

sem_molde, duplicados, conflitos, novos, iguais, atualiz = [], [], [], [], [], []
for cod in sorted(achados, key=lambda x: (re.sub(r"\D", "", x).zfill(4), x)):
    rels = achados[cod]
    if cod not in codigos:
        sem_molde.append((cod, rels))
        continue
    hashes = {}
    for rel in rels:
        hashes.setdefault(md5(os.path.join(BASE, rel)), []).append(rel)
    if len(rels) > 1:
        duplicados.append((cod, rels, "IDENTICOS" if len(hashes) == 1 else "DIFERENTES"))
    src = os.path.join(BASE, rels[0])
    h = md5(src)
    alvo = pecas_existentes.get(cod)
    entrada = por_molde.get(codigos[cod]["id"])
    if alvo:
        he = md5(os.path.join(PECAS, alvo))
        if he == h:
            iguais.append(cod)          # ja la, igual
        else:
            conflitos.append((cod, alvo, bool(entrada)))
    else:
        novos.append(cod)               # precisa copiar

print("NOVOS (sem png em assets/images/pecas): %d" % len(novos))
print("  ", novos)
print()
print("IGUAIS (ja existem identicos): %d" % len(iguais))
print("  ", iguais)
print()
print("CONFLITOS (ja existe png DIFERENTE): %d" % len(conflitos))
for cod, alvo, tem_entrada in conflitos:
    print("   %s -> %s existente | entrada desenhos.json: %s" % (cod, alvo, "sim" if tem_entrada else "nao"))
print()
print("DUPLICADOS em pastas diferentes: %d" % len(duplicados))
for cod, rels, tipo in duplicados:
    print("   %s [%s]" % (cod, tipo))
    for rel in rels:
        print("      ", rel.replace("\\", "/"))
print()
print("SEM MOLDE no projeto: %d" % len(sem_molde))
for cod, rels in sem_molde:
    print("   %s -> %s" % (cod, [r.replace("\\", "/") for r in rels]))
print()

# cobertura: moldes com/sem desenhoId e quao dos achados cobrem
com_id = [m for m in moldes if m.get("desenhoId")]
sem_id = [m for m in moldes if not m.get("desenhoId")]
cobertos = [m["codigo"] for m in moldes if m["codigo"] in achados]
print("moldes: %d total | com desenhoId: %d | sem desenhoId: %d" %
      (len(moldes), len(com_id), len(sem_id)))
print("codigos com desenho na pasta: %d" % len(cobertos))
print("  ainda sem desenho (nem pasta nem cadastro):",
      [m["codigo"] for m in moldes if m["codigo"] not in achados and not m.get("desenhoId")])

# detalhe dos que ja tem desenhoId: qual imagem usam
print()
print("cod | desenhoId atual | imagem atual | tem desenho na pasta")
for m in sorted(moldes, key=lambda x: (re.sub(r"\D", "", x["codigo"]).zfill(4), x["codigo"])):
    if not m.get("desenhoId"):
        continue
    d = next((d for d in desenhos if d["id"] == m["desenhoId"]), None)
    print("%-6s | %-22s | %-40s | %s" % (m["codigo"], m["desenhoId"],
          (d or {}).get("imagem", "?"), "SIM" if m["codigo"] in achados else "-"))
