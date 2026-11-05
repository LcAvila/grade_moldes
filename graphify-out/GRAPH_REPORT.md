# Graph Report - CustomFusionWeb  (2026-11-05)

## Corpus Check
- 64 files · ~133,117 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 23 file(s) not represented in the graph (top: (none) 5, .prefs 5, .xml 3)

## Summary
- 2370 nodes · 4774 edges · 133 communities (51 shown, 82 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 514 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `bc0a0217`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- extrair_desenhos.py
- com.fasterxml.jackson.databind.ObjectMapper
- jakarta.servlet.http.HttpServletResponse
- configLoader.js
- CatalogoRepository
- telaInicial.js
- GedFusionService
- visualizador3dAps.js
- persistencia.js
- app.js
- estado.js
- IdentificacaoFilter.java
- three.min.js
- Lt
- diagrama.js
- AGENTS.md
- impressao.js
- modal.js
- tabela.js
- Database.java
- At
- resumo.js
- svg.js
- desenhos-extraidos.md
- inventario-moldes.md
- ApsModeloService
- ht
- GradeRepository
- Database
- com.customfusion:CustomFusionWeb
- .constructor
- yo
- yc
- vt
- St
- .setAttribute
- nl
- pt
- jc
- xc
- Ce
- en
- Lc
- Xe
- no
- ml
- se
- dc
- fe
- ca
- i
- Ne
- yt
- ls
- sn
- pl
- .updateWorldMatrix
- tn
- visualizador3d.js
- bt
- Autodesk Platform Services (APS) — configuração
- kl
- jt
- visualizador3dFacade.js
- je
- MoldeService.java
- re
- SessaoFusion
- .copy
- registros.js
- eh
- mi
- ws
- ia
- parse
- _fromTexture
- ct
- ec
- ol
- gt
- ys
- Jl
- parseObject
- rh
- fl
- ge
- jakarta.servlet.http.HttpServletRequest
- Kn
- .parse
- Vs
- la
- ic
- ai
- jn
- .constructor
- Ll
- Aa
- .applyMatrix4
- Yl
- .constructor
- Tt
- vl
- Na
- s
- uc
- rc
- t
- dl
- ac
- ts
- Zc
- an
- cn
- dn
- fn
- hn
- ln
- on
- pn
- un

## God Nodes (most connected - your core abstractions)
1. `Lt` - 135 edges
2. `vt` - 90 edges
3. `tn` - 65 edges
4. `St` - 60 edges
5. `Ce` - 59 edges
6. `sn` - 53 edges
7. `se` - 49 edges
8. `At` - 37 edges
9. `pt()` - 35 edges
10. `en` - 35 edges

## Surprising Connections (you probably didn't know these)
- `4. Como o fluxo funciona` --references--> `IdentificacaoFilter`  [INFERRED]
  docs/aps-setup.md → src/main/java/fusion/developer/web/cavidades/seguranca/IdentificacaoFilter.java
- `4.3 Editor inline (`.editable`)` --references--> `criarGrade()`  [INFERRED]
  DESIGN.md → src/main/webapp/js/core/persistencia.js
- `Impressão (`.folha-impressao`) — tratada como tela` --references--> `pt()`  [INFERRED]
  DESIGN.md → src/main/webapp/js/lib/three/three.min.js
- `4.3 Editor inline (`.editable`)` --references--> `aplicarGrade()`  [INFERRED]
  DESIGN.md → src/main/webapp/js/render/cabecalho.js
- `2. Banco SQLite` --references--> `AppLifecycleListener`  [INFERRED]
  docs/arquitetura-fusion.md → src/main/java/fusion/developer/web/cavidades/config/AppLifecycleListener.java

## Import Cycles
- None detected.

## Communities (133 total, 82 thin omitted)

### Community 0 - "extrair_desenhos.py"
Cohesion: 0.06
Nodes (31): md5(), md5_bytes(), _blobs_desenho_xls(), _carve(), classificar(), codigo_de(), _e_grade(), _e_logo() (+23 more)

### Community 1 - "com.fasterxml.jackson.databind.ObjectMapper"
Cohesion: 0.23
Nodes (6): MoldeService, ApsModeloServlet, ApsTokenServlet, CavidadesServlet, MoldesServlet, UsuarioAtualServlet

### Community 3 - "configLoader.js"
Cohesion: 0.22
Nodes (13): buscarApi(), carregarCatalogo(), carregarDesenhos(), carregarFormas(), carregarLayout(), carregarMaquinas(), carregarModelos3d(), listarMaquinas() (+5 more)

### Community 5 - "telaInicial.js"
Cohesion: 0.11
Nodes (40): abrirCombobox(), abrirModal(), abrirModalCadastro(), acharDocumentoMolde(), acharItemExato(), atualizarBotaoConfirmar(), cardEl(), chipEl() (+32 more)

### Community 6 - "GedFusionService"
Cohesion: 0.19
Nodes (4): ConfiguracaoIncompletaException, GedFusionService, Resultado, PublicarGedServlet

### Community 7 - "visualizador3dAps.js"
Cohesion: 0.14
Nodes (38): abrir(), ajustar(), aoConcluirCalibracao(), aoConcluirMedida(), aoTrocarTipo(), aplicarUnidade(), ativarModo(), atualizarLista() (+30 more)

### Community 8 - "persistencia.js"
Cohesion: 0.23
Nodes (18): acharGrade(), api(), carregarGrade(), criarGrade(), descarregarPendente(), disponivel(), existeLegado(), flush() (+10 more)

### Community 9 - "app.js"
Cohesion: 0.06
Nodes (50): 1. Tema Visual e Atmosfera, 2. Paleta e Papéis Semânticos, 3. Tipografia, 4.10 Modal, 4.1 Botões, 4.2 Campos de formulário, 4.3 Editor inline (`.editable`), 4.4 Cards de resumo (`.resumo-status` / `.resumo-card`) (+42 more)

### Community 10 - "estado.js"
Cohesion: 0.16
Nodes (14): compararId(), normalizarRegistro(), registroVazio(), atualizarObsField(), atualizarSelecao(), clicarCavidade(), clicarZona(), criarOpcoesCavidades() (+6 more)

### Community 12 - "three.min.js"
Cohesion: 0.04
Nodes (25): bind(), br(), $c(), copy(), er(), getValue(), Hi(), intersectObject() (+17 more)

### Community 13 - "Lt"
Cohesion: 0.05
Nodes (4): as(), is(), Lt, os()

### Community 14 - "diagrama.js"
Cohesion: 0.53
Nodes (4): atualizarDiagrama(), montarDiagrama(), posicao(), transformacao()

### Community 15 - "AGENTS.md"
Cohesion: 0.40
Nodes (4): agent harnesses, build and runtime, design system, graphify

### Community 16 - "impressao.js"
Cohesion: 0.70
Nodes (4): definir(), initImpressao(), linhaFolha(), montarFolhaImpressao()

### Community 17 - "modal.js"
Cohesion: 0.60
Nodes (4): abrirDetalhes(), fecharModal(), initModal(), linha()

### Community 18 - "tabela.js"
Cohesion: 0.70
Nodes (4): atualizarTabela(), celulasDaCavidade(), montarTabela(), preencherCelulas()

### Community 25 - "ApsModeloService"
Cohesion: 0.08
Nodes (12): ApsConfig, ApsDerivativeService, Manifesto, ApsHttp, Resposta, ApsModeloService, Registro, Resultado (+4 more)

### Community 26 - "ht"
Cohesion: 0.20
Nodes (5): ht(), setFromCartesianCoords(), setFromVector3(), wo, xo

### Community 28 - "Database"
Cohesion: 0.08
Nodes (16): 1. Camadas, 2. Banco SQLite, 3. APIs, 4. Identificação de usuário (sem login próprio), 5. GED do Fusion, 6. Migração e backup, 7. Build e teste local, Arquitetura: SQLite + Fusion GED (+8 more)

### Community 31 - ".constructor"
Cohesion: 0.09
Nodes (39): MapControls, OrbitControls, dollyIn(), dollyOut(), getZoomScale(), handleKeyDown(), handleMouseDownDolly(), handleMouseDownPan() (+31 more)

### Community 32 - "yo"
Cohesion: 0.09
Nodes (9): ds(), F(), G(), k(), V(), z(), jo, qo (+1 more)

### Community 33 - "yc"
Cohesion: 0.09
Nodes (3): rt, xs, yc

### Community 36 - ".setAttribute"
Cohesion: 0.09
Nodes (12): ao(), bo, ci, Da, Ga, gn(), o(), mn (+4 more)

### Community 37 - "nl"
Cohesion: 0.08
Nodes (5): n(), il(), nl, parseShapes(), zl

### Community 39 - "jc"
Cohesion: 0.05
Nodes (3): jc, qc, setValue()

### Community 42 - "en"
Cohesion: 0.11
Nodes (4): en, Si(), ti, updateMatrixWorld()

### Community 44 - "Xe"
Cohesion: 0.07
Nodes (14): _a, cs, Do, fo(), go, hs, mo(), po (+6 more)

### Community 45 - "no"
Cohesion: 0.12
Nodes (14): co(), eo(), ho(), io(), ja(), ka(), no(), oo() (+6 more)

### Community 46 - "ml"
Cohesion: 0.08
Nodes (4): Al, el, ml, Rl

### Community 49 - "fe"
Cohesion: 0.11
Nodes (17): fe, ps(), B(), F(), G(), H(), I(), J() (+9 more)

### Community 50 - "ca"
Cohesion: 0.11
Nodes (10): Ah, ca, clone(), fh, hh, lo(), Mh, uh() (+2 more)

### Community 51 - "i"
Cohesion: 0.18
Nodes (5): cl, i(), load(), c(), vc

### Community 56 - "pl"
Cohesion: 0.15
Nodes (4): hl, pl, ul, o()

### Community 58 - "tn"
Cohesion: 0.06
Nodes (8): dt(), $e(), Ke(), ms(), Qe(), ss(), tn, ut()

### Community 59 - "visualizador3d.js"
Cohesion: 0.10
Nodes (68): abrirModal3D(), abrirPeloMenu(), adicionarMarcadorPendente(), adicionarPontoMedicao(), ajustarAoModelo(), alternarCorte(), aplicarGeometriaNaCena(), ativarModoMedicao() (+60 more)

### Community 61 - "Autodesk Platform Services (APS) — configuração"
Cohesion: 0.22
Nodes (8): 1. Criar o aplicativo no APS, 2. Configurar as credenciais, 3. Créditos e custos, 5. Onde ficam os STLs, 6. Validação, 7. Problemas comuns, 8. Segurança e privacidade, Autodesk Platform Services (APS) — configuração

### Community 64 - "visualizador3dFacade.js"
Cohesion: 0.42
Nodes (7): abrirModal3D(), codigoMolde(), getContextPath(), infoModelo(), jsonGet(), urlApi(), verificarAps()

### Community 68 - "SessaoFusion"
Cohesion: 0.31
Nodes (3): ResultadoPortal, SessaoFusion, UsuarioAtual

### Community 70 - "registros.js"
Cohesion: 0.61
Nodes (8): agendarAutoSalvar(), aplicarAutoSalvar(), atualizarTudo(), cancelarAutoSalvar(), definirStatusAutoSave(), excluir(), flushAutoSalvar(), initRegistros()

### Community 74 - "mi"
Cohesion: 0.19
Nodes (9): v(), y(), mi(), f(), g(), h(), m(), x() (+1 more)

### Community 75 - "ws"
Cohesion: 0.13
Nodes (12): bs, Et, C(), mt(), ws(), At(), Ct(), It() (+4 more)

### Community 78 - "parse"
Cohesion: 0.07
Nodes (17): bindSkeletons(), ei, ii, mc(), oc, i(), parse(), parseAnimations() (+9 more)

### Community 79 - "_fromTexture"
Cohesion: 0.09
Nodes (28): _allocateTargets(), _applyPMREM(), bc, _blur(), _cleanup(), compileCubemapShader(), compileEquirectangularShader(), _compileMaterial() (+20 more)

### Community 80 - "ct"
Cohesion: 0.16
Nodes (3): ct(), es(), ns()

### Community 83 - "gt"
Cohesion: 0.09
Nodes (12): bi(), fi(), gi(), li(), ni, pi(), vi(), wi() (+4 more)

### Community 86 - "parseObject"
Cohesion: 0.17
Nodes (4): fa, $l, parseObject(), tc

### Community 93 - ".parse"
Cohesion: 0.20
Nodes (3): STLLoader, isBinary(), matchDataViewAt()

### Community 98 - "jn"
Cohesion: 0.15
Nodes (3): jn(), update(), wh()

### Community 110 - "Na"
Cohesion: 0.25
Nodes (4): Ba, Ko, Na, _o

### Community 111 - "s"
Cohesion: 0.43
Nodes (6): s(), G(), H(), O(), U(), z()

### Community 123 - "t"
Cohesion: 0.28
Nodes (4): r(), t(), l(), o()

## Knowledge Gaps
- **1 isolated node(s):** `com.customfusion:CustomFusionWeb`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 749 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **82 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Lt` connect `Lt` to `app.js`, `three.min.js`, `ht`, `yc`, `.setAttribute`, `pt`, `jc`, `Ce`, `en`, `Xe`, `no`, `ml`, `dc`, `fe`, `Ne`, `sn`, `.updateWorldMatrix`, `je`, `re`, `.copy`, `.copy`, `mi`, `ws`, `parse`, `_fromTexture`, `ec`, `ys`, `rh`, `.getCenter`, `ic`, `jn`, `.constructor`, `Yl`, `.set`, `.constructor`, `.applyMatrix3`, `s`, `.copy`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **What connects `com.customfusion:CustomFusionWeb` to the rest of the system?**
  _1 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `extrair_desenhos.py` be split into smaller, more focused modules?**
  _Cohesion score 0.05879917184265011 - nodes in this community are weakly interconnected._
- **Why does `fr()` connect `app.js` to `three.min.js`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Should `telaInicial.js` be split into smaller, more focused modules?**
  _Cohesion score 0.10852713178294573 - nodes in this community are weakly interconnected._
- **Should `visualizador3dAps.js` be split into smaller, more focused modules?**
  _Cohesion score 0.13588850174216027 - nodes in this community are weakly interconnected._
- **Should `app.js` be split into smaller, more focused modules?**
  _Cohesion score 0.06079664570230608 - nodes in this community are weakly interconnected._