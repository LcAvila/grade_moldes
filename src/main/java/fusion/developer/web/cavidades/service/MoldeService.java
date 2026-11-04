package fusion.developer.web.cavidades.service;

import fusion.developer.web.cavidades.model.AtualizacaoCavidadesRequest;
import fusion.developer.web.cavidades.model.Cavidade;
import fusion.developer.web.cavidades.repository.CatalogoRepository;
import fusion.developer.web.cavidades.repository.GradeRepository;

import java.util.LinkedHashMap;
import java.util.Map;

public class MoldeService {

    private final CatalogoRepository catalogo = new CatalogoRepository();
    private final GradeRepository grades = new GradeRepository();

    public Map<String, Object> buscar(String codigo) throws Exception {
        return catalogo.buscarMoldePorCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Molde não encontrado: " + codigo));
    }

    /**
     * Compatibilidade com o endpoint antigo por molde. O fluxo principal do app
     * usa /api/grades; este caminho grava o mesmo formato em registros_molde.
     */
    public void salvar(String codigo, AtualizacaoCavidadesRequest req, String usuario) throws Exception {
        Map<String, Object> dadosCavidades = new LinkedHashMap<>();
        if (req != null && req.cavidades != null) {
            for (Cavidade cavidade : req.cavidades) {
                Map<String, Object> registro = new LinkedHashMap<>();
                registro.put("desvio", cavidade.desvio == null ? "" : cavidade.desvio);
                registro.put("data", cavidade.data == null ? "" : cavidade.data.toString());
                registro.put("vedado", "S".equals(cavidade.vedado) ? "S" : "N");
                registro.put("observacao", cavidade.desvioTexto == null ? "" : cavidade.desvioTexto);
                registro.put("controlador", usuario != null && !usuario.isBlank()
                        ? usuario
                        : (cavidade.controlador == null ? "" : cavidade.controlador));
                dadosCavidades.put(String.valueOf(cavidade.numero), registro);
            }
        }
        grades.salvarRegistrosMolde(codigo, dadosCavidades, new LinkedHashMap<>());
    }
}
