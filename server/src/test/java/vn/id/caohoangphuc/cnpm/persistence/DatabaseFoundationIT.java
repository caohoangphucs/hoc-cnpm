package vn.id.caohoangphuc.cnpm.persistence;

import jakarta.persistence.EntityManagerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.id.caohoangphuc.cnpm.KtxApplication;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentialsRepository;
import vn.id.caohoangphuc.cnpm.persistence.read.ReadRepository;

import static org.junit.jupiter.api.Assertions.*;

/** Runs only against a freshly prepared disposable PostgreSQL 16 database. */
@SpringBootTest(classes = KtxApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DatabaseFoundationIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired AccountCredentialsRepository credentials;
    @Autowired PasswordEncoder encoder;
    @Autowired ApplicationContext context;
    @Autowired EntityManagerFactory entityManagerFactory;
    @LocalServerPort int port;

    @Test
    void swaggerUiServesTheHandAuthoredOpenApiContract() throws Exception {
        var client = HttpClient.newHttpClient();
        var ui = client.send(HttpRequest.newBuilder(uri("/swagger-ui.html")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(302, ui.statusCode());

        var spec = client.send(HttpRequest.newBuilder(uri("/openapi.yaml")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, spec.statusCode());
        assertTrue(spec.body().contains("/api/v1/rooms"));

        var config = client.send(HttpRequest.newBuilder(uri("/v3/api-docs/swagger-config")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, config.statusCode());
        assertTrue(config.body().contains("/openapi.yaml"));
    }

    private URI uri(String path) { return URI.create("http://127.0.0.1:" + port + path); }

    @Test
    void schemaHasNoStoredProceduresOrUspFunctions() {
        assertEquals(27, entityManagerFactory.getMetamodel().getEntities().size());
        assertEquals(27, context.getBeansOfType(ReadRepository.class).size());
        assertEquals(27, jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables"
                        + " WHERE table_schema='public' AND table_type='BASE TABLE'"
                        + " AND table_name NOT LIKE 'svc_%'",
                Integer.class));
        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.routines"
                        + " WHERE specific_schema='public' AND routine_name LIKE 'usp_%'",
                Integer.class));
    }

    @Test
    void databaseConnectionUsesVietnamTimezone() {
        assertEquals("Asia/Ho_Chi_Minh", jdbc.queryForObject("SHOW TimeZone", String.class));
    }

    @Test
    void loginRepositoryUsesDirectSqlAndBcryptForEveryRole() {
        var roles = Map.of(
                "admin", "ADMIN",
                "sv001", "SINHVIEN",
                "ketoan1", "KETOAN",
                "kythuat1", "KYTHUAT",
                "baove1", "BAOVE");

        roles.forEach((username, role) -> {
            var user = credentials.findByUsername(username).orElseThrow();
            assertEquals(role, user.role());
            assertTrue(encoder.matches("123456", user.passwordHash()));
            assertFalse(encoder.matches("admin123", user.passwordHash()));
            assertFalse(encoder.matches("ci-wrong", user.passwordHash()));
        });

        assertTrue(credentials.findByUsername("ci-missing").isEmpty());
        assertTrue(credentials.findByUsername("admin' OR '1'='1").isEmpty());
        assertFalse(encoder.matches("123456", "broken-hash"));
    }
}
