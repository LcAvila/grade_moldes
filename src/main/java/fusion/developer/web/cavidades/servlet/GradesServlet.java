package fusion.developer.web.cavidades.servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fusion.developer.web.cavidades.repository.GradeRepository;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * API das grades abertas. Substitui o localStorage como fonte de verdade.
 *
 * GET    /api/grades              lista (com contagens por grade)
 * GET    /api/grades/{id}         pacote completo (dadosCavidades/dadosGrade)
 * POST   /api/grades              cria uma grade
 * POST   /api/grades/importar     importa grades do localStorage
 * PUT    /api/grades/{id}         salva o pacote completo
 * DELETE /api/grades/{id}         remove
 */
@WebServlet("/api/grades/*")
public class GradesServlet extends HttpServlet {

    private static final TypeReference<Map<String, Object>> MAPA = new TypeReference<>() {};

    private final ObjectMapper json = new ObjectMapper();
    private final GradeRepository repo = new GradeRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = id(req);
        try {
            if (id == null) {
                responder(resp, 200, Map.of("grades", repo.listar()));
                return;
            }
            Optional<Map<String, Object>> grade = repo.carregar(id);
            if (grade.isEmpty()) {
                erro(resp, 404, "Grade nao encontrada: " + id);
                return;
            }
            responder(resp, 200, grade.get());
        } catch (Exception e) {
            getServletContext().log("Erro ao ler grades", e);
            erro(resp, 500, "Erro ao ler o banco: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = id(req);
        try {
            Map<String, Object> corpo = lerCorpo(req);
            if ("importar".equals(id)) {
                importar(resp, corpo);
                return;
            }
            String moldeId = texto(corpo.get("moldeId"));
            if (moldeId == null) {
                erro(resp, 400, "Informe moldeId.");
                return;
            }
            Map<String, Object> grade = repo.criar(
                    moldeId,
                    texto(corpo.get("maquina")),
                    texto(corpo.get("dataEntrada")));
            Map<String, Object> resposta = new LinkedHashMap<>(grade);
            resposta.put("ok", true);
            responder(resp, 201, resposta);
        } catch (IllegalArgumentException e) {
            erro(resp, 400, e.getMessage());
        } catch (Exception e) {
            getServletContext().log("Erro ao criar grade", e);
            erro(resp, 500, "Erro ao gravar no banco: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = id(req);
        if (id == null) {
            erro(resp, 400, "Informe a grade: /api/grades/{id}");
            return;
        }
        try {
            Map<String, Object> corpo = lerCorpo(req);
            String moldeId = texto(corpo.get("moldeId"));
            if (moldeId == null) {
                erro(resp, 400, "Informe moldeId.");
                return;
            }
            repo.salvar(id, moldeId, mapa(corpo.get("dadosCavidades")), mapa(corpo.get("dadosGrade")));
            responder(resp, 200, Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            erro(resp, 400, e.getMessage());
        } catch (Exception e) {
            getServletContext().log("Erro ao salvar grade " + id, e);
            erro(resp, 500, "Erro ao gravar no banco: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = id(req);
        if (id == null) {
            erro(resp, 400, "Informe a grade: /api/grades/{id}");
            return;
        }
        try {
            repo.remover(id);
            responder(resp, 200, Map.of("ok", true));
        } catch (Exception e) {
            getServletContext().log("Erro ao remover grade " + id, e);
            erro(resp, 500, "Erro ao gravar no banco: " + e.getMessage());
        }
    }

    private void importar(HttpServletResponse resp, Map<String, Object> corpo) throws Exception {
        Object no = corpo.get("grades");
        if (!(no instanceof List<?> lista)) {
            erro(resp, 400, "Envie {\"grades\": [...]}.");
            return;
        }
        List<Map<String, Object>> grades = new ArrayList<>();
        for (Object item : lista) {
            grades.add(mapa(item));
        }
        Map<String, Object> resultado = new LinkedHashMap<>(repo.importar(grades));
        resultado.put("ok", true);
        responder(resp, 200, resultado);
    }

    private Map<String, Object> lerCorpo(HttpServletRequest req) throws IOException {
        JsonNode no = json.readTree(req.getInputStream());
        if (no == null || no.isNull()) {
            return new LinkedHashMap<>();
        }
        if (!no.isObject()) {
            throw new IllegalArgumentException("Body JSON precisa ser um objeto.");
        }
        return json.convertValue(no, MAPA);
    }

    private static String id(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            return null;
        }
        return path.substring(1);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Object valor) {
        if (valor instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return new LinkedHashMap<>();
    }

    private static String texto(Object valor) {
        if (valor == null) {
            return null;
        }
        String texto = String.valueOf(valor).trim();
        return texto.isEmpty() ? null : texto;
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
