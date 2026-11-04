# CustomFusionWeb

Controle de cavidades não conformes de moldes.

## Banco de dados

O catálogo e as grades ficam em SQLite:

- padrão no Linux: `/opt/neomind/fusion/config/customfusion.db`
- padrão no Windows: `%USERPROFILE%\.customfusion\customfusion.db`
- configuração: `app.sqlite.path` em `src/main/resources/application.properties`
  ou variável `APP_SQLITE_PATH`; se apontar para um diretório, o arquivo
  `customfusion.db` é criado dentro dele
- no servidor, o usuário do Tomcat precisa ter escrita em
  `/opt/neomind/fusion/config`
- seed inicial: `src/main/resources/db/customfusion-seed.db` (copiado apenas se o
  banco ainda não existir)
- schema: `src/main/resources/db/schema.sql`

O Fusion é usado apenas para identificação do usuário e publicação no GED.
Detalhes em [`docs/arquitetura-fusion.md`](docs/arquitetura-fusion.md).

## Build

Requisitos: Java 21, Jakarta Servlet 6 / Tomcat 10+, Jackson e `sqlite-jdbc`.

Sem Maven no PATH, compile com `javac` incluindo os JARs de `jakarta.servlet-api`,
`jackson-*` e `sqlite-jdbc`. O WAR final deve ser publicado como
`CustomFusionWeb`.

## Execução local sem Fusion

O catálogo e as grades abrem sem login. Cadastro de molde e publicação no GED
exigem usuário identificado; em desenvolvimento local, defina
`app.dev.usuario` em `src/main/resources/application.properties` ou a variável
`APP_DEV_USUARIO`.

## Migração

Os JSONs legados de `src/main/webapp/config/` foram removidos do repositório e
o seed já está em `src/main/resources/db/customfusion-seed.db`. O script
`scripts/migrar_json_sqlite.py` fica disponível para reconstruir o seed apenas
se esses JSONs forem restaurados manualmente. As grades antigas do `localStorage`
são migradas automaticamente na primeira carga do front.
