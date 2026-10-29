# Arquitetura de integração com o Fusion Platform

> Decisão aprovada: JSON para catálogo, GED para documentos, formulário do
> Fusion para dado operacional, identificação de usuário puxada do Fusion
> (sem tela de login própria). Este documento é a spec para homologação.

## 1. As três camadas

| Camada | Tecnologia | O que mora ali |
|---|---|---|
| Catálogo / configuração | **JSON** (`config/*.json`, inalterado) | moldes, desenhos, máquinas, formas, layouts, histórico |
| Documentos oficiais | **GED do Fusion** via API, publicado pelo **servidor** | relatórios/folhas do molde (arquivo + metadados + `neoId`) |
| Dado operacional | **Formulário do Fusion** (`PersistEngine`) | registros de cavidades não conformes por turno |
| Cache no navegador | `localStorage` (era fonte de verdade; agora é cache) | grades abertas, rascunho local |

- `app.persistencia=fusion` (**padrão**) grava no formulário do Fusion.
- `app.persistencia=mysql` mantém o caminho MySQL já existente (tabelas
  `moldes`/`cavidades`) — alternativa pronta, sem apagar nada.
- O `Token` do GED e qualquer credencial ficam **somente no servidor**
  (nunca no JS do navegador).

## 2. Identificação de usuário (sem autenticação própria)

Ordem em `SessaoFusion`:

1. `request.getRemoteUser()` — se o contexto estiver protegido pelo contêiner;
2. `PortalUtil.getCurrentUser()` do Fusion (**via reflexão**, porque o SDK do
   Fusion não está no classpath deste projeto) — classe configurável em
   `app.fusion.portal-util-class`, com candidatos embutidos;
3. `app.dev.usuario` — fallback **apenas** de desenvolvimento local.

Regras importantes:

- `GET /api/usuario-atual` **sempre** responde 200
  (`{autenticado, login, nome, ativo, origem}`) — é o detector do modo no front;
- qualquer outra `GET /api/*` sem usuário → **401 JSON**;
- dentro do Fusion sem sessão → 401 (o fallback `app.dev.usuario` **não**
  se aplica, porque a classe do PortalUtil foi encontrada);
- o autor gravado/publicado vem **da sessão**, não do body do cliente.

## 3. Endpoints

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/usuario-atual` | usuário identificado (sempre 200) |
| GET | `/api/moldes/{codigo}` | molde (somente `app.persistencia=mysql`) |
| PUT | `/api/moldes/{codigo}/cavidades` | grava cavidades; autor carimbado pela sessão |
| POST | `/api/moldes/{codigo}/publicar-ged` | publica no GED |

### PUT /api/moldes/115/cavidades

```json
{ "cavidades": [ { "numero": 1, "zona": 2, "desvio": "C", "desvioTexto": "...",
                   "data": "2026-10-22", "vedado": "N", "controlador": "..." } ] }
```

`usuario` do body é **ignorado quando há sessão** (a sessão manda).

### POST /api/moldes/115/publicar-ged

```json
{ "file": "relatorio-cavidades-115.pdf", "fileBytes": "<base64>" }
```

- `file` precisa ser **exatamente** o nome enviado ao GED (`fields.file`);
- respostas: `200 {ok, neoId, code, url, jaPublicado}` ·
  `400` payload inválido · `401` sem sessão ·
  `503` GED não configurado (lista o que falta) · `502` falha na API do GED;
- `409` do GED é tratado como **idempotência** → `jaPublicado: true`.

## 4. Chaves de configuração (`application.properties` ou variável de ambiente)

| Chave | Padrão | Uso |
|---|---|---|
| `app.persistencia` | `fusion` | `fusion` ou `mysql` |
| `app.fusion.formulario-cavidade` | `CavidadeNaoConforme` | nome interno do formulário |
| `app.fusion.portal-util-class` | (vazio) | FQN do `PortalUtil` |
| `app.fusion.adapter-utils-class` | (vazio) | FQN do `AdapterUtils` |
| `app.fusion.entity-wrapper-class` | (vazio) | FQN do `EntityWrapper` |
| `app.fusion.persist-engine-class` | (vazio) | FQN do `PersistEngine` |
| `app.dev.usuario` | (vazio) | usuário local de desenvolvimento |
| `app.fusion.base-url` | (vazio) | ex.: `http://fusion:8081` |
| `app.fusion.api-token` | (vazio) | Token da API GED |
| `app.fusion.doc-type` | `RELATORIO_CAVIDADES` | tipo de documento GED |
| `app.fusion.folder-base` | `Qualidade\Moldes` | pasta raiz no GED |
| `app.fusion.doc-status` | `published` | `edition` se houver assinatura |
| `app.fusion.doc-fields-extra` | (vazio) | JSON com campos extras do tipo |
| `app.fusion.author-padrao` | (vazio) | author de contingência (sem sessão) |
| `app.db.*` | — | só para `app.persistencia=mysql` |

