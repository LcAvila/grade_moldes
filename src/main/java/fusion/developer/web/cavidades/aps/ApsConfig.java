package fusion.developer.web.cavidades.aps;

import fusion.developer.web.cavidades.config.AppConfig;

import java.util.Locale;

/**
 * Configuração da integração com o Autodesk Platform Services.
 *
 * Chaves em application.properties (ou variáveis de ambiente equivalentes):
 *   app.aps.client-id       / APP_APS_CLIENT_ID
 *   app.aps.client-secret   / APP_APS_CLIENT_SECRET
 *   app.aps.bucket          / APP_APS_BUCKET          (opcional)
 *   app.aps.viewer-version  / APP_APS_VIEWER_VERSION  (opcional, padrão 7.100.0)
 *   app.aps.env             / APP_APS_ENV             (opcional, padrão AutodeskProduction2)
 *   app.aps.api             / APP_APS_API             (opcional, padrão streamingV2)
 *   app.aps.cache-file      / APP_APS_CACHE_FILE      (opcional)
 */
public final class ApsConfig {

    private ApsConfig() {
    }

    public static String clientId() {
        return limpo(AppConfig.get("app.aps.client-id"));
    }

    public static String clientSecret() {
        return limpo(AppConfig.get("app.aps.client-secret"));
    }

    /**
     * Bucket do OSS. Se não configurado, deriva um nome globalmente único a
     * partir do Client ID (o bucket precisa ser único entre TODOS os apps APS).
     */
    public static String bucket() {
        String configurado = limpo(AppConfig.get("app.aps.bucket"));
        if (configurado != null) {
            return configurado.toLowerCase(Locale.ROOT);
        }
        String id = clientId();
        if (id == null) {
            return null;
        }
        return "customfusion-" + ApsUtil.sha256Hex(id).substring(0, 12);
    }

    public static String viewerVersion() {
        String v = limpo(AppConfig.get("app.aps.viewer-version"));
        return v != null ? v : "7.100.0";
    }

    public static String ambiente() {
        String v = limpo(AppConfig.get("app.aps.env"));
        return v != null ? v : "AutodeskProduction2";
    }

    public static String api() {
        String v = limpo(AppConfig.get("app.aps.api"));
        return v != null ? v : "streamingV2";
    }

    public static String cacheFile() {
        return limpo(AppConfig.get("app.aps.cache-file"));
    }

    public static boolean configurado() {
        return clientId() != null && clientSecret() != null;
    }

    private static String limpo(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
