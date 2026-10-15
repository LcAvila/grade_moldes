package fusion.developer.web.cavidades.repository;

import fusion.developer.web.cavidades.model.Cavidade;
import fusion.developer.web.cavidades.model.Molde;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class MoldeRepository {

    public Optional<Molde> buscarCompletoPorCodigo(String codigo) throws Exception {
        String sm = "SELECT * FROM moldes WHERE codigo = ? AND ativo = 1";
        String sc = "SELECT * FROM cavidades WHERE molde_id = ? ORDER BY numero";

        try (Connection c = ConexaoMySQL.abrir();
             PreparedStatement p = c.prepareStatement(sm)) {

            p.setString(1, codigo);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return Optional.empty();

                Molde m = new Molde();
                m.id = r.getInt("id");
                m.codigo = r.getString("codigo");
                m.produto = r.getString("produto");
                m.modelo = r.getString("modelo");
                m.totalCavidades = r.getInt("total_cavidades");
                m.numZonas = r.getInt("num_zonas");
                m.cavidadesPorZona = r.getInt("cavidades_por_zona");
                m.tipoLayout = r.getString("tipo_layout");
                m.imagemPecaUrl = r.getString("imagem_peca_url");
                m.svgLayoutUrl = r.getString("svg_layout_url");
                m.material = r.getString("material");
                m.maquina = r.getString("maquina");

                try (PreparedStatement q = c.prepareStatement(sc)) {
                    q.setInt(1, m.id);
                    try (ResultSet x = q.executeQuery()) {
                        while (x.next()) {
                            Cavidade v = new Cavidade();
                            v.numero = x.getInt("numero");
                            v.zona = (Integer) x.getObject("zona");
                            v.desvio = x.getString("desvio");
                            v.desvioTexto = x.getString("desvio_texto");
                            if (x.getDate("data_registro") != null) {
                                v.data = x.getDate("data_registro").toLocalDate();
                            }
                            v.vedado = x.getString("vedado");
                            v.controlador = x.getString("controlador");
                            m.cavidades.add(v);
                        }
                    }
                }
                return Optional.of(m);
            }
        }
    }
}