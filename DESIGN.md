---
version: alpha
name: CustomFusionWeb — Controle de Cavidades Não Conformes
description: >-
  Sistema interno de chão de fábrica (Compactor — Cia. de Canetas) para registrar
  cavidades não conformes de moldes de injeção. A linguagem visual é a de uma
  folha de controle técnica: densa, planilhal, sem decoração, com um único azul
  institucional para ações, um semáforo fixo (verde = OK/ativa, amarelo =
  defeito, vermelho = vedada) e paridade obrigatória entre a tela e a folha A4
  impressa. Tipografia única (Arial/Helvetica), cantos de 4–6px, sombras
  discretas e rótulos em CAIXA ALTA.

colors:
  primary: "#1976a8"        # --blue — ação principal, seleção, foco
  primary-dark: "#125a80"   # --blue-dark — hover do primário, texto de marca
  canvas: "#f3f5f7"         # --bg — fundo da aplicação
  surface: "#ffffff"        # --card / #fff — painéis, tabelas, modais
  surface-soft: "#fafcfd"   # caixas de aviso/info, cards de resumo
  surface-sunken: "#f7f9fa" # cabeçalho de tabela
  border: "#d5dde3"         # --border — divisórias internas
  border-strong: "#9caab4"  # --border-dark — contornos de blocos
  ink: "#34404a"            # --text
  ink-muted: "#74808a"      # --muted
  success: "#2e7d32"        # --green — OK, grade ativa
  success-soft: "#e8f5e9"
  success-border: "#b6dcb8"
  warning: "#f2b01e"        # --yellow — defeito (vedado = N)
  warning-ink: "#a66d00"    # texto de defeito
  warning-soft: "#fff9e6"
  warning-border: "#f0ddab"
  danger: "#d32f2f"         # --red — vedada (vedado = S)
  danger-hover: "#a92323"
  danger-soft: "#fff0f0"
  primary-soft: "#eaf4f9"   # chips, contador, hover secundário
  primary-selected: "#e6f2f8" # célula/cavidade selecionada
  print-logo: "#c1121f"     # fallback COMPACTOR na folha impressa
  print-product: "#1a3fb0"  # produto na folha impressa

typography:
  family: "Arial, Helvetica, sans-serif"
  base: "13px / 1.45"
  scale:
    micro: "10px"
    caption: "11px"
    small: "12px"
    body: "13px"
    label: "14px"
    block-title: "16px"
    subheading: "17px"
    stat: "20px"
    heading: "21px"
    page-title: "22px"
    svg-hub: "26px"
    empty-icon: "34px"
    print-body: "6.6px"
    print-title: "8px"
    print-heading: "17px"

rounded:
  badge: "3px"
  control: "4px"
  card: "6px"
  chip: "10px"
  pill: "12px"
  counter: "16px"

spacing:
  unit: "2px"
  scale: ["3px", "4px", "5px", "6px", "8px", "9px", "10px", "12px", "14px", "16px", "18px", "20px", "22px", "44px"]
  control-height: "36px"
  nav-control-height: "38px"
---

# DESIGN.md — CustomFusionWeb

> Fonte da verdade visual deste projeto. Documento derivado de `src/main/webapp/css/style.css`,
> `src/main/webapp/index.jsp` e dos módulos `src/main/webapp/js/render/*`.
> A coleção `design-md/` na raiz é **referência externa** (84 DESIGN.md de marcas);
> **este arquivo descreve o que o sistema é hoje** e deve ser seguido em qualquer UI nova.

---

## 1. Tema Visual e Atmosfera

**É uma folha de controle, não uma landing page.**

O app é usado em pé, no chão de fábrica, em monitor industrial, com uma planilha
de 64+ cavidades aberta ao lado de um diagrama. A interface existe para ser lida
em 2 segundos e preenchida com o mínimo de cliques.

Princípios:

1. **Densidade controlada** — base de 13px, alturas de linha de 25px na tabela,
   gaps de 8–12px. Não "arejar" a tela: espaço vazio aqui é informação perdida.
2. **Semáforo é contrato** — verde/amarelo/vermelho significam exatamente
   OK-ativa / defeito / vedada. Nunca usar essas cores como decoração.
