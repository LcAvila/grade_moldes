"use strict";

(function (CF) {

  const { estado, dividir, dividirPorTamanhos, registroVazio, normalizarRegistro } = CF.estado;
  const cfg = CF.cfg;
  const persistencia = CF.persistencia;
  const telaInicial = CF.telaInicial;
  const { normalizarLayout, montarDiagrama } = CF.diagrama;
  const { montarTabela } = CF.tabela;
  const { criarOpcoesCavidades, sincronizarFormulario, atualizarSelecao, limparFormulario, temEdicaoNaoSalva, initFormulario } = CF.formulario;
  const { aplicarGrade, initGrade } = CF.cabecalho;
  const { atualizarResumo } = CF.resumo;
  const { abrirDetalhes, initModal } = CF.modal;
  const { initImpressao } = CF.impressao;
  const { initIO } = CF.io;
  const { initRegistros } = CF.registros;

  function el(id) { return document.getElementById(id); }

  function mostrarCarregando(texto) {
    const info = el("moldeInfo");
    if (info) info.textContent = texto || "";
  }

  function mostrarAviso(msg) {
    const aviso = el("avisoLayout");
    if (aviso) { aviso.textContent = msg; aviso.style.display = "block"; }
    const svg = el("svgDiagrama");
    if (svg) svg.style.display = "none";
  }

  function esconderAviso() {
    const aviso = el("avisoLayout");
    if (aviso) aviso.style.display = "none";
    const svg = el("svgDiagrama");
    if (svg) svg.style.display = "";
  }

  function acharMolde(moldeId) {
    return estado.moldes.find((m) => m.id === moldeId) || null;
  }

  function aplicarDesenho(desenho) {
    const imagem = desenho && desenho.imagem ? cfg.urlRecurso(desenho.imagem) : "";
    const peca = el("pecaImg");
    const erro = el("pecaErro");
    if (peca) {
      if (imagem) {
        peca.style.display = "";
        peca.src = imagem;
        if (erro) erro.style.display = "none";
      } else {
        peca.style.display = "none";
        if (erro) { erro.style.display = "block"; erro.textContent = "Desenho da peça não cadastrado para este molde."; }
      }
    }
    const folhaPeca = el("folhaPecaImg");
    if (folhaPeca && imagem) folhaPeca.src = imagem;
  }

  function inicializarDados(molde, persistido) {
    const persistCav = (persistido && persistido.dadosCavidades) || {};
    const dadosCavidades = {};
    molde.cavidades.map(String).forEach((id) => {
      dadosCavidades[id] = persistCav[id] ? normalizarRegistro(persistCav[id]) : registroVazio();
    });

    const dadosGrade = Object.assign({}, molde.padroes || {}, (persistido && persistido.dadosGrade) || {});
    dadosGrade.moldeNum = molde.codigo;

    return { dadosCavidades, dadosGrade };
  }

  // ---- Tela principal (grades abertas) --------------------------------------

  function montarCards() {
    return persistencia.listarGrades().map((g) => {
      const molde = acharMolde(g.moldeId);
      const dados = persistencia.carregarGrade(g.id) || { dadosCavidades: {}, dadosGrade: {} };
      const ids = molde ? molde.cavidades.map(String) : [];
      let ved = 0;
      let def = 0;
      ids.forEach((id) => {
        const d = dados.dadosCavidades[id] || {};
        if (d.vedado === "S") ved++;
        else if (d.desvio) def++;
      });
      const dadosGrade = Object.assign({}, (molde && molde.padroes) || {}, dados.dadosGrade || {});
      return {
        id: g.id,
        moldeCodigo: molde ? molde.codigo : g.moldeId,
        moldeNome: molde ? molde.nomePeca : "(molde não cadastrado)",
        maquina: dadosGrade.maquina || "",
        material: dadosGrade.material || "",
        total: ids.length,
        ativas: ids.length - ved,
        vedadas: ved,
        defeitos: def,
        abertaEm: g.abertaEm
      };
    });
  }

  function renderTelaInicial() {
    telaInicial.renderizar(montarCards());
  }

  function voltarParaInicial() {
    if (temEdicaoNaoSalva() && !confirm("Há alterações não salvas no formulário. Voltar para as grades?")) {
      return;
    }
    renderTelaInicial();
    telaInicial.mostrar();
  }

  // ---- Abrir / carregar grade ----------------------------------------------

  async function criarEAbrirGrade(moldeId) {
    const molde = acharMolde(moldeId);
    if (!molde) { alert("Molde não encontrado: " + moldeId); return; }
    const grade = persistencia.criarGrade(moldeId);
    await carregarGradeInterno(grade);
  }

  async function abrirGrade(gradeId) {
    const grade = persistencia.acharGrade(gradeId);
    if (!grade) { alert("Grade não encontrada."); return; }
    await carregarGradeInterno(grade);
  }

  async function carregarGradeInterno(grade) {
    const token = ++estado.loadToken;
    mostrarCarregando("Carregando grade...");

    try {
      const molde = acharMolde(grade.moldeId);
      if (!molde) throw new Error("Molde não cadastrado: " + grade.moldeId);

      const layoutBruto = await cfg.carregarLayout(molde.layoutId);
      if (token !== estado.loadToken) return;

      const desenho = estado.desenhos[molde.desenhoId] || null;
      const ids = molde.cavidades.map(String);
      const modelo = normalizarLayout(layoutBruto, ids);
      const persistido = persistencia.carregarGrade(grade.id);
      const dados = inicializarDados(molde, persistido);

      estado.gradeAtual = grade;
      estado.molde = molde;
      estado.layout = layoutBruto;
      estado.modelo = modelo;
      estado.desenho = desenho;
      estado.dadosCavidades = dados.dadosCavidades;
      estado.dadosGrade = dados.dadosGrade;
      estado.selecionadas = [];

      const blocosTabela = (layoutBruto.tabela && layoutBruto.tabela.blocos) || 2;
      estado.blocosTabela = dividir(ids, blocosTabela);

      const tamImp = layoutBruto.impressao && layoutBruto.impressao.tamanhosBlocos;
      estado.blocosImpressao = tamImp ? dividirPorTamanhos(ids, tamImp) : dividir(ids, 2);

      telaInicial.esconder();
      aplicarGrade();
      criarOpcoesCavidades(ids);
      montarTabela();
      montarDiagrama(modelo, estado.formas);
      atualizarResumo();
      aplicarDesenho(desenho);
      limparFormulario();
      sincronizarFormulario();
      atualizarSelecao();
      esconderAviso();

      mostrarCarregando("Molde " + molde.codigo + " · " + ids.length + " cavidades");
    } catch (e) {
      if (token !== estado.loadToken) return;
      console.error(e);
      mostrarCarregando("");
      mostrarAviso("Não foi possível abrir a grade do molde \"" + grade.moldeId + "\": " + e.message + " — Layout indisponível ou ainda não validado.");
    }
  }

  async function migrarSeNecessario() {
    if (!persistencia.existeLegado() || persistencia.legadoJaMigrado()) return;
    const ok = confirm(
      "Encontramos registros do Molde 168 no formato antigo (localStorage).\n" +
      "Migrar para o novo formato por grade? Os dados antigos serão mantidos como backup (" + "cavidades168.bak" + ")."
    );
    if (!ok) return;
    if (persistencia.migrarLegado("molde-168")) {
      alert("Migração concluída. Os registros do Molde 168 foram atualizados.");
    }
  }

  async function iniciar() {
    const body = document.body;
    estado.contextPath = body.dataset.context || "";
    estado.onDetalhes = abrirDetalhes;

    initFormulario();
    initGrade();
    initModal();
    initRegistros();
    initIO();
    initImpressao();
    telaInicial.init({ onAbrirGrade: abrirGrade, onAbrirMolde: criarEAbrirGrade });

    const btnVoltar = el("btnVoltar");
    if (btnVoltar) btnVoltar.addEventListener("click", voltarParaInicial);

    const campoData = el("data");
    if (campoData) campoData.value = estado.hoje;

    try {
      estado.moldes = await cfg.carregarCatalogo();
      estado.formas = await cfg.carregarFormas();
      estado.desenhos = await cfg.carregarDesenhos();
    } catch (e) {
      console.error(e);
      mostrarAviso("Não foi possível carregar a configuração: " + e.message);
      mostrarCarregando("");
      telaInicial.mostrar();
      return;
    }

    await migrarSeNecessario();
    persistencia.migrarParaGrades();

    renderTelaInicial();
    telaInicial.mostrar();
  }

  CF.app = {
    abrirGrade: abrirGrade,
    criarEAbrirGrade: criarEAbrirGrade,
    voltarParaInicial: voltarParaInicial,
    montarCards: montarCards,
    renderTelaInicial: renderTelaInicial
  };

  iniciar();

})(window.CF = window.CF || {});
