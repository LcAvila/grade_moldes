package fusion.developer.web.cavidades.repository;

import fusion.developer.web.cavidades.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexaoMySQL {
    private ConexaoMySQL() {}

    public static Connection abrir() throws SQLException {
        return DriverManager.getConnection(
            AppConfig.required("app.db.url"),
            AppConfig.required("app.db.user"),
            AppConfig.required("app.db.password")
        );
    }
}