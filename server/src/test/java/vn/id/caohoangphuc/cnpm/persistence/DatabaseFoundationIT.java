package vn.id.caohoangphuc.cnpm.persistence;

import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