3. **Azul só para ação** — `#1976a8` indica o que é clicável, selecionado ou focado.
4. **Chapa única** — todo bloco é um retângulo branco com borda `#9caab4` e raio
   4–6px, sobre fundo `#f3f5f7`. Nada de gradiente, vidro, ou excesso de sombra.
5. **Paridade tela ↔ papel** — tudo que importa na tela existe na folha A4
   (`.folha-impressao`). A impressão não é um "extra": é o produto final.
6. **Rótulo em CAIXA ALTA** — títulos de seção, botões e cabeçalhos de tabela
   são escritos em maiúsculas no próprio HTML (não via `text-transform`, exceto
   `.nav-link`, `.resumo-card .lbl`, `.gc-stat .lbl`, `.gc-status`).

---

## 2. Paleta e Papéis Semânticos

Variáveis reais em `:root` de `style.css`. **Não criar cores novas**: qualquer
tom necessário já existe abaixo ou é uma variação de opacidade.

| Token | Hex | Papel | Onde aparece |
| --- | --- | --- | --- |
| `--blue` | `#1976a8` | Ação primária, foco, seleção | `.btn`, `:focus`, `.nav-link.active`, `.ti-cabecalho` (borda esq.), seleção SVG |
| `--blue-dark` | `#125a80` | Hover do primário, texto de marca | `.btn:hover`, `.logo-text`, `.nav-nome`, `.gc-molde` |
| `--bg` | `#f3f5f7` | Fundo da aplicação | `body` |
| `--card` | `#ffffff` | Superfície de painel/bloco | `.painel-form`, `.painel-grade`, `.grade-card` |
| `--border` | `#d5dde3` | Divisória interna, célula de tabela | `td`, `h2/h3` underline, `.info-linha` |
| `--border-dark` | `#9caab4` | Contorno de bloco/campo | painéis, inputs, `.header-grade`, `.modal` |
| `--text` | `#34404a` | Texto padrão | `body`, labels, valores |
| `--muted` | `#74808a` | Texto secundário, dica, unidade | `.hint`, `.legenda-box`, `.data-cell`, `.gc-chip-muted` |
| `--green` | `#2e7d32` | OK / grade ativa | `.res-ok .num`, `.gc-status`, `.grade-card` (borda topo) |
| `--yellow` | `#f2b01e` | Defeito (vedado = N) | `.status-defeito` (SVG), badge de desvio, `.gc-status.com-defeito` (ponto) |
| `#a66d00` | `#a66d00` | Texto/contorno de defeito | `.res-def .num`, `.gc-def .num`, `strong` do "N" |
| `--red` | `#d32f2f` | Vedada (vedado = S), ação destrutiva | `.btn-danger`, `.res-ved .num`, `.status-vedado`, célula vedada |
| `#a92323` | `#a92323` | Hover/stroke de vermelho | `.btn-danger:hover`, `stroke` de `status-vedado` |
| `#eaf4f9` | `#eaf4f9` | Lavagem azul clara | `.gc-chip`, `.ti-contador`, `.btn-secondary:hover` |
| `#e6f2f8` | `#e6f2f8` | Seleção em célula | `.tabela-cav td.cav-selecionada` (`!important`) |
| `#fafcfd` | `#fafcfd` | Superfície rebaixada | `.aviso-box`, `.resumo-card`, `.info-box`, `.cav-num`, `.grades-vazio` |
| `#f7f9fa` | `#f7f9fa` | Cabeçalho de tabela | `.tabela-cav th` |
| `#fff9e6` / `#fff0f0` | | Fundo de linha com defeito / vedada | `.cav-com-def-cel`, `.cav-vedada-cel` |
| `#e8f5e9` + `#b6dcb8` | | Pill "Ativa" | `.gc-status` |
| `#fff2f2` + `#f0b7b7` | | Caixa de erro de formulário | `.form-erro` |
| `#c1121f` | | Fallback do logo **na impressão** | `.folha-cab-logo .fb-nome` |
| `#1a3fb0` | | Nome do produto **na impressão** | `.folha-cab-info .produto` |

**Regras fixas do semáforo:**

- `d.vedado === "S"` → vermelho (linha, célula, círculo, texto do "Vedado").
- `d.desvio` preenchido e `vedado !== "S"` → amarelo.
- sem registro → branco/cinza neutro.
- Verde **nunca** sinaliza cavidade individual — verde é agregado (contagem OK,
  grade ativa). Isso evita competir com o amarelo.

---

