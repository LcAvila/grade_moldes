package fusion.developer.web.cavidades.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import fusion.developer.web.cavidades.service.MoldeService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Busca de molde. Despachada por ApiMoldesServlet: GET /api/moldes/{codigo}. */
public class MoldesServlet extends HttpServlet {
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();
    private final MoldeService service = new MoldeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String p = req.getPathInfo();
        if (p == null || p.length() < 2) {
            resp.sendError(400, "Use /api/moldes/{codigo}");
            return;
        }
        try {
            resp.setContentType("application/json;charset=UTF-8");
            json.writeValue(resp.getWriter(), service.buscar(p.substring(1)));
        } catch (IllegalArgumentException e) {
            resp.sendError(404, e.getMessage());
        } catch (Exception e) {
            getServletContext().log("Erro API molde", e);
            resp.sendError(500, "Erro interno");
        }
    }
}