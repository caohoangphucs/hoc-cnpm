package vn.id.caohoangphuc.cnpm.api;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Sends real HTTP requests through the seven use-case controllers and PostgreSQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemainingUseCasesApiBlackBoxIT {
    private static final String PASSWORD = "123456";
    private static final String STUDENT = "sv001";

    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    private record ApiSession(HttpClient client, String csrfToken) {}

    @BeforeEach
    void clearSeededAuthenticatorSecrets() {
        jdbc.update("UPDATE tai_khoan SET BiMatTOTP=NULL WHERE TenDangNhap IN ('admin', 'ketoan1')");
    }

    @AfterEach
    void clearSeededAuthenticatorSecretsAfterTest() {
        jdbc.update("UPDATE tai_khoan SET BiMatTOTP=NULL WHERE TenDangNhap IN ('admin', 'ketoan1')");
        jdbc.update("DELETE FROM svc_repair_files WHERE repair_id IN"
                + " (SELECT MaYeuCau FROM yeu_cau_sua_chua WHERE TieuDe='API test light')");
        jdbc.update("DELETE FROM svc_repair_history WHERE repair_id IN"
                + " (SELECT MaYeuCau FROM yeu_cau_sua_chua WHERE TieuDe='API test light')");
        jdbc.update("DELETE FROM yeu_cau_sua_chua WHERE TieuDe='API test light'");
        jdbc.update("DELETE FROM svc_invoice_draft WHERE actor_id=2 AND month='2026-10'");
    }

    @Test
    void uc12AdministratorDecisionRouteReachesRegistrationService() throws Exception {
        var admin = enrolledLogin("admin");
        var response = post(admin, ApiRoutes.BOOKING_DECISION.replace("{id}", "2147483647"),
                "{\"decision\":\"DUYET\",\"reason\":null,\"version\":1}");

        assertEquals(404, response.statusCode());
        assertEquals("NOT_FOUND", json.readTree(response.body()).path("code").asString());
    }

    @Test
    void uc17AccountingCanPreviewAnInvoiceDraftOverHttp() throws Exception {
        var accountant = enrolledLogin("ketoan1");
        var response = post(accountant, ApiRoutes.INVOICE_DRAFTS,
                "{\"month\":\"2026-10\",\"roomIds\":[\"A-101\"]}");

        assertEquals(200, response.statusCode(), response.body());
        assertEquals("2026-10", json.readTree(response.body()).path("month").asString());
        assertEquals(1, json.readTree(response.body()).path("rooms").size());
    }

    @Test
    void uc08PaymentRouteReportsWhenNoProviderAdapterIsConfigured() throws Exception {
        var student = login(STUDENT);
        var response = post(student, ApiRoutes.PAYMENT_SESSIONS,
                "{\"invoiceIds\":[1],\"gateway\":\"VNPAY\"}");

        assertEquals(503, response.statusCode());
        assertEquals("PAYMENT_GATEWAY_UNAVAILABLE", json.readTree(response.body()).path("code").asString());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM svc_payment_session", Integer.class));
    }

    @Test
    void uc22StudentCanCreateARepairRequestOverHttp() throws Exception {
        var student = login(STUDENT);
        var response = post(student, ApiRoutes.REPAIRS,
                "{\"title\":\"API test light\",\"description\":\"Light is not working\","
                        + "\"priority\":\"TRUNG_BINH\",\"fileIds\":[]}");

        assertEquals(200, response.statusCode(), response.body());
        var result = json.readTree(response.body());
        assertEquals("MOI", result.path("status").asString());
        int repairId = Integer.parseInt(result.path("id").asString());
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM yeu_cau_sua_chua WHERE MaYeuCau=? AND MaSinhVien='21110001'",
                Integer.class, repairId));
    }

    @Test
    void uc25GuardEntryRejectsAnUnknownCardOverHttp() throws Exception {
        var guard = login("baove1");
        var response = post(guard, ApiRoutes.ENTRIES,
                "{\"eventId\":\"" + UUID.randomUUID() + "\",\"cardToken\":\"unknown-card\","
                        + "\"type\":\"VAO\",\"deviceId\":\"API-TEST-GATE\",\"note\":null}");

        assertEquals(409, response.statusCode());
        assertEquals("CONFLICT", json.readTree(response.body()).path("code").asString());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM svc_gate_event WHERE device_id='API-TEST-GATE'",
                Integer.class));
    }

    @Test
    void uc14AdministratorCanReadThePendingViolationQueueOverHttp() throws Exception {
        var admin = enrolledLogin("admin");
        var response = send(admin.client(), HttpRequest.newBuilder(uri(ApiRoutes.VIOLATIONS + "?page=1&size=10"))
                .GET().build());

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(json.readTree(response.body()).isArray());
        assertFalse(json.readTree(response.body()).isEmpty());
    }

    @Test
    void uc30StudentCanReadActivitiesOverHttp() throws Exception {
        var student = login(STUDENT);
        var response = send(student.client(), HttpRequest.newBuilder(uri(ApiRoutes.ACTIVITIES + "?page=1&size=10"))
                .GET().build());

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(json.readTree(response.body()).isArray());
        assertTrue(json.readTree(response.body()).size() >= 3);
    }

    private ApiSession enrolledLogin(String username) throws Exception {
        var session = login(username);
        var setup = post(session, ApiRoutes.AUTH_TWO_FACTOR_SETUP, "");
        assertEquals(200, setup.statusCode(), setup.body());
        String secret = json.readTree(setup.body()).path("secret").asString();
        var confirmed = post(session, ApiRoutes.AUTH_TWO_FACTOR_CONFIRM,
                "{\"code\":\"" + currentTotp(secret) + "\"}");
        assertEquals(200, confirmed.statusCode(), confirmed.body());
        return new ApiSession(session.client(), json.readTree(confirmed.body()).path("csrfToken").asString());
    }

    private ApiSession login(String username) throws Exception {
        var cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        var client = HttpClient.newBuilder().cookieHandler(cookies).connectTimeout(Duration.ofSeconds(5)).build();
        var anonymous = send(client, HttpRequest.newBuilder(uri(ApiRoutes.AUTH_SESSION)).GET().build());
        var csrf = json.readTree(anonymous.body()).path("csrfToken").asString();
        var response = send(client, HttpRequest.newBuilder(uri(ApiRoutes.AUTH_LOGIN))
                .header("Content-Type", "application/json")
                .header("X-CSRF-TOKEN", csrf)
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .build());
        assertEquals(200, response.statusCode(), response.body());
        var body = json.readTree(response.body());
        return new ApiSession(client, body.path("csrfToken").asString());
    }

    private HttpResponse<String> post(ApiSession session, String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .header("X-CSRF-TOKEN", session.csrfToken())
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Request-Id", UUID.randomUUID().toString());
        return send(session.client(), builder.POST(HttpRequest.BodyPublishers.ofString(body)).build());
    }

    private HttpResponse<String> send(HttpClient client, HttpRequest request) throws Exception {
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }

    private String currentTotp(String secret) throws Exception {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        byte[] key = new byte[secret.length() * 5 / 8];
        int buffer = 0;
        int bits = 0;
        int index = 0;
        for (char value : secret.toCharArray()) {
            buffer = (buffer << 5) | alphabet.indexOf(value);
            bits += 5;
            if (bits >= 8) key[index++] = (byte) (buffer >> (bits -= 8));
        }
        long counter = System.currentTimeMillis() / 1000 / 30;
        var mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(counter).array());
        int offset = hash[hash.length - 1] & 0x0f;
        int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
        return String.format("%06d", binary % 1_000_000);
    }
}
