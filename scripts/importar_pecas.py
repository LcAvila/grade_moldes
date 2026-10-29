# -*- coding: utf-8 -*-
"""Importa os desenhos de peca ('desenho - molde N.png') das pastas do usuario.

Para cada molde com desenho na pasta:
  - copia o PNG para assets/images/pecas/<codigo>.png (substituindo a versao
    extraida da planilha, que e movida para assets/images/pecas/_substituidas/
    para nao perder nada);
  - cria/atualiza a entrada em config/desenhos.json (papel 'peca');
  - aponta molde.desenhoId para essa entrada quando o molde nao tinha desenho.

Casos:
  - 168 mantem o cadastro atual (desenho-168-r05): o arquivo da pasta e
    identico byte a byte ao ja usado.
  - 186/187 tem arquivos repetidos em pastas diferentes (mesmo conteudo):
    usa o primeiro encontrado.
  - Sem desenho na pasta: 69, 81, 109, 137, 147B, 161 (reportados).
  - 101 mantem a peca ja extraida da planilha (sem arquivo na pasta).

Uso: python scripts/importar_pecas.py [--aplicar]
"""
import io
import json
import os
import re
import shutil
import sys
import hashlib

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8")

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WEB = os.path.join(RAIZ, "src", "main", "webapp")
BASE = os.path.join(WEB, "assets", "imagens e grades moldes")
PECAS = os.path.join(WEB, "assets", "images", "pecas")
SUBST = os.path.join(PECAS, "_substituidas")
CFG = os.path.join(WEB, "config")

RE_DESENHO = re.compile(r"(?i)^desenho\s*-\s*molde\s*(.+?)\s*\.png$")
APLICAR = "--aplicar" in sys.argv
MANTER = {"168"}  # cadastro validado, arquivo identico

OBS = ("Desenho da peca do molde ('desenho - molde N.png') das pastas "
       "organizadas pelo usuario, integrado ao cadastro.")


def md5(p):
    return hashlib.md5(open(p, "rb").read()).hexdigest()


def gravar(caminho, dados):
    txt = json.dumps(dados, ensure_ascii=False, indent=2)
    with open(caminho, "w", encoding="utf-8", newline="\r\n") as f:
        f.write(txt + "\r\n")


def main():
    catalogo = json.load(open(os.path.join(CFG, "moldes.json"), encoding="utf-8"))
    desenhos = json.load(open(os.path.join(CFG, "desenhos.json"), encoding="utf-8"))
    porcodigo = {m["codigo"]: m for m in catalogo["moldes"]}

    # varre as pastas
    achados = {}      # cod -> [rel]
    for raiz, dirs, arqs in os.walk(BASE):
        for n in arqs:
            m = RE_DESENHO.match(n)
            if m:
                cod = m.group(1).strip().upper()
                rel = os.path.relpath(os.path.join(raiz, n), BASE)
                achados.setdefault(cod, []).append(rel)

    copiados, atualizados, criados, notas, sem_desenho = [], [], [], [], []

    for cod in sorted(porcodigo, key=lambda x: (re.sub(r"\D", "", x).zfill(4), x)):
        molde = porcodigo[cod]
        if cod not in achados:
            if not molde.get("desenhoId"):
                sem_desenho.append(cod)
            continue
        if cod in MANTER:
            notas.append("%s: mantido '%s' (arquivo da pasta identico ao atual)" %
                         (cod, molde.get("desenhoId")))
            continue

        rels = achados[cod]
        if len(rels) > 1:
            notas.append("%s: %d arquivos repetidos; usando %s" %
                         (cod, len(rels), rels[0].replace("\\", "/")))
        # checa se o nome da pasta confere com o codigo do arquivo
        pasta = rels[0].split(os.sep)[0]
        mp = re.search(r"(?i)molde\s+([0-9]+[A-Z]*)", pasta)
        if mp and mp.group(1).upper() != cod:
            notas.append("%s: ATENCAO pasta '%s' nao confere com o arquivo" % (cod, pasta))

        src = os.path.join(BASE, rels[0])
        dst = os.path.join(PECAS, "%s.png" % cod)
        precisa_copiar = (not os.path.exists(dst)) or md5(dst) != md5(src)
        if precisa_copiar and APLICAR:
            os.makedirs(PECAS, exist_ok=True)
            if os.path.exists(dst):
                os.makedirs(SUBST, exist_ok=True)
                shutil.move(dst, os.path.join(SUBST, "%s.png" % cod))
            shutil.copyfile(src, dst)

        # entrada no catalogo de desenhos
        eid = "desenho-%s-peca" % cod
        entrada = next((d for d in desenhos["desenhos"]
                        if d.get("moldeId") == molde["id"] and d.get("papel") == "peca"), None)
        if entrada is None:
            entrada = {"id": eid, "moldeId": molde["id"], "papel": "peca",
                       "imagem": "assets/images/pecas/%s.png" % cod,
                       "documento": None,
                       "origem": rels[0].replace("\\", "/"),
                       "observacao": OBS}
            desenhos["desenhos"].append(entrada)
            criados.append(cod)
        else:
            mudou = (entrada.get("imagem") != "assets/images/pecas/%s.png" % cod or
                     entrada.get("origem") != rels[0].replace("\\", "/"))
            entrada["imagem"] = "assets/images/pecas/%s.png" % cod
            entrada["origem"] = rels[0].replace("\\", "/")
            entrada["observacao"] = OBS
            entrada["documento"] = entrada.get("documento")
            if mudou:
                atualizados.append(cod)

        if molde.get("desenhoId") != eid:
            molde["desenhoId"] = eid
            if cod not in atualizados and cod not in criados:
                atualizados.append(cod)
        if precisa_copiar:
            copiados.append(cod)

    print("Copiados para assets/images/pecas (%d): %s" % (len(copiados), ", ".join(copiados) or "-"))
    print()
    print("Entradas novas em desenhos.json (%d): %s" % (len(criados), ", ".join(criados) or "-"))
    print()
    print("Entradas atualizadas (%d): %s" % (len(atualizados), ", ".join(atualizados) or "-"))
    print()
    if notas:
        print("Notas:")
        for n in notas:
            print("  -", n)
        print()
    print("Moldes SEM desenho algum (%d): %s" % (len(sem_desenho), ", ".join(sem_desenho) or "-"))
    print()
    print("Modo:", "APLICADO" if APLICAR else "auditoria (use --aplicar)")
    if APLICAR:
        gravar(os.path.join(CFG, "moldes.json"), catalogo)
        gravar(os.path.join(CFG, "desenhos.json"), desenhos)
        print("moldes.json e desenhos.json gravados.")


if __name__ == "__main__":
    main()
