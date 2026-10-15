"use strict";

(function (CF) {

  const { estado } = CF.estado;

  let onAbrirGrade = null;
  let onAbrirMolde = null;

  function init(opts) {
    const o = opts || {};
    onAbrirGrade = o.onAbrirGrade || null;
    onAbrirMolde = o.onAbrirMolde || null;

    const btnAbrir = document.getElementById("btnAbrirGrade");
    if (btnAbrir) btnAbrir.addEventListener("click", abrirModal);

    const btnCancelar = document.getElementById("btnCancelarAbrir");
    if (btnCancelar) btnCancelar.addEventListener("click", fecharModal);

    const overlay = document.getElementById("modalAbrirGrade");
    if (overlay) {
      overlay.addEventListener("click", (e) => {
        if (e.target.id === "modalAbrirGrade") fecharModal();
      });
    }
  }

  function mostrar() {
    const ti = document.getElementById("telaInicial");
    const tg = document.getElementById("telaGrade");
    if (ti) ti.style.display = "block";
    if (tg) tg.style.display = "none";
    window.scrollTo(0, 0);
  }

  function esconder() {
    const ti = document.getElementById("telaInicial");
    const tg = document.getElementById("telaGrade");
    if (ti) ti.style.display = "none";
    if (tg) tg.style.display = "";
  }

  function renderizar(cards) {
    const cont = document.getElementById("listaGrades");
    const vazio = document.getElementById("gradesVazio");
    if (!cont) return;

    cont.innerHTML = "";
    const lista = cards || [];
    if (!lista.length) {
      if (vazio) vazio.style.display = "block";
      return;
    }
    if (vazio) vazio.style.display = "none";
    lista.forEach((c) => cont.appendChild(cardEl(c)));
  }

  function cardEl(c) {
    const card = document.createElement("article");
    card.className = "grade-card";
    card.dataset.id = c.id;
    card.setAttribute("tabindex", "0");

    const top = document.createElement("div");
    top.className = "gc-top";

    const molde = document.createElement("span");
    molde.className = "gc-molde";
    molde.textContent = "Molde " + c.moldeCodigo;

    const maq = document.createElement("span");
    maq.className = "gc-maquina";
    maq.textContent = c.maquina || "Máquina não informada";

    top.appendChild(molde);
    top.appendChild(maq);
    card.appendChild(top);

    const nome = document.createElement("div");
    nome.className = "gc-nome";
    nome.textContent = c.moldeNome || "";
    card.appendChild(nome);

    const stats = document.createElement("div");
    stats.className = "gc-stats";

    stats.appendChild(statEl(c.ativas, "Ativas", ""));
    if (c.vedadas > 0) stats.appendChild(statEl(c.vedadas, "Vedadas", "gc-ved"));

    card.appendChild(stats);

    const mat = document.createElement("div");
    mat.className = "gc-material";
    mat.textContent = "Material: " + (c.material || "—");
    card.appendChild(mat);

    const abrir = document.createElement("div");
    abrir.className = "gc-abrir";
    abrir.textContent = "Abrir grade ›";
    card.appendChild(abrir);

    const acao = () => { if (onAbrirGrade) onAbrirGrade(c.id); };
    card.addEventListener("click", acao);
    card.addEventListener("keydown", (e) => {
      if (e.key === "Enter" || e.key === " ") { e.preventDefault(); acao(); }
    });

    return card;
  }

  function statEl(valor, rotulo, classe) {
    const div = document.createElement("div");
    div.className = "gc-stat " + (classe || "");
    const num = document.createElement("span");
    num.className = "num";
    num.textContent = valor;
    const lbl = document.createElement("span");
    lbl.className = "lbl";
    lbl.textContent = rotulo;
    div.appendChild(num);
    div.appendChild(lbl);
    return div;
  }

  function abrirModal() {
    const cont = document.getElementById("listaMoldesModal");
    const overlay = document.getElementById("modalAbrirGrade");
    if (!cont || !overlay) return;

    cont.innerHTML = "";
    const moldes = estado.moldes || [];
    if (!moldes.length) {
      const vazio = document.createElement("div");
      vazio.className = "hint";
      vazio.textContent = "Nenhum molde cadastrado.";
      cont.appendChild(vazio);
    }

    moldes.forEach((m) => {
      const b = document.createElement("button");
      b.type = "button";
      b.className = "molde-opcao";

      const cod = document.createElement("span");
      cod.className = "mo-codigo";
      cod.textContent = "Molde " + m.codigo;

      const nom = document.createElement("span");
      nom.className = "mo-nome";
      nom.textContent = m.nomePeca || "";

      const qtd = document.createElement("span");
      qtd.className = "mo-qtd";
      qtd.textContent = (m.cavidades || []).length + " cav.";

      b.appendChild(cod);
      b.appendChild(nom);
      b.appendChild(qtd);
      b.addEventListener("click", () => {
        fecharModal();
        if (onAbrirMolde) onAbrirMolde(m.id);
      });
      cont.appendChild(b);
    });

    overlay.classList.add("active");
  }

  function fecharModal() {
    const overlay = document.getElementById("modalAbrirGrade");
    if (overlay) overlay.classList.remove("active");
  }

  CF.telaInicial = {
    init: init,
    mostrar: mostrar,
    esconder: esconder,
    renderizar: renderizar,
    abrirModal: abrirModal,
    fecharModal: fecharModal
  };

})(window.CF = window.CF || {});
