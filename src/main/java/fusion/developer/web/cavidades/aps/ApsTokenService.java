package fusion.developer.web.cavidades.aps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * OAuth 2.0 "two-legged" do APS.
 *
 * Existem dois tokens com escopos diferentes:
 *   - servidor: upload/tradução (NUNCA vai para o navegador);
 *   - viewer:   somente leitura de viewables (vai para o navegador).
 */
public final class ApsTokenService {

    private static final String URL_TOKEN =
            "https://developer.api.autodesk.com/authentication/v2/token";
    private static final String ESCOPO_SERVIDOR =
            "data:read data:write bucket:create bucket:read viewables:read";
    private static final String ESCOPO_VIEWER = "viewables:read";
    private static final ObjectMapper JSON = new ObjectMapper();

    private static Token tokenServidor;
    private static Token tokenViewer;

    private ApsTokenService() {
    }

    /** Token com permissão de escrita, usado apenas no backend. */
    public static synchronized String paraServidor() {
        tokenServidor = renovarSePreciso(tokenServidor, ESCOPO_SERVIDOR);
        return tokenServidor.valor();
    }

    /** Token de leitura entregue ao navegador para inicializar o Viewer. */
    public static synchronized Token paraViewer() {
        tokenViewer = renovarSePreciso(tokenViewer, ESCOPO_VIEWER);
        return tokenViewer;
    }

    private static Token renovarSePreciso(Token atual, String escopo) {
        long agora = System.currentTimeMillis();
        if (atual != null && atual.expiraEm() > agora + 60_000L) {
            return atual;
        }
        return requisitar(escopo);
    }

    private static Token requisitar(String escopo) {
        if (!ApsConfig.configurado()) {
            throw new IllegalStateException(
                    "APS não configurado: defina app.aps.client-id e app.aps.client-secret.");
        }

        String credenciais = ApsConfig.clientId() + ":" + ApsConfig.clientSecret();
        String basic = Base64.getEncoder()
                .encodeToString(credenciais.getBytes(StandardCharsets.UTF_8));

        String formulario = "grant_type=client_credentials&scope="
                + URLEncoder.encode(escopo, StandardCharsets.UTF_8);

        HttpRequest pedido = HttpRequest.newBuilder(URI.create(URL_TOKEN))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formulario, StandardCharsets.UTF_8))
                .build();

        ApsHttp.Resposta resposta = ApsHttp.enviar(pedido);
        if (!resposta.ok()) {
            throw new IllegalStateException(
                    "Falha ao obter token do APS (" + ApsHttp.resumo(resposta) + ")");
        }

        JsonNode corpo;
        try {
            corpo = JSON.readTree(resposta.corpo());
        } catch (Exception e) {
            throw new IllegalStateException("Resposta inválida do token APS: " + e.getMessage(), e);
        }

        String valor = corpo.path("access_token").asText(null);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("APS não devolveu access_token.");
        }

        long expiraEm = System.currentTimeMillis()
                + Math.max(60, corpo.path("expires_in").asInt(3600)) * 1000L;
        return new Token(valor, expiraEm);
    }

    public record Token(String valor, long expiraEm) {
        public long segundosRestantes() {
            return Math.max(0, (expiraEm - System.currentTimeMillis()) / 1000L);
        }
    }
}