## 3. Tipografia

Família única: **Arial, Helvetica, sans-serif**. Sem webfonts, sem Google Fonts,
sem ícones de biblioteca (apenas glifos Unicode: `‹`, `›`, `▣`, `+`).

| Nível | Tamanho / Peso | Uso |
| --- | --- | --- |
| Título de página | 22px / 600, `letter-spacing: 1px` | `.ti-titulo h1` |
| Título de bloco | 21px / 600, `letter-spacing: 1px` | `.title-box h1` |
| Subtítulo de painel | 17px / 600 | `.painel-form h2` |
| Nome do molde no card | 17px / 700, `letter-spacing: .4px` | `.gc-molde` |
| Título de modal | 16px / 700 | `.modal h3` |
| Marca | 15px / 700, `letter-spacing: .6px` | `.nav-nome` |
| Número de destaque | 20–21px / 700 | `.resumo-card .num`, `.gc-stat .num` |
| Rótulo de bloco | 13px / 600 | `.info-box h4`, `.legenda-box h3`, `.info-entrada h3` |
| Corpo / campo | 13px / 400 | `body`, `input`, `select`, `textarea`, `.det-linha` |
| Rótulo de campo | 12px / 600 | `.form-group label` |
| Dado tabular | 12px / 400–700 | `.tabela-cav` |
| Micro-rótulo | 10px / 400–700, uppercase, `letter-spacing .4–.6px` | `.gc-stat .lbl`, `.resumo-card .lbl`, `.gc-status` |
| Dica | 11–12px / 400, `--muted` | `.hint`, `.leg-item`, `.info-linha` |
| Sigla de legenda | 11px / 700, `min-width: 52px` | `.leg-sigla` |
| Texto em SVG | 700 / 20px (cavidade), 26px (zona) | `.cav-text`, `.hub-text` |

Regras:

- **Nunca** tamanho < 10px na tela; < 6.6px só na folha impressa.
- Datas exibidas sempre `dd/mm/aaaa` (`CF.estado.br()`); ISO só no estado/persistência.
- Números de cavidade são `700` para leitura rápida no diagrama.
- Não usar `font-weight` 300/500 — só 400, 600, 700, 800.

---

## 4. Componentes

### 4.1 Botões

```css
.btn                 /* 36px alto, padding 8px 14px, raio 4px, 600 13px Arial */
.btn:hover           /* --blue-dark */
.btn-secondary       /* fundo #fff, texto --text, borda --border-dark */
.btn-danger          /* --red, hover #a92323 */
.btn-success         /* --green, hover #205c25 */
.btn:disabled        /* opacity .55, cursor not-allowed, hover neutralizado */
```

- Rótulo **em caixa alta** no HTML (`SALVAR`, `EXCLUIR REG.`, `+ ABRIR GRADE`).
- Em `.btn-group`, todos os botões têm `flex: 1 1 120px` → larguras iguais.
- No `.navbar`, `.btn` é `flex: 0 0 auto` com `min-width: 170px` / 38px de altura.
- Hover **escurece** (nunca clareia); `btn-secondary` inverte para `#eaf4f9`.
- Ação destrutiva sempre `btn-danger` e separada visualmente das demais.

### 4.2 Campos de formulário

```css
.form-group input|select|textarea  /* min-height 36px, padding 8px 10px,
                                      borda 1px --border-dark, raio 4px, 13px Arial */
:focus                             /* borda --blue + ring 0 0 0 2px rgba(25,118,168,.14) */
.form-group label                  /* 12px / 600, margin-bottom 6px */
.hint                              /* 11px --muted; <strong> em --blue-dark */
.hint-erro                         /* --red, 600 */
.form-erro                         /* caixa #fff2f2 + borda #f0b7b7, 12px/600 */
```

- `.radio-group`: `display:flex`, `gap:18px`, `accent-color: var(--blue)`.
- `.obsField` / `#obsField` aparece só quando `desvio === "REB."` ou `"OUTRO"`.
- Campo `Desvio / Defeito` é **híbrido**: `input` + `datalist` (selecionar ou digitar).
- Placeholder sempre descritivo e em pt-BR, nunca um exemplo genérico.

### 4.3 Editor inline (`.editable`)

Blocos "de papel" (produto, molde, material, controlador, atualização,
observações) são `contenteditable` com borda tracejada **transparente**:

