#!/usr/bin/env python3
"""Converte os catalogos JSON legados no banco semente SQLite.

Uso:
    python scripts/migrar_json_sqlite.py

Gera src/main/resources/db/customfusion-seed.db. O banco e copiado para
%USERPROFILE%\\.customfusion\\customfusion.db na primeira subida da aplicacao.
Os JSONs em src/main/webapp/config nao sao apagados por este script.
"""

from __future__ import annotations

import json
import sqlite3
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONFIG = ROOT / "src" / "main" / "webapp" / "config"
SCHEMA = ROOT / "src" / "main" / "resources" / "db" / "schema.sql"
SEED = ROOT / "src" / "main" / "resources" / "db" / "customfusion-seed.db"


def compact(value) -> str:
    return json.dumps(value, ensure_ascii=False, separators=(",", ":"))


def ler_json(caminho: Path):
    return json.loads(caminho.read_text(encoding="utf-8"))


def texto(valor) -> str | None:
    if valor is None:
        return None
    texto = str(valor).strip()
    return texto or None


def inteiro(valor, padrao: int = 0) -> int:
    try:
        return int(valor)
    except (TypeError, ValueError):
        return padrao


def main() -> int:
    if not CONFIG.is_dir():
        print(f"Pasta de configuracao nao encontrada: {CONFIG}", file=sys.stderr)
        return 1
    if not SCHEMA.is_file():
        print(f"Schema nao encontrado: {SCHEMA}", file=sys.stderr)
        return 1

    if SEED.exists():
        SEED.unlink()
    SEED.parent.mkdir(parents=True, exist_ok=True)

    db = sqlite3.connect(SEED)
    db.executescript(SCHEMA.read_text(encoding="utf-8"))
    db.execute("PRAGMA foreign_keys = ON")

    moldes_root = ler_json(CONFIG / "moldes.json")
    moldes = moldes_root.get("moldes", [])
    ids_moldes = {str(m.get("id")) for m in moldes if m.get("id")}
    codigos = {
        str(m.get("codigo", "")).strip().upper(): str(m.get("id"))
        for m in moldes
        if m.get("codigo") and m.get("id")
    }

    for molde in moldes:
        molde_id = str(molde["id"])
        db.execute(
            """
            INSERT INTO moldes
                (id, codigo, nome_peca, familia, revisao_cadastro, layout_id,
                 desenho_id, modelo3d_id, padroes_json, origem)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                molde_id,
                str(molde.get("codigo", "")).strip(),
                str(molde.get("nomePeca", "")).strip(),
                texto(molde.get("familia")),
                texto(molde.get("revisaoCadastro")),
                texto(molde.get("layoutId")),
                texto(molde.get("desenhoId")),
                texto(molde.get("modelo3dId")),
                compact(molde.get("padroes") or {}),
                texto(molde.get("origem")),
            ),
        )
        for ordem, cavidade in enumerate(molde.get("cavidades") or []):
            db.execute(
                "INSERT INTO molde_cavidades (molde_id, numero, ordem) VALUES (?, ?, ?)",
                (molde_id, str(cavidade), ordem),
            )

    def molde_ref(valor) -> str | None:
        bruto = texto(valor)
        if not bruto:
            return None
        if bruto in ids_moldes:
            return bruto
        if not bruto.lower().startswith("molde-"):
            return codigos.get(bruto.upper())
        return bruto if bruto in ids_moldes else None

    desenhos = ler_json(CONFIG / "desenhos.json").get("desenhos", [])
    for desenho in desenhos:
        db.execute(
            """
            INSERT INTO desenhos
                (id, molde_id, papel, revisao, imagem, documento, origem,
                 observacao, dados_json)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                str(desenho["id"]),
                molde_ref(desenho.get("moldeId") or desenho.get("molde")),
                texto(desenho.get("papel") or desenho.get("tipo")),
                texto(desenho.get("revisao")),
                texto(desenho.get("imagem")),
                texto(desenho.get("documento")),
                texto(desenho.get("origem")),
                texto(desenho.get("observacao")),
                compact(desenho),
            ),
        )

    formas = ler_json(CONFIG / "formas.json").get("formas", [])
    for forma in formas:
        db.execute(
            "INSERT INTO formas (id, revisao, descricao, dados_json) VALUES (?, ?, ?, ?)",
            (
                str(forma["id"]),
                texto(forma.get("revisao")),
                texto(forma.get("descricao")),
                compact(forma),
            ),
        )

    maquinas = ler_json(CONFIG / "maquinas.json").get("maquinas", [])
    for maquina in maquinas:
        db.execute(
            "INSERT INTO maquinas (id, nome, ocorrencias, dados_json) VALUES (?, ?, ?, ?)",
            (
                str(maquina["id"]),
                str(maquina.get("nome", maquina["id"])).strip(),
                inteiro(maquina.get("ocorrencias")),
                compact(maquina),
            ),
        )

    layouts_dir = CONFIG / "layouts"
    layouts = 0
    if layouts_dir.is_dir():
        for caminho in sorted(layouts_dir.glob("*.json")):
            layout = ler_json(caminho)
            layout_id = str(layout.get("id") or caminho.stem)
            db.execute(
                """
                INSERT INTO layouts (id, molde_id, tipo, revisao, dados_json)
                VALUES (?, ?, ?, ?, ?)
                """,
                (
                    layout_id,
                    molde_ref(layout.get("moldeId")),
                    texto(layout.get("tipo")),
                    texto(layout.get("revisao")),
                    compact(layout),
                ),
            )
            layouts += 1

    modelos3d = 0
    catalogo3d = CONFIG / "modelos3d.json"
    if catalogo3d.is_file():
        for modelo in ler_json(catalogo3d).get("modelos", []):
            db.execute(
                """
                INSERT INTO modelos3d
                    (id, arquivo, nome, revisao, codigo_tecnico,
                     documento_original, fallback_procedural, dados_json)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    str(modelo["id"]),
                    str(modelo["arquivo"]).lstrip("/"),
                    texto(modelo.get("nome")),
                    texto(modelo.get("revisao")),
                    texto(modelo.get("codigoTecnico")),
                    texto(modelo.get("documentoOriginal")),
                    1 if modelo.get("fallbackProcedural") else 0,
                    compact(modelo),
                ),
            )
            modelos3d += 1

    contagens = {
        "moldes": len(moldes),
        "molde_cavidades": sum(len(m.get("cavidades") or []) for m in moldes),
        "desenhos": len(desenhos),
        "formas": len(formas),
        "maquinas": len(maquinas),
        "layouts": layouts,
        "modelos3d": modelos3d,
    }
    agora = datetime.now(timezone.utc).replace(microsecond=0).isoformat()
    db.executemany(
        "INSERT INTO meta (chave, valor) VALUES (?, ?)",
        [
            ("schema_version", "1"),
            ("seed_gerado_em", agora),
            ("seed_contagens", compact(contagens)),
        ],
    )
    db.commit()

    problemas: list[str] = []
    for tabela, esperado in contagens.items():
        obtido = db.execute(f"SELECT COUNT(*) FROM {tabela}").fetchone()[0]
        if obtido != esperado:
            problemas.append(f"{tabela}: esperado {esperado}, obtido {obtido}")
    fks = db.execute("PRAGMA foreign_key_check").fetchall()
    if fks:
        problemas.append(f"foreign_key_check retornou {len(fks)} problema(s)")
    db.close()

    tamanho_mb = SEED.stat().st_size / (1024 * 1024)
    print(f"Banco semente: {SEED} ({tamanho_mb:.1f} MB)")
    print("Contagens:", json.dumps(contagens, ensure_ascii=False))
    if problemas:
        for problema in problemas:
            print("ERRO:", problema, file=sys.stderr)
        return 1
    print("Migracao validada com sucesso.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
