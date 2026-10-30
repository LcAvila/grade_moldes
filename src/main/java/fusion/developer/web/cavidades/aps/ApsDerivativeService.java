package fusion.developer.web.cavidades.aps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Model Derivative: dispara e acompanha a tradução do STL para SVF2. */
final class ApsDerivativeService {

    private static final String BASE = "https://developer.api.autodesk.com/modelderivative/v2";
    private static final ObjectMapper JSON = new ObjectMapper();

    private ApsDerivativeService() {
    }

    static void iniciar(String urn) {
        String token = ApsTokenService.paraServidor();
        String corpo = "{\"input\":{\"urn\":\"" + urn + "\"},"
                + "\"output\":{\"formats\":[{\"type\":\"svf2\",\"views\":[\"3d\"]}]}}";

        ApsHttp.Resposta resposta = ApsHttp.enviar(
                HttpRequest.newBuilder(URI.create(BASE + "/designdata/job"))
                        .timeout(Duration.ofSeconds(60))
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8))
                        .build());

        // 409 = job já existente para este URN; pode seguir consultando o manifesto.
        if (!resposta.ok() && resposta.status() != 409) {
            throw new IllegalStateException(
                    "Falha ao iniciar tradução no APS (" + ApsHttp.resumo(resposta) + ")");
        }
    }

    record Manifesto(String status, String progresso, String mensagem) {
    }

    static Manifesto consultar(String urn) {
        String token = ApsTokenService.paraServidor();
        ApsHttp.Resposta resposta = ApsHttp.enviar(
                ApsHttp.get(BASE + "/designdata/" + urn + "/manifest", token).build());

        if (resposta.status() == 404) {
            return new Manifesto("pending", "0%", "Tradução ainda não iniciada.");
        }
        if (!resposta.ok()) {
            throw new IllegalStateException(
                    "Falha ao consultar tradução no APS (" + ApsHttp.resumo(resposta) + ")");
        }

        JsonNode corpo;
        try {
            corpo = JSON.readTree(resposta.corpo());
        } catch (Exception e) {
            throw new IllegalStateException("Manifesto APS inválido: " + e.getMessage(), e);
        }

        String status = corpo.path("status").asText("pending");
        String progresso = corpo.path("progress").asText("0%");

        String mensagem = null;
        JsonNode mensagens = corpo.path("messages");
        if (mensagens.isArray()) {
            for (JsonNode m : mensagens) {
                String texto = m.path("message").asText(null);
                if (texto == null || texto.isBlank()) {
                    continue;
                }
                if ("error".equalsIgnoreCase(m.path("type").asText())) {
                    mensagem = texto;
                    break;
                }
                if (mensagem == null) {
                    mensagem = texto;
                }
            }
        }

        return new Manifesto(status, progresso, mensagem);
    }
}