- **Origem do valor**: `estado.dadosGrade`, montado por `app.js`
  (`dadosGradeDoMolde`) como `molde.padroes` + o que está salvo na grade. Campo
  salvo **em branco** não apaga o padrão — só valor preenchido sobrescreve.
- **Máquina e data de entrada são fixas e não editáveis**: vêm do modal de
  abertura da grade (`selMaquina` / `dataAbertura` em `telaInicial.js`), gravadas
  em `grade.maquina` / `grade.dataEntrada` por `persistencia.criarGrade` e
  reaplicadas por `inicializarDados(molde, persistido, grade)`. Os `span`
  `data-field="maquina"` e `data-field="dataEntrada"` no `index.jsp` não têm
  `contenteditable` nem `.editable`.
- **Revisão é fixa e não editável**: `CF.estado.revisaoFixa` (`07` /
  `02/01/2027`, em `js/core/estado.js`) é aplicada a **todas as grades** em
  `dadosGradeDoMolde` e sobrescreve tanto `molde.padroes` quanto o registro
  salvo. Os `span` de revisão no `index.jsp` não têm `contenteditable` nem a
  classe `.editable`; `aplicarGrade` preenche todo `[data-field]`, mas
  `coletarGrade`/`initGrade` só agem em `.editable[data-field]`. A impressão lê
  o mesmo objeto (`folhaRevisao`, em `impressao.js`), então também sai fixa.
- repouso: indistinguível de texto;
- hover: borda `--yellow` + fundo `#fffdf0` (dica de editabilidade);
- foco: borda `--blue` + ring `rgba(25,118,168,.12)`.
- Ao sair do campo (`blur`), `cabecalho.js` copia o texto para `dadosGrade` e
  persiste.

### 4.4 Cards de resumo (`.resumo-status` / `.resumo-card`)

Grade de 4 colunas (Total / OK / Defeitos / Vedadas), separadores por borda, não
por sombra. Número 21px/700 colorido pelo status; rótulo 10px uppercase `--muted`.
Vazio = `0` (nunca `—`).

### 4.5 Card de grade (`.grade-card`)

Componente de entrada do fluxo principal:

- `border: 1px --border-dark`, `border-top: 4px --green` (vira `--yellow` com ocorrências), raio 6px, padding 16px.
- Hover: `translateY(-3px)` + `box-shadow: 0 8px 20px rgba(25,50,65,.18)` + borda-topo `--blue`.
- Foco de teclado: `box-shadow: 0 0 0 3px rgba(25,118,168,.3)`; é `tabindex="0"` e responde a `Enter`/`Espaço`.
- Estrutura fixa: `Molde <código>` + pill de status → nome da peça → chips
  (máquina, material) → 3 estatísticas (Ativas / Defeitos / Vedadas) → rodapé
  "Aberta em dd/mm/aaaa · hh:mm" + "Abrir grade ›" com seta que desliza 4px no hover.
- Pill `.gc-status`: raio 12px, 10px/700 uppercase, `letter-spacing .6px`, com
  **ponto de 7px** via `::before`. Texto: `Ativa` ou `Com ocorrências`.

### 4.6 Navbar (`.navbar`)

- `position: sticky; top: 0; z-index: 60`, fundo branco, borda inferior
  `--border-dark`, `box-shadow: 0 2px 10px rgba(25,50,65,.14)`.
- Margem negativa (`-10px`) para encostar nas bordas do `body` com padding 10px.
- Estrutura: logo (máx. 150×40) → nome da marca com borda esquerda → links
  (12px/600 uppercase, `letter-spacing .6px`, ativo com sublinhado de 2px em `--blue`)
  → ações à direita (`margin-left:auto`).
- Só existe **um** item de navegação hoje (`Moldes ativos`); não inventar abas
  sem rota real.

### 4.7 Cabeçalho de seção (`.ti-cabecalho`)

`border-left: 4px solid var(--blue)` é a assinatura da tela inicial. Título 22px
+ subtítulo 12px `--muted` à esquerda; `.ti-contador` em pill (raio 16px,
`#eaf4f9`, 12px/700) à direita, com estado `.zero` em cinza.

### 4.8 Tabela de cavidades (`.tabela-cav`)

