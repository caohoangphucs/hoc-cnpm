package vn.id.caohoangphuc.cnpm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBContext {
    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            // Tự động phát hiện môi trường chạy (Docker Container vs Local Host máy thật)
            boolean isRunningInDocker = new java.io.File("/.dockerenv").exists();
            String defaultHost = isRunningInDocker ? "cnpm-db" : "localhost";
            String defaultPort = isRunningInDocker ? "5432" : "5432";

            String host = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : defaultHost;
            String port = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : defaultPort;
            String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "cnpm_db";
            String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "cnpm_user";
            String pass = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : "cnpm_pass_2026";

            config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + dbName);
            config.setUsername(user);
            config.setPassword(pass);
            config.setDriverClassName("org.postgresql.Driver");
            config.setMaximumPoolSize(15);
            config.setMinimumIdle(3);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(10000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("HikariDataSource is not initialized");
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
