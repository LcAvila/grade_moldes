<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Cavidades não conformes - Compactor</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">

</head>
<body data-context="${pageContext.request.contextPath}">

	<section class="tela-inicial" id="telaInicial">
		<header class="ti-cabecalho">
			<div class="ti-logo">
				<img
					src="${pageContext.request.contextPath}/assets/logos/Logo%20Compactor.svg"
					alt="Logo Compactor">
			</div>
			<div class="ti-titulo">
				<h1>GRADES DE MOLDES ABERTAS</h1>
				<p>Selecione uma grade para registrar não conformidades.</p>
			</div>
			<div class="ti-acao">
				<button class="btn btn-success" id="btnAbrirGrade" type="button">+ ABRIR GRADE</button>
			</div>
		</header>

		<div class="grades-grid" id="listaGrades"></div>
		<div class="grades-vazio" id="gradesVazio" style="display: none">
			Nenhuma grade aberta. Clique em <strong>ABRIR GRADE</strong> para começar.
		</div>
	</section>

	<div class="app" id="telaGrade" style="display: none">

		<aside class="painel-form">

			<div class="grade-atual">
				<button class="btn btn-secondary btn-voltar" id="btnVoltar"
					type="button">‹ GRADES</button>
				<div class="hint" id="moldeInfo"></div>
			</div>

			<h2>REGISTRAR NÃO CONFORMIDADE</h2>

			<div class="resumo-status">
				<div class="resumo-card">
					<div class="num" id="resTotal">64</div>
					<div class="lbl">Total</div>
				</div>
				<div class="resumo-card res-ok">
					<div class="num" id="resOk">64</div>
					<div class="lbl">OK</div>
				</div>
				<div class="resumo-card res-def">
					<div class="num" id="resDef">0</div>
					<div class="lbl">Defeitos</div>
				</div>
				<div class="resumo-card res-ved">
					<div class="num" id="resVed">0</div>
					<div class="lbl">Vedadas</div>
				</div>
			</div>

			<div class="form-group">
				<label for="cavidade" id="lblCavidade">Cavidade</label> <select
					id="cavidade"><option value="">Selecione a
						cavidade</option></select>
				<div class="hint" id="selecaoInfo">Nenhuma cavidade
					selecionada.</div>
			</div>

			<div class="form-group">
				<label for="desvio">Desvio / Defeito</label> <input type="text"
					id="desvio" list="listaDesvios"
					placeholder="Selecione um item da lista ou digite livremente"
					autocomplete="off">
				<datalist id="listaDesvios">
					<option value="F.P.F.Q.">F.P.F.Q. - Frente do pino de
						formato quebrada</option>
					<option value="P.F.Q.">P.F.Q. - Pino do formato quebrado</option>
					<option value="V.P.F.">V.P.F. - Vedada pela ferramentaria</option>
					<option value="REB.">REB. - Rebarba</option>
					<option value="B.V.">B.V. - Bucha virada</option>
					<option value="C.P.Q.">C.P.Q. - Contra pino quebrado</option>
					<option value="B.B.F.Q.">B.B.F.Q. - Boca da bucha do
						formato quebrada</option>
				</datalist>
			</div>

			<div class="form-group" id="obsField" style="display: none">
				<label for="observacao">Observação / Descrição</label>
				<textarea id="observacao"
					placeholder="Descreva o defeito ou a região da rebarba..."></textarea>
			</div>

			<div class="form-group">
				<label for="data">Data do Registro</label> <input type="date"
					id="data">
			</div>

			<div class="form-group">
				<label>Vedado?</label>
				<div class="radio-group">
					<label><input type="radio" name="vedado" value="S">
						Sim</label> <label><input type="radio" name="vedado" value="N"
						checked> Não</label>
				</div>
			</div>

			<div class="form-group">
				<label for="controlador">Controlador Responsável</label> <input
					type="text" id="controlador" value="">
			</div>

			<div class="btn-group">
				<button class="btn" id="btnSalvar" type="button">SALVAR</button>
				<button class="btn btn-secondary" id="btnLimpar" type="button">LIMPAR</button>
				<button class="btn btn-danger" id="btnExcluir" type="button">EXCLUIR
					REG.</button>
			</div>

			<div class="btn-group">
				<button class="btn btn-success" id="btnExportar" type="button">EXPORTAR
					JSON</button>
				<button class="btn btn-secondary" id="btnImportar" type="button">IMPORTAR</button>
				<input type="file" id="fileImport" accept=".json,application/json"
					hidden>
				<button class="btn btn-secondary" id="btnImprimir" type="button">IMPRIMIR</button>
			</div>

			<div class="info-box">
				<h4>Como usar</h4>
				<p>Clique em uma cavidade no diagrama ou tabela para
					selecioná-la.</p>
				<p>
					<strong>Ctrl (ou Cmd) + clique</strong> em várias cavidades para
					selecionar mais de uma ao mesmo tempo.
				</p>
				<p>
					<strong>Clique no número central de uma zona</strong> (1, 2, 3 ou
					4) para selecionar as 16 cavidades daquela zona de uma vez — útil
					para <strong>fechar a zona inteira</strong>.
				</p>
				<p>
					Preencha os campos e clique em <strong>SALVAR</strong>: o registro
					é aplicado a todas as cavidades selecionadas.
				</p>
				<p>
					No campo <strong>Desvio / Defeito</strong>, escolha uma opção da
					lista ou digite livremente — os dois modos funcionam no mesmo
					campo.
				</p>
				<p>
					<strong style="color: var(--red)">Vermelho</strong> = Vedado (SIM).
					<strong style="color: #a66d00">Amarelo</strong> = Defeito (Vedado
					NÃO). <strong style="color: var(--muted)">Cinza</strong> = Normal.
				</p>
				<p>
					<strong>Dica:</strong> clique duas vezes em uma cavidade com
					registro para ver detalhes. A impressão usa um layout compacto em
					folha A4 única.
				</p>
			</div>
		</aside>

		<section class="painel-grade">
			<div class="header-grade">
				<div class="logo-box">
					<img
						src="${pageContext.request.contextPath}/assets/logos/Logo%20Compactor.svg"
						alt="Logo Compactor">
					<div class="logo-fallback" id="logoFallback">
						<div class="logo-text">COMPACTOR</div>
						<div class="logo-sub">Cia. de Canetas</div>
					</div>
				</div>
				<div class="title-box">
					<h1>CAVIDADES NÃO CONFORMES</h1>
				</div>
				<div class="info-molde">
					<div class="produto">
						<span class="editable" data-field="produto" contenteditable="true">CONEXÕES
							ESFEROGRÁFICAS</span>
					</div>
					<div class="molde-num">
						<strong>Molde </strong><span class="editable"
							data-field="moldeNum" contenteditable="true">168</span>
					</div>
					<div class="molde-mod">
						<span class="editable" data-field="moldeMod"
							contenteditable="true">Coreano SW 1307</span>
					</div>
				</div>
			</div>

			<div class="corpo-grade">
				<div class="tabela-cav">
					<table>
						<thead>
							<tr>
								<th colspan="5">Esquerda</th>
								<th colspan="5">Direita</th>
							</tr>
							<tr>
								<th>Cav</th>
								<th>Desvio</th>
								<th>Data</th>
								<th>Vedado</th>
								<th>Controlador</th>
								<th>Cav</th>
								<th>Desvio</th>
								<th>Data</th>
								<th>Vedado</th>
								<th>Controlador</th>
							</tr>
						</thead>
						<tbody id="tbodyCavidades"></tbody>
					</table>
				</div>

				<div class="area-direita">
					<div class="diagrama-box">
						<svg viewBox="0 0 1300 1350" id="svgDiagrama"
							xmlns="http://www.w3.org/2000/svg"></svg>
						<div class="aviso-box" id="avisoLayout" style="display: none"></div>
					</div>

					<div class="peca-box">
						<img id="pecaImg"
							src="${pageContext.request.contextPath}/assets/images/conex%C3%A3o%20esferografica.png"
							alt="Desenho da peça - Conexão Esferográfica"
							onerror="this.style.display='none'; document.getElementById('pecaErro').style.display='block';">
						<div class="peca-erro" id="pecaErro" style="display: none">
							Imagem não encontrada. Verifique se o arquivo está em:<br>
							<code>assets/grades cavidades svg/molde 168/CONEXÃO
								ESFEROGRÁFICAS.png</code>
							<br> (caminho relativo à pasta onde está este index.html)
						</div>
					</div>

					<div class="legenda-box">
						<h3>LEGENDA</h3>
						<div class="leg-item">
							<span class="leg-sigla">F.P.F.Q.</span> Frente do pino de formato
							quebrada
						</div>
						<div class="leg-item">
							<span class="leg-sigla">P.F.Q.</span> Pino do formato quebrado
						</div>
						<div class="leg-item">
							<span class="leg-sigla">V.P.F.</span> Vedada pela ferramentaria
						</div>
						<div class="leg-item">
							<span class="leg-sigla">REB.</span> Rebarba - especificar região
						</div>
						<div class="leg-item">
							<span class="leg-sigla">B.V.</span> Bucha virada
						</div>
						<div class="leg-item">
							<span class="leg-sigla">C.P.Q.</span> Contra pino quebrado
						</div>
						<div class="leg-item">
							<span class="leg-sigla">B.B.F.Q.</span> Boca da bucha do formato
							quebrada
						</div>
					</div>

					<div class="info-entrada">
						<h3>INFORMAÇÕES DE ENTRADA DO MOLDE</h3>
						<div class="info-linha">
							<span class="info-label">Material</span><span class="editable"
								data-field="material" contenteditable="true">POLIPROPILENO
								PP</span>
						</div>
						<div class="info-linha">
							<span class="info-label">Máquinas</span><span class="editable"
								data-field="maquina" contenteditable="true">ENGEL 33</span>
						</div>
						<div class="info-linha">
							<span class="info-label">Controlador resp. abertura</span><span
								class="editable" data-field="controladorAbertura"
								contenteditable="true"></span>
						</div>
						<div class="info-linha">
							<span class="info-label">Data da entrada do molde</span><span
								class="editable" data-field="dataEntrada" contenteditable="true"></span>
						</div>
						<div class="info-linha">
							<span class="info-label">Atualização / responsável</span><span
								class="editable" data-field="dataAtualizacao"
								contenteditable="true"></span>
						</div>
						<div class="info-linha">
							<span class="info-label">Observações adicionais</span><span
								class="editable" data-field="observacoes" contenteditable="true"></span>
						</div>
					</div>

					<div class="revisao">
						Revisão <span class="editable" data-field="revisaoNum"
							contenteditable="true">05</span> - Data <span class="editable"
							data-field="revisaoData" contenteditable="true">20 Março
							2020</span>
					</div>
				</div>
			</div>
		</section>
	</div>

	<!-- ===== FOLHA DE IMPRESSÃO (montada via JS, dimensionada em mm reais de A4) ===== -->
	<div class="folha-impressao" id="folhaImpressao">
		<div class="folha-cabecalho">
			<div class="folha-cab-logo" id="folhaLogoBox">
				<img id="folhaLogoImg"
					src="${pageContext.request.contextPath}/assets/logos/Logo%20Compactor.svg"
					alt="Logo"
					onerror="this.style.display='none'; document.getElementById('folhaLogoFallback').style.display='block';">
				<div id="folhaLogoFallback" style="display: none">
					<div class="fb-nome">COMPACTOR</div>
					<div class="fb-sub">Cia. de Canetas</div>
				</div>
			</div>
			<div class="folha-cab-titulo">
				<h1>CAVIDADES NÃO CONFORMES</h1>
			</div>
			<div class="folha-cab-info">
				<div class="produto" id="folhaProduto">CONEXÕES ESFEROGRÁFICAS</div>
				<div class="molde" id="folhaMoldeNum">Molde 168</div>
				<div id="folhaMoldeMod">Coreano SW 1307</div>
			</div>
		</div>
		<div class="folha-corpo-impressao">
			<div class="folha-col">
				<div class="tabela-esquerda-wrap">
					<table class="folha-tabela">
						<thead>
							<tr>
								<th>Cav.</th>
								<th>Desvio</th>
								<th>Data</th>
								<th>Vedado</th>
								<th>Controlador</th>
							</tr>
						</thead>
						<tbody id="folhaTbodyEsquerda"></tbody>
					</table>
				</div>
			</div>
			<div class="folha-col folha-bloco-direita">
				<div class="tabela-direita-wrap">
					<table class="folha-tabela">
						<thead>
							<tr>
								<th>Cav.</th>
								<th>Desvio</th>
								<th>Data</th>
								<th>Vedado</th>
								<th>Controlador</th>
							</tr>
						</thead>
						<tbody id="folhaTbodyDireita"></tbody>
					</table>
				</div>
				<div class="folha-diagrama" id="folhaDiagramaBox"></div>
				<div class="folha-peca">
					<img id="folhaPecaImg"
						src="assets/grades cavidades svg/molde 168/CONEXÃO ESFEROGRÁFICAS.png"
						alt="Peça">
				</div>
				<div class="folha-legenda">
					<h3>LEGENDA:</h3>
					<div class="fl-item">
						<span class="fl-sigla">F.P.F.Q.</span><span>Frente do pino
							de formato quebrada</span>
					</div>
					<div class="fl-item">
						<span class="fl-sigla">P.F.Q.</span><span>Pino do formato
							Quebrado</span>
					</div>
					<div class="fl-item">
						<span class="fl-sigla">V.P.F.</span><span>Vedada pela
							ferramentaria</span>
					</div>
					<div class="fl-item">
						<span class="fl-sigla">REB.</span><span>Rebarba
							(especificar região da rebarba)</span>
					</div>
					<div class="fl-item">
						<span class="fl-sigla">B.V.</span><span>Bucha Virada</span>
					</div>
					<div class="fl-item">
						<span class="fl-sigla">C.P.Q.</span><span>Contra pino
							quebrado</span>
					</div>
					<div class="fl-item">
						<span class="fl-sigla">B.B.F.Q.</span><span>Boca da bucha
							do formato quebrada</span>
					</div>
				</div>
				<div class="folha-info">
					<h3>INFORMAÇÕES DE ENTRADA DO MOLDE</h3>
					<div class="fi-linha">
						<span>Material:</span><strong id="folhaMaterial"></strong>
					</div>
					<div class="fi-linha">
						<span>Máquina:</span><strong id="folhaMaquina"></strong>
					</div>
					<div class="fi-linha">
						<span>Controlador resp.(abertura do documento):</span><strong
							id="folhaControladorAbertura"></strong>
					</div>
					<div class="fi-linha">
						<span>Data da entrada do molde:</span><strong
							id="folhaDataEntrada"></strong>
					</div>
					<div class="fi-linha">
						<span>Data de atualização da grade/Responsável:</span><strong
							id="folhaDataAtualizacao"></strong>
					</div>
					<div style="margin-top: 1mm;">
						<em>Observações adicionais:</em> <span id="folhaObservacoes"></span>
					</div>
				</div>
				<div class="folha-revisao" id="folhaRevisao"></div>
			</div>
		</div>
	</div>

	<div class="modal-overlay" id="modalOverlay">
		<div class="modal">
			<h3>
				Detalhes da Cavidade <span id="modalCavNum"></span>
			</h3>
			<div id="modalConteudo"></div>
			<div class="modal-actions">
				<button class="btn" id="modalEditar" type="button">EDITAR</button>
				<button class="btn btn-danger" id="modalExcluir" type="button">EXCLUIR</button>
				<button class="btn btn-secondary" id="modalFechar" type="button">FECHAR</button>
			</div>
		</div>
	</div>

	<div class="modal-overlay" id="modalAbrirGrade">
		<div class="modal">
			<h3>Abrir grade</h3>
			<div class="hint">Escolha o molde para abrir uma nova grade.</div>
			<div class="lista-moldes" id="listaMoldesModal"></div>
			<div class="modal-actions">
				<button class="btn btn-secondary" id="btnCancelarAbrir" type="button">CANCELAR</button>
			</div>
		</div>
	</div>

	<script src="${pageContext.request.contextPath}/js/core/estado.js"></script>
	<script src="${pageContext.request.contextPath}/js/core/persistencia.js"></script>
	<script src="${pageContext.request.contextPath}/js/layouts/generators.js"></script>
	<script src="${pageContext.request.contextPath}/js/render/svg.js"></script>
	<script src="${pageContext.request.contextPath}/js/render/formulario.js"></script>
	<script src="${pageContext.request.contextPath}/js/render/cabecalho.js"></script>
	<script src="${pageContext.request.contextPath}/js/render/tabela.js"></script>
	<script src="${pageContext.request.contextPath}/js/render/diagrama.js"></script>
	<script src="${pageContext.request.contextPath}/js/render/resumo.js"></script>
	<script src="${pageContext.request.contextPath}/js/core/registros.js"></script>
	<script src="${pageContext.request.contextPath}/js/render/modal.js"></script>
	<script src="${pageContext.request.contextPath}/js/render/impressao.js"></script>
	<script src="${pageContext.request.contextPath}/js/core/io.js"></script>
	<script src="${pageContext.request.contextPath}/js/core/configLoader.js"></script>
	<script src="${pageContext.request.contextPath}/js/render/telaInicial.js"></script>
	<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>