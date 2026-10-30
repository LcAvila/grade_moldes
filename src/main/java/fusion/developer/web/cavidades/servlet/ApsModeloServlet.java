package fusion.developer.web.cavidades.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import fusion.developer.web.cavidades.aps.ApsConfig;
import fusion.developer.web.cavidades.aps.ApsModeloService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Prepara (upload + tradução SVF2) e informa o estado de um STL no APS.
 *
 * GET /api/aps/modelo?verificar=1&arquivo=conexao-padrao.stl
 *   -> consulta rápida, sem upload nem créditos; usado pela fachada para
 *      decidir entre APS e viewer local.
 *
 * GET /api/aps/modelo?arquivo=conexao-padrao.stl
 *   -> garante upload/tradução e devolve o URN quando estiver pronto
 *      (o front faz polling desta rota até status = ready).
 *
 * Resposta: { configurado, status, urn?, arquivo, progresso?, mensagem? }
 * status: processing | ready | error | desconfigurado
 */
@WebServlet("/api/aps/modelo")
public class ApsModeloServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        String arquivo = req.getParameter("arquivo");
        boolean verificar = "1".equals(req.getParameter("verificar"))
                || "true".equalsIgnoreCase(req.getParameter("verificar"));

        try {
            ApsModeloService.Resultado resultado = verificar
                    ? ApsModeloService.verificar(arquivo)
                    : ApsModeloService.obter(diretorioModelos(), arquivo);

            json.writeValue(resp.getWriter(), paraJson(resultado));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.writeValue(resp.getWriter(), Map.of(
                    "configurado", ApsConfig.configurado(),
                    "erro", e.getMessage()));
        } catch (RuntimeException e) {
            getServletContext().log("Erro na API APS de modelo", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.writeValue(resp.getWriter(), Map.of(
                    "configurado", ApsConfig.configurado(),
                    "erro", e.getMessage() != null ? e.getMessage() : "Erro ao preparar o modelo no APS."));
        }
    }

    /**
     * Os STLs servidos ao viewer local ficam em /assets/models dentro do WAR.
     * getRealPath exige deploy explodido (padrão do Tomcat/Eclipse WTP).
     */
    private Path diretorioModelos() {
        String real = getServletContext().getRealPath("/assets/models");
        if (real == null) {
            throw new IllegalStateException(
                    "Não foi possível localizar /assets/models: publique o WAR explodido no Tomcat.");
        }
        return Paths.get(real);
    }

    private static Map<String, Object> paraJson(ApsModeloService.Resultado resultado) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("configurado", resultado.configurado());
        corpo.put("status", resultado.status());
        if (resultado.urn() != null) {
            corpo.put("urn", resultado.urn());
        }
        if (resultado.arquivo() != null) {
            corpo.put("arquivo", resultado.arquivo());
        }
        if (resultado.progresso() != null) {
            corpo.put("progresso", resultado.progresso());
        }
        if (resultado.mensagem() != null) {
            corpo.put("mensagem", resultado.mensagem());
        }
        return corpo;
    }
}
