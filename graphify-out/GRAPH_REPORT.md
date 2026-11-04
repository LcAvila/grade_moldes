# Graph Report - CustomFusionWeb  (2026-11-04)

## Corpus Check
- 64 files · ~132,633 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 22 file(s) not represented in the graph (top: (none) 5, .prefs 5, .xml 2)

## Summary
- 2350 nodes · 4722 edges · 135 communities (50 shown, 85 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 516 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3d7aa852`
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
- At
- resumo.js
- svg.js
- desenhos-extraidos.md
- inventario-moldes.md
- ApsModeloService
- fe
- GradeRepository
- Database
- com.customfusion:CustomFusionWeb
- .constructor
- bl
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
- ht
- ca
- i
- Ne
- yt
- ls
- sn
- parse
- .updateWorldMatrix
- tn
- visualizador3d.js
- bt
- qc
- kl
- jt
- visualizador3dFacade.js
- je
- Na
- re
- SessaoFusion
- .copy
- Autodesk Platform Services (APS) — configuração
- eh
- ps
- GedFusionService.java
- gt
- ws
- ia
- Aa
- _fromTexture
- ct
- ec
- ol
- ni
- ys
- Jl
- parseObject
- rh
- Rl
- .doGet
- .doPost
- Kn
- Al
- .parse
- el
- la
- wo
- ai
- .copy
- .constructor
- Ll
- pl
- vl
- .constructor
- ic
- Tt
- fl
- rt
- s
- Wl
- xs
- Yl
- t
- constructor
- dl
- ac
- ts
- rc
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
- .parse

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

## Communities (135 total, 85 thin omitted)

### Community 0 - "extrair_desenhos.py"
Cohesion: 0.06
Nodes (31): md5(), md5_bytes(), _blobs_desenho_xls(), _carve(), classificar(), codigo_de(), _e_grade(), _e_logo() (+23 more)

### Community 1 - "com.fasterxml.jackson.databind.ObjectMapper"
Cohesion: 0.19
Nodes (7): AtualizacaoCavidadesRequest, Cavidade, MoldeService, ApsTokenServlet, CavidadesServlet, MoldesServlet, UsuarioAtualServlet

### Community 3 - "configLoader.js"
Cohesion: 0.22
Nodes (13): buscarApi(), carregarCatalogo(), carregarDesenhos(), carregarFormas(), carregarGradeHistorica(), carregarIndiceHistorico(), carregarLayout(), carregarMaquinas() (+5 more)

### Community 5 - "telaInicial.js"
Cohesion: 0.16
Nodes (25): abrirModal(), abrirModalCadastro(), atualizarBotaoConfirmar(), atualizarOpcoesHistorico(), cardEl(), carregarIndiceHistorico(), chipEl(), coletarChecklist() (+17 more)

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
Cohesion: 0.05
Nodes (54): 1. Tema Visual e Atmosfera, 2. Paleta e Papéis Semânticos, 3. Tipografia, 4.10 Modal, 4.1 Botões, 4.2 Campos de formulário, 4.3 Editor inline (`.editable`), 4.4 Cards de resumo (`.resumo-status` / `.resumo-card`) (+46 more)

### Community 10 - "estado.js"
Cohesion: 0.13
Nodes (18): compararId(), normalizarRegistro(), registroVazio(), atualizarTudo(), excluir(), initRegistros(), limpar(), salvar() (+10 more)

### Community 12 - "three.min.js"
Cohesion: 0.03
Nodes (33): as(), bind(), br(), $c(), copy(), er(), getValue(), Hi() (+25 more)

### Community 13 - "Lt"
Cohesion: 0.04
Nodes (4): jn(), Lt, update(), wh()

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

### Community 28 - "Database"
Cohesion: 0.08
Nodes (16): 1. Camadas, 2. Banco SQLite, 3. APIs, 4. Identificação de usuário (sem login próprio), 5. GED do Fusion, 6. Migração e backup, 7. Build e teste local, Arquitetura: SQLite + Fusion GED (+8 more)

### Community 31 - ".constructor"
Cohesion: 0.09
Nodes (39): MapControls, OrbitControls, dollyIn(), dollyOut(), getZoomScale(), handleKeyDown(), handleMouseDownDolly(), handleMouseDownPan() (+31 more)

### Community 36 - ".setAttribute"
Cohesion: 0.08
Nodes (15): ao(), bo, ci, clone(), co(), Da, Ga, gn() (+7 more)

### Community 37 - "nl"
Cohesion: 0.05
Nodes (6): n(), il(), nl, qo, yo, zl

### Community 40 - "xc"
Cohesion: 0.07
Nodes (8): ds(), F(), G(), k(), V(), z(), jo, xc

### Community 42 - "en"
Cohesion: 0.09
Nodes (4): en, Si(), ti, updateMatrixWorld()

### Community 44 - "Xe"
Cohesion: 0.08
Nodes (10): _a, cs, Do, fo(), go, mo(), po, Rs (+2 more)

### Community 45 - "no"
Cohesion: 0.10
Nodes (17): Ah, eo(), fa, ho(), io(), ja(), ka(), lo() (+9 more)

### Community 49 - "ht"
Cohesion: 0.11
Nodes (3): hs, so(), w()

### Community 50 - "ca"
Cohesion: 0.18
Nodes (4): ca, fh, Mh, uo()

### Community 51 - "i"
Cohesion: 0.17
Nodes (5): cl, i(), load(), c(), vc

### Community 56 - "parse"
Cohesion: 0.08
Nodes (15): bindSkeletons(), ei, hl, ii, mc(), parse(), parseAnimations(), parseImages() (+7 more)

### Community 58 - "tn"
Cohesion: 0.06
Nodes (8): dt(), $e(), Ke(), ms(), Qe(), ss(), tn, ut()

### Community 59 - "visualizador3d.js"
Cohesion: 0.10
Nodes (68): abrirModal3D(), abrirPeloMenu(), adicionarMarcadorPendente(), adicionarPontoMedicao(), ajustarAoModelo(), alternarCorte(), aplicarGeometriaNaCena(), ativarModoMedicao() (+60 more)

### Community 60 - "bt"
Cohesion: 0.14
Nodes (3): bt, li(), Ra

### Community 64 - "visualizador3dFacade.js"
Cohesion: 0.42
Nodes (7): abrirModal3D(), codigoMolde(), getContextPath(), infoModelo(), jsonGet(), urlApi(), verificarAps()

### Community 66 - "Na"
Cohesion: 0.25
Nodes (4): Ba, Ko, Na, _o

### Community 68 - "SessaoFusion"
Cohesion: 0.31
Nodes (3): ResultadoPortal, SessaoFusion, UsuarioAtual

### Community 70 - "Autodesk Platform Services (APS) — configuração"
Cohesion: 0.20
Nodes (9): 1. Criar o aplicativo no APS, 2. Configurar as credenciais, 3. Créditos e custos, 4. Como o fluxo funciona, 5. Onde ficam os STLs, 6. Validação, 7. Problemas comuns, 8. Segurança e privacidade (+1 more)

### Community 72 - "ps"
Cohesion: 0.27
Nodes (16): ps(), B(), F(), G(), H(), I(), J(), K() (+8 more)

### Community 74 - "gt"
Cohesion: 0.16
Nodes (11): bi(), fi(), gi(), mi(), f(), h(), m(), x() (+3 more)

### Community 75 - "ws"
Cohesion: 0.17
Nodes (11): bs, C(), mt(), ws(), At(), Ct(), Dt(), It() (+3 more)

### Community 76 - "ia"
Cohesion: 0.17
Nodes (3): ia, parseSkeletons(), ta

### Community 79 - "_fromTexture"
Cohesion: 0.10
Nodes (24): _allocateTargets(), _applyPMREM(), _blur(), _cleanup(), compileCubemapShader(), compileEquirectangularShader(), _compileMaterial(), fromCubemap() (+16 more)

### Community 80 - "ct"
Cohesion: 0.16
Nodes (3): ct(), es(), ns()

### Community 82 - "ol"
Cohesion: 0.14
Nodes (3): ol, setTexturePath(), uc

### Community 83 - "ni"
Cohesion: 0.20
Nodes (4): Et, ni, Tt(), yi()

### Community 86 - "parseObject"
Cohesion: 0.22
Nodes (3): $l, parseObject(), tc

### Community 93 - ".parse"
Cohesion: 0.20
Nodes (3): STLLoader, isBinary(), matchDataViewAt()

### Community 111 - "s"
Cohesion: 0.43
Nodes (6): s(), G(), H(), O(), U(), z()

### Community 123 - "t"
Cohesion: 0.47
Nodes (3): t(), l(), o()

### Community 127 - "constructor"
Cohesion: 0.11
Nodes (7): bc, constructor(), d(), u(), ge, setDirection(), setLength()

### Community 154 - ".parse"
Cohesion: 0.20
Nodes (5): oc, i(), parseGeometries(), sc, yn()

## Knowledge Gaps
- **1 isolated node(s):** `com.customfusion:CustomFusionWeb`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 748 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **85 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Lt` connect `Lt` to `app.js`, `three.min.js`, `fe`, `.parse`, `bl`, `yc`, `.setAttribute`, `pt`, `Ce`, `en`, `ml`, `ht`, `Ne`, `yt`, `sn`, `.updateWorldMatrix`, `je`, `re`, `.copy`, `ws`, `.copy`, `ec`, `ys`, `rh`, `wo`, `.constructor`, `.addVectors`, `.constructor`, `.applyMatrix3`, `s`, `xs`, `Yl`, `constructor`?**
  _High betweenness centrality (0.079) - this node is a cross-community bridge._
- **What connects `com.customfusion:CustomFusionWeb` to the rest of the system?**
  _1 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `extrair_desenhos.py` be split into smaller, more focused modules?**
  _Cohesion score 0.0579476861167002 - nodes in this community are weakly interconnected._
- **Why does `tn` connect `tn` to `yc`, `.setAttribute`, `xc`, `en`, `ws`, `three.min.js`, `Xe`, `fl`, `no`, `dc`, `_fromTexture`, `ca`, `yt`, `parseObject`, `sn`, `.updateWorldMatrix`, `t`, `constructor`?**
  _High betweenness centrality (0.035) - this node is a cross-community bridge._
- **Should `visualizador3dAps.js` be split into smaller, more focused modules?**
  _Cohesion score 0.13588850174216027 - nodes in this community are weakly interconnected._
- **Should `app.js` be split into smaller, more focused modules?**
  _Cohesion score 0.05376972530683811 - nodes in this community are weakly interconnected._
- **Should `estado.js` be split into smaller, more focused modules?**
  _Cohesion score 0.12923076923076923 - nodes in this community are weakly interconnected._