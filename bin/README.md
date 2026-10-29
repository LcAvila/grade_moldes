# CustomFusionWeb — reconstrução do protótipo anexado

## O que este pacote é
Projeto Maven WAR recuperado do index.html anexado à conversa. Preserva a marcação,
o diagrama gerado em JavaScript, estilos e funções do arquivo original; separa CSS e JS
em arquivos externos e cria index.jsp. Não é uma cópia do workspace final do usuário.
O original completo está em referencias/index-original.html.

## Limitações importantes
- Tela funcional original para o Molde 168, 64 cavidades. A adaptação multimolde foi
  planejada na conversa, mas não está implementada neste pacote.
- O logo é a representação inline existente no HTML original. O arquivo oficial
  Logo Compactor.svg não foi anexado e não foi inventado.
- Não foram recuperados os Servlets, classes Java, CSS/JS e JSP finais do computador.
- Não há banco de dados, autenticação, permissões nem auditoria corporativa.
- O salvamento original usa localStorage do navegador: não é um banco central nem backup.
  Exporte JSON para preservar os registros. Trocar navegador/origem pode separar os dados.
- Ausência de registro mantém a semântica visual original; não é confirmação de inspeção.
- Não é versão homologada para produção. Não exponha na internet.
- XLSX e imagens de referência não são carregados automaticamente nem importados pela tela.
- Mudar o texto do número do molde NÃO troca a geometria, quantidade ou armazenamento.
  Não use essa edição para registrar outro molde.

## Importar no Eclipse
1. Extraia o ZIP em uma pasta nova. Não sobrescreva seu projeto antigo.
2. Se já existir CustomFusionWeb no workspace, use outro workspace para não confundir.
3. File > Import > Maven > Existing Maven Projects.
4. Em Root Directory selecione a pasta CustomFusionWeb extraída (contém pom.xml).
5. Selecione o pom.xml e Finish. Aguarde download de plugins/APIs pelo Maven.
6. Project > Properties > Java Build Path / Installed JREs: configure JDK 21.
7. Properties > Targeted Runtimes: selecione Apache Tomcat 10.1.
8. Maven > Update Project. Se necessário: Run As > Maven build... > clean package.
9. Servers > Add and Remove: adicione CustomFusionWeb. Inicie o Tomcat.
10. Abra http://localhost:8080/CustomFusionWeb/index.jsp.
    Se a porta for diferente, substitua 8080. Use Tomcat 10.1, não Tomcat 8.5.

O Maven/integração m2e-wtp deve configurar o deploy. Não inclua arquivos de teste
no Deployment Assembly. Não copie JARs manualmente para lib do Tomcat.
O protótipo recuperado não usa ObjectMapper: Jackson não é dependência deste pacote.
Ao restaurar seus Servlets, declare as dependências necessárias no pom.xml.

## Verificar manualmente após iniciar
- Compare visual com referencias/index-original.html.
- Selecione uma cavidade, registre um desvio de teste e salve.
- Recarregue a página e confira persistência no mesmo navegador/origem.
- Exporte JSON, teste importação usando apenas arquivos confiáveis e impressão.
- Não misture esses testes com registros oficiais.

## Próxima fase acordada
Preservar o visual existente. Parametrizar nomes, desenhos, IDs das cavidades e contagem.
Cadastrar layouts reais e revisados; não inventar posição física de cavidades.
Arruela de vedação, Molde 202: 2 cavidades. Molde 168: 64 cavidades.
Separar armazenamento por molde e revisão antes de habilitar troca de moldes.
Validar o desenho completo: objetos/células/shapes podem não estar na imagem embutida.
Não interpretar campos vazios ou S:/N: não marcados como conforme/não vedado.

## Segurança / testes
Não foram executados Maven, Eclipse, Jasper ou testes no navegador neste ambiente.
Feitas verificações estruturais do ZIP, XML e separação dos arquivos.
Dependências fixadas para reprodutibilidade: revisar vulnerabilidades e versões
aprovadas pela TI antes de homologar. Não há senha, token ou configuração de banco.
