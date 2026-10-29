package fusion.developer.web.cavidades.seguranca;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exige usuário identificado em toda chamada /api/*.
 *
 * O usuário NÃO é logado aqui: vem do Fusion Platform (SessaoFusion).
 * Sem usuário -> 401 JSON, exceto em GET /api/usuario-atual, que sempre
 * responde (é o endpoint de detecção do modo do front).
 */
@WebFilter(urlPatterns = "/api/*")
public class IdentificacaoFilter implements Filter {

    private final ObjectMapper json = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest r = (HttpServletRequest) req;
        UsuarioAtual usuario = SessaoFusion.resolver(r);
        if (usuario != null) {
            r.setAttribute("usuarioAtual", usuario);
        }

        boolean consultaUsuario = r.getRequestURI().endsWith("/api/usuario-atual");
        if (usuario == null && !consultaUsuario) {
            HttpServletResponse s = (HttpServletResponse) resp;
            s.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            s.setContentType("application/json;charset=UTF-8");

            Map<String, Object> corpo = new LinkedHashMap<>();
            corpo.put("erro", "Usuário do Fusion Platform não identificado.");
            corpo.put("dica", "Faça login no Fusion Platform. Em desenvolvimento local, "
                    + "configure app.dev.usuario em application.properties.");
            json.writeValue(s.getWriter(), corpo);
            return;
        }

        chain.doFilter(req, resp);
    }
}