- `border-collapse: collapse`, 12px, dentro de wrapper com `overflow-x:auto`.
- `th`: fundo `#f7f9fa`, borda `--border-dark`, 7px de padding vertical, 600, centralizado.
- `td`: altura 25px, padding `4px 5px`, borda `--border`, fundo branco.
- Larguras: `cav-num` 34px (fundo `#fafcfd`, 700), `desvio-cell` 130px
  (600, `cursor:pointer`, `title` = observação), `data-cell` 82px (11px, `--muted`).
- Estado por linha (aplicado às 5 células): selecionada `#e6f2f8`, com defeito
  `#fff9e6`, vedada `#fff0f0`. **Vedada vence defeito.**
- Badge `.cav-defeito`: raio 3px, `2px 5px`, 10px, fundo vermelho (texto branco)
  ou amarelo (texto `--text`), definido inline pelo JS.
- Coluna "Vedado" mostra `S` / `N` em 700, colorido (`--red` / `#a66d00`) —
  nunca ícone, nunca "Sim/Não" na tabela (só no modal).

### 4.9 Diagrama SVG (`.diagrama-box`)

- Um `<svg>` com `viewBox` vindo do layout (`config/layouts/*.json`), `width:100%`
  com `max-width:520px`, centralizado. O `viewBox` varia de `248×2260` a
  `2513×3391`, então a escala é própria de cada layout. Na prática, com a coluna
  direita limitada a 470px, o diagrama desenha em no máximo **454px** de largura
  (470px menos os 8px de padding de cada lado do `.diagrama-box`).
- **Regra de fit:** o teto é aplicado no próprio `<svg>`
  (`max-height: clamp(260px, 58vh, 560px)`), aproveitando o
  `preserveAspectRatio` padrão (`xMidYMid meet`). O desenho inteiro encolhe e
  centraliza dentro do teto — **nunca corta e nunca rola**. É isso que faz uma
  grade de `248×2260` (que esticada passava de 4.400px) caber na tela.
- `.diagrama-box` **não** tem `overflow:auto` nem `max-height` próprio: qualquer
  um dos dois volta a cortar o diagrama em vez de reduzi-lo.
- Consequência aceita: grades muito estreitas ficam pequenas na tela (ex.:
  `layout-183`, `248×2260` → ~57px de largura dentro de 522px de altura). O
  requisito é ver o molde inteiro; se a legibilidade incomodar, o ajuste é o teto
  do `clamp` (uma linha), não a volta do scroll.
- O `max-height` no SVG, nunca `height` fixo nem no box: quando o diagrama é baixo
  (ex.: `layout-202`, `751×245` → 170px) ele usa só a altura que precisa.
- A impressão não é afetada: o clone vai para `.folha-diagrama`, cujo
  `width:100%;height:100%` sobrescreve o teto de tela.
- `.moldura-rect`: contorno preto de 4px (a peça).
- Cavidade: `status-normal` (branco / preto 2px), `status-defeito`
  (`--yellow` / `#a66d00`), `status-vedado` (`--red` / `#a92323`).
- Seleção: `filter: drop-shadow(0 0 6px var(--blue))` + `stroke: var(--blue); stroke-width: 5`.
- Hover: `filter: brightness(.88)` — sem mudança de tamanho.
- Texto de cavidade 700/20px preto centralizado; vira branco quando vedada
  (`.status-vedado-txt`); texto de zona (hub) 700/26px.
- **Tamanho por layout**: `diagrama.js` respeita `r` e `fontSize` de cada cavidade
  do JSON (`layout.cavidades[]`), e `r`/`fontSize` do hub. Como o layout é
  desenhado inteiro em ~520px de altura, o que importa é a **proporção contra o
  `viewBox`**, não o número absoluto. Regra prática: `r / largura do viewBox ≈
  0,026` e `fontSize / largura ≈ 0,020` (é o que `layout-200`, `2513×3391` com 96
  cavidades, usa: `r:65`, `fontSize:50`). Antes disso ele estava em `r:45` /
  `fontSize:34` e o número ficava com ~5px na tela.
- Hub de zona é **clicável** e seleciona as 16 cavidades daquela zona.
- Nunca animar o SVG além de `filter`/`transition: .15s`.

### 4.10 Modal

- Overlay `rgba(20,35,45,.55)`, `padding:20px`, `z-index:1000`, fecha ao clicar
  no fundo ou `Escape`.
