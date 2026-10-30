package fusion.developer.web.cavidades.aps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Cliente HTTP compartilhado entre os serviços APS. */
final class ApsHttp {

    static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private ApsHttp() {
    }

    record Resposta(int status, String corpo) {
        boolean ok() {
            return status >= 200 && status < 300;
        }
    }

    static Resposta enviar(HttpRequest pedido) {
        try {
            HttpResponse<String> r = CLIENTE.send(pedido,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new Resposta(r.statusCode(), r.body());
        } catch (IOException e) {
            throw new IllegalStateException("Falha de rede ao chamar o APS: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Chamada ao APS interrompida.", e);
        }
    }

    static HttpRequest.Builder get(String url, String token) {
        return HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(45))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET();
    }

    static String resumo(Resposta r) {
        String corpo = r.corpo() == null ? "" : r.corpo().trim();
        if (corpo.length() > 300) {
            corpo = corpo.substring(0, 300) + "...";
        }
        return "HTTP " + r.status() + (corpo.isEmpty() ? "" : " — " + corpo);
    }
}
