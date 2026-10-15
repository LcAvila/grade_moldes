package fusion.developer.web.cavidades.config;

import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final Properties P = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) P.load(in);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private AppConfig() {}

    public static String get(String key) {
        String env = System.getenv(key.toUpperCase().replace('.', '_'));
        return env != null && !env.isBlank() ? env : P.getProperty(key);
    }

    public static String required(String key) {
        String v = get(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Configuração não encontrada: " + key);
        }
        return v;
    }
}