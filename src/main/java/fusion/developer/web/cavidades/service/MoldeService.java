package fusion.developer.web.cavidades.service;

import fusion.developer.web.cavidades.model.AtualizacaoCavidadesRequest;
import fusion.developer.web.cavidades.model.Molde;
import fusion.developer.web.cavidades.repository.CavidadeRepository;
import fusion.developer.web.cavidades.repository.MoldeRepository;

public class MoldeService {
    private final MoldeRepository moldes = new MoldeRepository();
    private final CavidadeRepository cavidades = new CavidadeRepository();

    public Molde buscar(String codigo) throws Exception {
        return moldes.buscarCompletoPorCodigo(codigo)
            .orElseThrow(() -> new IllegalArgumentException("Molde não encontrado: " + codigo));
    }

    public void salvar(String codigo, AtualizacaoCavidadesRequest req) throws Exception {
        Molde m = buscar(codigo);
        cavidades.salvarLote(m.id, req.cavidades);
    }
}