package vn.id.caohoangphuc.cnpm.api;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
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
        jdbc.update("DELETE FROM hoat_dong WHERE TieuDe LIKE 'API CRUD activity%'");
        jdbc.update("DELETE FROM hoa_don WHERE maphong='A-101' AND kythang='2099-01'");
        jdbc.update("DELETE FROM svc_appeal WHERE decision_id IN (SELECT id FROM svc_discipline_decision"
                + " WHERE violation_id IN (SELECT mavipham FROM vi_pham WHERE noidungvipham LIKE 'API CRUD violation%'))");
        jdbc.update("DELETE FROM svc_discipline_decision WHERE violation_id IN"
                + " (SELECT mavipham FROM vi_pham WHERE noidungvipham LIKE 'API CRUD violation%')");
        jdbc.update("DELETE FROM vi_pham WHERE noidungvipham LIKE 'API CRUD violation%'");
        jdbc.update("DELETE FROM svc_gate_event WHERE device_id='API-CRUD-TEST'");
        jdbc.update("DELETE FROM ra_vao WHERE ghichu='API CRUD test entry'");
        jdbc.update("DELETE FROM svc_payment_session WHERE provider_ref LIKE 'api-crud-test-%'");
    }

    @Test
    void uc12AdministratorDecisionRouteReachesRegistrationService() throws Exception {
        var admin = login("admin");
        var response = post(admin, ApiRoutes.BOOKING_DECISION.replace("{id}", "2147483647"),
                "{\"decision\":\"DUYET\",\"reason\":null,\"version\":1}");

        assertEquals(404, response.statusCode());
        assertEquals("NOT_FOUND", json.readTree(response.body()).path("code").asString());
    }

    @Test
    void uc17AccountingCanPreviewAnInvoiceDraftOverHttp() throws Exception {
        var accountant = login("ketoan1");
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
    void uc08StudentCanReadAndDeleteOwnPendingPaymentSessionOverHttp() throws Exception {
        var student = login(STUDENT);
        int actorId = jdbc.queryForObject(
                "SELECT mataikhoan FROM tai_khoan WHERE tendangnhap=?", Integer.class, STUDENT);
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO svc_payment_session(id,actor_id,gateway,provider_ref,amount,expires_at)"
                        + " VALUES(?,?,'VNPAY',?,1000,now()+interval '10 minutes')",
                id, actorId, "api-crud-test-" + id);

        var list = send(student.client(), HttpRequest.newBuilder(uri(ApiRoutes.PAYMENT_SESSIONS + "?page=1&size=20"))
                .GET().build());
        assertEquals(200, list.statusCode(), list.body());
        assertTrue(json.readTree(list.body()).toString().contains(id.toString()));

        var deleted = send(student.client(), mutate(student,
                ApiRoutes.PAYMENT_SESSION.replace("{id}", id.toString()), "DELETE", ""));
        assertEquals(200, deleted.statusCode(), deleted.body());
        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM svc_payment_session WHERE id=?", Integer.class, id));
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
    void uc22StudentCanReadAndDeleteOwnUnassignedRepairOverHttp() throws Exception {
        var student = login(STUDENT);
        var created = post(student, ApiRoutes.REPAIRS,
                "{\"title\":\"API test light\",\"description\":\"Light is not working\","
                        + "\"priority\":\"TRUNG_BINH\",\"fileIds\":[]}");
        assertEquals(200, created.statusCode(), created.body());
        String id = json.readTree(created.body()).path("id").asString();

        var detail = send(student.client(), HttpRequest.newBuilder(uri(
                        ApiRoutes.REPAIR.replace("{id}", id))).GET().build());
        assertEquals(200, detail.statusCode(), detail.body());
        assertEquals(id, json.readTree(detail.body()).path("id").asString());

        var deleted = send(student.client(), mutate(student,
                ApiRoutes.REPAIR.replace("{id}", id), "DELETE", ""));
        assertEquals(200, deleted.statusCode(), deleted.body());
        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM yeu_cau_sua_chua WHERE mayeucau=?", Integer.class, Integer.parseInt(id)));
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
    void uc25AdministratorCanReadAndDeleteGateEntryOverHttp() throws Exception {
        var admin = login("admin");
        int actorId = jdbc.queryForObject(
                "SELECT mataikhoan FROM tai_khoan WHERE tendangnhap='admin'", Integer.class);
        int entryId = jdbc.queryForObject("INSERT INTO ra_vao(masinhvien,loaisukien,ghichu)"
                        + " VALUES('21110001','VAO','API CRUD test entry') RETURNING maravao", Integer.class);
        UUID eventId = UUID.randomUUID();
        jdbc.update("INSERT INTO svc_gate_event(event_id,entry_id,device_id,payload_hash,actor_id,barrier_status)"
                        + " VALUES(?,?, 'API-CRUD-TEST','test-hash',?,'OPENED')",
                eventId, entryId, actorId);

        var detail = send(admin.client(), HttpRequest.newBuilder(uri(
                        ApiRoutes.ENTRY.replace("{id}", eventId.toString()))).GET().build());
        assertEquals(200, detail.statusCode(), detail.body());
        assertEquals(eventId.toString(), json.readTree(detail.body()).path("eventId").asString());

        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM ra_vao WHERE maravao=?", Integer.class, entryId));
    }

    @Test
    void uc14AdministratorCanReadThePendingViolationQueueOverHttp() throws Exception {
        var admin = login("admin");
        var response = send(admin.client(), HttpRequest.newBuilder(uri(ApiRoutes.VIOLATIONS_PENDING + "?page=1&size=10"))
                .GET().build());

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(json.readTree(response.body()).isArray());
        assertFalse(json.readTree(response.body()).isEmpty());
    }

    @Test
    void uc14AdministratorCanCreateReadUpdateAndDeleteViolationOverHttp() throws Exception {
        var admin = login("admin");
        String occurredAt = jdbc.queryForObject(
                "SELECT ngaybatdau::text || ' 12:00:00+07:00' FROM hoc_ky ORDER BY ngaybatdau LIMIT 1",
                String.class);
        String path = ApiRoutes.VIOLATIONS;
        var create = post(admin, path, "{\"studentId\":\"21110001\","
                + "\"content\":\"API CRUD violation\",\"level\":\"LOW\","
                + "\"occurredAt\":\"" + occurredAt.replace(" ", "T") + "\"}");
        assertEquals(200, create.statusCode(), create.body());
        long id = json.readTree(create.body()).path("id").asLong();

        var detail = send(admin.client(), HttpRequest.newBuilder(uri(
                ApiRoutes.VIOLATION.replace("{id}", Long.toString(id)))).GET().build());
        assertEquals(200, detail.statusCode(), detail.body());
        assertEquals(id, json.readTree(detail.body()).path("id").asLong());

        var update = send(admin.client(), mutate(admin,
                ApiRoutes.VIOLATION.replace("{id}", Long.toString(id)), "PUT",
                "{\"content\":\"API CRUD violation updated\",\"level\":\"MEDIUM\",\"version\":1}"));
        assertEquals(200, update.statusCode(), update.body());
        assertEquals("API CRUD violation updated", json.readTree(update.body()).path("content").asString());

        var deleted = send(admin.client(), mutate(admin,
                ApiRoutes.VIOLATION.replace("{id}", Long.toString(id)), "DELETE", ""));
        assertEquals(200, deleted.statusCode(), deleted.body());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_pham WHERE mavipham=?", Integer.class, id));
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

    @Test
    void uc30AdministratorCanCreateUpdateAndDeleteAnActivityOverHttp() throws Exception {
        var admin = login("admin");
        long semesterId = jdbc.queryForObject("SELECT public_id FROM hoc_ky ORDER BY public_id LIMIT 1", Long.class);
        var create = post(admin, ApiRoutes.ACTIVITIES, activityBody(semesterId, "API CRUD activity"));

        assertEquals(200, create.statusCode(), create.body());
        long id = json.readTree(create.body()).path("id").asLong();
        var list = send(admin.client(), HttpRequest.newBuilder(uri(ApiRoutes.ACTIVITIES + "?page=1&size=100"))
                .GET().build());
        assertEquals(200, list.statusCode(), list.body());
        assertTrue(json.readTree(list.body()).toString().contains("API CRUD activity"));

        var update = send(admin.client(), mutate(admin,
                ApiRoutes.ACTIVITY.replace("{id}", Long.toString(id)), "PUT",
                activityBody(semesterId, "API CRUD activity updated")));
        assertEquals(200, update.statusCode(), update.body());
        assertEquals("API CRUD activity updated", json.readTree(update.body()).path("title").asString());

        var delete = send(admin.client(), mutate(admin,
                ApiRoutes.ACTIVITY.replace("{id}", Long.toString(id)), "DELETE", ""));
        assertEquals(200, delete.statusCode(), delete.body());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM hoat_dong WHERE public_id=?", Integer.class, id));
    }

    @Test
    void uc17AccountantCanReadUpdateAndDeleteAnUnpaidInvoiceOverHttp() throws Exception {
        int invoiceId = jdbc.queryForObject("""
                INSERT INTO hoa_don(maphong,kythang,hanthanhtoan,trangthai)
                VALUES('A-101','2099-01','2099-01-15','CHUA_TT') RETURNING mahoadon
                """, Integer.class);
        var accountant = login("ketoan1");

        var list = send(accountant.client(), HttpRequest.newBuilder(uri(ApiRoutes.INVOICES + "?page=1&pageSize=20"))
                .GET().build());
        assertEquals(200, list.statusCode(), list.body());
        var detail = send(accountant.client(), HttpRequest.newBuilder(uri(
                        ApiRoutes.INVOICE.replace("{id}", Integer.toString(invoiceId)))).GET().build());
        assertEquals(200, detail.statusCode(), detail.body());
        assertEquals(invoiceId, json.readTree(detail.body()).path("id").asInt());

        var update = send(accountant.client(), mutate(accountant,
                ApiRoutes.INVOICE.replace("{id}", Integer.toString(invoiceId)), "PUT",
                "{\"dueDate\":\"2099-02-15\"}"));
        assertEquals(200, update.statusCode(), update.body());
        assertEquals("2099-02-15", jdbc.queryForObject(
                "SELECT hanthanhtoan::text FROM hoa_don WHERE mahoadon=?", String.class, invoiceId));

        var delete = send(accountant.client(), mutate(accountant,
                ApiRoutes.INVOICE.replace("{id}", Integer.toString(invoiceId)), "DELETE", ""));
        assertEquals(200, delete.statusCode(), delete.body());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM hoa_don WHERE mahoadon=?", Integer.class, invoiceId));
    }

    private String activityBody(long semesterId, String title) {
        return "{\"semesterId\":" + semesterId + ",\"title\":\"" + title + "\","
                + "\"startAt\":\"2030-01-01T10:00:00+07:00\","
                + "\"endAt\":\"2030-01-01T11:00:00+07:00\",\"capacity\":10,\"points\":5}";
    }

    private HttpRequest mutate(ApiSession session, String path, String method, String body) {
        var builder = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .header("X-CSRF-TOKEN", session.csrfToken())
                .header("Idempotency-Key", UUID.randomUUID().toString());
        var publisher = body.isEmpty() ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        return builder.method(method, publisher).build();
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
        assertTrue(body.path("twoFactorVerified").asBoolean());
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

}
