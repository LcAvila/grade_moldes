# Arquitetura: SQLite + Fusion GED

> Decisão aprovada: o **SQLite** é a fonte de verdade do catálogo e dos dados
> operacionais. O Fusion é usado apenas para **identificação do usuário** e
> **publicação de documentos no GED**. Não há mais persistência de cavidades
> em JSON, MySQL ou no formulário do Fusion.

## 1. Camadas

| Camada | Tecnologia | O que mora ali |
|---|---|---|
| Catálogo / configuração | **SQLite** | moldes, cavidades, desenhos, formas, máquinas, layouts, modelos 3D e histórico |
| Dados operacionais | **SQLite** | grades, cavidades da grade, registros por molde e checklist |
| Documentos oficiais | **GED do Fusion** via API | relatórios/folhas do molde (arquivo + metadados + `neoId`) |
| Identificação | Sessão do Fusion / contêiner | login e nome do controlador |
| Cache no navegador | `localStorage` | apenas rascunho/migração; a fonte de verdade é a API |

## 2. Banco SQLite

- Caminho padrão no Linux: `/opt/neomind/fusion/config/customfusion.db`.
- Caminho padrão no Windows: `%USERPROFILE%\.customfusion\customfusion.db`.
- Configurável em `application.properties` por `app.sqlite.path` ou pela
  variável `APP_SQLITE_PATH`; caminhos relativos são resolvidos a partir da
  pasta do usuário e um diretório informado recebe `customfusion.db` dentro.
- Na primeira subida, `Database` copia o seed versionado
  `src/main/resources/db/customfusion-seed.db` para o caminho configurado.
  Arquivos existentes **não** são sobrescritos.
- O schema idempotente fica em `src/main/resources/db/schema.sql` e é aplicado
  no startup por `AppLifecycleListener`.
- Conexões usam `foreign_keys=ON`, `journal_mode=WAL` e `busy_timeout`.

### Tabelas

| Tabela | Conteúdo |
|---|---|
| `meta` | versão do schema, data/contagens do seed |
| `moldes` | código, nome, desenho, layout, modelo 3D, padrões e metadados |
| `molde_cavidades` | números de cavidade e zona de cada molde |
| `desenhos` / `formas` / `maquinas` | catálogo auxiliar |
| `layouts` | layout estático ou parametrizado em `dados_json` |
| `modelos3d` | catálogo de modelos STL/GLB e arquivo |
| `historico_grades` | índice e pacotes de grades históricas importadas do Excel |
| `grades` | grade operacional (molde, máquina, data, status, `dados_json`) |
| `grade_cavidades` | desvio, data, vedado, observação e controlador por cavidade |
| `registros_molde` | espelho por molde/cavidade usado pelo fluxo legado de Moldes/Cavidades |

## 3. APIs

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/config/moldes` | catálogo de moldes com cavidades |
| GET | `/api/config/desenhos` | catálogo de desenhos |
| GET | `/api/config/formas` | catálogo de formas |
| GET | `/api/config/maquinas` | catálogo de máquinas |
| GET | `/api/config/modelos3d` | catálogo de modelos 3D |
| GET | `/api/config/layouts/{id}` | layout do molde |
| GET | `/api/config/historico` | índice de grades históricas por molde |
| GET | `/api/config/historico?arquivo=...` | pacote de uma grade histórica |
| GET | `/api/grades` | lista de grades com contagens |
| POST | `/api/grades` | cria grade |
| GET | `/api/grades/{id}` | carrega grade + cavidades |
| PUT | `/api/grades/{id}` | salva grade + cavidades (transação) |
| DELETE | `/api/grades/{id}` | remove grade |
| POST | `/api/grades/importar` | importa grades do localStorage |
| GET | `/api/moldes/{codigo}` | molde no formato do fluxo legado |
| PUT | `/api/moldes/{codigo}/cavidades` | grava cavidades em `registros_molde` |
| POST | `/api/moldes/cadastrar` | cadastra molde no SQLite + arquivos em `assets` |
| POST | `/api/moldes/{codigo}/publicar-ged` | publica relatório no GED |

## 4. Identificação de usuário (sem login próprio)

Ordem em `SessaoFusion`:

1. `request.getRemoteUser()` — se o contexto estiver protegido pelo contêiner;
2. `PortalUtil.getCurrentUser()` do Fusion (**via reflexão**, classe configurável
   em `app.fusion.portal-util-class`);
3. `app.dev.usuario` — fallback **apenas** de desenvolvimento local.

Regras:

- `GET /api/usuario-atual` sempre responde 200;
- `/api/config/*` e `/api/grades/*` são anônimos (o app funciona fora do Fusion);
- as demais `/api/*` sem usuário respondem **401 JSON**;
- o autor gravado/publicado vem da sessão, nunca do body do cliente.

## 5. GED do Fusion

Configuração em `application.properties` (ou variável de ambiente equivalente):

| Chave | Padrão | Uso |
|---|---|---|
| `app.fusion.base-url` | (vazio) | ex.: `http://fusion:8081` |
| `app.fusion.api-token` | (vazio) | Token da API GED |
| `app.fusion.doc-type` | `RELATORIO_CAVIDADES` | tipo de documento GED |
| `app.fusion.folder-base` | `Qualidade\Moldes` | pasta raiz no GED |
| `app.fusion.doc-status` | `published` | `edition` se houver assinatura |
| `app.fusion.doc-fields-extra` | (vazio) | JSON com campos extras do tipo |
| `app.fusion.author-padrao` | (vazio) | autor de contingência |
| `app.fusion.portal-util-class` | (vazio) | FQN do `PortalUtil` |

- `POST /api/moldes/{codigo}/publicar-ged` recebe
  `{ "file": "relatorio-cavidades-115.pdf", "fileBytes": "<base64>" }`;
- `file` precisa ser exatamente o nome enviado ao GED;
- respostas: `200 {ok, neoId, code, url, jaPublicado}`, `400` payload inválido,
  `401` sem sessão, `503` GED não configurado, `502` falha na API;
- `409` do GED é tratado como idempotência (`jaPublicado: true`).

## 6. Migração e backup

- `scripts/migrar_json_sqlite.py` gera o seed a partir dos JSONs legados
  (`src/main/webapp/config/`), valida contagens e chaves estrangeiras.
- Contagens atuais do seed: 73 moldes, 2671 cavidades, 133 desenhos, 4 formas,
  40 máquinas, 75 layouts, 1 modelo 3D e 1849 grades históricas.
- O front migra automaticamente as grades antigas do `localStorage` na primeira
  carga, usando a chave `customfusion.sqlite.migrado.v1`.
- Backup: copiar o arquivo `.db` com a aplicação parada (ou usar `.backup` do
  SQLite). O seed em `src/main/resources/db/` não contém dados operacionais.

## 7. Build e teste local

- Compilação: Java 21 + `javac` com `jakarta.servlet-api`, `jackson-*` e
  `sqlite-jdbc`; não há `mvn` no PATH desta máquina.
- Aplicação: Tomcat 10+ (Jakarta Servlet 6), WAR `CustomFusionWeb`.
- Teste rápido do banco: rodar `SmokeTest`/consultas Python contra o seed ou
  contra uma cópia temporária apontada por `APP_SQLITE_PATH`.
- Front: verificar em 1250px e 800px, além do preview A4, conforme `DESIGN.md`.
