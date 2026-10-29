package fusion.developer.web.cavidades.service;

import fusion.developer.web.cavidades.config.AppConfig;
import fusion.developer.web.cavidades.model.AtualizacaoCavidadesRequest;
import fusion.developer.web.cavidades.model.Molde;
import fusion.developer.web.cavidades.repository.CavidadeRepository;
import fusion.developer.web.cavidades.repository.CavidadeRepositoryFusion;
import fusion.developer.web.cavidades.repository.MoldeRepository;

public class MoldeService {
    private final MoldeRepository moldes = new MoldeRepository();
    private final CavidadeRepository cavidades = new CavidadeRepository();

    public Molde buscar(String codigo) throws Exception {
        return moldes.buscarCompletoPorCodigo(codigo)
            .orElseThrow(() -> new IllegalArgumentException("Molde não encontrado: " + codigo));
    }

    /**
     * Persiste as cavidades. O destino é escolhido por app.persistencia:
     *   fusion (padrão) = formulário do Fusion Platform — caminho aprovado,
     *                     não toca no MySQL e não exige schema próprio;
     *   mysql           = tabelas cavidades/moldes existentes (via JDBC).
     * O usuário vem da sessão do Fusion (IdentificacaoFilter); só cai no
     * body (req.usuario) quando não há sessão (ex.: testes locais).
     */
    public void salvar(String codigo, AtualizacaoCavidadesRequest req, String usuario) throws Exception {
        String modo = AppConfig.get("app.persistencia");
        modo = (modo == null || modo.isBlank()) ? "fusion" : modo.trim();

        if ("mysql".equalsIgnoreCase(modo)) {
            Molde m = buscar(codigo);
            cavidades.salvarLote(m.id, req.cavidades);
            return;
        }

        String responsavel = (usuario != null && !usuario.isBlank()) ? usuario : req.usuario;
        new CavidadeRepositoryFusion().salvarLote(codigo, req.cavidades, responsavel);
    }
}