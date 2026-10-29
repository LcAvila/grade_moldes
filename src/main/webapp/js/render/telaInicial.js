"use strict";

(function (CF) {

  const { estado, compararId } = CF.estado;

  let onAbrirGrade = null;
  let onAbrirMolde = null;
  let indiceHistorico = null;
  let pacoteHistorico = null;

  function init(opts) {
    const o = opts || {};
    onAbrirGrade = o.onAbrirGrade || null;
    onAbrirMolde = o.onAbrirMolde || null;

    const btnAbrir = document.getElementById("btnAbrirGrade");
    if (btnAbrir) btnAbrir.addEventListener("click", abrirModal);

    const btnCadastrar = document.getElementById("btnCadastrarMolde");
    if (btnCadastrar) btnCadastrar.addEventListener("click", cadastrarMolde);

    const btnCancelar = document.getElementById("btnCancelarAbrir");
    if (btnCancelar) btnCancelar.addEventListener("click", fecharModal);

    const btnConfirmar = document.getElementById("btnConfirmarAbrir");
    if (btnConfirmar) btnConfirmar.addEventListener("click", confirmarAbrir);

    const selMolde = document.getElementById("selMolde");
    if (selMolde) selMolde.addEventListener("change", function () {
      atualizarOpcoesHistorico(selMolde.value);
      atualizarBotaoConfirmar();
    });

    const selMaquina = document.getElementById("selMaquina");
    if (selMaquina) selMaquina.addEventListener("change", limparErro);

    const selHistorico = document.getElementById("selHistorico");
    if (selHistorico) selHistorico.addEventListener("change", trocarHistorico);

    const overlay = document.getElementById("modalAbrirGrade");
    if (overlay) {
      overlay.addEventListener("click", (e) => {
        if (e.target.id === "modalAbrirGrade") fecharModal();
      });
    }

    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape") fecharModal();
    });
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
    const contador = document.getElementById("gradesContador");
    if (!cont) return;

    cont.innerHTML = "";
    const lista = cards || [];
    if (contador) {
      contador.textContent = lista.length === 1
        ? "1 grade ativa"
        : lista.length + " grades ativas";
      contador.classList.toggle("zero", lista.length === 0);
    }
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

    const comDefeito = (c.defeitos || 0) > 0 || (c.vedadas || 0) > 0;
    if (comDefeito) card.classList.add("com-defeito");

    const status = document.createElement("span");
    status.className = "gc-status" + (comDefeito ? " com-defeito" : "");
    status.textContent = comDefeito ? "Com ocorrências" : "Ativa";

    top.appendChild(molde);
    top.appendChild(status);
    card.appendChild(top);

    const nome = document.createElement("div");
    nome.className = "gc-nome";
    nome.textContent = c.moldeNome || "";
    card.appendChild(nome);

    const chips = document.createElement("div");
    chips.className = "gc-chips";
    chips.appendChild(chipEl(c.maquina || "Máquina não informada", false));
    chips.appendChild(chipEl("Material: " + (c.material || "—"), true));
    card.appendChild(chips);

    const stats = document.createElement("div");
    stats.className = "gc-stats";
    stats.appendChild(statEl(c.ativas, "Ativas", ""));
    stats.appendChild(statEl(c.defeitos || 0, "Defeitos", "gc-def"));
    stats.appendChild(statEl(c.vedadas, "Vedadas", "gc-ved"));
    card.appendChild(stats);

    const abrir = document.createElement("div");
    abrir.className = "gc-abrir";

    const aberta = document.createElement("span");
    aberta.className = "gc-aberta";
    aberta.textContent = formatarData(c.abertaEm);
    abrir.appendChild(aberta);

    const acaoTxt = document.createElement("span");
    acaoTxt.textContent = "Abrir grade";
    const seta = document.createElement("span");
    seta.className = "gc-seta";
    seta.textContent = "›";
    acaoTxt.appendChild(seta);
    abrir.appendChild(acaoTxt);
    card.appendChild(abrir);

    const acao = () => { if (onAbrirGrade) onAbrirGrade(c.id); };
    card.addEventListener("click", acao);
    card.addEventListener("keydown", (e) => {
      if (e.key === "Enter" || e.key === " ") { e.preventDefault(); acao(); }
    });

    return card;
  }

  function chipEl(texto, muted) {
    const span = document.createElement("span");
    span.className = "gc-chip" + (muted ? " gc-chip-muted" : "");
    span.textContent = texto;
    return span;
  }

  function formatarData(iso) {
    if (!iso) return "";
    const d = new Date(iso);
    if (isNaN(d.getTime())) return "";
    try {
      return "Aberta em " + d.toLocaleDateString("pt-BR") + " · " +
        d.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
    } catch (e) {
      return "Aberta em " + d.getDate() + "/" + (d.getMonth() + 1) + "/" + d.getFullYear();
    }
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

  function hojeISO() {
    const d = new Date();
    const mes = String(d.getMonth() + 1).padStart(2, "0");
    const dia = String(d.getDate()).padStart(2, "0");
    return d.getFullYear() + "-" + mes + "-" + dia;
  }

  function dataBR(iso) {
    if (!iso) return "";
    const p = String(iso).split("-");
    return p.length === 3 ? (p[2] + "/" + p[1] + "/" + p[0]) : iso;
  }

  function abrirModal() {
    const overlay = document.getElementById("modalAbrirGrade");
    const selMolde = document.getElementById("selMolde");
    const selMaquina = document.getElementById("selMaquina");
    const dataAbertura = document.getElementById("dataAbertura");
    const dataHoje = document.getElementById("dataAberturaHoje");
    if (!overlay || !selMolde || !selMaquina || !dataAbertura) return;

    // --- Moldes cadastrados ---
    preencherOpcoes(selMolde, "Selecione o molde",
      (estado.moldes || [])
        .slice()
        .sort((a, b) => compararId(a.codigo, b.codigo))
        .map((m) => ({
          valor: m.id,
          texto: "Molde " + m.codigo + " — " + (m.nomePeca || "") +
            " (" + (m.cavidades || []).length + " cav.)"
        })),
      "Nenhum molde cadastrado. Verifique config/moldes.json.");

    // --- Máquinas cadastradas ---
    const maquinas = (CF.cfg && CF.cfg.listarMaquinas) ? CF.cfg.listarMaquinas() : [];
    preencherOpcoes(selMaquina, "Selecione a máquina",
      maquinas.map((nome) => ({ valor: nome, texto: nome })),
      "Nenhuma máquina cadastrada. Verifique config/maquinas.json.");

    // --- Data: sempre a data de hoje, editável ---
    const hoje = hojeISO();
    dataAbertura.value = hoje;
    if (dataHoje) dataHoje.textContent = dataBR(hoje);

    // --- Grade histórica (opcional) ---
    pacoteHistorico = null;
    const selHistorico = document.getElementById("selHistorico");
    if (selHistorico) selHistorico.innerHTML = '<option value="">Nova grade (sem importar registros)</option>';
    const grpHist = document.getElementById("grpHistorico");
    if (grpHist) grpHist.style.display = "none";
    carregarIndiceHistorico().then(() => {
      atualizarOpcoesHistorico(selMolde.value);
    });

    limparErro();
    atualizarBotaoConfirmar();
    overlay.classList.add("active");
    selMolde.focus();
  }

  function carregarIndiceHistorico() {
    if (indiceHistorico) return Promise.resolve(indiceHistorico);
    if (!CF.cfg || !CF.cfg.carregarIndiceHistorico) {
      indiceHistorico = {};
      return Promise.resolve(indiceHistorico);
    }
    return CF.cfg.carregarIndiceHistorico().then((idx) => {
      indiceHistorico = idx || {};
      return indiceHistorico;
    }).catch(() => {
      indiceHistorico = {};
      return indiceHistorico;
    });
  }

  function atualizarOpcoesHistorico(moldeId) {
    const sel = document.getElementById("selHistorico");
    const grp = document.getElementById("grpHistorico");
    const hint = document.getElementById("selHistoricoHint");
    if (!sel || !grp) return;

    pacoteHistorico = null;
    sel.innerHTML = '<option value="">Nova grade (sem importar registros)</option>';

    const entradas = (indiceHistorico && indiceHistorico[moldeId]) || [];
    if (!entradas.length) {
      grp.style.display = "none";
      return;
    }

    entradas.slice().reverse().forEach((e, i) => {
      const opt = document.createElement("option");
      opt.value = e.arquivo;
      const detalhe = [];
      if (e.defeitos) detalhe.push(e.defeitos + " defeito" + (e.defeitos > 1 ? "s" : ""));
      if (e.vedadas) detalhe.push(e.vedadas + " vedada" + (e.vedadas > 1 ? "s" : ""));
      if (!detalhe.length) detalhe.push("sem ocorrências");
      opt.textContent = e.rotulo + " — " + detalhe.join(", ");
      sel.appendChild(opt);
    });

    grp.style.display = "block";
    if (hint) {
      hint.textContent = entradas.length + " grade(s) histórica(s) encontrada(s) para este molde.";
    }
  }

  function trocarHistorico() {
    const sel = document.getElementById("selHistorico");
    const selMaquina = document.getElementById("selMaquina");
    const dataAbertura = document.getElementById("dataAbertura");
    if (!sel) return;

    pacoteHistorico = null;
    if (!sel.value) return;

    if (!CF.cfg || !CF.cfg.carregarGradeHistorica) return;
    CF.cfg.carregarGradeHistorica(sel.value).then((pacote) => {
      pacoteHistorico = pacote;
      const dg = (pacote && pacote.dadosGrade) || {};

      if (dg.maquina && selMaquina) {
        for (let i = 0; i < selMaquina.options.length; i++) {
          if (selMaquina.options[i].value === dg.maquina) {
            selMaquina.selectedIndex = i;
            break;
          }
        }
      }
      if (dataAbertura && dg.dataEntrada) {
        const d = paraDataISO(dg.dataEntrada);
        if (d) dataAbertura.value = d;
      }
      limparErro();
    }).catch((e) => {
      pacoteHistorico = null;
      mostrarErro("Não foi possível carregar a grade histórica: " + e.message);
      sel.value = "";
    });
  }

  function paraDataISO(bruto) {
    const s = String(bruto || "").trim();
    if (/^\d{4}-\d{2}-\d{2}/.test(s)) return s.slice(0, 10);
    const m = s.match(/^(\d{1,2})[\/.](\d{1,2})[\/.](\d{2,4})/);
    if (!m) return "";
    const ano = m[3].length === 2 ? "20" + m[3] : m[3];
    return ano + "-" + String(m[2]).padStart(2, "0") + "-" + String(m[1]).padStart(2, "0");
  }

  function preencherOpcoes(select, rotuloVazio, opcoes, aviso) {
    select.innerHTML = "";
    const vazio = document.createElement("option");
    vazio.value = "";
    vazio.textContent = rotuloVazio;
    select.appendChild(vazio);

    opcoes.forEach((o) => {
      const opt = document.createElement("option");
      opt.value = o.valor;
      opt.textContent = o.texto;
      select.appendChild(opt);
    });

    select.disabled = opcoes.length === 0;

    const hint = document.getElementById(select.id + "Hint");
    if (hint) {
      hint.innerHTML = "";
      if (!opcoes.length && aviso) {
        hint.classList.add("hint-erro");
        hint.textContent = aviso;
      } else {
        hint.classList.remove("hint-erro");
        hint.innerHTML = select.id === "selMolde"
          ? "Todos os moldes cadastrados em <strong>config/moldes.json</strong>."
          : "Máquinas cadastradas em <strong>config/maquinas.json</strong>.";
      }
    }
  }

  function atualizarBotaoConfirmar() {
    const btn = document.getElementById("btnConfirmarAbrir");
    const selMolde = document.getElementById("selMolde");
    if (btn) btn.disabled = !selMolde || !selMolde.value;
    limparErro();
  }

  function mostrarErro(msg) {
    const erro = document.getElementById("abrirGradeErro");
    if (!erro) return;
    erro.textContent = msg;
    erro.style.display = "block";
  }

  function limparErro() {
    const erro = document.getElementById("abrirGradeErro");
    if (!erro) return;
    erro.textContent = "";
    erro.style.display = "none";
  }

  function confirmarAbrir() {
    const selMolde = document.getElementById("selMolde");
    const selMaquina = document.getElementById("selMaquina");
    const dataAbertura = document.getElementById("dataAbertura");
    if (!selMolde || !selMaquina || !dataAbertura) return;

    const moldeId = selMolde.value;
    if (!moldeId) {
      mostrarErro("Selecione o molde para abrir a grade.");
      selMolde.focus();
      return;
    }

    const maquina = selMaquina.value;
    const temMaquinas = selMaquina.options.length > 1;
    if (temMaquinas && !maquina) {
      mostrarErro("Selecione a máquina utilizada neste molde.");
      selMaquina.focus();
      return;
    }

    const data = dataAbertura.value || hojeISO();

    fecharModal();
    if (onAbrirMolde) {
      onAbrirMolde(moldeId, {
        maquina: maquina,
        dataEntrada: data,
        historico: pacoteHistorico || null
      });
    }
  }

  function fecharModal() {
    const overlay = document.getElementById("modalAbrirGrade");
    if (overlay) overlay.classList.remove("active");
  }

  // Abre a tela de cadastro de molde (servidor local em :8787).
  // Abre a janela já no gesto do clique (evita bloqueador de popup) e, se o
  // servidor não estiver rodando, fecha e orienta a executar o cadastrar-molde.ps1.
  function cadastrarMolde() {
    const url = "http://localhost:8787/";
    const w = window.open(url, "_blank");
    fetch(url, { mode: "no-cors" }).catch(() => {
      if (w && !w.closed) w.close();
      alert("A tela de cadastro não está rodando.\n\nPara abrir, execute no PowerShell:\n\n" +
        "powershell -NoProfile -ExecutionPolicy Bypass -File " +
        "\"C:\\Users\\lucas_avila\\Desktop\\Cavidade não conforme\\cadastrar-molde.ps1\"\n\n" +
        "Depois clique novamente em \"CADASTRAR MOLDE\".");
    });
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
