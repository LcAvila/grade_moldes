"use strict";

(function (CF) {

  const { estado } = CF.estado;

  let cacheFormas = null;
  let cacheDesenhos = null;

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
    urlRecurso: urlRecurso
  };

})(window.CF = window.CF || {});
