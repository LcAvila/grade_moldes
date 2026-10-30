package fusion.developer.web.cavidades.aps;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Object Storage Service (OSS): bucket persistente e upload do STL. */
final class ApsOssService {

    private static final String BASE = "https://developer.api.autodesk.com/oss/v2";

    private ApsOssService() {
    }

    static void garantirBucket(String bucket) {
        String token = ApsTokenService.paraServidor();

        ApsHttp.Resposta detalhes = ApsHttp.enviar(
                ApsHttp.get(BASE + "/buckets/" + bucket + "/details", token).build());
        if (detalhes.ok()) {
            return;
        }
        if (detalhes.status() != 404) {
            throw new IllegalStateException(
                    "Falha ao consultar bucket APS (" + ApsHttp.resumo(detalhes) + ")");
        }

        String corpo = "{\"bucketKey\":\"" + bucket + "\",\"policyKey\":\"persistent\"}";
        ApsHttp.Resposta criado = ApsHttp.enviar(HttpRequest.newBuilder(URI.create(BASE + "/buckets"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8))
                .build());
        if (!criado.ok() && criado.status() != 409) {
            throw new IllegalStateException(
                    "Falha ao criar bucket APS (" + ApsHttp.resumo(criado) + ")");
        }
    }

    static boolean existe(String bucket, String objeto) {
        String token = ApsTokenService.paraServidor();
        ApsHttp.Resposta resposta = ApsHttp.enviar(ApsHttp.get(
                BASE + "/buckets/" + bucket + "/objects/" + objeto + "/details", token).build());
        if (resposta.status() == 404) {
            return false;
        }
        if (!resposta.ok()) {
            throw new IllegalStateException(
                    "Falha ao consultar objeto APS (" + ApsHttp.resumo(resposta) + ")");
        }
        return true;
    }

    static void enviar(String bucket, String objeto, byte[] conteudo) {
        String token = ApsTokenService.paraServidor();
        HttpRequest pedido = HttpRequest.newBuilder(
                        URI.create(BASE + "/buckets/" + bucket + "/objects/" + objeto))
                .timeout(Duration.ofSeconds(90))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/octet-stream")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(conteudo))
                .build();

        ApsHttp.Resposta resposta = ApsHttp.enviar(pedido);
        if (!resposta.ok()) {
            throw new IllegalStateException(
                    "Falha ao enviar modelo ao APS (" + ApsHttp.resumo(resposta) + ")");
        }
    }
}
