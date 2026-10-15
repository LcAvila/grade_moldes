"use strict";

(function (CF) {

  const { estado, normalizarRegistro } = CF.estado;
  const { persistirAtual } = CF.persistencia;
  const { coletarGrade, aplicarGrade } = CF.cabecalho;
  const { atualizarTabela } = CF.tabela;
  const { atualizarDiagrama } = CF.diagrama;
  const { atualizarResumo } = CF.resumo;
  const { sincronizarFormulario, atualizarSelecao } = CF.formulario;

  const FORMATO = "customfusion.cavidades";
  const VERSAO = 2;

  function exportar() {
    if (!estado.molde) return;
    coletarGrade();
    const pacote = {
      formato: FORMATO,
      versao: VERSAO,
      moldeId: estado.molde.id,
      codigo: estado.molde.codigo,
      nomePeca: estado.molde.nomePeca,
      layoutId: estado.molde.layoutId,
      exportadoEm: new Date().toISOString(),
      dadosCavidades: estado.dadosCavidades,
      dadosGrade: estado.dadosGrade
    };
    const blob = new Blob([JSON.stringify(pacote, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "cavidades-molde-" + estado.molde.codigo + ".json";
    a.click();
    URL.revokeObjectURL(url);
  }

  function importarTexto(texto) {
    if (!estado.molde) return;
    let pacote;
    try {
      pacote = JSON.parse(texto);
    } catch (e) {
      alert("Arquivo JSON inválido.");
      return;
    }
    if (!pacote || typeof pacote !== "object" || !pacote.dadosCavidades) {
      alert("Formato de arquivo não reconhecido.");
      return;
    }

    const cavidades = estado.molde.cavidades.map(String);
    const origemMoldeId = pacote.moldeId || (pacote.versao === 1 ? "molde-168" : null);

    if (origemMoldeId && origemMoldeId !== estado.molde.id) {
      const ok = confirm(
        "Este arquivo parece ser do molde " + origemMoldeId + ", diferente do molde atual (" + estado.molde.id + ").\n" +
        "Somente as cavidades com identificador compatível serão importadas. Continuar?"
      );
      if (!ok) return;
    }

    const novos = {};
    let importados = 0;
    let ignorados = 0;
    Object.keys(pacote.dadosCavidades).forEach((id) => {
      if (cavidades.indexOf(String(id)) >= 0) {
        novos[String(id)] = normalizarRegistro(pacote.dadosCavidades[id]);
        importados++;
      } else {
        ignorados++;
      }
    });

    const ok2 = confirm(
      "Substituir os registros do molde atual pelos dados importados?\n" +
      importados + " cavidade(s) importada(s); " + ignorados + " ignorada(s)."
    );
    if (!ok2) return;

    cavidades.forEach((id) => {
      estado.dadosCavidades[id] = novos[id] || { desvio: "", data: "", vedado: "N", observacao: "", controlador: "" };
    });

    if (pacote.dadosGrade && typeof pacote.dadosGrade === "object") {
      Object.keys(pacote.dadosGrade).forEach((k) => {
        if (k === "moldeNum") return;
        estado.dadosGrade[k] = pacote.dadosGrade[k];
      });
    }

    aplicarGrade();
    persistirAtual();
    atualizarTabela();
    atualizarDiagrama();
    atualizarResumo();
    sincronizarFormulario();
    atualizarSelecao();
    alert("Dados importados com sucesso.");
  }

  function initIO() {
    document.getElementById("btnExportar").addEventListener("click", exportar);

    const fileInput = document.getElementById("fileImport");
    document.getElementById("btnImportar").addEventListener("click", () => fileInput.click());
    fileInput.addEventListener("change", function (e) {
      const f = e.target.files[0];
      if (!f) return;
      const leitor = new FileReader();
      leitor.onload = (x) => importarTexto(x.target.result);
      leitor.readAsText(f);
      e.target.value = "";
    });
  }

  CF.io = {
    exportar: exportar,
    initIO: initIO
  };

})(window.CF = window.CF || {});
