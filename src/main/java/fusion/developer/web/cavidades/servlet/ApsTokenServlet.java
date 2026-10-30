package fusion.developer.web.cavidades.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import fusion.developer.web.cavidades.aps.ApsConfig;
import fusion.developer.web.cavidades.aps.ApsTokenService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entrega ao navegador um token de LEITURA do APS (escopo viewables:read).
 *
 * O token de escrita (upload/tradução) fica só no backend, em ApsTokenService.
 * Nunca devolva app.aps.client-secret por aqui.
 *
 * GET /api/aps/token -> { configurado, accessToken, expiresIn }
 */
@WebServlet("/api/aps/token")
public class ApsTokenServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        if (!ApsConfig.configurado()) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            json.writeValue(resp.getWriter(), Map.of(
                    "configurado", false,
                    "erro", "APS não configurado: defina app.aps.client-id e app.aps.client-secret."));
            return;
        }

        try {
            ApsTokenService.Token token = ApsTokenService.paraViewer();

            Map<String, Object> corpo = new LinkedHashMap<>();
            corpo.put("configurado", true);
            corpo.put("accessToken", token.valor());
            corpo.put("expiresIn", token.segundosRestantes());
            json.writeValue(resp.getWriter(), corpo);
        } catch (RuntimeException e) {
            getServletContext().log("Erro ao emitir token do APS", e);
            resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            json.writeValue(resp.getWriter(), Map.of(
                    "configurado", true,
                    "erro", e.getMessage() != null ? e.getMessage() : "Falha ao obter token do APS."));
        }
    }
}
