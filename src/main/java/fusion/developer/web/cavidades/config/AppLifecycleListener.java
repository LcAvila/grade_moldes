package fusion.developer.web.cavidades.config;

import fusion.developer.web.cavidades.db.Database;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Garante que o banco SQLite exista antes de qualquer servlet ou filtro. */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Database.inicializar();
        event.getServletContext().log("SQLite: " + Database.caminho().toAbsolutePath());
    }
}
