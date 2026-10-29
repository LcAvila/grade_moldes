"use strict";

(function (CF) {

  const { estado } = CF.estado;

  let cacheFormas = null;
  let cacheDesenhos = null;
  let cacheMaquinas = null;
  let cacheHistorico = null;

  function url(rel) {
    const caminho = String(rel == null ? "" : rel).replace(/^\/+/, "");
    const partes = caminho.split("/").map(encodeURIComponent).join("/");
    return estado.contextPath + "/" + partes;
  }

  async function buscarJson(rel) {
    const resp = await fetch(url(rel), { cache: "no-store" });
    if (!resp.ok) {
      throw new Error("Recurso não encontrado (" + resp.status + "): " + rel);
    }
    return resp.json();
  }

  async function carregarCatalogo() {
    const cat = await buscarJson("config/moldes.json");
    return Array.isArray(cat.moldes) ? cat.moldes : [];
  }

  function acharMolde(id) {
    return estado.moldes.find((m) => m.id === id) || null;
  }

  async function carregarLayout(layoutId) {
    if (!layoutId) {
      throw new Error("Layout não cadastrado para este molde.");
    }
    return buscarJson("config/layouts/" + layoutId + ".json");
  }

  async function carregarFormas() {
    if (cacheFormas) return cacheFormas;
    const cat = await buscarJson("config/formas.json");
    const mapa = {};
    (cat.formas || []).forEach((f) => { mapa[f.id] = f; });
    cacheFormas = mapa;
    return mapa;
  }

  async function carregarDesenhos() {
    if (cacheDesenhos) return cacheDesenhos;
    const cat = await buscarJson("config/desenhos.json");
    const mapa = {};
    (cat.desenhos || []).forEach((d) => { mapa[d.id] = d; });
    cacheDesenhos = mapa;
    return mapa;
  }

  async function carregarMaquinas() {
    if (cacheMaquinas) return cacheMaquinas;
    let lista = [];
    try {
      const cat = await buscarJson("config/maquinas.json");
      lista = Array.isArray(cat.maquinas) ? cat.maquinas : [];
    } catch (e) {
      console.warn("Catálogo de máquinas indisponível (" + e.message + ").");
    }
    cacheMaquinas = lista;
    return lista;
  }

  async function carregarIndiceHistorico() {
    if (cacheHistorico) return cacheHistorico;
    try {
      const idx = await buscarJson("config/historico/index.json");
      cacheHistorico = (idx && typeof idx === "object") ? idx : {};
    } catch (e) {
      console.warn("Índice de grades históricas indisponível (" + e.message + ").");
      cacheHistorico = {};
    }
    return cacheHistorico;
  }

  async function carregarGradeHistorica(caminho) {
    if (!caminho) throw new Error("Arquivo histórico não informado.");
    return buscarJson("config/historico/" + caminho);
  }

  // Lista final usada na abertura de grade: catálogo + máquinas citadas nos moldes.
  function listarMaquinas() {
    const saida = [];
    const vistos = {};

    function adicionar(nome) {
      const texto = String(nome == null ? "" : nome).trim();
      if (!texto) return;
      const chave = texto.toUpperCase();
      if (vistos[chave]) return;
      vistos[chave] = true;
      saida.push(texto);
    }

    (estado.maquinas || []).forEach((m) => {
      adicionar(typeof m === "string" ? m : (m && (m.nome || m.codigo)) || "");
    });
    (estado.moldes || []).forEach((mol) => {
      adicionar(mol.padroes && mol.padroes.maquina);
    });

    saida.sort((a, b) => a.localeCompare(b, undefined, { sensitivity: "base" }));
    return saida;
  }

  function urlRecurso(rel) {
    if (!rel) return "";
    return url(rel);
  }

  CF.cfg = {
    carregarCatalogo: carregarCatalogo,
    acharMolde: acharMolde,
    carregarLayout: carregarLayout,
    carregarFormas: carregarFormas,
    carregarDesenhos: carregarDesenhos,
    carregarMaquinas: carregarMaquinas,
    listarMaquinas: listarMaquinas,
    carregarIndiceHistorico: carregarIndiceHistorico,
    carregarGradeHistorica: carregarGradeHistorica,
    urlRecurso: urlRecurso
  };

})(window.CF = window.CF || {});
