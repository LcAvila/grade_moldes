package fusion.developer.web.cavidades.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fusion.developer.web.cavidades.seguranca.UsuarioAtual;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Devolve o usuário identificado na requisição (sempre 200).
 * O front usa este endpoint para detectar se há backend Fusion:
 *   autenticado=true  -> modo Fusion (grava via API, com autor no registro)
 *   autenticado=false -> sem sessão (modo local: localStorage)
 */
@WebServlet("/api/usuario-atual")
public class UsuarioAtualServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UsuarioAtual u = (UsuarioAtual) req.getAttribute("usuarioAtual");

        ObjectNode n = json.createObjectNode();
        n.put("autenticado", u != null);
        if (u != null) {
            n.put("login", u.login);
            n.put("nome", u.nome);
            n.put("ativo", u.ativo);
            n.put("origem", u.origem);
        } else {
            n.putNull("login");
            n.putNull("nome");
            n.put("ativo", false);
            n.putNull("origem");
        }

        resp.setContentType("application/json;charset=UTF-8");
        json.writeValue(resp.getWriter(), n);
    }
}
