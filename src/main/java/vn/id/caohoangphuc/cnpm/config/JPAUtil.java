package vn.id.caohoangphuc.cnpm.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class JPAUtil {
    private static final EntityManagerFactory emf;

    static {
        try {
            String host = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "127.0.0.1";
            String port = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "5432";
            String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "cnpm_db";
            String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "cnpm_user";
            String pass = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : "RootPassword2026!";

            Map<String, Object> overrides = new HashMap<>();
            overrides.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://" + host + ":" + port + "/" + dbName);
            overrides.put("jakarta.persistence.jdbc.user", user);
            overrides.put("jakarta.persistence.jdbc.password", pass);

            emf = Persistence.createEntityManagerFactory("CNPMPersistenceUnit", overrides);
        } catch (Throwable ex) {
            System.err.println("Initial EntityManagerFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
