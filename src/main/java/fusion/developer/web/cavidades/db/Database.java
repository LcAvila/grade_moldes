package fusion.developer.web.cavidades.db;

import fusion.developer.web.cavidades.config.AppConfig;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ponto unico de acesso ao SQLite.
 *
 * O arquivo fica fora do WAR e e criado a partir de /db/customfusion-seed.db na
 * primeira subida. Padrao por sistema operacional:
 *   Linux: /opt/neomind/fusion/config/customfusion.db
 *   Windows: %USERPROFILE%\.customfusion\customfusion.db
 *
 * app.sqlite.path (ou APP_SQLITE_PATH) sobrescreve o caminho. Se apontar para
 * um diretorio, o arquivo customfusion.db e criado dentro dele. O schema e
 * aplicado de forma idempotente em toda inicializacao, permitindo evoluir o
 * banco sem perder dados.
 */
public final class Database {

    private static final Logger LOG = Logger.getLogger(Database.class.getName());
    private static final String SEED_RESOURCE = "/db/customfusion-seed.db";
    private static final String SCHEMA_RESOURCE = "/db/schema.sql";
    private static final String DB_FILE = "customfusion.db";
    private static final String LINUX_CONFIG_DIR = "/opt/neomind/fusion/config";

    private static final Object LOCK = new Object();
    private static volatile boolean inicializado = false;
    private static Path caminho;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Driver sqlite-jdbc ausente no classpath: " + e.getMessage());
        }
    }

    private Database() {
    }

    public static Path caminho() {
        garantirInicializado();
        return caminho;
    }

    public static Connection abrir() throws SQLException {
        garantirInicializado();
        Connection conexao = DriverManager.getConnection(url());
        try (Statement s = conexao.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            s.execute("PRAGMA busy_timeout = 5000");
        }
        return conexao;
    }

    public static void inicializar() {
        synchronized (LOCK) {
            if (inicializado) {
                return;
            }
            try {
                caminho = resolverCaminho();
                Path pasta = caminho.getParent();
                if (pasta != null) {
                    Files.createDirectories(pasta);
                }
                copiarSeedSeNecessario();
                try (Connection conexao = DriverManager.getConnection(url())) {
                    try (Statement s = conexao.createStatement()) {
                        s.execute("PRAGMA journal_mode = WAL");
                        s.execute("PRAGMA synchronous = NORMAL");
                        s.execute("PRAGMA foreign_keys = ON");
                        s.execute("PRAGMA busy_timeout = 5000");
                    }
                    aplicarSchema(conexao);
                }
                inicializado = true;
                LOG.info("SQLite inicializado em " + caminho.toAbsolutePath());
            } catch (Exception e) {
                throw new IllegalStateException("Falha ao inicializar o SQLite", e);
            }
        }
    }

    private static void garantirInicializado() {
        if (!inicializado) {
            inicializar();
        }
    }

    private static String url() {
        return "jdbc:sqlite:" + caminho.toAbsolutePath().toString().replace('\\', '/');
    }

    private static Path resolverCaminho() {
        String configurado = AppConfig.get("app.sqlite.path");
        if (configurado == null || configurado.isBlank()) {
            return caminhoPadrao();
        }

        Path destino = Paths.get(configurado.trim());
        if (!destino.isAbsolute()) {
            destino = Paths.get(System.getProperty("user.home")).resolve(destino);
        }
        Path nome = destino.getFileName();
        if (Files.isDirectory(destino) || (nome != null && !nome.toString().contains("."))) {
            destino = destino.resolve(DB_FILE);
        }
        return destino.normalize();
    }

    private static Path caminhoPadrao() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("linux")) {
            return Paths.get(LINUX_CONFIG_DIR, DB_FILE).normalize();
        }
        return Paths.get(System.getProperty("user.home"), ".customfusion", DB_FILE).normalize();
    }

    private static void copiarSeedSeNecessario() throws Exception {
        if (Files.exists(caminho)) {
            return;
        }
        try (InputStream seed = Database.class.getResourceAsStream(SEED_RESOURCE)) {
            if (seed == null) {
                LOG.warning("Seed " + SEED_RESOURCE + " nao encontrado; criando banco vazio.");
                return;
            }
            Path temporario = caminho.resolveSibling(caminho.getFileName() + ".tmp");
            Files.copy(seed, temporario, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temporario, caminho, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Banco semente copiado para " + caminho.toAbsolutePath());
        }
    }

    private static void aplicarSchema(Connection conexao) throws Exception {
        String script;
        try (InputStream in = Database.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Schema " + SCHEMA_RESOURCE + " nao encontrado no classpath");
            }
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        try (Statement s = conexao.createStatement()) {
            for (String comando : script.split(";")) {
                String sql = comando.replaceAll("(?m)--.*$", "").trim();
                if (sql.isEmpty()) {
                    continue;
                }
                try {
                    s.execute(sql);
                } catch (SQLException e) {
                    LOG.log(Level.SEVERE, "Falha ao aplicar schema: " + sql, e);
                    throw e;
                }
            }
        }
    }
}
