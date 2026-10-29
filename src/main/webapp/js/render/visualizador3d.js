"use strict";

(function (CF) {

  let cena = null;
  let camera = null;
  let renderer = null;
  let controls = null;
  let objetoAtual = null;
  let malhaBordas = null;
  let gradeSolo = null;
  let animacaoId = null;
  let autoGirar = false;
  let faceSelecionadaNormal = null;
  let raioEsfera = 25;
  let centroModelo = null;
  let modeloCarregadoUrl = null;
  let animandoCamera = false;
  let inicializado = false;

  const VISTAS = {
    1: { nome: "Frontal", atalho: "Ctrl+1", dir: [0, 0, 1], up: [0, 1, 0] },
    2: { nome: "Posterior", atalho: "Ctrl+2", dir: [0, 0, -1], up: [0, 1, 0] },
    3: { nome: "Lateral Esquerda", atalho: "Ctrl+3", dir: [-1, 0, 0], up: [0, 1, 0] },
    4: { nome: "Lateral Direita", atalho: "Ctrl+4", dir: [1, 0, 0], up: [0, 1, 0] },
    5: { nome: "Superior", atalho: "Ctrl+5", dir: [0, 1, 0], up: [0, 0, -1] },
    6: { nome: "Inferior", atalho: "Ctrl+6", dir: [0, -1, 0], up: [0, 0, 1] },
    7: { nome: "Perspectiva Isométrica", atalho: "Ctrl+7", dir: [0.7071, 0.7071, 0.7071], up: [0, 1, 0] },
    8: { nome: "Normal a Face / Alinhada", atalho: "Ctrl+8", dir: [0, 0, 1], up: [0, 1, 0] }
  };

  function el(id) {
    return document.getElementById(id);
  }

  function getContextPath() {
    var bodyCtx = document.body && document.body.dataset && document.body.dataset.context;
    return bodyCtx != null ? bodyCtx : "/CustomFusionWeb";
  }

  function resolverUrl(rel) {
    if (CF.cfg && typeof CF.cfg.urlRecurso === "function") {
      return CF.cfg.urlRecurso(rel);
    }
    var ctx = getContextPath();
    var limpo = String(rel || "").replace(/^\/+/, "");
    return (ctx ? ctx + "/" : "/") + limpo;
  }

  function criarGeometriaConexaoProcedural() {
    // Perfil técnico exato do corte da Conexão Esferográfica (REV05 - 23.05.01-06)
    // Coordenadas (Raio X, Altura Y ao longo do eixo de revolução):
    var pontos = [
      new THREE.Vector2(1.5, -7.6),   // Furo frontal do bocal
      new THREE.Vector2(2.4, -7.6),   // Face externa frontal do bocal
      new THREE.Vector2(2.5, -7.1),   // Início do cone externo
      new THREE.Vector2(4.1, -0.1),   // Fim do cone / corpo cilíndrico externo
      new THREE.Vector2(4.1, 6.2),    // Parede cilíndrica externa
      new THREE.Vector2(4.7, 6.6),    // Degrau do flange/batente traseiro
      new THREE.Vector2(4.7, 7.6),    // Borda do flange traseiro
      new THREE.Vector2(4.2, 7.6),    // Face traseira da saia
      new THREE.Vector2(4.2, 6.4),    // Rebaixo da saia
      new THREE.Vector2(3.7, 6.2),    // Parede externa da câmara anular
      new THREE.Vector2(3.7, -2.4),   // Fundo da câmara anular
      new THREE.Vector2(2.1, -2.4),   // Raiz do pino/tubo central interno
      new THREE.Vector2(2.1, 4.9),    // Comprimento do tubo central estendendo para trás
      new THREE.Vector2(1.3, 4.9),    // Face da borda do tubo central
      new THREE.Vector2(1.3, -1.1),   // Furo passante interno
      new THREE.Vector2(1.6, -1.6),   // Câmara frontal interna
      new THREE.Vector2(1.5, -7.6)    // Fechamento no bocal
    ];

    var geom = new THREE.LatheGeometry(pontos, 64);
    geom.computeVertexNormals();
    geom.center();
    return geom;
  }

  function inicializarThree() {
    if (cena) return true;
    if (typeof THREE === "undefined") {
      console.warn("Biblioteca Three.js não encontrada.");
      return false;
    }

    var container = el("m3dViewport");
    if (!container) return false;

    var largura = container.clientWidth || 800;
    var altura = container.clientHeight || 450;

    // Cena — fundo gradiente estilo SolidWorks (cinza claro neutro)
    cena = new THREE.Scene();
    cena.background = new THREE.Color(0xe8e8e8);

    // Câmera
    camera = new THREE.PerspectiveCamera(45, largura / altura, 0.1, 2000);
    camera.position.set(35, 35, 35);
    camera.up.set(0, 1, 0);

    // Renderizador WebGL
    renderer = new THREE.WebGLRenderer({
      canvas: el("m3dCanvas"),
      antialias: true,
      alpha: false,
      powerPreference: "high-performance"
    });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    renderer.setSize(largura, altura, false);
    // Tone mapping para renderização mais realista estilo CAD
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 1.1;
    renderer.outputEncoding = THREE.sRGBEncoding;

    // OrbitControls
    if (typeof THREE.OrbitControls === "function") {
      controls = new THREE.OrbitControls(camera, renderer.domElement);
      controls.enableDamping = true;
      controls.dampingFactor = 0.08;
      controls.rotateSpeed = 0.8;
      controls.zoomSpeed = 1.0;
      controls.panSpeed = 0.8;
      controls.screenSpacePanning = true;
      controls.target.set(0, 0, 0);
    }

    // Iluminação estilo SolidWorks — neutra, sem tons azulados
    var luzAmbiente = new THREE.AmbientLight(0xffffff, 0.55);
    cena.add(luzAmbiente);

    var luzHemisferio = new THREE.HemisphereLight(0xffffff, 0xd6d6d6, 0.5);
    cena.add(luzHemisferio);

    // Luz principal (key light) — de cima/frente
    var luzDir1 = new THREE.DirectionalLight(0xffffff, 0.9);
    luzDir1.position.set(50, 80, 60);
    cena.add(luzDir1);

    // Luz de preenchimento (fill light) — neutra
    var luzDir2 = new THREE.DirectionalLight(0xffffff, 0.4);
    luzDir2.position.set(-60, -30, -50);
    cena.add(luzDir2);

    // Luz de cima
    var luzDir3 = new THREE.DirectionalLight(0xffffff, 0.35);
    luzDir3.position.set(0, 100, 0);
    cena.add(luzDir3);

    // Grade de piso estilo SolidWorks — neutra
    gradeSolo = new THREE.GridHelper(80, 40, 0xa0a0a0, 0xd8d8d8);
    gradeSolo.position.y = -10;
    cena.add(gradeSolo);

    // Raycast para seleção de faces
    configurarRaycast();

    // Loop de renderização
    iniciarLoop();

    return true;
  }

  function iniciarLoop() {
    if (animacaoId) cancelAnimationFrame(animacaoId);

    function loop() {
      animacaoId = requestAnimationFrame(loop);

      if (autoGirar && controls && !animandoCamera) {
        controls.autoRotate = true;
        controls.autoRotateSpeed = 2.0;
      } else if (controls) {
        controls.autoRotate = false;
      }

      if (controls) controls.update();
      if (renderer && cena && camera) {
        renderer.render(cena, camera);
      }
    }

    loop();
  }

  function configurarRaycast() {
    if (!renderer || !renderer.domElement) return;
    var raycaster = new THREE.Raycaster();
    var mouse = new THREE.Vector2();
    var canvas = renderer.domElement;

    canvas.addEventListener("click", function (evt) {
      if (!objetoAtual) return;
      var rect = canvas.getBoundingClientRect();
      mouse.x = ((evt.clientX - rect.left) / rect.width) * 2 - 1;
      mouse.y = -((evt.clientY - rect.top) / rect.height) * 2 + 1;

      raycaster.setFromCamera(mouse, camera);
      var intersects = raycaster.intersectObject(objetoAtual, true);

      if (intersects.length > 0) {
        var hit = intersects[0];
        if (hit.face) {
          var normalMatrix = new THREE.Matrix3().getNormalMatrix(hit.object.matrixWorld);
          faceSelecionadaNormal = hit.face.normal.clone().applyMatrix3(normalMatrix).normalize();
          atualizarIndicadorVista("Face selecionada (Pressione Ctrl+8 para alinhar)");
        }
      }
    });
  }

  function redimensionar() {
    var container = el("m3dViewport");
    if (!container || !renderer || !camera) return;

    var w = container.clientWidth;
    var h = container.clientHeight;
    if (w === 0 || h === 0) return;

    camera.aspect = w / h;
    camera.updateProjectionMatrix();
    renderer.setSize(w, h, false);
  }

  function aplicarGeometriaNaCena(geometry, infoExtra) {
    if (objetoAtual) {
      cena.remove(objetoAtual);
      if (malhaBordas) cena.remove(malhaBordas);
      objetoAtual = null;
      malhaBordas = null;
    }

    geometry.computeVertexNormals();
    geometry.center();

    // Material padrão SolidWorks — cinza metálico neutro
    var material = new THREE.MeshStandardMaterial({
      color: 0xb4b4b4,
      metalness: 0.35,
      roughness: 0.45,
      flatShading: false,
      envMapIntensity: 0.6
    });

    var mesh = new THREE.Mesh(geometry, material);

    try {
      var edges = new THREE.EdgesGeometry(geometry, 25);
      malhaBordas = new THREE.LineSegments(
        edges,
        new THREE.LineBasicMaterial({ color: 0x2a2a2a, linewidth: 1 })
      );
      mesh.add(malhaBordas);
    } catch (e) {
      // Ignora erro de borda
    }

    objetoAtual = mesh;
    cena.add(objetoAtual);

    geometry.computeBoundingBox();
    var bbox = geometry.boundingBox;
    var sphere = geometry.boundingSphere;

    centroModelo = sphere ? sphere.center.clone() : new THREE.Vector3(0, 0, 0);
    raioEsfera = sphere ? sphere.radius : Math.max(bbox.max.x - bbox.min.x, bbox.max.y - bbox.min.y, bbox.max.z - bbox.min.z);

    if (gradeSolo) {
      gradeSolo.position.y = bbox.min.y - 1.0;
    }

    if (controls) {
      controls.target.copy(centroModelo);
    }

    var loading = el("m3dLoading");
    if (loading) loading.style.display = "none";

    var infoOrigem = el("m3dInfoOrigem");
    if (infoOrigem) {
      infoOrigem.textContent = infoExtra || "Modelo 3D CAD: Conexão Esferográfica";
    }

    ajustarAoModelo();
    irParaVista(7, false);
  }

  function carregarModelo(url, nomePeca, infoExtra) {
    var loading = el("m3dLoading");
    if (loading) {
      loading.style.display = "flex";
      loading.textContent = "Carregando modelo 3D (" + (nomePeca || "Peça") + ")...";
    }

    if (modeloCarregadoUrl === url && objetoAtual) {
      if (loading) loading.style.display = "none";
      ajustarAoModelo();
      irParaVista(7, false);
      return;
    }

    // Se STLLoader estiver disponível, tenta carregar o arquivo .stl
    if (typeof THREE.STLLoader === "function") {
      var loader = new THREE.STLLoader();
      loader.load(
        url,
        function (geometry) {
          modeloCarregadoUrl = url;
          aplicarGeometriaNaCena(geometry, infoExtra);
        },
        function (xhr) {
          if (xhr.lengthComputable && loading) {
            var pct = Math.round((xhr.loaded / xhr.total) * 100);
            loading.textContent = "Carregando modelo 3D (" + pct + "%)...";
          }
        },
        function (error) {
          console.warn("Arquivo STL não carregado via rede (" + url + "). Usando geometria procedural interna.");
          // Fallback robusto garantido: constrói a peça proceduramente
          modeloCarregadoUrl = "procedural";
          var geom = criarGeometriaConexaoProcedural();
          aplicarGeometriaNaCena(geom, "Geometria CAD 3D: Conexão Esferográfica Padrão (REV05)");
        }
      );
    } else {
      // Sem STLLoader: constrói proceduramente
      modeloCarregadoUrl = "procedural";
      var geom = criarGeometriaConexaoProcedural();
      aplicarGeometriaNaCena(geom, "Geometria CAD 3D: Conexão Esferográfica Padrão (REV05)");
    }
  }

  function ajustarAoModelo() {
    if (!camera || !controls) return;
    var dist = Math.max(raioEsfera * 2.8, 35);
    camera.near = Math.max(0.1, raioEsfera / 50);
    camera.far = raioEsfera * 200;
    camera.updateProjectionMatrix();

    if (centroModelo) {
      controls.target.copy(centroModelo);
    }
    controls.update();
  }

  function irParaVista(n, animar) {
    if (animar === undefined) animar = true;
    var vista = VISTAS[n];
    if (!vista || !camera || !controls) return;

    var dir = vista.dir;
    var up = vista.up;

    if (n === 8) {
      if (faceSelecionadaNormal) {
        dir = [faceSelecionadaNormal.x, faceSelecionadaNormal.y, faceSelecionadaNormal.z];
        up = Math.abs(dir[1]) > 0.95 ? [0, 0, 1] : [0, 1, 0];
      } else {
        dir = [0, 0, 1];
        up = [0, 1, 0];
      }
    }

    var dist = Math.max(raioEsfera * 2.8, 35);
    var alvo = centroModelo || new THREE.Vector3(0, 0, 0);

    var posFinal = new THREE.Vector3(
      alvo.x + dir[0] * dist,
      alvo.y + dir[1] * dist,
      alvo.z + dir[2] * dist
    );
    var upFinal = new THREE.Vector3(up[0], up[1], up[2]).normalize();

    atualizarBotoesVista(n);
    atualizarIndicadorVista(vista.nome + " (" + vista.atalho + ")");

    if (!animar) {
      camera.position.copy(posFinal);
      camera.up.copy(upFinal);
      controls.target.copy(alvo);
      camera.lookAt(alvo);
      controls.update();
      return;
    }

    // Animação suave estilo SolidWorks
    animandoCamera = true;
    var posInicial = camera.position.clone();
    var upInicial = camera.up.clone();
    var t0 = performance.now();
    var duracao = 320;

    function animarPasso(agora) {
      var t = Math.min(1, (agora - t0) / duracao);
      var ease = t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;

      camera.position.lerpVectors(posInicial, posFinal, ease);
      camera.up.lerpVectors(upInicial, upFinal, ease).normalize();
      camera.lookAt(alvo);
      controls.target.copy(alvo);
      controls.update();

      if (t < 1) {
        requestAnimationFrame(animarPasso);
      } else {
        camera.position.copy(posFinal);
        camera.up.copy(upFinal);
        controls.update();
        animandoCamera = false;
      }
    }

    requestAnimationFrame(animarPasso);
  }

  function atualizarBotoesVista(n) {
    document.querySelectorAll(".m3d-view-btn[data-view], .m3d-btn[data-view]").forEach(function (btn) {
      if (parseInt(btn.dataset.view, 10) === n) {
        btn.classList.add("ativo");
      } else {
        btn.classList.remove("ativo");
      }
    });
  }

  function atualizarIndicadorVista(texto) {
    var ind = el("m3dViewTexto") || el("m3dViewIndicator");
    if (ind) ind.textContent = "Vista: " + texto;
  }

  function obterUrlModeloParaMolde(moldeCodigo) {
    var cod = String(moldeCodigo || "").trim().toUpperCase();

    // Mapeamento dos moldes da Conexão Esferográfica compartilhada
    var moldesConexao = ["168", "200", "98", "167A", "156"];
    if (moldesConexao.indexOf(cod) !== -1 || !cod) {
      return {
        url: resolverUrl("assets/models/conexao-padrao.stl"),
        titulo: "Visualização 3D da Peça — Conexão Esferográfica",
        subtitulo: "REV05 · 23.05.01-06 · Moldes 168, 200, 98, 167A e 156",
        infoExtra: "Arquivo CAD SolidWorks: 23.05.01-06-REV05 - CONEXÃO ESF. PADRÃO (DESENHO).SLDPRT"
      };
    }

    return {
      url: resolverUrl("assets/models/molde-" + cod + ".stl"),
      titulo: "Desenho 3D da Peça — Molde " + cod,
      subtitulo: "Visualização técnica 3D",
      infoExtra: "Arquivo 3D: molde-" + cod + ".stl"
    };
  }

  function abrirModal3D(dados) {
    var estado = CF.estado && CF.estado.estado;
    var molde = (dados && dados.molde) || (estado && estado.molde);
    var moldeCodigo = (molde && molde.codigo) || (estado && estado.dadosGrade && estado.dadosGrade.moldeNum) || "168";

    var infoModelo = obterUrlModeloParaMolde(moldeCodigo);

    var tituloEl = el("modalPeca3dTitulo");
    if (tituloEl) tituloEl.textContent = infoModelo.titulo;

    var subEl = el("modalPeca3dSub");
    if (subEl) subEl.textContent = infoModelo.subtitulo;

    var overlay = el("modalPeca3D");
    if (overlay) {
      overlay.classList.add("active");
    }

    if (!inicializarThree()) {
      alert("Não foi possível inicializar o visualizador WebGL.");
      return;
    }

    setTimeout(function () {
      redimensionar();
      carregarModelo(infoModelo.url, molde && (molde.nomePeca || molde.codigo), infoModelo.infoExtra);
    }, 50);
  }

  function fecharModal3D() {
    var overlay = el("modalPeca3D");
    if (overlay) {
      overlay.classList.remove("active");
    }
    autoGirar = false;
    var btnGirar = el("btnM3dGirar");
    if (btnGirar) btnGirar.classList.remove("ativo");
  }

  function onKeyDown(evt) {
    var overlay = el("modalPeca3D");
    if (!overlay || !overlay.classList.contains("active")) return;

    // Atalhos SolidWorks: Ctrl + 1 até Ctrl + 8
    if (evt.ctrlKey && !evt.shiftKey && !evt.altKey) {
      var num = parseInt(evt.key, 10);
      if (num >= 1 && num <= 8) {
        evt.preventDefault();
        evt.stopPropagation();
        irParaVista(num, true);
        return;
      }
    }

    if (evt.key === "f" || evt.key === "F") {
      evt.preventDefault();
      ajustarAoModelo();
      return;
    }

    if (evt.key === "Escape") {
      evt.preventDefault();
      fecharModal3D();
    }
  }

  function init() {
    if (inicializado) return;
    inicializado = true;

    // Vincula cliques no card da peça
    var pecaBox = el("pecaBox") || document.querySelector(".peca-box");
    if (pecaBox) {
      pecaBox.addEventListener("click", function (e) {
        abrirModal3D();
      });
    }

    var pecaGatilho = el("pecaGatilho");
    if (pecaGatilho) {
      pecaGatilho.addEventListener("click", function (e) {
        e.stopPropagation();
        abrirModal3D();
      });
    }

    var btn3d = el("btnAbrir3D");
    if (btn3d) {
      btn3d.addEventListener("click", function (e) {
        e.stopPropagation();
        abrirModal3D();
      });
    }

    // Botões de fechar
    var btnFecharTopo = el("btnFecharModal3D");
    if (btnFecharTopo) btnFecharTopo.addEventListener("click", fecharModal3D);

    var btnFecharRodape = el("btnFecharModal3DRodape");
    if (btnFecharRodape) btnFecharRodape.addEventListener("click", fecharModal3D);

    var overlay = el("modalPeca3D");
    if (overlay) {
      overlay.addEventListener("click", function (e) {
        if (e.target === overlay) fecharModal3D();
      });
    }

    // Vistas 1..8
    document.querySelectorAll(".m3d-view-btn[data-view], .m3d-btn[data-view]").forEach(function (btn) {
      btn.addEventListener("click", function () {
        var vistaNum = parseInt(this.dataset.view, 10);
        irParaVista(vistaNum, true);
      });
    });

    // Ajustar / Fit
    var btnFit = el("btnM3dFit");
    if (btnFit) {
      btnFit.addEventListener("click", function () {
        ajustarAoModelo();
      });
    }

    // Girar Auto
    var btnGirar = el("btnM3dGirar");
    if (btnGirar) {
      btnGirar.addEventListener("click", function () {
        autoGirar = !autoGirar;
        if (autoGirar) {
          btnGirar.classList.add("ativo");
        } else {
          btnGirar.classList.remove("ativo");
        }
      });
    }

    // Grade
    var btnGrade = el("btnM3dGrade");
    if (btnGrade) {
      btnGrade.addEventListener("click", function () {
        if (gradeSolo) {
          gradeSolo.visible = !gradeSolo.visible;
          if (gradeSolo.visible) {
            btnGrade.classList.remove("ativo");
          } else {
            btnGrade.classList.add("ativo");
          }
        }
      });
    }

    // Redimensionamento
    window.addEventListener("resize", function () {
      var ov = el("modalPeca3D");
      if (ov && ov.classList.contains("active")) {
        redimensionar();
      }
    });

    // Atalhos globais
    window.addEventListener("keydown", onKeyDown, true);
  }

  CF.visualizador3d = {
    init: init,
    abrirModal3D: abrirModal3D,
    fecharModal3D: fecharModal3D,
    irParaVista: irParaVista,
    ajustarAoModelo: ajustarAoModelo
  };

  // Auto-inicializa com segurança
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    setTimeout(init, 0);
  }

})(window.CF = window.CF || {});
