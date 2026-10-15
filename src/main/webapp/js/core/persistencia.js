"use strict";

(function (CF) {

  const { estado } = CF.estado;

  const CHAVE_GRADES = "customfusion.grades";
  const PREFIXO_GRADE = "customfusion.grade.";
  const PREFIXO_REGISTROS = "customfusion.registros.";
  const CHAVE_LEGADA = "cavidades168";
  const CHAVE_BACKUP = "cavidades168.bak";
  const CHAVE_MIGRADO = "customfusion.migracao.v1";
  const VERSAO = 3;

  function disponivel() {
    try {
      const k = "__cf_teste__";
      window.localStorage.setItem(k, "1");
      window.localStorage.removeItem(k);
      return true;
    } catch (e) {
      return false;
    }
  }

  function novoId() {
    return "g" + Date.now().toString(36) + Math.random().toString(36).slice(2, 7);
  }

  function lerJson(chave) {
    const raw = window.localStorage.getItem(chave);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch (e) {
      return null;
    }
  }

  // ---- Lista de grades abertas ---------------------------------------------

  function listarGrades() {
    if (!disponivel()) return [];
    const lista = lerJson(CHAVE_GRADES);
    return Array.isArray(lista) ? lista : [];
  }

  function salvarGrades(lista) {
    if (!disponivel()) return;
    window.localStorage.setItem(CHAVE_GRADES, JSON.stringify(lista || []));
  }

  function acharGrade(gradeId) {
    return listarGrades().find((g) => g.id === gradeId) || null;
  }

  function criarGrade(moldeId) {
    const grade = { id: novoId(), moldeId: moldeId, abertaEm: new Date().toISOString() };
    const lista = listarGrades();
    lista.push(grade);
    salvarGrades(lista);
    return grade;
  }

  function removerGrade(gradeId) {
    if (!disponivel()) return;
    const lista = listarGrades().filter((g) => g.id !== gradeId);
    salvarGrades(lista);
    window.localStorage.removeItem(PREFIXO_GRADE + gradeId);
  }

  // ---- Dados por grade ------------------------------------------------------

  function carregarGrade(gradeId) {
    if (!disponivel() || !gradeId) return null;
    const p = lerJson(PREFIXO_GRADE + gradeId);
    if (!p || typeof p !== "object") return null;
    return {
      versao: p.versao || VERSAO,
      gradeId: gradeId,
      moldeId: p.moldeId || null,
      dadosCavidades: p.dadosCavidades || {},
      dadosGrade: p.dadosGrade || {}
    };
  }

  function salvarGrade(gradeId, moldeId, dadosCavidades, dadosGrade) {
    if (!disponivel() || !gradeId) return;
    const pacote = {
      versao: VERSAO,
      gradeId: gradeId,
      moldeId: moldeId || null,
      dadosCavidades: dadosCavidades || {},
      dadosGrade: dadosGrade || {}
    };
    window.localStorage.setItem(PREFIXO_GRADE + gradeId, JSON.stringify(pacote));
  }

  function persistirAtual() {
    if (!estado.gradeAtual) return;
    salvarGrade(estado.gradeAtual.id, estado.gradeAtual.moldeId, estado.dadosCavidades, estado.dadosGrade);
  }

  // ---- Compatibilidade com o modelo antigo (por molde) ----------------------

  function carregarRegistrosMolde(moldeId) {
    if (!disponivel() || !moldeId) return null;
    const p = lerJson(PREFIXO_REGISTROS + moldeId);
    if (!p || typeof p !== "object") return null;
    return {
      dadosCavidades: p.dadosCavidades || {},
      dadosGrade: p.dadosGrade || {}
    };
  }

  function salvarRegistrosMolde(moldeId, dadosCavidades, dadosGrade) {
    if (!disponivel() || !moldeId) return;
    const pacote = {
      versao: 2,
      moldeId: moldeId,
      dadosCavidades: dadosCavidades || {},
      dadosGrade: dadosGrade || {}
    };
    window.localStorage.setItem(PREFIXO_REGISTROS + moldeId, JSON.stringify(pacote));
  }

  function moldeIdsComRegistros() {
    const ids = [];
    if (!disponivel()) return ids;
    for (let i = 0; i < window.localStorage.length; i++) {
      const k = window.localStorage.key(i);
      if (k && k.indexOf(PREFIXO_REGISTROS) === 0) {
        ids.push(k.substring(PREFIXO_REGISTROS.length));
      }
    }
    return ids;
  }

  // Converte os registros antigos (um por molde) em grades abertas.
  function migrarParaGrades() {
    if (!disponivel()) return 0;
    if (window.localStorage.getItem(CHAVE_GRADES) != null) return 0;

    const lista = [];
    moldeIdsComRegistros().forEach((moldeId) => {
      const antigo = carregarRegistrosMolde(moldeId);
      if (!antigo) return;
      const id = novoId();
      salvarGrade(id, moldeId, antigo.dadosCavidades, antigo.dadosGrade);
      lista.push({ id: id, moldeId: moldeId, abertaEm: new Date().toISOString() });
    });
    salvarGrades(lista);
    return lista.length;
  }

  // ---- Legado v1 (cavidades168) --------------------------------------------

  function existeLegado() {
    if (!disponivel()) return false;
    return window.localStorage.getItem(CHAVE_LEGADA) != null;
  }

  function legadoJaMigrado() {
    if (!disponivel()) return false;
    return window.localStorage.getItem(CHAVE_MIGRADO) === "1";
  }

  function migrarLegado(moldeIdDestino) {
    if (!disponivel()) return false;
    const raw = window.localStorage.getItem(CHAVE_LEGADA);
    if (!raw) return false;

    let p;
    try {
      p = JSON.parse(raw);
    } catch (e) {
      return false;
    }
    if (!p || typeof p !== "object") return false;

    if (window.localStorage.getItem(CHAVE_BACKUP) == null) {
      window.localStorage.setItem(CHAVE_BACKUP, raw);
    }

    const destino = moldeIdDestino || "molde-168";
    const legado = {
      dadosCavidades: p.dadosCavidades || {},
      dadosGrade: p.dadosGrade || {}
    };

    const atual = carregarRegistrosMolde(destino);
    let dadosCavidades;
    let dadosGrade;
    if (atual) {
      dadosCavidades = Object.assign({}, legado.dadosCavidades, atual.dadosCavidades);
      dadosGrade = Object.assign({}, legado.dadosGrade, atual.dadosGrade);
    } else {
      dadosCavidades = legado.dadosCavidades;
      dadosGrade = legado.dadosGrade;
    }

    salvarRegistrosMolde(destino, dadosCavidades, dadosGrade);
    window.localStorage.setItem(CHAVE_MIGRADO, "1");
    return true;
  }

  CF.persistencia = {
    listarGrades: listarGrades,
    acharGrade: acharGrade,
    criarGrade: criarGrade,
    removerGrade: removerGrade,
    carregarGrade: carregarGrade,
    salvarGrade: salvarGrade,
    persistirAtual: persistirAtual,
    migrarParaGrades: migrarParaGrades,
    existeLegado: existeLegado,
    legadoJaMigrado: legadoJaMigrado,
    migrarLegado: migrarLegado
  };

})(window.CF = window.CF || {});
