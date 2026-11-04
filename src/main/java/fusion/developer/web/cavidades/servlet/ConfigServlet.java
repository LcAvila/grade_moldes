package fusion.developer.web.cavidades.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import fusion.developer.web.cavidades.repository.CatalogoRepository;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * API de leitura dos catalogos que antes eram arquivos config/*.json.
 *
 * GET /api/config/moldes
 * GET /api/config/desenhos
 * GET /api/config/formas
 * GET /api/config/maquinas
 * GET /api/config/modelos3d
 * GET /api/config/layouts/{id}
 * GET /api/config/historico
 * GET /api/config/historico?arquivo={caminho}
 */
@WebServlet("/api/config/*")
public class ConfigServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper();
    private final CatalogoRepository repo = new CatalogoRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        String[] partes = segmentos(path);
        try {
            if (partes.length == 0) {
                Map<String, Object> indice = new LinkedHashMap<>();
                indice.put("moldes", "/api/config/moldes");
                indice.put("desenhos", "/api/config/desenhos");
                indice.put("formas", "/api/config/formas");
                indice.put("maquinas", "/api/config/maquinas");
                indice.put("modelos3d", "/api/config/modelos3d");
                indice.put("layouts", "/api/config/layouts/{id}");
                indice.put("historico", "/api/config/historico");
                responder(resp, 200, indice);
                return;
            }

            switch (partes[0]) {
                case "moldes" -> responder(resp, 200, Map.of("moldes", repo.listarMoldes()));
                case "desenhos" -> responder(resp, 200, Map.of("desenhos", repo.listarDesenhos()));
                case "formas" -> responder(resp, 200, Map.of("formas", repo.listarFormas()));
                case "maquinas" -> responder(resp, 200, Map.of("maquinas", repo.listarMaquinas()));
                case "modelos3d" -> responder(resp, 200, Map.of("modelos", repo.listarModelos3d()));
                case "layouts" -> {
                    if (partes.length < 2) {
                        erro(resp, 400, "Informe o layout: /api/config/layouts/{id}");
                        return;
                    }
                    Optional<Map<String, Object>> layout = repo.carregarLayout(partes[1]);
                    if (layout.isEmpty()) {
                        erro(resp, 404, "Layout nao encontrado: " + partes[1]);
                        return;
                    }
                    responder(resp, 200, layout.get());
                }
                case "historico" -> {
                    String arquivo = req.getParameter("arquivo");
                    if (arquivo != null && !arquivo.isBlank()) {
                        Optional<Map<String, Object>> pacote = repo.carregarHistorico(arquivo);
                        if (pacote.isEmpty()) {
                            erro(resp, 404, "Grade historica nao encontrada: " + arquivo);
                            return;
                        }
                        responder(resp, 200, pacote.get());
                    } else {
                        responder(resp, 200, repo.indiceHistorico());
                    }
                }
                default -> erro(resp, 404, "Recurso de configuracao desconhecido: " + partes[0]);
            }
        } catch (Exception e) {
            getServletContext().log("Erro na API de configuracao", e);
            erro(resp, 500, "Erro ao ler o banco: " + e.getMessage());
        }
    }

    private static String[] segmentos(String path) {
        if (path == null || path.length() < 2) {
            return new String[0];
        }
        return path.substring(1).split("/");
    }

    private void responder(HttpServletResponse resp, int status, Object corpo) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        json.writeValue(resp.getWriter(), corpo);
    }

    private void erro(HttpServletResponse resp, int status, String mensagem) throws IOException {
        responder(resp, status, Map.of("erro", mensagem));
    }
}
