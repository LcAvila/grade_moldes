package fusion.developer.web.cavidades.aps;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;

/** Utilitários internos da integração APS (hash, base64url e nomes de objeto). */
final class ApsUtil {

    private ApsUtil() {
    }

    static String sha256Hex(byte[] dados) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(dados);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível.", e);
        }
    }

    static String sha256Hex(String texto) {
        return sha256Hex(texto.getBytes(StandardCharsets.UTF_8));
    }

    /** URN que o Model Derivative e o Viewer esperam (base64url sem padding). */
    static String base64Url(String texto) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Nome de objeto seguro para o OSS: minúsculo, sem espaços/acentos e com
     * hash do conteúdo para que uma alteração no STL gere um URN novo.
     */
    static String nomeObjeto(String arquivo, byte[] conteudo) {
        String nome = arquivo.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "-")
                .replaceAll("-+", "-");

        int ponto = nome.lastIndexOf('.');
        String raiz = ponto > 0 ? nome.substring(0, ponto) : nome;
        String extensao = ponto > 0 ? nome.substring(ponto) : ".stl";
        if (raiz.length() > 60) {
            raiz = raiz.substring(raiz.length() - 60);
        }
        return raiz + "-" + sha256Hex(conteudo).substring(0, 12) + extensao;
    }
}
