package fusion.developer.web.cavidades.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import fusion.developer.web.cavidades.model.AtualizacaoCavidadesRequest;
import fusion.developer.web.cavidades.service.MoldeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/** Gravação de cavidades. Despachada por ApiMoldesServlet: PUT /api/moldes/{codigo}/cavidades. */
public class CavidadesServlet extends HttpServlet {
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();
    private final MoldeService service = new MoldeService();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] partes = req.getRequestURI().split("/");
        if (partes.length < 4) {
            resp.sendError(400, "Código inválido");
            return;
        }
        String codigo = partes[partes.length - 2];
        try {
            AtualizacaoCavidadesRequest body = json.readValue(req.getInputStream(), AtualizacaoCavidadesRequest.class);

            // o autor NÃO vem confiável do cliente: a sessão do Fusion manda
            fusion.developer.web.cavidades.seguranca.UsuarioAtual usuario =
                    (fusion.developer.web.cavidades.seguranca.UsuarioAtual) req.getAttribute("usuarioAtual");
            if (usuario != null) {
                body.usuario = usuario.login;
            }

            service.salvar(codigo, body, body.usuario);
            resp.setContentType("application/json;charset=UTF-8");
            json.writeValue(resp.getWriter(), Map.of("ok", true));
        } catch (Exception e) {
            getServletContext().log("Erro ao salvar cavidades", e);
            resp.sendError(500, e.getMessage());
        }
    }
}