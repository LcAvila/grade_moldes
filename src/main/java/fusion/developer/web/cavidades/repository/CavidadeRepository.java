package fusion.developer.web.cavidades.repository;

import fusion.developer.web.cavidades.model.Cavidade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

public class CavidadeRepository {

    public void salvarLote(int moldeId, List<Cavidade> lista) throws Exception {
        String sql = "INSERT INTO cavidades(molde_id, numero, zona, desvio, desvio_texto, data_registro, vedado, controlador) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE zona=VALUES(zona), desvio=VALUES(desvio), desvio_texto=VALUES(desvio_texto), "
                   + "data_registro=VALUES(data_registro), vedado=VALUES(vedado), controlador=VALUES(controlador)";

        try (Connection c = ConexaoMySQL.abrir();
             PreparedStatement p = c.prepareStatement(sql)) {

            c.setAutoCommit(false);
            for (Cavidade v : lista) {
                p.setInt(1, moldeId);
                p.setInt(2, v.numero);
                if (v.zona == null) p.setNull(3, Types.INTEGER); else p.setInt(3, v.zona);
                p.setString(4, v.desvio);
                p.setString(5, v.desvioTexto);
                if (v.data == null) p.setNull(6, Types.DATE);
                else p.setDate(6, java.sql.Date.valueOf(v.data));
                p.setString(7, "S".equals(v.vedado) ? "S" : "N");
                p.setString(8, v.controlador);
                p.addBatch();
            }
            p.executeBatch();
            c.commit();
        }
    }
}