package fusion.developer.web.cavidades.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Roteador de /api/moldes/*.
 *
 * Motivo: o Servlet so aceita curinga no FIM do padrao; curinga no meio do
 * caminho (ex.: /api/moldes/ + estrela + /cavidades) e invalido e nunca casa
 * (as requisicoes caiam no MoldesServlet e voltavam 405). Um unico servlet
 * despacha pelo pathInfo:
 *
 *   GET   /api/moldes/{codigo}              -> MoldesServlet.doGet
 *   PUT   /api/moldes/{codigo}/cavidades    -> CavidadesServlet.doPut
 *   POST  /api/moldes/{codigo}/publicar-ged -> PublicarGedServlet.doPost
 */
@WebServlet("/api/moldes/*")
public class ApiMoldesServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper();
    private final MoldesServlet moldes = new MoldesServlet();
    private final CavidadesServlet cavidades = new CavidadesServlet();
    private final PublicarGedServlet ged = new PublicarGedServlet();

    /**
     * Inicializa os sub-servlets com o mesmo config: sem isso,
     * getServletContext() (usado nos logs de erro deles) seria NPE,
     * pois instancias criadas com new nao passam por init().
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        moldes.init(config);
        cavidades.init(config);
        ged.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] p = segmentos(req);
        if (p != null && p.length == 1) {
            moldes.doGet(req, resp);
            return;
        }
        naoSuportado(req, resp, "GET");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] p = segmentos(req);
        if (p != null && p.length == 2 && "cavidades".equals(p[1])) {
            cavidades.doPut(req, resp);
            return;
        }
        naoSuportado(req, resp, "PUT");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] p = segmentos(req);
        if (p != null && p.length == 2 && "publicar-ged".equals(p[1])) {
            ged.doPost(req, resp);
            return;
        }
        naoSuportado(req, resp, "POST");
    }

    /** pathInfo ("/115/cavidades") -> segmentos nao vazios, ou null. */
    private static String[] segmentos(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            return null;
        }
        return path.substring(1).split("/");
    }

    private void naoSuportado(HttpServletRequest req, HttpServletResponse resp, String metodo)
            throws IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        resp.setContentType("application/json;charset=UTF-8");
        json.writeValue(resp.getWriter(), Map.of(
                "erro", metodo + " não suportado para " + req.getRequestURI(),
                "formatos", "GET /api/moldes/{codigo} | "
                        + "PUT /api/moldes/{codigo}/cavidades | "
                        + "POST /api/moldes/{codigo}/publicar-ged"));
    }
}
