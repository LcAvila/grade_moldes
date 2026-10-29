package fusion.developer.web.cavidades.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fusion.developer.web.cavidades.seguranca.UsuarioAtual;
import fusion.developer.web.cavidades.service.GedFusionService;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Base64;

/**
 * Publicação no GED. Despachada por ApiMoldesServlet:
 * POST /api/moldes/{codigo}/publicar-ged
 *
 * Body JSON: { "file": "relatorio-115.pdf", "fileBytes": "<base64>" }
 *   (mesmos nomes da API GED; "file" deve ser igual a fields.file enviado)
 *
 * author = usuário da sessão do Fusion (IdentificacaoFilter), nunca do body:
 * o Token do GED nunca chega ao navegador.
 */
public class PublicarGedServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper();
    private final GedFusionService ged = new GedFusionService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] partes = req.getRequestURI().split("/");
        if (partes.length < 2) {
            resp.sendError(400, "Código do molde inválido");
            return;
        }
        String codigo = partes[partes.length - 2];

        JsonNode corpo;
        try {
            corpo = json.readTree(req.getInputStream());
        } catch (Exception e) {
            respondeErro(resp, 400, "Body JSON inválido: " + e.getMessage());
            return;
        }

        String nome = texto(corpo, "file");
        String base64 = texto(corpo, "fileBytes");
        if (nome == null || nome.isBlank() || base64 == null || base64.isBlank()) {
            respondeErro(resp, 400,
                    "Envie {\"file\": \"nome.pdf\", \"fileBytes\": \"<base64>\"}. "
                    + "\"file\" precisa ser igual ao nome enviado ao GED.");
            return;
        }

        byte[] bytes;
        try {
            // aceita também data URLs vindas do canvas (data:...;base64,XXXX)
            int virgula = base64.indexOf("base64,");
            String limpo = virgula >= 0 ? base64.substring(virgula + 7) : base64;
            bytes = Base64.getDecoder().decode(limpo.replaceAll("\\s", ""));
        } catch (IllegalArgumentException e) {
            respondeErro(resp, 400, "fileBytes não é base64 válido.");
            return;
        }

        UsuarioAtual usuario = (UsuarioAtual) req.getAttribute("usuarioAtual");
        String autor = usuario != null ? usuario.login : null;

        try {
            GedFusionService.Resultado r = ged.publicar(codigo, nome, bytes, autor);

            ObjectNode resposta = json.createObjectNode();
            resposta.put("ok", true);
            resposta.put("jaPublicado", r.jaPublicado());
            if (r.neoId() != null) resposta.put("neoId", r.neoId());
            if (r.code() != null) resposta.put("code", r.code());
            if (r.url() != null) resposta.put("url", r.url());

            resp.setContentType("application/json;charset=UTF-8");
            json.writeValue(resp.getWriter(), resposta);

        } catch (GedFusionService.ConfiguracaoIncompletaException e) {
            // ambiente sem token/base-url -> o front mostra "GED não configurado"
            getServletContext().log("GED não configurado: " + e.getMessage());
            respondeErro(resp, 503, e.getMessage());
        } catch (Exception e) {
            String motivo = e.getMessage() != null && !e.getMessage().isBlank()
                    ? e.getMessage() : e.getClass().getName();
            getServletContext().log("Erro ao publicar no GED (molde " + codigo + ")", e);
            respondeErro(resp, 502, "Falha na API do GED: " + motivo);
        }
    }

    private static String texto(JsonNode n, String campo) {
        JsonNode v = n.get(campo);
        return v != null && !v.isNull() ? v.asText() : null;
    }

    private void respondeErro(HttpServletResponse resp, int status, String mensagem) throws IOException {
        ObjectNode n = json.createObjectNode();
        n.put("erro", mensagem);
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        json.writeValue(resp.getWriter(), n);
    }
}
