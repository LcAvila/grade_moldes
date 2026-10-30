# Autodesk Platform Services (APS) — configuração

O visualizador 3D usa o **Autodesk Platform Services** quando há credenciais
configuradas. Sem credenciais (ou sem internet/sessão), o sistema cai
automaticamente no viewer Three.js local, que continua funcionando offline.

Nada é enviado para a Autodesk enquanto o APS não estiver configurado.

---

## 1. Criar o aplicativo no APS

1. Acesse <https://aps.autodesk.com/myapps> e entre com uma conta Autodesk.
2. Clique em **Create App**.
3. Escolha **Traditional Web App** (ou "Server-side").
4. Preencha:
   - **App name**: `CustomFusionWeb` (ou o nome da sua instalação);
   - **Callback URL**: pode ser `http://localhost:8080/CustomFusionWeb/`
     (o fluxo usado aqui é *2-legged*, sem login do usuário final, então a
     callback não é chamada, mas o campo é obrigatório).
5. Copie o **Client ID** e o **Client Secret**.

> O Client Secret é uma credencial de servidor. Nunca coloque no JavaScript,
> no JSP ou em repositório público.

## 2. Configurar as credenciais

Edite `src/main/resources/application.properties`:

```properties
app.aps.client-id=SEU_CLIENT_ID
app.aps.client-secret=SEU_CLIENT_SECRET
```

Ou use variáveis de ambiente (úteis em produção):

```
APP_APS_CLIENT_ID=SEU_CLIENT_ID
APP_APS_CLIENT_SECRET=SEU_CLIENT_SECRET
```

Chaves opcionais:

| Chave | Padrão | Para que serve |
| --- | --- | --- |
| `app.aps.bucket` | `customfusion-<hash do client id>` | Bucket OSS. Precisa ser único entre **todos** os apps APS do mundo; se colidir, informe outro nome. |
| `app.aps.viewer-version` | `7.100.0` | Versão fixa do Viewer. |
| `app.aps.env` | `AutodeskProduction2` | Ambiente do Viewer. |
| `app.aps.api` | `streamingV2` | API de streaming do Viewer. |
| `app.aps.cache-file` | `%TEMP%/customfusion-aps-cache.json` | Cache local dos URNs já traduzidos. |

Depois de alterar, reinicie o Tomcat (o `AppConfig` lê as propriedades na
inicialização da classe).

## 3. Créditos e custos

O APS é cobrado por consumo:

- **Model Derivative**: cada tradução de STL para SVF2 consome créditos.
- **OSS**: armazenamento e transferência do arquivo.
- **Viewer**: a carga do modelo traduzido também consome.

O cache em disco evita traduzir o mesmo STL novamente. O nome do objeto inclui
um hash do conteúdo (`conexao-padrao-<hash>.stl`), então:

- subir o mesmo arquivo de novo → reaproveita o URN em cache;
- alterar o STL → gera um objeto/URN novo e uma nova tradução.

Para limpar o cache: pare o Tomcat e apague o arquivo indicado em
`app.aps.cache-file` (ou `%TEMP%/customfusion-aps-cache.json`). A próxima
abertura reenvia e retraduz.

## 4. Como o fluxo funciona

```
Navegador                      Backend (Tomcat)                  Autodesk
   |                                  |                              |
   |-- GET /api/aps/modelo?verificar ->| (só checa config; sem upload)|
   |                                  |                              |
   |-- GET /api/aps/modelo?arquivo= ->|-- OAuth 2-legged ----------->|
   |                                  |-- cria bucket (se preciso) -->|
   |                                  |-- PUT STL no OSS ----------->|
   |                                  |-- POST job SVF2 ------------>|
   |                                  |-- GET manifest (polling) --->|
   |<-- {status, urn, progresso} -----|                              |
   |                                  |                              |
   |-- GET /api/aps/token ----------->|-- token viewables:read ----->|
   |<-- {accessToken, expiresIn} -----|                              |
   |                                  |                              |
   |-- SDK Viewer + urn ------------->|                              |
```

- `GET /api/aps/token` devolve um token **somente leitura**
  (`viewables:read`). O token de escrita fica restrito ao servidor.