- `.modal` `min(450px,100%)` (480px em `.modal-abrir`), raio 4px,
  `box-shadow: 0 8px 30px rgba(0,0,0,.22)`.
- Cabeçalho `<h3>` 16px com divisória inferior; conteúdo em `.det-linha`
  (`label` 115px `--muted` 600 + valor).
- Ações alinhadas à direita, `gap:8px`, na ordem: **ação primária → destrutiva →
  cancelar/fechar**.
- Nunca mais de um modal aberto; nunca modal sem ação de fechar.

---

## 5. Layout e Espaçamento

- **Escala**: múltiplos de 2px, com passos de 4/6/8/10/12/14/16/18/20/22.
  Não usar 7, 13, 17, 23, 25.
- **Grade principal**: `grid-template-columns: 420px minmax(0,1fr)`, `gap:12px`,
  `max-width:1800px`, `margin:auto`. Painel de formulário é `sticky; top:10px`.
- **Corpo da grade**: `minmax(0,1fr) minmax(280px,470px)` — tabela à esquerda,
  diagrama/peça/legenda/infos empilhados à direita com `gap:8px`. A direita tem
  **teto fixo de 470px** (não `fr`): crescer junto com a tabela só aumentava o
  diagrama e desperdiçava largura que a tabela de 64+ cavidades precisa.
- A coluna esquerda usa `minmax(0,1fr)`, não `540px`: com o teto à direita, um
  mínimo de 540px à esquerda estourava o corpo entre 1251px e 1419px e criava
  rolagem horizontal na página. Com `0`, o espaço que sobra vai para a tabela e,
  quando ela fica estreita, rola dentro de si (`.tabela-cav`), que é a regra já
  documentada — nunca rolar a página.
- **Cabeçalho da grade**: `grid-template-columns: 200px minmax(0,1fr) 280px`,
  altura mínima 78px, bordas verticais em `--border-dark` (efeito de formulário
  impresso, não de card).
- **Cards de grade**: `repeat(auto-fill, minmax(290px, 1fr))`, `gap:14px`.
- **Padding de painel**: 12–16px. Nunca > 20px.
- Alinhamento: tudo centralizado nas células; rótulos à esquerda apenas em
  `.form-group` e `.info-linha`.

---

## 6. Profundidade e Elevação

Só existem **4 níveis**. Qualquer sombra fora desta lista é um bug visual.

| Nível | Sombra | Uso |
| --- | --- | --- |
| 0 — plano | `none` | tabelas, blocos internos, SVG |
| 1 — painel | `0 2px 8px rgba(25,50,65,.12)` (`--shadow`) | `.painel-form`, `.painel-grade`, `.grade-card`, `.ti-cabecalho` |
| 2 — fixo | `0 2px 10px rgba(25,50,65,.14)` | `.navbar` (sticky) |
| 3 — modal | `0 8px 30px rgba(0,0,0,.22)` | `.modal` |
| hover | `0 8px 20px rgba(25,50,65,.18)` + `translateY(-3px)` | `.grade-card:hover` |

Foco de teclado (obrigatório em todo elemento interativo):

```css
box-shadow: 0 0 0 3px rgba(25,118,168,.30);   /* cards */
box-shadow: 0 0 0 2px rgba(25,118,168,.14);   /* campos */
```

Hierarquia de superfícies (do fundo para frente):
`--bg` → `#fff` (painel) → `#fafcfd` (bloco rebaixado) → `#eaf4f9` (chip/realce).

---

## 7. Do's and Don'ts

**Faça**

- Reutilize as classes existentes antes de criar novas.
- Use `--blue` para tudo que é clicável/focado/selecionado e nada mais.
- Mantenha a tabela e o SVG sincronizados: todo estado novo precisa aparecer
  nos dois (`tabela.js`, `diagrama.js`, `resumo.js`).
- Escreva rótulos em CAIXA ALTA, curtos, em pt-BR (`SALVAR`, `EXPORTAR JSON`).
- Preserve a paridade com `.folha-impressao`: se adicionar dado na tela,
  avalie se ele precisa ir para o A4.
- Garanta alvo de toque ≥ 34px (36px é o padrão; 25px de linha é só tabela).
- Datas em `dd/mm/aaaa`; números de cavidade como `string` (existem IDs `107A`).

**Não faça**

