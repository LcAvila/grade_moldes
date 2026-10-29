package fusion.developer.web.cavidades.seguranca;

import fusion.developer.web.cavidades.config.AppConfig;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolve o usuário logado no Fusion Platform, sem tela de login própria.
 *
 * Ordem de resolução:
 *  1. sessão do contêiner (getRemoteUser), se o contexto estiver protegido;
 *  2. PortalUtil.getCurrentUser() do Fusion, via reflexão — o SDK do Fusion
 *     não está no classpath deste projeto (builda fora do Fusion), por isso o
 *     FQN é configurável em application.properties (app.fusion.portal-util-class);
 *  3. app.dev.usuario, fallback exclusivo de desenvolvimento local.
 */
public final class SessaoFusion {

    // Pacote real deste Fusion 4.25.1: com.neomind.* (o projeto irmão CustonFusion
    // importa com.neomind.fusion.*). "br.com.neomind.*" é o prefixo de versoes antigas
    // e não casa aqui — mantido por último só como fallback.
    private static final String[] CANDIDATOS_PORTAL_UTIL = {
        "com.neomind.fusion.portal.PortalUtil",
        "com.neomind.portal.PortalUtil",
        "com.neomind.fusion.PortalUtil",
        "br.com.neomind.fusion.portal.PortalUtil"
    };

    private SessaoFusion() {}

    public static UsuarioAtual resolver(HttpServletRequest req) {
        // 1) contêiner
        try {
            String remote = req.getRemoteUser();
            if (remote != null && !remote.isBlank()) {
                return new UsuarioAtual(remote, remote, true, "container");
            }
        } catch (Exception ignore) {
            // segue para o próximo método
        }

        // 2) Fusion (PortalUtil via reflexão)
        ResultadoPortal portal = portalUtil();
        if (portal.resolvido()) {
            // null aqui = dentro do Fusion sem sessao -> 401, sem cair no fallback dev
            return portal.usuario();
        }

        // 3) desenvolvimento local
        String dev = AppConfig.get("app.dev.usuario");
        if (dev != null && !dev.isBlank()) {
            return new UsuarioAtual(dev.trim(), dev.trim(), true, "dev");
        }
        return null;
    }

    // ------------------------------------------------------------------
    // PortalUtil (reflexão)
    // ------------------------------------------------------------------

    /** Resultado da tentativa de uso do PortalUtil: resolvido = a classe foi encontrada. */
    private record ResultadoPortal(UsuarioAtual usuario, boolean resolvido) {}

    private static ResultadoPortal portalUtil() {
        List<String> candidatos = new ArrayList<>();
        String configurado = AppConfig.get("app.fusion.portal-util-class");
        if (configurado != null && !configurado.isBlank()) {
            candidatos.add(configurado.trim());
        }
        for (String c : CANDIDATOS_PORTAL_UTIL) {
            candidatos.add(c);
        }

        for (String nome : candidatos) {
            Class<?> classe;
            try {
                classe = Class.forName(nome);
            } catch (ClassNotFoundException | NoClassDefFoundError ignore) {
                continue; // SDK ausente neste classloader (dev local)
            }
            try {
                Method m = classe.getMethod("getCurrentUser");
                Object usuario = m.invoke(null);
                if (usuario == null) {
                    // classe existe, mas nao ha login -> resolvido sem usuario
                    return new ResultadoPortal(null, true);
                }
                return new ResultadoPortal(extrair(usuario), true);
            } catch (NoSuchMethodException ignore) {
                // classe existe mas não tem getCurrentUser -> tenta próximo candidato
            } catch (Throwable t) {
                // classe existe mas não pôde ser usada (dependência faltando etc.)
                // -> tenta próximo candidato; se nenhum funcionar, o fallback dev é inócuo
            }
        }
        return new ResultadoPortal(null, false);
    }

    private static UsuarioAtual extrair(Object usuario) {
        String login = primeiroTexto(usuario, "getLogin", "getUserName", "getUsername", "getEmail");
        String nome = primeiroTexto(usuario, "getName", "getNome", "getNomeCompleto", "getFullName");
        boolean ativo = primeiroBooleano(usuario, true, "isActive", "isAtivo", "getAtivo");

        if (login == null) {
            login = nome != null ? nome : String.valueOf(usuario);
        }
        return new UsuarioAtual(login, nome != null ? nome : login, ativo, "fusion");
    }

    private static String primeiroTexto(Object alvo, String... metodos) {
        for (String nome : metodos) {
            try {
                Method m = alvo.getClass().getMethod(nome);
                Object v = m.invoke(alvo);
                if (v != null && !String.valueOf(v).isBlank()) {
                    return String.valueOf(v).trim();
                }
            } catch (Throwable ignore) {
                // método não existe neste tipo de usuário -> próximo
            }
        }
        return null;
    }

    private static boolean primeiroBooleano(Object alvo, boolean padrao, String... metodos) {
        for (String nome : metodos) {
            try {
                Method m = alvo.getClass().getMethod(nome);
                Object v = m.invoke(alvo);
                if (v instanceof Boolean b) {
                    return b;
                }
            } catch (Throwable ignore) {
                // método não existe -> próximo
            }
        }
        return padrao;
    }
}
