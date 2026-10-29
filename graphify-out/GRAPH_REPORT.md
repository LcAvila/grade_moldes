# Graph Report - CustomFusionWeb  (2026-10-29)

## Corpus Check
- 1983 files · ~1,535,245 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 31 file(s) not represented in the graph (top: .stl 8, (none) 7, .prefs 4)

## Summary
- 2191 nodes · 4104 edges · 155 communities (64 shown, 91 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 498 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `927566e5`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- extrair_desenhos.py
- Cavidade
- jakarta.servlet.http.HttpServletRequest
- configLoader.js
- DESIGN.md — CustomFusionWeb
- telaInicial.js
- GedFusionService
- FormularioFusion
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
- io.js
- At
- resumo.js
- svg.js
- desenhos-extraidos.md
- inventario-moldes.md
- 08-24-01-25-Conector para estojo de esferográfica - Molde 187_7891b1ad.md
- Conexão Esf. - 96 cav. - Molde 200_a48aacca.md
- Corpo Dianteiro - Duo Plus_109addbd.md
- README.md
- com.customfusion:CustomFusionWeb
- .constructor
- wo
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
- .copy
- no
- ml
- se
- .set
- .copy
- ya
- i
- Ne
- yt
- ls
- sn
- parse
- tn
- visualizador3d.js
- bt
- constructor
- kl
- jt
- GLTFLoader.js
- je
- fe
- re
- GLTFParser
- .copy
- us
- eh
- ps
- AppConfig
- gt
- ws
- ia
- .constructor
- .assignTexture
- _fromTexture
- ct
- ec
- ol
- .parse
- ys
- pl
- parseObject
- rh
- nc
- .get
- GLTFLoader
- Kn
- .loadTextureImage
- .parse
- ge
- la
- ai
- .parse
- ni
- Ll
- xs
- 4. Componentes
- .constructor
- ic
- Tt
- GLTFMaterialsPbrSpecularGlossinessExtension
- fl
- .getHex
- s
- GLTFCubicSplineInterpolant
- GLTFLightsExtension
- .setHSL
- jn
- Jl
- pl
- .copy
- S
- Yl
- t
- mo
- 4.3 Editor inline (`.editable`)
- Na
- dl
- ac
- $c
- rt
- ts
- vl
- rc
- uc
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
- `4.3 Editor inline (`.editable`)` --references--> `dadosGradeDoMolde()`  [INFERRED]
  DESIGN.md → src/main/webapp/js/app.js
- `4.3 Editor inline (`.editable`)` --references--> `criarGrade()`  [INFERRED]
  DESIGN.md → src/main/webapp/js/core/persistencia.js
- `Impressão (`.folha-impressao`) — tratada como tela` --references--> `pt()`  [INFERRED]
  DESIGN.md → src/main/webapp/js/lib/three/three.min.js
- `4.8 Tabela de cavidades (`.tabela-cav`)` --references--> `S()`  [INFERRED]
  DESIGN.md → src/main/webapp/js/lib/three/three.min.js
- `5. Especificação do formulário Fusion (criar no ambiente)` --references--> `S()`  [INFERRED]
  docs/arquitetura-fusion.md → src/main/webapp/js/lib/three/three.min.js

## Import Cycles
- None detected.

## Communities (155 total, 91 thin omitted)

### Community 0 - "extrair_desenhos.py"
Cohesion: 0.07
Nodes (25): md5(), md5_bytes(), _blobs_desenho_xls(), _carve(), classificar(), codigo_de(), _e_grade(), _e_logo() (+17 more)

### Community 1 - "Cavidade"
Cohesion: 0.13
Nodes (8): AtualizacaoCavidadesRequest, Cavidade, Molde, CavidadeRepository, CavidadeRepositoryFusion, ConexaoMySQL, MoldeRepository, MoldeService

### Community 2 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.19
Nodes (4): ApiMoldesServlet, CavidadesServlet, MoldesServlet, UsuarioAtualServlet

### Community 3 - "configLoader.js"
Cohesion: 0.10
Nodes (23): 10. Build e teste local, 1. As três camadas, 2. Identificação de usuário (sem autenticação própria), 3. Endpoints, 4. Chaves de configuração (`application.properties` ou variável de ambiente), 6. Especificação GED (criar no ambiente), 7. Descobrir os FQN das classes (SDK ausente nesta máquina), 8. Checklist de homologação (+15 more)

### Community 4 - "DESIGN.md — CustomFusionWeb"
Cohesion: 0.14
Nodes (13): 1. Tema Visual e Atmosfera, 2. Paleta e Papéis Semânticos, 3. Tipografia, 5. Layout e Espaçamento, 6. Profundidade e Elevação, 7. Do's and Don'ts, 9. Guia de Prompt para Agentes, Checklist antes de concluir (+5 more)

### Community 5 - "telaInicial.js"
Cohesion: 0.19
Nodes (20): abrirModal(), atualizarBotaoConfirmar(), atualizarOpcoesHistorico(), cadastrarMolde(), cardEl(), carregarIndiceHistorico(), chipEl(), confirmarAbrir() (+12 more)

### Community 6 - "GedFusionService"
Cohesion: 0.23
Nodes (4): ConfiguracaoIncompletaException, GedFusionService, Resultado, PublicarGedServlet

### Community 8 - "persistencia.js"
Cohesion: 0.28
Nodes (18): acharGrade(), carregarGrade(), carregarRegistrosMolde(), criarGrade(), disponivel(), existeLegado(), legadoJaMigrado(), lerJson() (+10 more)

### Community 9 - "app.js"
Cohesion: 0.29
Nodes (17): abrirGrade(), acharMolde(), aplicarDesenho(), carregarGradeInterno(), criarEAbrirGrade(), dadosGradeDoMolde(), el(), esconderAviso() (+9 more)

### Community 10 - "estado.js"
Cohesion: 0.13
Nodes (18): compararId(), normalizarRegistro(), registroVazio(), atualizarTudo(), excluir(), initRegistros(), limpar(), salvar() (+10 more)

### Community 12 - "three.min.js"
Cohesion: 0.03
Nodes (23): bind(), br(), copy(), er(), getValue(), Hi(), ji(), Jr() (+15 more)

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

### Community 19 - "io.js"
Cohesion: 0.83
Nodes (3): exportar(), importarTexto(), initIO()

### Community 31 - ".constructor"
Cohesion: 0.09
Nodes (39): MapControls, OrbitControls, dollyIn(), dollyOut(), getZoomScale(), handleKeyDown(), handleMouseDownDolly(), handleMouseDownPan() (+31 more)

### Community 33 - "yc"
Cohesion: 0.08
Nodes (3): qo, yc, yo

### Community 36 - ".setAttribute"
Cohesion: 0.09
Nodes (12): ao(), bo, ci, Da, Ga, gn(), o(), mn (+4 more)

### Community 37 - "nl"
Cohesion: 0.08
Nodes (5): n(), il(), nl, parseShapes(), zl

### Community 39 - "jc"
Cohesion: 0.05
Nodes (3): jc, qc, setValue()

### Community 40 - "xc"
Cohesion: 0.07
Nodes (8): ds(), F(), G(), k(), V(), z(), jo, xc

### Community 42 - "en"
Cohesion: 0.11
Nodes (4): en, Si(), ti, updateMatrixWorld()

### Community 44 - ".copy"
Cohesion: 0.12
Nodes (6): _a, Do, go, Rs, Xe(), zo

### Community 45 - "no"
Cohesion: 0.09
Nodes (16): co(), eo(), fa, ho(), io(), ja(), ka(), lo() (+8 more)

### Community 46 - "ml"
Cohesion: 0.08
Nodes (4): Al, el, ml, Rl

### Community 50 - "ya"
Cohesion: 0.12
Nodes (7): Ah, ca, clone(), fh, hh, Mh, uh()

### Community 51 - "i"
Cohesion: 0.18
Nodes (5): cl, i(), load(), c(), vc

### Community 56 - "parse"
Cohesion: 0.11
Nodes (12): bindSkeletons(), ei, ii, mc(), parse(), parseAnimations(), parseImages(), s() (+4 more)

### Community 58 - "tn"
Cohesion: 0.10
Nodes (3): $e(), Ke(), tn

### Community 59 - "visualizador3d.js"
Cohesion: 0.24
Nodes (19): abrirModal3D(), ajustarAoModelo(), aplicarGeometriaNaCena(), atualizarBotoesVista(), atualizarIndicadorVista(), carregarModelo(), configurarRaycast(), criarGeometriaConexaoProcedural() (+11 more)

### Community 60 - "bt"
Cohesion: 0.12
Nodes (4): Aa, bt, li(), Ra

### Community 61 - "constructor"
Cohesion: 0.23
Nodes (6): bc, constructor(), d(), u(), setDirection(), setLength()

### Community 64 - "GLTFLoader.js"
Cohesion: 0.18
Nodes (11): addMorphTargets(), addPrimitiveAttributes(), addUnknownExtensionsToUserData(), assignExtrasToUserData(), createAttributesKey(), createDefaultMaterial(), createPrimitiveKey(), createDracoPrimitive() (+3 more)

### Community 70 - "us"
Cohesion: 0.25
Nodes (6): cs, v(), g(), us(), b(), M()

### Community 72 - "ps"
Cohesion: 0.27
Nodes (16): ps(), B(), F(), G(), H(), I(), J(), K() (+8 more)

### Community 74 - "gt"
Cohesion: 0.16
Nodes (11): bi(), fi(), gi(), mi(), f(), h(), m(), x() (+3 more)

### Community 75 - "ws"
Cohesion: 0.17
Nodes (11): bs, C(), mt(), ws(), At(), Ct(), Dt(), It() (+3 more)

### Community 77 - ".constructor"
Cohesion: 0.15
Nodes (3): Ea(), a(), o()

### Community 78 - ".assignTexture"
Cohesion: 0.14
Nodes (3): GLTFMaterialsClearcoatExtension, GLTFMaterialsTransmissionExtension, GLTFMaterialsUnlitExtension

### Community 79 - "_fromTexture"
Cohesion: 0.13
Nodes (21): _allocateTargets(), _applyPMREM(), _blur(), _cleanup(), compileCubemapShader(), compileEquirectangularShader(), _compileMaterial(), fromCubemap() (+13 more)

### Community 80 - "ct"
Cohesion: 0.15
Nodes (3): ct(), es(), ns()

### Community 83 - ".parse"
Cohesion: 0.15
Nodes (4): GLTFBinaryExtension, GLTFDracoMeshCompressionExtension, GLTFMeshQuantizationExtension, GLTFTextureTransformExtension

### Community 85 - "pl"
Cohesion: 0.29
Nodes (3): hl, ul, o()

### Community 86 - "parseObject"
Cohesion: 0.12
Nodes (5): $l, parseObject(), ta, tc, Wl

### Community 87 - "rh"
Cohesion: 0.16
Nodes (4): ht(), rh, setFromCartesianCoords(), setFromVector3()

### Community 89 - ".get"
Cohesion: 0.29
Nodes (3): ResultadoPortal, SessaoFusion, UsuarioAtual

### Community 92 - ".loadTextureImage"
Cohesion: 0.22
Nodes (3): GLTFTextureBasisUExtension, GLTFTextureWebPExtension, resolveURL()

### Community 93 - ".parse"
Cohesion: 0.20
Nodes (3): STLLoader, isBinary(), matchDataViewAt()

### Community 98 - ".parse"
Cohesion: 0.20
Nodes (5): oc, i(), parseGeometries(), sc, yn()

### Community 99 - "ni"
Cohesion: 0.12
Nodes (7): Et, ni, pi(), Wn, Tt(), Xn(), yi()

### Community 102 - "4. Componentes"
Cohesion: 0.22
Nodes (9): 4.10 Modal, 4.1 Botões, 4.2 Campos de formulário, 4.4 Cards de resumo (`.resumo-status` / `.resumo-card`), 4.5 Card de grade (`.grade-card`), 4.6 Navbar (`.navbar`), 4.7 Cabeçalho de seção (`.ti-cabecalho`), 4.9 Diagrama SVG (`.diagrama-box`) (+1 more)

### Community 111 - "s"
Cohesion: 0.43
Nodes (6): s(), G(), H(), O(), U(), z()

### Community 112 - "GLTFCubicSplineInterpolant"
Cohesion: 0.29
Nodes (3): computeBounds(), getNormalizedComponentScale(), GLTFCubicSplineInterpolant

### Community 114 - ".setHSL"
Cohesion: 0.33
Nodes (3): dt(), Qe(), ut()

### Community 115 - "jn"
Cohesion: 0.22
Nodes (3): jn(), update(), wh()

### Community 120 - "S"
Cohesion: 0.25
Nodes (7): 4.8 Tabela de cavidades (`.tabela-cav`), 8. Responsividade e Impressão, Breakpoints (existem apenas dois), Impressão (`.folha-impressao`) — tratada como tela, 5. Especificação do formulário Fusion (criar no ambiente), S(), y()

### Community 123 - "t"
Cohesion: 0.28
Nodes (4): r(), t(), l(), o()

### Community 124 - "mo"
Cohesion: 0.22
Nodes (3): fo(), mo(), po

### Community 126 - "4.3 Editor inline (`.editable`)"
Cohesion: 0.70
Nodes (4): 4.3 Editor inline (`.editable`), aplicarGrade(), coletarGrade(), initGrade()

### Community 127 - "Na"
Cohesion: 0.18
Nodes (6): Ba, Ko, Na, _o, qa(), to()

### Community 133 - "$c"
Cohesion: 0.67
Nodes (4): $c(), intersectObject(), intersectObjects(), Kc()

## Knowledge Gaps
- **1 isolated node(s):** `com.customfusion:CustomFusionWeb`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 762 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **91 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Lt` connect `Lt` to `.copy`, `.applyMatrix3`, `three.min.js`, `wo`, `yc`, `.setAttribute`, `pt`, `jc`, `en`, `no`, `ml`, `.set`, `.copy`, `Ne`, `sn`, `.updateWorldMatrix`, `constructor`, `je`, `fe`, `re`, `.copy`, `us`, `ws`, `.constructor`, `ec`, `ys`, `rh`, `ge`, `.parse`, `xs`, `.setFromMatrixPosition`, `.addVectors`, `.constructor`, `ic`, `s`, `jn`, `S`, `Yl`?**
  _High betweenness centrality (0.075) - this node is a cross-community bridge._
- **What connects `com.customfusion:CustomFusionWeb` to the rest of the system?**
  _1 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `extrair_desenhos.py` be split into smaller, more focused modules?**
  _Cohesion score 0.07130333138515488 - nodes in this community are weakly interconnected._
- **Why does `vt` connect `vt` to `dl`, `.copy`, `.applyMatrix3`, `three.min.js`, `Lt`, `.toArray`, `.setAttribute`, `.copy`, `no`, `ml`, `.set`, `.copy`, `sn`, `bt`, `kl`, `jt`, `us`, `eh`, `ws`, `.constructor`, `_fromTexture`, `Ll`, `.addVectors`, `ic`, `fl`, `jn`, `.copy`, `S`, `Yl`, `mo`?**
  _High betweenness centrality (0.064) - this node is a cross-community bridge._
- **Should `Cavidade` be split into smaller, more focused modules?**
  _Cohesion score 0.12561576354679804 - nodes in this community are weakly interconnected._
- **Why does `tn` connect `tn` to `.setStyle`, `three.min.js`, `Lt`, `yc`, `.setAttribute`, `xc`, `.copy`, `no`, `.set`, `ya`, `sn`, `.updateWorldMatrix`, `constructor`, `ws`, `.constructor`, `parseObject`, `ni`, `ic`, `fl`, `.getHex`, `.setHSL`, `t`, `mo`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Should `configLoader.js` be split into smaller, more focused modules?**
  _Cohesion score 0.09971509971509972 - nodes in this community are weakly interconnected._