- Não introduza cores fora da paleta (nem "um cinza mais bonito").
- Não use verde para cavidade individual, nem vermelho para ação não destrutiva.
- Não use `border-radius` > 6px em painéis/cards; pill (10–16px) é só para
  chips, status e contador.
- Não adicione gradientes, blur, glassmorphism, dark mode ou sombra colorida.
- Não troque a fonte, não adicione webfont, não use ícones de biblioteca.
- Não use `!important` (a única exceção é `.cav-selecionada` para vencer
  `.cav-com-def-cel`/`.cav-vedada-cel`).
- Não crie modal empilhado, tooltip flutuante ou toast — o feedback é inline
  (`.hint`, `.form-erro`, `.aviso-box`).
- Não mova a lógica de negócio para o CSS nem estilos para o JS além de cores de
  status e `display` condicional já existentes.
- Não altere `config/*.json` para "resolver" layout — o layout vem do JSON do molde.

---

## 8. Responsividade e Impressão

### Breakpoints (existem apenas dois)

| Largura | Mudança |
| --- | --- |
| `≤ 1250px` | `.app` → `360px minmax(0,1fr)`; `.corpo-grade` → 1 coluna (tabela em cima, diagrama embaixo) |
| `≤ 800px` | `body` padding 6px; `.app` vira bloco; `.painel-form` deixa de ser sticky; `.header-grade` empilha (1 coluna, divisórias horizontais); `.navbar` quebra com links em linha própria (`order:3`); `.grades-grid` 1 coluna; `.ti-cabecalho` alinhado à esquerda |

- Não criar breakpoint intermediário (não há tablet dedicado).
- Alvos de toque continuam ≥ 36px no mobile.
- Tabela larga rola horizontalmente (`.tabela-cav { overflow-x:auto }`), nunca
  "encolhe" a fonte.

### Impressão (`.folha-impressao`) — tratada como tela

- `@page { size: A4 portrait; margin: 8mm }`; folha fixa **194mm × 281mm**,
  `overflow:hidden`, tudo dimensionado em `mm`/`pt` (nunca `px`).
- `@media print`: `.app` e `.tela-inicial` somem; só a folha aparece.
- Cabeçalho 20mm em 3 colunas (38mm logo / `1fr` título / 46mm dados), bordas
  `1.4pt` externas e `0.5pt` internas, texto preto puro.
- Corpo em 2 colunas: **coluna esquerda só com a tabela** (altura livre da
  folha), **coluna direita com o resto da tabela + diagrama + peça + legenda +
  infos**. Linhas de tabela com altura padrão de **4,5mm**
  (`table-layout:fixed`) via `--folha-linha-tabela`, diagrama 96mm, peça 22mm,
  legenda 30mm, infos `1fr` e revisão 6mm.
- **Altura de linha por molde**: `layout.impressao.linhaMm` reduz a altura das
  linhas no papel (padrão `4,5`). É o recurso para moldes com muitas cavidades,
  em que 4,5mm não cabem. Regra: `maxEsq = floor((261 - 4.2) / linhaMm)`,
  `maxDir = floor(77.8 / linhaMm)` (`77,8mm` = altura útil menos diagrama 96,
  peça 22, legenda 30, revisão 6, `thead` 4,2 e 25mm de respiro para as infos);
  escolhe-se a maior `linhaMm` cuja soma `maxEsq + maxDir` comporte o total.
  Hoje só o molde **200** (96 cavidades) precisa: `[74, 22]` + `linhaMm: 3.45`.
- **Blocos da tabela no papel**: `layout.impressao.tamanhosBlocos` define a
  divisão entre a coluna esquerda e a direita (`app.js` →
  `estado.blocosImpressao`). A divisão é **proposital e desbalanceada** e segue
  o molde 168, que é a referência: a direita fica com **9 linhas** (as mesmas do
  168) quando o molde tem mais de 18 cavidades, e a esquerda recebe o resto até
  encher a altura útil. Tabela vigente: `24 → [15,9]`, `32 → [23,9]`,
  `48 → [39,9]`, `64 → [55,9]` (168), `96 → [74,22] @3.45`. Moldes com até 18
  cavidades continuam divididos em duas metades iguais (`8 → [4,4]`,
  `16 → [8,8]`, `2 → [1,1]`, `1 → [1,0]`). **Não "igualar" a divisão** nos
  moldes grandes (32/32, 48/48 etc.) sem conferir a prévia: metades iguais
  empurram diagrama, legenda, infos e revisão para fora da folha (medido em PDF
  A4: 48/48 e 64/32 perdem a legenda/infos/revisão; 39/9 e 55/9 não perdem
  nada). A soma dos blocos tem que ser o total de cavidades.
