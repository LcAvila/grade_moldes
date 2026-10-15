"use strict";

(function (CF) {

  const { estado, compararId } = CF.estado;
  const { persistirAtual } = CF.persistencia;
  const { atualizarTabela } = CF.tabela;
  const { atualizarDiagrama } = CF.diagrama;
  const { atualizarResumo } = CF.resumo;
  const { sincronizarFormulario, atualizarSelecao, limparFormulario, lerFormulario } = CF.formulario;
  const { coletarGrade } = CF.cabecalho;

  function atualizarTudo() {
    atualizarTabela();
    atualizarDiagrama();
    atualizarResumo();
  }

  function salvar() {
    if (estado.selecionadas.length === 0) {
      alert("Selecione ao menos uma cavidade.");
      return;
    }
    const reg = lerFormulario();
    if (!reg.desvio) {
      alert("Selecione ou digite um desvio/defeito.");
      return;
    }
    const alvo = estado.selecionadas.slice();
    if (alvo.length > 1 && !confirm("Aplicar este registro às " + alvo.length + " cavidades selecionadas (" + alvo.slice().sort(compararId).join(", ") + ")?")) {
      return;
    }
    alvo.forEach((id) => {
      estado.dadosCavidades[id] = {
        desvio: reg.desvio,
        data: reg.data,
        vedado: reg.vedado,
        observacao: reg.observacao,
        controlador: reg.controlador
      };
    });
    coletarGrade();
    persistirAtual();
    atualizarTudo();
    sincronizarFormulario();
  }

  function limpar() {
    estado.selecionadas = [];
    limparFormulario();
    sincronizarFormulario();
    atualizarSelecao();
  }

  function excluir() {
    if (estado.selecionadas.length === 0) {
      alert("Selecione ao menos uma cavidade.");
      return;
    }
    const alvo = estado.selecionadas.slice();
    const msg = alvo.length === 1
      ? ("Apagar o registro da cavidade " + alvo[0] + "?")
      : ("Apagar os registros das " + alvo.length + " cavidades selecionadas?");
    if (!confirm(msg)) return;

    alvo.forEach((id) => {
      estado.dadosCavidades[id] = { desvio: "", data: "", vedado: "N", observacao: "", controlador: "" };
    });
    persistirAtual();
    limpar();
    atualizarTudo();
  }

  function initRegistros() {
    document.getElementById("btnSalvar").addEventListener("click", salvar);
    document.getElementById("btnLimpar").addEventListener("click", limpar);
    document.getElementById("btnExcluir").addEventListener("click", excluir);
  }

  CF.registros = {
    salvar: salvar,
    limpar: limpar,
    excluir: excluir,
    initRegistros: initRegistros
  };

})(window.CF = window.CF || {});
