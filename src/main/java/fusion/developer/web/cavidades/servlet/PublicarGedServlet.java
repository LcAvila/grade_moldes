package fusion.developer.web.cavidades.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/moldes/*/publicar-ged")
public class PublicarGedServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(501, "Integração GED pendente de configuração e validação do ambiente Fusion.");
    }
}