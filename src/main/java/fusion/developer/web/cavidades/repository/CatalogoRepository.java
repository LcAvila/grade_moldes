package fusion.developer.web.cavidades.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fusion.developer.web.cavidades.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Leitura e escrita dos catalogos que antes viviam em config/*.json. */
public class CatalogoRepository {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAPA = new TypeReference<>() {};

    // ---- Moldes -------------------------------------------------------------

    public List<Map<String, Object>> listarMoldes() throws Exception {
        try (Connection c = Database.abrir()) {
            Map<String, List<String>> cavidades = carregarTodasCavidades(c);
            List<Map<String, Object>> saida = new ArrayList<>();
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT * FROM moldes ORDER BY codigo COLLATE NOCASE");
                 ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    saida.add(montarMolde(r, cavidades.get(r.getString("id"))));
                }
            }
            return saida;
        }
    }

    public Optional<Map<String, Object>> buscarMoldePorCodigo(String codigo) throws Exception {
        try (Connection c = Database.abrir();
             PreparedStatement p = c.prepareStatement(
                     "SELECT * FROM moldes WHERE codigo = ? COLLATE NOCASE")) {
            p.setString(1, codigo == null ? "" : codigo.trim());
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) {
                    return Optional.empty();
                }
                String id = r.getString("id");
                return Optional.of(montarMolde(r, carregarCavidades(c, id)));
            }
        }
    }

    private static Map<String, Object> montarMolde(ResultSet r, List<String> cavidades) throws Exception {
        Map<String, Object> molde = new LinkedHashMap<>();
        molde.put("id", r.getString("id"));
        molde.put("codigo", r.getString("codigo"));
        molde.put("nomePeca", r.getString("nome_peca"));
        colocarSePreenchido(molde, "familia", r.getString("familia"));
        colocarSePreenchido(molde, "revisaoCadastro", r.getString("revisao_cadastro"));
        molde.put("cavidades", cavidades == null ? List.of() : cavidades);
        colocarSePreenchido(molde, "layoutId", r.getString("layout_id"));
        colocarSePreenchido(molde, "desenhoId", r.getString("desenho_id"));
        colocarSePreenchido(molde, "modelo3dId", r.getString("modelo3d_id"));
        molde.put("padroes", lerMapa(r.getString("padroes_json")));
        colocarSePreenchido(molde, "origem", r.getString("origem"));
        return molde;
    }

    private static Map<String, List<String>> carregarTodasCavidades(Connection c) throws Exception {
        Map<String, List<String>> mapa = new LinkedHashMap<>();
        try (PreparedStatement p = c.prepareStatement(
                "SELECT molde_id, numero FROM molde_cavidades ORDER BY molde_id, ordem");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                mapa.computeIfAbsent(r.getString("molde_id"), k -> new ArrayList<>())
                        .add(r.getString("numero"));
            }
        }
        return mapa;
    }

    private static List<String> carregarCavidades(Connection c, String moldeId) throws Exception {
        List<String> lista = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement(
                "SELECT numero FROM molde_cavidades WHERE molde_id = ? ORDER BY ordem")) {
            p.setString(1, moldeId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    lista.add(r.getString("numero"));
                }
            }
        }
        return lista;
    }

    // ---- Desenhos / formas / maquinas / modelos 3D --------------------------

    public List<Map<String, Object>> listarDesenhos() throws Exception {
        return listarDados("SELECT dados_json FROM desenhos ORDER BY id");
    }

    public List<Map<String, Object>> listarFormas() throws Exception {
        return listarDados("SELECT dados_json FROM formas ORDER BY id");
    }

    public List<Map<String, Object>> listarMaquinas() throws Exception {
        return listarDados("SELECT dados_json FROM maquinas ORDER BY nome COLLATE NOCASE");
    }

    public List<Map<String, Object>> listarModelos3d() throws Exception {
        return listarDados("SELECT dados_json FROM modelos3d ORDER BY id");
    }

    private static List<Map<String, Object>> listarDados(String sql) throws Exception {
        List<Map<String, Object>> saida = new ArrayList<>();
        try (Connection c = Database.abrir();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                saida.add(lerMapa(r.getString(1)));
            }
        }
        return saida;
    }

    // ---- Layouts ------------------------------------------------------------

    public Optional<Map<String, Object>> carregarLayout(String id) throws Exception {
        try (Connection c = Database.abrir();
             PreparedStatement p = c.prepareStatement("SELECT dados_json FROM layouts WHERE id = ?")) {
            p.setString(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? Optional.of(lerMapa(r.getString(1))) : Optional.empty();
            }
        }
    }

    // ---- Escritas (cadastro de molde) ---------------------------------------

    public String salvarMolde(Map<String, Object> molde) throws Exception {
        String codigo = texto(molde.get("codigo"));
        if (codigo == null) {
            throw new IllegalArgumentException("Codigo do molde obrigatorio.");
        }
        try (Connection c = Database.abrir()) {
            c.setAutoCommit(false);
            try {
                String id = buscarIdPorCodigo(c, codigo);
                if (id == null) {
                    id = texto(molde.get("id"));
                }
                if (id == null) {
                    throw new IllegalArgumentException("Id do molde obrigatorio.");
                }

                String sql = """
                        INSERT INTO moldes
                            (id, codigo, nome_peca, familia, revisao_cadastro, layout_id,
                             desenho_id, modelo3d_id, padroes_json, origem, atualizado_em)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
                        ON CONFLICT(id) DO UPDATE SET
                            codigo = excluded.codigo,
                            nome_peca = excluded.nome_peca,
                            familia = excluded.familia,
                            revisao_cadastro = excluded.revisao_cadastro,
                            layout_id = COALESCE(excluded.layout_id, moldes.layout_id),
                            desenho_id = COALESCE(excluded.desenho_id, moldes.desenho_id),
                            modelo3d_id = COALESCE(excluded.modelo3d_id, moldes.modelo3d_id),
                            padroes_json = excluded.padroes_json,
                            origem = excluded.origem,
                            atualizado_em = datetime('now')
                        """;
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    p.setString(1, id);
                    p.setString(2, codigo);
                    p.setString(3, texto(molde.get("nomePeca")));
                    p.setString(4, texto(molde.get("familia")));
                    p.setString(5, texto(molde.get("revisaoCadastro")));
                    p.setString(6, texto(molde.get("layoutId")));
                    p.setString(7, texto(molde.get("desenhoId")));
                    p.setString(8, texto(molde.get("modelo3dId")));
                    p.setString(9, json(molde.get("padroes"), "{}"));
                    p.setString(10, texto(molde.get("origem")));
                    p.executeUpdate();
                }

                try (PreparedStatement d = c.prepareStatement(
                        "DELETE FROM molde_cavidades WHERE molde_id = ?")) {
                    d.setString(1, id);
                    d.executeUpdate();
                }
                try (PreparedStatement i = c.prepareStatement(
                        "INSERT INTO molde_cavidades (molde_id, numero, ordem) VALUES (?, ?, ?)")) {
                    int ordem = 0;
                    for (Object cavidade : lista(molde.get("cavidades"))) {
                        i.setString(1, id);
                        i.setString(2, String.valueOf(cavidade));
                        i.setInt(3, ordem++);
                        i.addBatch();
                    }
                    i.executeBatch();
                }

                c.commit();
                return id;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void salvarDesenho(Map<String, Object> desenho) throws Exception {
        String id = texto(desenho.get("id"));
        if (id == null) {
            throw new IllegalArgumentException("Id do desenho obrigatorio.");
        }
        String sql = """
                INSERT INTO desenhos
                    (id, molde_id, papel, revisao, imagem, documento, origem,
                     observacao, dados_json)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    molde_id = excluded.molde_id,
                    papel = excluded.papel,
                    revisao = excluded.revisao,
                    imagem = excluded.imagem,
                    documento = excluded.documento,
                    origem = excluded.origem,
                    observacao = excluded.observacao,
                    dados_json = excluded.dados_json
                """;
        try (Connection c = Database.abrir();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, id);
            p.setString(2, texto(desenho.get("moldeId")));
            p.setString(3, texto(desenho.get("papel") != null ? desenho.get("papel") : desenho.get("tipo")));
            p.setString(4, texto(desenho.get("revisao")));
            p.setString(5, texto(desenho.get("imagem")));
            p.setString(6, texto(desenho.get("documento")));
            p.setString(7, texto(desenho.get("origem")));
            p.setString(8, texto(desenho.get("observacao")));
            p.setString(9, json(desenho, "{}"));
            p.executeUpdate();
        }
    }

    public void salvarModelo3d(Map<String, Object> modelo) throws Exception {
        String id = texto(modelo.get("id"));
        if (id == null) {
            throw new IllegalArgumentException("Id do modelo 3D obrigatorio.");
        }
        String sql = """
                INSERT INTO modelos3d
                    (id, arquivo, nome, revisao, codigo_tecnico, documento_original,
                     fallback_procedural, dados_json)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    arquivo = excluded.arquivo,
                    nome = excluded.nome,
                    revisao = excluded.revisao,
                    codigo_tecnico = excluded.codigo_tecnico,
                    documento_original = excluded.documento_original,
                    fallback_procedural = excluded.fallback_procedural,
                    dados_json = excluded.dados_json
                """;
        try (Connection c = Database.abrir();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, id);
            p.setString(2, texto(modelo.get("arquivo")));
            p.setString(3, texto(modelo.get("nome")));
            p.setString(4, texto(modelo.get("revisao")));
            p.setString(5, texto(modelo.get("codigoTecnico")));
            p.setString(6, texto(modelo.get("documentoOriginal")));
            p.setInt(7, Boolean.TRUE.equals(modelo.get("fallbackProcedural")) ? 1 : 0);
            p.setString(8, json(modelo, "{}"));
            p.executeUpdate();
        }
    }

    private static String buscarIdPorCodigo(Connection c, String codigo) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT id FROM moldes WHERE codigo = ? COLLATE NOCASE")) {
            p.setString(1, codigo);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getString(1) : null;
            }
        }
    }

    // ---- Helpers ------------------------------------------------------------

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

    private static String json(Object valor, String padrao) {
        if (valor == null) {
            return padrao;
        }
        try {
            return JSON.writeValueAsString(valor);
        } catch (Exception e) {
            return padrao;
        }
    }

    private static List<?> lista(Object valor) {
        return valor instanceof List<?> l ? l : List.of();
    }

    private static String texto(Object valor) {
        if (valor == null) {
            return null;
        }
        String texto = String.valueOf(valor).trim();
        return texto.isEmpty() ? null : texto;
    }

    private static void colocarSePreenchido(Map<String, Object> mapa, String chave, String valor) {
        if (valor != null && !valor.isBlank()) {
            mapa.put(chave, valor);
        }
    }
}