- `GET /api/aps/modelo?verificar=1` não envia arquivo nem consome créditos;
  serve para a fachada decidir entre APS e viewer local.
- `GET /api/aps/modelo?arquivo=...` garante upload/tradução e é consultado em
  polling até `status = "ready"`.

Todas as rotas `/api/*` passam pelo `IdentificacaoFilter`. Em desenvolvimento
local, sem sessão Fusion, configure `app.dev.usuario` para não receber 401
(não use isso em produção).

## 5. Onde ficam os STLs

O backend lê os arquivos de `/assets/models` dentro do WAR publicado:

```
src/main/webapp/assets/models/
  conexao-padrao.stl
  molde-156.stl
  molde-167A.stl
  molde-168.stl
  molde-200.stl
  molde-98.stl
```

O nome enviado pelo front é exatamente o nome do arquivo (sem caminho). O
servlet valida extensão `.stl`, bloqueia `..`/barras e confirma que o arquivo
existe antes de qualquer upload.

> O Tomcat precisa publicar o WAR **explodido** (padrão). Se
> `getRealPath("/assets/models")` retornar `null`, a API responde com erro
> explicando que o deploy precisa ser explodido.

## 6. Validação

Sem credenciais, o comportamento esperado é:

1. Abrir uma peça → viewer Three.js local, sem chamadas à Autodesk.
2. `GET /api/aps/modelo?verificar=1` → `{"configurado":false,...}`.

Com credenciais:

1. Abra uma peça e confirme no DevTools → Network:
   - `GET /api/aps/modelo?verificar=1` → `configurado: true`;
   - `GET /api/aps/modelo?arquivo=...` → `status: processing` e depois `ready`;
   - `GET /api/aps/token` → `accessToken` presente.
2. O modal "AUTODESK APS" deve abrir com o viewer oficial e o painel de medição.
3. Meça uma distância em **mm** e troque para **cm**; o valor deve ser
   convertido e o separador decimal deve ser vírgula.
4. Se a escala estiver errada (STL não declara unidade), use **Calibrar**:
   clique em dois pontos conhecidos e informe a medida real.
5. Feche e reabra a peça: as medições são limpas e o modelo deve abrir mais
   rápido (URN em cache).

## 7. Problemas comuns

| Sintoma | Causa provável | Solução |
| --- | --- | --- |
| Sempre abre o viewer local | Credenciais ausentes ou `configurado:false` | Preencha `app.aps.client-id/secret` e reinicie o Tomcat. |
| `/api/aps/*` retorna 401 | Sem sessão Fusion e sem `app.dev.usuario` | Faça login no Fusion ou configure o usuário de dev. |
| `Falha ao criar bucket APS (403)` | Token sem `bucket:create` ou bucket já existente em outra conta | Confira os escopos e informe um `app.aps.bucket` único. |
| `Modelo não encontrado` | STL ausente em `/assets/models` ou nome com caminho | Confira o nome exato do arquivo no WAR. |
| `A tradução do modelo falhou` | STL inválido, job recusado ou créditos esgotados | Veja `status`/`mensagem` no JSON e o log do Tomcat. |
| Viewer não carrega (tela cinza) | SDK bloqueado por rede/proxy/CSP | Libere `developer.api.autodesk.com`; o botão "ABRIR VIEWER LOCAL" aparece no erro. |
| Medição com escala errada | STL sem unidade declarada | Use **Calibrar** no painel APS. |
| Abre, mas os botões de medição não respondem | Modelo ainda carregando ou WebGL indisponível | Aguarde o overlay sumir; verifique o console. |

## 8. Segurança e privacidade

- O Client Secret e o token de escrita **nunca** vão para o navegador.
- O token do viewer tem apenas `viewables:read` e é renovado pelo backend.
- O STL é enviado para a nuvem Autodesk (OSS) e traduzido lá. Se o desenho for
  confidencial, avalie a política da empresa e a região do bucket antes de
  habilitar o APS. Sem credenciais, nada sai da máquina.
- O cache local guarda apenas URNs e nomes de objeto, não o conteúdo do STL.
