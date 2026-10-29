package fusion.developer.web.cavidades.seguranca;

/**
 * Usuário identificado na requisição.
 * origem: "container" (sessão do servlet), "fusion" (PortalUtil.getCurrentUser)
 * ou "dev" (app.dev.usuario, apenas desenvolvimento local).
 */
public class UsuarioAtual {
    public final String login;
    public final String nome;
    public final boolean ativo;
    public final String origem;

    public UsuarioAtual(String login, String nome, boolean ativo, String origem) {
        this.login = login;
        this.nome = nome;
        this.ativo = ativo;
        this.origem = origem;
    }
}
