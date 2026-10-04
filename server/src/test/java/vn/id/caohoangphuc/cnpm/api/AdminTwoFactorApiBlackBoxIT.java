package vn.id.caohoangphuc.cnpm.api;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Clock;
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
import vn.id.caohoangphuc.cnpm.service.TotpService;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises password login, OTP and protected endpoints over real HTTP with disposable PostgreSQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminTwoFactorApiBlackBoxIT {
    // RFC 4226 test secret, used only in the disposable test database.
    private static final String SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    @Autowired TotpService totp;
    @Autowired Clock clock;
    private String adminSecret;
    private String accountantSecret;

    private record Session(HttpClient client, String csrf, boolean verified) {}

    /** Preserve seed state before installing test authenticator keys. */
    @BeforeEach
    void preserveAuthenticatorKeys() {
        adminSecret = secretFor("admin");
        accountantSecret = secretFor("ketoan1");
        saveSecret("admin", totp.encrypt(SECRET));
    }

    /** Never leave enrollment fixtures in the seed database after a test. */
    @AfterEach
    void restoreAuthenticatorKeys() {
        saveSecret("admin", adminSecret);
        saveSecret("ketoan1", accountantSecret);
    }

    /** Main/negative flow: password alone cannot reach business APIs; only a valid OTP unlocks them. */
    @Test
    void enrolledAdministratorMustVerifyOtpAndEveryNewLoginResetsVerification() throws Exception {
        var session = login("admin");
        assertFalse(session.verified());
        assertFalse(json.readTree(get(session, ApiRoutes.AUTH_SESSION).body()).path("twoFactorVerified").asBoolean());
        assertEquals(403, get(session, ApiRoutes.INVOICES).statusCode());
        assertEquals("TWO_FACTOR_REQUIRED", json.readTree(get(session, ApiRoutes.INVOICES).body()).path("code").asString());

        // Pick a six-digit code that is outside all accepted adjacent time steps.
        String invalid = "000000";
        while (totp.matches(SECRET, invalid, clock.instant().getEpochSecond())) {
            invalid = String.format("%06d", Integer.parseInt(invalid) + 1);
        }
        assertEquals(401, post(session, ApiRoutes.AUTH_TWO_FACTOR, "{\"code\":\"" + invalid + "\"}").statusCode());
        assertEquals(403, get(session, ApiRoutes.INVOICES).statusCode());

        var verified = post(session, ApiRoutes.AUTH_TWO_FACTOR, "{\"code\":\"" + currentCode() + "\"}");
        assertEquals(200, verified.statusCode(), verified.body());
        assertTrue(json.readTree(verified.body()).path("twoFactorVerified").asBoolean());
        assertEquals(200, get(session, ApiRoutes.INVOICES).statusCode());
        assertTrue(json.readTree(get(session, ApiRoutes.AUTH_SESSION).body()).path("twoFactorVerified").asBoolean());

        var relogin = post(session, ApiRoutes.AUTH_LOGIN, "{\"username\":\"admin\",\"password\":\"123456\"}");
        assertEquals(200, relogin.statusCode(), relogin.body());
        assertFalse(json.readTree(relogin.body()).path("twoFactorVerified").asBoolean());
        assertEquals(403, get(session, ApiRoutes.INVOICES).statusCode());
    }

    /** Boundary inputs must not mark the session verified. Pending administrators can still log out. */
    @Test
    void malformedOtpLeavesSessionLockedAndLogoutRemainsAvailable() throws Exception {
        var session = login("admin");
        for (String value : new String[]{"", "12345", "1234567", "abcdef"}) {
            assertEquals(400, post(session, ApiRoutes.AUTH_TWO_FACTOR, "{\"code\":\"" + value + "\"}").statusCode());
            assertEquals(403, get(session, ApiRoutes.INVOICES).statusCode());
        }
        assertEquals(200, post(session, ApiRoutes.AUTH_LOGOUT, "").statusCode());
        assertFalse(json.readTree(get(session, ApiRoutes.AUTH_SESSION).body()).path("authenticated").asBoolean());
        assertEquals(401, get(session, ApiRoutes.INVOICES).statusCode());
    }

    /** Alternative flow: an administrator who has not enrolled can log in and start enrollment. */
    @Test
    void unenrolledAdministratorCanLogInAndBeginQrEnrollment() throws Exception {
        saveSecret("admin", null);
        var session = login("admin");
        assertTrue(session.verified());
        assertEquals(200, get(session, ApiRoutes.INVOICES).statusCode());
        var setup = post(session, ApiRoutes.AUTH_TWO_FACTOR_SETUP, "");
        assertEquals(200, setup.statusCode(), setup.body());
        assertTrue(json.readTree(setup.body()).path("provisioningUri").asString().startsWith("otpauth://totp/"));
        assertNull(secretFor("admin"));
    }

    /** Enrolled accountants also need OTP, while students and unenrolled accountants remain available. */
    @Test
    void enrolledAccountantRequiresOtpButStudentAndUnenrolledAccountantDoNot() throws Exception {
        saveSecret("ketoan1", totp.encrypt(SECRET));
        var accountant = login("ketoan1");
        assertFalse(accountant.verified());
        assertEquals(403, get(accountant, ApiRoutes.INVOICES).statusCode());
        var verified = post(accountant, ApiRoutes.AUTH_TWO_FACTOR, "{\"code\":\"" + currentCode() + "\"}");
        assertEquals(200, verified.statusCode(), verified.body());
        assertTrue(json.readTree(verified.body()).path("twoFactorVerified").asBoolean());
        assertEquals(200, get(accountant, ApiRoutes.INVOICES).statusCode());

        saveSecret("ketoan1", null);
        for (String username : new String[]{"sv001", "ketoan1"}) {
            var session = login(username);
            assertTrue(session.verified());
            assertEquals(200, get(session, ApiRoutes.INVOICES).statusCode());
        }
    }

    /** Every advertised student demo account must accept the documented password over HTTP. */
    @Test
    void allStudentDemoAccountsAcceptDocumentedPassword() throws Exception {
        for (int index = 1; index <= 55; index++) {
            var session = login(String.format("sv%03d", index));
            assertTrue(session.verified());
            assertEquals(200, post(session, ApiRoutes.AUTH_LOGOUT, "").statusCode());
        }
    }

    /** Generate the app code independently with the standard test-secret bytes and current counter. */
    private String currentCode() throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec("12345678901234567890".getBytes(java.nio.charset.StandardCharsets.US_ASCII), "HmacSHA1"));
        byte[] digest = mac.doFinal(ByteBuffer.allocate(8).putLong(clock.instant().getEpochSecond() / 30).array());
        int offset = digest[digest.length - 1] & 15;
        int value = ByteBuffer.wrap(digest, offset, 4).getInt() & 0x7fffffff;
        return String.format("%06d", value % 1_000_000);
    }

    /** Read the encrypted key so teardown can restore the original fixture exactly. */
    private String secretFor(String username) {
        return jdbc.queryForObject("SELECT bimattotp FROM tai_khoan WHERE tendangnhap=?", String.class, username);
    }

    /** Install or restore a key only in the disposable test database. */
    private void saveSecret(String username, String value) {
        jdbc.update("UPDATE tai_khoan SET bimattotp=? WHERE tendangnhap=?", value, username);
    }

    /** Keep cookies across the CSRF bootstrap, password login and second-factor request. */
    private Session login(String username) throws Exception {
        var client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
        var anonymous = client.send(HttpRequest.newBuilder(uri(ApiRoutes.AUTH_SESSION)).GET().build(), HttpResponse.BodyHandlers.ofString());
        var session = new Session(client, json.readTree(anonymous.body()).path("csrfToken").asString(), false);
        var response = post(session, ApiRoutes.AUTH_LOGIN, "{\"username\":\"" + username + "\",\"password\":\"123456\"}");
        assertEquals(200, response.statusCode(), response.body());
        var body = json.readTree(response.body());
        return new Session(client, body.path("csrfToken").asString(), body.path("twoFactorVerified").asBoolean());
    }

    /** Send a protected read request through the actual HTTP server. */
    private HttpResponse<String> get(Session session, String path) throws Exception {
        return session.client().send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    /** Send a mutation with the session's CSRF token, including OTP and logout actions. */
    private HttpResponse<String> post(Session session, String path, String body) throws Exception {
        return session.client().send(HttpRequest.newBuilder(uri(path)).header("Content-Type", "application/json")
                .header("X-CSRF-TOKEN", session.csrf()).POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    /** Resolve paths against the random port assigned to the test application. */
    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }
}