- Cores na impressão: preto `#000`, logo `#c1121f`, produto `#1a3fb0`,
  vedado `S` `#c1121f`, `N` `#a66d00`, `th` `#eee`.
- Tamanhos de texto: 6.6px corpo, 7.5px títulos de bloco, 17px título da folha.
- Nada de sombra; a hierarquia vem de borda e peso de fonte.

---

## 9. Guia de Prompt para Agentes

### Referência rápida

```
Primária:   #1976a8   hover: #125a80
Sucesso:    #2e7d32   (OK / grade ativa)
Defeito:    #f2b01e   texto #a66d00
Vedada:     #d32f2f   hover #a92323
Fundo:      #f3f5f7   Painel: #ffffff   Rebaixado: #fafcfd
Borda:      #d5dde3   Borda forte: #9caab4
Texto:      #34404a   Secundário: #74808a
Fonte:      Arial, Helvetica, sans-serif · base 13px
Raio:       3 (badge) / 4 (controle) / 6 (card) / 12–16 (pill)
Sombra:     0 2px 8px rgba(25,50,65,.12)
```

### Onde mexer

| Preciso de… | Arquivo |
| --- | --- |
| Variável/cor/estilo global | `src/main/webapp/css/style.css` (`:root`) |
| Estrutura de tela | `src/main/webapp/index.jsp` |
| Card da tela inicial, modal de abrir grade | `js/render/telaInicial.js` |
| Tabela de cavidades | `js/render/tabela.js` |
| Diagrama SVG | `js/render/diagrama.js` + `js/render/svg.js` |
| Formulário/seleção múltipla | `js/render/formulario.js` |
| Contadores | `js/render/resumo.js` |
| Modal de detalhes | `js/render/modal.js` |
| Folha A4 | `js/render/impressao.js` + `css/style.css` (`@media print`) |
| Cabeçalho/dados do molde | `js/render/cabecalho.js` |

### Prompts prontos

**Novo painel/bloco na tela da grade**

> Adicione um bloco "X" em `index.jsp` dentro de `.area-direita`, seguindo
> `DESIGN.md`: superfície branca, borda `1px #9caab4`, raio 4px, título 13px/600
> com divisória inferior `#d5dde3`, corpo 11–12px `#74808a`. Sem sombra. Use as
> classes existentes (`.legenda-box`, `.info-entrada`) como base e não crie cores
> novas.

**Novo estado/indicador**

> Estenda `tabela.js`, `diagrama.js` e `resumo.js` juntos. O estado deve usar
> apenas verde `#2e7d32` (agregado), amarelo `#f2b01e` (defeito) ou vermelho
> `#d32f2f` (vedado). Nada de nova cor, ícone ou animação além de `filter`.

**Nova tela**

> Crie a tela em `index.jsp` com `.navbar` (sticky, sombra nível 2), um
> `.ti-cabecalho` com `border-left:4px solid #1976a8` e cards
> `repeat(auto-fill, minmax(290px,1fr))` com `gap:14px`. Rótulos em CAIXA ALTA,
> Arial 13px, fundo `#f3f5f7`. Verifique o layout em 1250px, 800px e na impressão.

**Revisão de UI**

> Compare a alteração com `DESIGN.md` e liste violações: cor fora da paleta,
> raio > 6px em painel, sombra fora dos 4 níveis, fonte diferente de
> Arial/Helvetica, texto < 10px, ausência de foco visível, ou quebra da paridade
> com a folha A4.

### Checklist antes de concluir

- [ ] Só cores da paleta (nenhum hex novo)
- [ ] Semáforo correto: verde agregado, amarelo defeito, vermelho vedada
- [ ] Todo elemento clicável tem hover **e** foco visível
- [ ] Rótulos em caixa alta, pt-BR, curtos
- [ ] Raio ≤ 6px em painéis; pill só em chip/status/contador
- [ ] Tabela, diagrama e resumo atualizados no mesmo commit
- [ ] Layout conferido em 1250px e 800px
- [ ] Impressão em A4 ainda cabe em uma folha
