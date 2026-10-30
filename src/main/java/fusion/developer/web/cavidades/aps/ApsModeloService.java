package fusion.developer.web.cavidades.aps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fusion.developer.web.cavidades.config.AppConfig;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orquestra upload + tradução do STL e guarda o URN resultante.
 *
 * O cache em disco evita reenviar/re-traduzir o mesmo arquivo a cada restart
 * do Tomcat (cada tradução consome créditos do Model Derivative).
 */
public final class ApsModeloService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<String, Registro> CACHE = new ConcurrentHashMap<>();
    private static final Object TRAVA = new Object();
    private static final long INTERVALO_CONSULTA_MS = 4000L;

    static {
        carregarCacheDisco();
    }

    private ApsModeloService() {
    }

    public record Resultado(boolean configurado, String status, String urn, String arquivo,
                            String progresso, String mensagem) {
    }

    private static final class Registro {
        String urn;
        String objeto;
        String status = "pendente";
        String progresso = "0%";
        String mensagem;
        long ultimaConsulta;
    }

    /** Consulta rápida (sem upload) usada pelo front para decidir entre APS e viewer local. */
    public static Resultado verificar(String arquivo) {
        if (!ApsConfig.configurado()) {
            return new Resultado(false, "desconfigurado", null, arquivo, null,
                    "APS não configurado.");
        }
        Registro reg = arquivo == null ? null : CACHE.get(arquivo.trim());
        if (reg == null) {
            return new Resultado(true, "pendente", null, arquivo, "0%",
                    "Modelo ainda não enviado ao APS.");
        }
        return converter(reg, arquivo);
    }

    public static Resultado obter(Path diretorio, String arquivo) {
        if (!ApsConfig.configurado()) {
            return new Resultado(false, "desconfigurado", null, arquivo, null,
                    "APS não configurado.");
        }

        Path base = diretorio.toAbsolutePath().normalize();
        String nome = validarArquivo(base, arquivo);

        synchronized (TRAVA) {
            Registro reg = CACHE.get(nome);
            if (reg == null) {
                reg = new Registro();
                CACHE.put(nome, reg);
            }

            if ("ready".equals(reg.status)) {
                return converter(reg, nome);
            }

            long agora = System.currentTimeMillis();
            if ("processing".equals(reg.status)
                    && agora - reg.ultimaConsulta < INTERVALO_CONSULTA_MS) {
                return converter(reg, nome);
            }

            try {
                if (reg.urn == null) {
                    preparar(base.resolve(nome), nome, reg);
                }

                ApsDerivativeService.Manifesto manifesto =
                        ApsDerivativeService.consultar(reg.urn);
                reg.ultimaConsulta = System.currentTimeMillis();
                reg.progresso = manifesto.progresso();

                String status = manifesto.status() == null ? "pending" : manifesto.status();
                if ("success".equalsIgnoreCase(status)) {
                    reg.status = "ready";
                    reg.mensagem = null;
                    salvarCacheDisco();
                } else if ("failed".equalsIgnoreCase(status)
                        || "timeout".equalsIgnoreCase(status)) {
                    reg.status = "error";
                    reg.mensagem = manifesto.mensagem() != null
                            ? manifesto.mensagem()
                            : "A tradução do modelo falhou no APS.";
                } else {
                    reg.status = "processing";
                    reg.mensagem = manifesto.mensagem();
                }
            } catch (RuntimeException e) {
                reg.status = "error";
                reg.mensagem = e.getMessage();
            }

            return converter(reg, nome);
        }
    }

    private static void preparar(Path origem, String nome, Registro reg) {
        byte[] conteudo;
        try {
            conteudo = Files.readAllBytes(origem);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível ler " + nome + ": " + e.getMessage(), e);
        }

        String bucket = ApsConfig.bucket();
        String objeto = ApsUtil.nomeObjeto(nome, conteudo);

        ApsOssService.garantirBucket(bucket);
        if (!ApsOssService.existe(bucket, objeto)) {
            ApsOssService.enviar(bucket, objeto, conteudo);
        }

        String objectId = "urn:adsk.objects:os.object:" + bucket + "/" + objeto;
        reg.urn = ApsUtil.base64Url(objectId);
        reg.objeto = objeto;
        reg.status = "processing";
        reg.progresso = "0%";
        reg.mensagem = "Modelo enviado; iniciando tradução.";

        ApsDerivativeService.iniciar(reg.urn);
    }

    private static Resultado converter(Registro reg, String arquivo) {
        String status = reg.status;
        if ("pendente".equals(status) || "processing".equals(status)) {
            status = "processing";
        }
        return new Resultado(true, status, reg.urn, arquivo, reg.progresso, reg.mensagem);
    }

    private static String validarArquivo(Path base, String arquivo) {
        if (arquivo == null || arquivo.isBlank()) {
            throw new IllegalArgumentException("Informe o parâmetro 'arquivo'.");
        }
        String nome = arquivo.trim();
        if (nome.contains("/") || nome.contains("\\") || nome.contains("..")) {
            throw new IllegalArgumentException("Nome de arquivo inválido.");
        }
        if (!nome.toLowerCase(Locale.ROOT).endsWith(".stl")) {
            throw new IllegalArgumentException("Somente arquivos .stl são suportados.");
        }

        Path destino = base.resolve(nome).normalize();
        if (!destino.startsWith(base) || !Files.isRegularFile(destino)) {
            throw new IllegalArgumentException("Modelo não encontrado: " + nome);
        }
        return nome;
    }

    private static Path caminhoCache() {
        String configurado = ApsConfig.cacheFile();
        if (configurado != null) {
            return Paths.get(configurado);
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), "customfusion-aps-cache.json");
    }

    private static void carregarCacheDisco() {
        Path origem = caminhoCache();
        if (!Files.isRegularFile(origem)) {
            return;
        }
        try {
            JsonNode raiz = JSON.readTree(Files.readAllBytes(origem));
            Iterator<Map.Entry<String, JsonNode>> campos = raiz.fields();
            while (campos.hasNext()) {
                Map.Entry<String, JsonNode> campo = campos.next();
                String urn = campo.getValue().path("urn").asText(null);
                if (urn == null || urn.isBlank()) {
                    continue;
                }
                Registro reg = new Registro();
                reg.urn = urn;
                reg.objeto = campo.getValue().path("objeto").asText(null);
                reg.status = "ready";
                reg.progresso = "100%";
                CACHE.put(campo.getKey(), reg);
            }
        } catch (Exception e) {
            // Cache é otimização; se estiver corrompido, ignora e recria.
        }
    }

    private static void salvarCacheDisco() {
        ObjectNode raiz = JSON.createObjectNode();
        for (Map.Entry<String, Registro> entrada : CACHE.entrySet()) {
            Registro reg = entrada.getValue();
            if (!"ready".equals(reg.status) || reg.urn == null) {
                continue;
            }
            ObjectNode item = raiz.putObject(entrada.getKey());
            item.put("urn", reg.urn);
            if (reg.objeto != null) {
                item.put("objeto", reg.objeto);
            }
        }

        Path destino = caminhoCache();
        try {
            if (destino.getParent() != null) {
                Files.createDirectories(destino.getParent());
            }
            Path temporario = destino.resolveSibling(destino.getFileName() + ".tmp");
            JSON.writerWithDefaultPrettyPrinter().writeValue(temporario.toFile(), raiz);
            try {
                Files.move(temporario, destino,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            // Cache é otimização; falha de escrita não deve quebrar a requisição.
        }
    }
}
