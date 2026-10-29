package fusion.developer.web.cavidades.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fusion.developer.web.cavidades.config.AppConfig;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * Publicação de documentos no GED do Fusion Platform.
 *
 * API (doc: materiais-tecnicos/apis/GED/documentos/publicar):
 *   POST {app.fusion.base-url}/fusion/api/v1/documents
 *   header: Token: {app.fusion.api-token}
 *   multipart: "document" (JSON de metadados) + "file" (binário)
 *   OBRIGATÓRIOS: author (usuário válido no Fusion), docTypeName,
 *                 folder.folderPath e fields.file == nome do arquivo enviado.
 *   Resposta 200: { neoId, code, url }
 *   409 = documento já existente (tratado como idempotência: jaPublicado=true).
 *
 * O Token fica SOMENTE no servidor (nunca no JS do navegador).
 */
public class GedFusionService {

    /** base-url/token/doc-type não configurados no ambiente. */
    public static class ConfiguracaoIncompletaException extends RuntimeException {
        public ConfiguracaoIncompletaException(String mensagem) {
            super(mensagem);
        }
    }

    /** Resposta da API GED preservando o que ela devolve. */
    public record Resultado(String neoId, String code, String url, boolean jaPublicado) {}

    private final ObjectMapper json = new ObjectMapper();

    public Resultado publicar(String codigoMolde, String nomeArquivo, byte[] conteudo, String autor) {
        String base = AppConfig.get("app.fusion.base-url");
        String token = AppConfig.get("app.fusion.api-token");
        String docType = AppConfig.get("app.fusion.doc-type");
        String pastaBase = AppConfig.get("app.fusion.folder-base");
        String status = AppConfig.get("app.fusion.doc-status");

        base = (base == null || base.isBlank()) ? null : base.replaceAll("/+$", "");
        token = (token == null || token.isBlank()) ? null : token.trim();
        docType = (docType == null || docType.isBlank()) ? null : docType.trim();
        pastaBase = (pastaBase == null || pastaBase.isBlank()) ? null : pastaBase.trim();
        status = (status == null || status.isBlank()) ? "published" : status.trim();

        if (autor == null || autor.isBlank()) {
            autor = AppConfig.get("app.fusion.author-padrao");
        }

        StringBuilder faltando = new StringBuilder();
        if (base == null) faltando.append("app.fusion.base-url ");
        if (token == null) faltando.append("app.fusion.api-token ");
        if (docType == null) faltando.append("app.fusion.doc-type ");
        if (pastaBase == null) faltando.append("app.fusion.folder-base ");
        if (autor == null || autor.isBlank()) faltando.append("usuário da sessão (ou app.fusion.author-padrao) ");
        if (faltando.length() > 0) {
            throw new ConfiguracaoIncompletaException(
                    "Publicação no GED pendente de configuração. Faltando: " + faltando.toString().trim());
        }
        if (nomeArquivo == null || nomeArquivo.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do arquivo (fields.file).");
        }
        // nome sem quebras de linha nem aspas (evita quebrar o Content-Disposition)
        nomeArquivo = nomeArquivo.replace("\r", "").replace("\n", "").replace("\"", "").trim();

        try {
            // ---- JSON de metadados -----------------------------------
            ObjectNode documento = json.createObjectNode();
            documento.put("author", autor);
            documento.put("docTypeName", docType);
            documento.put("status", status);

            ObjectNode pasta = documento.putObject("folder");
            String separador = pastaBase.contains("/") ? "/" : "\\";
            pasta.put("folderPath", pastaBase + separador + codigoMolde);

            ObjectNode campos = documento.putObject("fields");
            campos.put("file", nomeArquivo);

            // campos extras exigidos pelo tipo de documento (JSON opcional)
            String extras = AppConfig.get("app.fusion.doc-fields-extra");
            if (extras != null && !extras.isBlank()) {
                JsonNode extra = json.readTree(extras);
                extra.fields().forEachRemaining(e -> campos.set(e.getKey(), e.getValue()));
            }

            ArrayNode tags = documento.putArray("tags");
            tags.add("cavidades");
            tags.add("molde-" + codigoMolde);

            byte[] corpoMultipart = montarMultipart(nomeArquivo, conteudo, documento.toString());

            // ---- POST no GED ----------------------------------------
            String boundary = extrairBoundary(corpoMultipart);
            HttpRequest pedido = HttpRequest.newBuilder(URI.create(base + "/fusion/api/v1/documents"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Token", token)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(corpoMultipart))
                    .build();

            HttpClient cliente = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> resposta =
                    cliente.send(pedido, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            int statusHttp = resposta.statusCode();
            String corpo = resposta.body() != null ? resposta.body() : "";

            if (statusHttp >= 200 && statusHttp < 300) {
                return parseResposta(corpo, false);
            }
            if (statusHttp == 409) {
                // ja existe documento equivalente -> idempotencia
                try {
                    return parseResposta(corpo, true);
                } catch (Exception ignore) {
                    return new Resultado(null, null, null, true);
                }
            }
            throw new IllegalStateException("GED retornou HTTP " + statusHttp + ": " + truncar(corpo));

        } catch (ConfiguracaoIncompletaException | IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicação no GED interrompida.", e);
        } catch (Exception e) {
            String motivo = e.getMessage() != null && !e.getMessage().isBlank()
                    ? e.getMessage() : e.getClass().getName();
            throw new IllegalStateException("Falha ao publicar no GED: " + motivo, e);
        }
    }

    // ------------------------------------------------------------------

    private Resultado parseResposta(String corpo, boolean jaPublicado) throws Exception {
        JsonNode n = json.readTree(corpo);
        String neoId = texto(n, "neoId");
        String code = texto(n, "code");
        String url = texto(n, "url");
        if (url == null) {
            url = texto(n, "link"); // versões antigas devolvem "link"
        }
        return new Resultado(neoId, code, url, jaPublicado);
    }

    private static String texto(JsonNode n, String campo) {
        JsonNode v = n.get(campo);
        return v != null && !v.isNull() ? v.asText() : null;
    }

    private static byte[] montarMultipart(String nomeArquivo, byte[] conteudo, String documentJson) {
        String boundary = "----CustomFusionWeb" + UUID.randomUUID();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"document\"\r\n"
                    + "Content-Type: application/json; charset=UTF-8\r\n\r\n")
                    .getBytes(StandardCharsets.UTF_8));
            out.write(documentJson.getBytes(StandardCharsets.UTF_8));
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));

            out.write(("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"file\"; filename=\"" + nomeArquivo + "\"\r\n"
                    + "Content-Type: application/octet-stream\r\n\r\n")
                    .getBytes(StandardCharsets.UTF_8));
            out.write(conteudo != null ? conteudo : new byte[0]);
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));

            out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao montar multipart: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    private static String extrairBoundary(byte[] corpoMultipart) {
        String inicio = new String(corpoMultipart, 0, Math.min(corpoMultipart.length, 120), StandardCharsets.US_ASCII);
        int fim = inicio.indexOf("\r\n");
        return fim > 2 ? inicio.substring(2, fim) : "";
    }

    private static String truncar(String s) {
        if (s == null) return "";
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }
}