Toda chave aceita variável de ambiente equivalente
(`app.fusion.api-token` → `APP_FUSION_API_TOKEN`).

## 5. Especificação do formulário Fusion (criar no ambiente)

**Nome interno:** `CavidadeNaoConforme` (ou mudar
`app.fusion.formulario-cavidade`).

**Campos — os NOMES INTERNOS têm que ser exatamente estes:**

| Nome interno | Tipo | Origem |
|---|---|---|
| `molde` | texto | código do molde (ex.: `115`) |
| `numero` | inteiro | número da cavidade |
| `zona` | inteiro (opcional) | zona do layout |
| `desvio` | texto (1) | código do desvio |
| `desvio_texto` | texto longo | descrição do desvio |
| `data` | texto/data ISO `yyyy-MM-dd` | data do registro |
| `vedado` | texto (1) | `S`/`N` |
| `controlador` | texto | **usuário do Fusion** (da sessão) |

Upsert por `(molde, numero)`: salvar de novo atualiza o registro.

## 6. Especificação GED (criar no ambiente)

1. **Token de API** no Fusion (doc: GED → Token) → `app.fusion.api-token`;
2. **Tipo de documento** com campo de anexo (`file`) → `app.fusion.doc-type`;
   se exigir mais campos, preencher `app.fusion.doc-fields-extra`;
3. **Pasta** `Qualidade\Moldes\{codigo}` será criada na publicação
   (`folder.folderPath`, separador `\\`);
4. `author` = código do usuário do Fusion. **O login do controlador precisa
   existir como usuário no Fusion**, senão a API devolve erro de validação.

## 7. Descobrir os FQN das classes (SDK ausente nesta máquina)

As libs do Fusion (`lib fusion/`, referenciadas pelo projeto `CustonFusion`)
não estão nesta máquina. Quando tiver acesso ao ambiente Fusion:

1. No Eclipse, abra um projeto Fusion (ou adicione os JARs) e use **F3** em
   `PortalUtil`, `AdapterUtils`, `EntityWrapper`, `PersistEngine` para ver o
   pacote completo;
2. preencha as chaves `app.fusion.*-class` em `application.properties`;
3. se os candidatos embutidos já acertarem, nada a fazer (eles são testados
   em ordem antes de reclamar).

Enquanto isso, tudo **compila e roda fora do Fusion** — o adapter só falha no
ponto exato onde o SDK existe, com mensagem indicando a chave a configurar.

## 8. Checklist de homologação

- [ ] Criar o formulário + campos (seção 5);
- [ ] Configurar FQN das classes (seção 7) e conferir candidatos;
- [ ] Gerar Token + `base-url` + tipo de documento (seção 6);
- [ ] `GET /api/usuario-atual` dentro do Fusion → `origem: "fusion"`;
- [ ] `PUT .../cavidades` → registro no formulário com `controlador` correto;
- [ ] `POST .../publicar-ged` → `neoId`/`url`; repetir → `jaPublicado: true`;
- [ ] Front em modo API (próximo passo): detectar `/api/usuario-atual` e
      usar `localStorage` só como cache.

## 9. Limitações conhecidas (spike)

- `buscarTodos` carrega os registros do formulário e casa em memória por
  `(molde, numero)` — suficiente para 2 controladores por turno; evoluir para
  consulta nativa (`PersistEngine.getConnection`) se o volume crescer;
- `GET /api/moldes/{codigo}` continua ligado ao MySQL (o front ainda não o usa);
- o front ainda grava **somente** em `localStorage` (integração da próxima
  etapa; o fluxo atual continua funcionando sem backend);
- assinatura eletrônica: trocar `app.fusion.doc-status` para `edition`.

## 10. Build e teste local

- Compilação (sem `mvn` no PATH): `javac` com JARs do `.m2`
  (`jakarta.servlet-api`, `jackson-*`) → `target/classes`;
- Tomcat de teste isolado (porta 8181): `CATALINA_BASE=%TEMP%\opencode\catbase`
  com `catalina.bat run`; sem `APP_DEV_USUARIO` → 401 esperado;
  com `APP_DEV_USUARIO=dev.local` → modo dev.
