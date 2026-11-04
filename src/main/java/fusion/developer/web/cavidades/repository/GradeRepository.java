package fusion.developer.web.cavidades.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fusion.developer.web.cavidades.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Grades abertas e registros de cavidades (substitui o localStorage). */
public class GradeRepository {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAPA = new TypeReference<>() {};

    public List<Map<String, Object>> listar() throws Exception {
        String sql = """
                SELECT g.id, g.molde_id, g.aberta_em, g.maquina, g.data_entrada,
                       g.dados_grade_json,
                       COUNT(c.cavidade) AS total,
                       COALESCE(SUM(CASE WHEN c.vedado = 'S' THEN 1 ELSE 0 END), 0) AS vedadas,
                       COALESCE(SUM(CASE WHEN c.vedado <> 'S'
                                          AND c.desvio IS NOT NULL
                                          AND c.desvio <> '' THEN 1 ELSE 0 END), 0) AS defeitos
                  FROM grades g
                  LEFT JOIN grade_cavidades c ON c.grade_id = g.id
                 GROUP BY g.id
                 ORDER BY g.aberta_em DESC, g.id DESC
                """;
        List<Map<String, Object>> saida = new ArrayList<>();
        try (Connection c = Database.abrir();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                int total = r.getInt("total");
                int vedadas = r.getInt("vedadas");
                Map<String, Object> grade = new LinkedHashMap<>();
                grade.put("id", r.getString("id"));
                grade.put("moldeId", r.getString("molde_id"));
                grade.put("abertaEm", r.getString("aberta_em"));
                grade.put("maquina", r.getString("maquina"));
                grade.put("dataEntrada", r.getString("data_entrada"));
                grade.put("dadosGrade", lerMapa(r.getString("dados_grade_json")));
                grade.put("total", total);
                grade.put("vedadas", vedadas);
                grade.put("defeitos", r.getInt("defeitos"));
                grade.put("ativas", total - vedadas);
                saida.add(grade);
            }
        }
        return saida;
    }

    public Optional<Map<String, Object>> carregar(String id) throws Exception {
        try (Connection c = Database.abrir()) {
            Map<String, Object> grade = new LinkedHashMap<>();
            try (PreparedStatement p = c.prepareStatement("SELECT * FROM grades WHERE id = ?")) {
                p.setString(1, id);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) {
                        return Optional.empty();
                    }
                    grade.put("versao", 3);
                    grade.put("gradeId", r.getString("id"));
                    grade.put("id", r.getString("id"));
                    grade.put("moldeId", r.getString("molde_id"));
                    grade.put("abertaEm", r.getString("aberta_em"));
                    grade.put("maquina", r.getString("maquina"));
                    grade.put("dataEntrada", r.getString("data_entrada"));
                    grade.put("dadosGrade", lerMapa(r.getString("dados_grade_json")));
                }
            }
            grade.put("dadosCavidades", carregarCavidades(c, id));
            return Optional.of(grade);
        }
    }

    public Map<String, Object> criar(String moldeId, String maquina, String dataEntrada) throws Exception {
        String id = "g" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String abertaEm = Instant.now().toString();
        Map<String, Object> dadosGrade = new LinkedHashMap<>();
        if (maquina != null && !maquina.isBlank()) {
            dadosGrade.put("maquina", maquina.trim());
        }
        if (dataEntrada != null && !dataEntrada.isBlank()) {
            dadosGrade.put("dataEntrada", dataEntrada.trim());
        }

        String sql = """
                INSERT INTO grades
                    (id, molde_id, aberta_em, maquina, data_entrada, dados_grade_json)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection c = Database.abrir();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, id);
            p.setString(2, moldeId);
            p.setString(3, abertaEm);
            p.setString(4, texto(maquina));
            p.setString(5, texto(dataEntrada));
            p.setString(6, json(dadosGrade));
            p.executeUpdate();
        }

        Map<String, Object> grade = new LinkedHashMap<>();
        grade.put("id", id);
        grade.put("moldeId", moldeId);
        grade.put("abertaEm", abertaEm);
        grade.put("maquina", texto(maquina));
        grade.put("dataEntrada", texto(dataEntrada));
        grade.put("dadosGrade", dadosGrade);
        grade.put("total", 0);
        grade.put("vedadas", 0);
        grade.put("defeitos", 0);
        grade.put("ativas", 0);
        return grade;
    }

    public void salvar(String id, String moldeId, Map<String, Object> dadosCavidades,
                       Map<String, Object> dadosGrade) throws Exception {
        Map<String, Object> gradeDados = dadosGrade == null ? new LinkedHashMap<>() : dadosGrade;
        try (Connection c = Database.abrir()) {
            c.setAutoCommit(false);
            try {
                String sql = """
                        UPDATE grades
                           SET molde_id = ?, maquina = ?, data_entrada = ?,
                               dados_grade_json = ?, atualizado_em = datetime('now')
                         WHERE id = ?
                        """;
                int atualizadas;
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    p.setString(1, moldeId);
                    p.setString(2, texto(gradeDados.get("maquina")));
                    p.setString(3, texto(gradeDados.get("dataEntrada")));
                    p.setString(4, json(gradeDados));
                    p.setString(5, id);
                    atualizadas = p.executeUpdate();
                }
                if (atualizadas == 0) {
                    String insert = """
                            INSERT INTO grades
                                (id, molde_id, aberta_em, maquina, data_entrada, dados_grade_json)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """;
                    try (PreparedStatement p = c.prepareStatement(insert)) {
                        p.setString(1, id);
                        p.setString(2, moldeId);
                        p.setString(3, Instant.now().toString());
                        p.setString(4, texto(gradeDados.get("maquina")));
                        p.setString(5, texto(gradeDados.get("dataEntrada")));
                        p.setString(6, json(gradeDados));
                        p.executeUpdate();
                    }
                }
                substituirCavidades(c, id, dadosCavidades);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void remover(String id) throws Exception {
        try (Connection c = Database.abrir();
             PreparedStatement p = c.prepareStatement("DELETE FROM grades WHERE id = ?")) {
            p.setString(1, id);
            p.executeUpdate();
        }
    }

    public Map<String, Object> importar(List<Map<String, Object>> grades) throws Exception {
        int importadas = 0;
        int ignoradas = 0;
        try (Connection c = Database.abrir()) {
            c.setAutoCommit(false);
            try {
                for (Map<String, Object> item : grades) {
                    String moldeId = texto(item.get("moldeId"));
                    if (moldeId == null || !moldeExiste(c, moldeId)) {
                        ignoradas++;
                        continue;
                    }
                    String id = texto(item.get("id"));
                    if (id == null) {
                        id = "g" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                    }
                    String abertaEm = texto(item.get("abertaEm"));
                    if (abertaEm == null) {
                        abertaEm = Instant.now().toString();
                    }
                    Map<String, Object> dadosGrade = mapa(item.get("dadosGrade"));
                    Map<String, Object> dadosCavidades = mapa(item.get("dadosCavidades"));

                    String sql = """
                            INSERT INTO grades
                                (id, molde_id, aberta_em, maquina, data_entrada, dados_grade_json)
                            VALUES (?, ?, ?, ?, ?, ?)
                            ON CONFLICT(id) DO UPDATE SET
                                molde_id = excluded.molde_id,
                                aberta_em = excluded.aberta_em,
                                maquina = excluded.maquina,
                                data_entrada = excluded.data_entrada,
                                dados_grade_json = excluded.dados_grade_json,
                                atualizado_em = datetime('now')
                            """;
                    try (PreparedStatement p = c.prepareStatement(sql)) {
                        p.setString(1, id);
                        p.setString(2, moldeId);
                        p.setString(3, abertaEm);
                        p.setString(4, texto(dadosGrade.get("maquina")));
                        p.setString(5, texto(dadosGrade.get("dataEntrada")));
                        p.setString(6, json(dadosGrade));
                        p.executeUpdate();
                    }
                    substituirCavidades(c, id, dadosCavidades);
                    importadas++;
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("importadas", importadas);
        resultado.put("ignoradas", ignoradas);
        return resultado;
    }

    // ---- Compatibilidade com o endpoint por molde ---------------------------

    public void salvarRegistrosMolde(String codigo, Map<String, Object> dadosCavidades,
                                     Map<String, Object> dadosGrade) throws Exception {
        String sql = """
                INSERT INTO registros_molde
                    (molde_id, dados_cavidades_json, dados_grade_json, atualizado_em)
                VALUES (?, ?, ?, datetime('now'))
                ON CONFLICT(molde_id) DO UPDATE SET
                    dados_cavidades_json = excluded.dados_cavidades_json,
                    dados_grade_json = excluded.dados_grade_json,
                    atualizado_em = datetime('now')
                """;
        try (Connection c = Database.abrir();
             PreparedStatement p = c.prepareStatement(sql)) {
            String moldeId = buscarIdPorCodigo(c, codigo);
            if (moldeId == null) {
                throw new IllegalArgumentException("Molde nao encontrado: " + codigo);
            }
            p.setString(1, moldeId);
            p.setString(2, json(dadosCavidades));
            p.setString(3, json(dadosGrade));
            p.executeUpdate();
        }
    }

    public Optional<Map<String, Object>> carregarRegistrosMolde(String codigo) throws Exception {
        try (Connection c = Database.abrir();
             PreparedStatement p = c.prepareStatement("""
                     SELECT r.dados_cavidades_json, r.dados_grade_json
                       FROM registros_molde r
                       JOIN moldes m ON m.id = r.molde_id
                      WHERE m.codigo = ? COLLATE NOCASE
                     """)) {
            p.setString(1, codigo);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) {
                    return Optional.empty();
                }
                Map<String, Object> pacote = new LinkedHashMap<>();
                pacote.put("dadosCavidades", lerMapa(r.getString("dados_cavidades_json")));
                pacote.put("dadosGrade", lerMapa(r.getString("dados_grade_json")));
                return Optional.of(pacote);
            }
        }
    }

    // ---- Helpers ------------------------------------------------------------

    private static Map<String, Object> carregarCavidades(Connection c, String gradeId) throws Exception {
        Map<String, Object> dados = new LinkedHashMap<>();
        try (PreparedStatement p = c.prepareStatement(
                "SELECT * FROM grade_cavidades WHERE grade_id = ? ORDER BY cavidade")) {
            p.setString(1, gradeId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Map<String, Object> registro = new LinkedHashMap<>();
                    registro.put("desvio", nuloParaVazio(r.getString("desvio")));
                    registro.put("data", nuloParaVazio(r.getString("data")));
                    registro.put("vedado", "S".equals(r.getString("vedado")) ? "S" : "N");
                    registro.put("observacao", nuloParaVazio(r.getString("observacao")));
                    registro.put("controlador", nuloParaVazio(r.getString("controlador")));
                    dados.put(r.getString("cavidade"), registro);
                }
            }
        }
        return dados;
    }

    private static void substituirCavidades(Connection c, String gradeId,
                                            Map<String, Object> dadosCavidades) throws Exception {
        try (PreparedStatement d = c.prepareStatement(
                "DELETE FROM grade_cavidades WHERE grade_id = ?")) {
            d.setString(1, gradeId);
            d.executeUpdate();
        }
        if (dadosCavidades == null || dadosCavidades.isEmpty()) {
            return;
        }
        String sql = """
                INSERT INTO grade_cavidades
                    (grade_id, cavidade, desvio, data, vedado, observacao, controlador)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement p = c.prepareStatement(sql)) {
            for (Map.Entry<String, Object> entrada : dadosCavidades.entrySet()) {
                Map<String, Object> registro = mapa(entrada.getValue());
                p.setString(1, gradeId);
                p.setString(2, entrada.getKey());
                p.setString(3, texto(registro.get("desvio")));
                p.setString(4, texto(registro.get("data")));
                p.setString(5, "S".equals(texto(registro.get("vedado"))) ? "S" : "N");
                p.setString(6, texto(registro.get("observacao")));
                p.setString(7, texto(registro.get("controlador")));
                p.addBatch();
            }
            p.executeBatch();
        }
    }

    private static boolean moldeExiste(Connection c, String moldeId) throws Exception {
        try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM moldes WHERE id = ?")) {
            p.setString(1, moldeId);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    private static String buscarIdPorCodigo(Connection c, String codigo) throws Exception {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT id FROM moldes WHERE codigo = ? COLLATE NOCASE")) {
            p.setString(1, codigo);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getString(1) : null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Object valor) {
        if (valor instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return new LinkedHashMap<>();
    }

    private static Map<String, Object> lerMapa(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return JSON.readValue(json, MAPA);
        } catch (Exception e) {
            throw new IllegalStateException("JSON invalido no banco: " + e.getMessage(), e);
        }
    }

    private static String json(Object valor) {
        try {
            return JSON.writeValueAsString(valor == null ? new LinkedHashMap<>() : valor);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String texto(Object valor) {
        if (valor == null) {
            return null;
        }
        String texto = String.valueOf(valor).trim();
        return texto.isEmpty() ? null : texto;
    }

    private static String nuloParaVazio(String valor) {
        return valor == null ? "" : valor;
    }
}
