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
        jdbc.update("DELETE FROM svc_invoice_snapshot WHERE input_hash='api-legacy-access'");
        jdbc.update("DELETE FROM luu_tru WHERE masinhvien='21110091'");
        jdbc.update("UPDATE sinh_vien SET maphonghientai=NULL,trangthai='CHO_DUYET' WHERE masinhvien='21110091'");
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
        jdbc.update("DELETE FROM svc_visitor WHERE identity_encrypted='API GET read test'");
        jdbc.update("DELETE FROM svc_gate_event WHERE device_id='API-GET-READ'");
        jdbc.update("DELETE FROM ra_vao WHERE ghichu='API GET read test entry'");
        jdbc.update("DELETE FROM svc_payment_session WHERE provider_ref LIKE 'api-crud-test-%'");
    }

    /** Regression #27: legacy residents see only their current room, with matching totals. */
    @Test
    void legacyResidentCanReadOwnRoomInvoicesOverHttp() throws Exception {
        var student = login(STUDENT);
        var response = assertGetSucceeds(student.client(), ApiRoutes.INVOICES + "?page=1&pageSize=100");
        var page = json.readTree(response.body());
        assertEquals(3, page.path("total").asInt(), response.body());
        assertEquals(3, page.path("items").size(), response.body());
        for (var invoice : page.path("items")) {
            assertEquals("A-102", invoice.path("roomId").asString());
            assertGetSucceeds(student.client(), "/api/v1/invoices/" + invoice.path("id").asInt());
        }
        assertEquals(404, get(student, "/api/v1/invoices/1").statusCode());
    }

    /** Negative flow: a waiting student with a stale room reference receives no invoices. */
    @Test
    void waitingStudentCannotUseLegacyRoomReferenceOverHttp() throws Exception {
        jdbc.update("UPDATE sinh_vien SET maphonghientai='A-102' WHERE masinhvien='21110091'");
        var student = login("sv_new_nam");
        var response = assertGetSucceeds(student.client(), ApiRoutes.INVOICES);
        assertEquals(0, json.readTree(response.body()).path("total").asInt());
        assertEquals(404, get(student, "/api/v1/invoices/2").statusCode());
    }

    /** Boundary flow: a one-day stay on August 31 includes August but excludes September. */
    @Test
    void datedResidencyOverridesLegacyCurrentRoomOverHttp() throws Exception {
        jdbc.update("UPDATE sinh_vien SET maphonghientai='A-102',trangthai='DANG_O' WHERE masinhvien='21110091'");
        jdbc.update("INSERT INTO luu_tru(masinhvien,magiuong,ngaybatdau,ngayketthuc,trangthai)"
                + " SELECT '21110091',magiuong,'2026-08-31','2026-08-31','DA_TRA_PHONG'"
                + " FROM giuong WHERE maphong='A-101' ORDER BY magiuong LIMIT 1");
        var student = login("sv_new_nam");
        assertEquals(200, get(student, "/api/v1/invoices/1").statusCode());
        assertEquals(404, get(student, "/api/v1/invoices/21").statusCode());
        assertEquals(404, get(student, "/api/v1/invoices/2").statusCode());
        var response = assertGetSucceeds(student.client(), ApiRoutes.INVOICES);
        assertEquals(1, json.readTree(response.body()).path("total").asInt());
    }

    /** Alternative flow: the issued snapshot grants access across moves and excludes non-occupants. */
    @Test
    void issuedSnapshotControlsInvoiceAccessOverHttp() throws Exception {
        jdbc.update("UPDATE sinh_vien SET maphonghientai='A-101',trangthai='DANG_O' WHERE masinhvien='21110091'");
        jdbc.update("INSERT INTO svc_invoice_snapshot(invoice_id,input_hash,inputs)"
                + " VALUES(2,'api-legacy-access','{\"occupants\":[{\"studentId\":\"21110091\"}]}')");
        var student = login("sv_new_nam");
        assertEquals(200, get(student, "/api/v1/invoices/2").statusCode());
        jdbc.update("UPDATE svc_invoice_snapshot SET inputs='{\"occupants\":[]}'"
                + " WHERE input_hash='api-legacy-access'");
        jdbc.update("UPDATE sinh_vien SET maphonghientai='A-102' WHERE masinhvien='21110091'");
        assertEquals(404, get(student, "/api/v1/invoices/2").statusCode());
    }

    /** Regression #25: seeded open activities are visible through the student HTTP endpoint. */
    @Test
    void studentCanReadSeededCommunityActivitiesOverHttp() throws Exception {
        var student = login(STUDENT);
        var response = assertGetSucceeds(student.client(), ApiRoutes.ACTIVITIES + "?page=1&size=100");
        var activities = json.readTree(response.body());
        assertTrue(activities.isArray(), response.body());
        assertTrue(activities.size() >= 3, response.body());
        var firstPage = assertGetSucceeds(student.client(), ApiRoutes.ACTIVITIES + "?page=1&size=1");
        assertEquals(1, json.readTree(firstPage.body()).size());
        var beyondLastPage = assertGetSucceeds(student.client(), ApiRoutes.ACTIVITIES + "?page=10000&size=1");
        assertEquals(0, json.readTree(beyondLastPage.body()).size());
        assertEquals(422, get(student, ApiRoutes.ACTIVITIES + "?page=0&size=10").statusCode());
    }

    /** Alternative flow: a closed activity disappears without deleting its registration history. */
    @Test
    void closedCommunityActivityIsNotAdvertisedOverHttp() throws Exception {
        long id = jdbc.queryForObject("SELECT public_id FROM hoat_dong WHERE mahoatdong='ACT-03'", Long.class);
        jdbc.update("UPDATE hoat_dong SET trangthai='DONG_DANG_KY' WHERE mahoatdong='ACT-03'");
        try {
            var student = login(STUDENT);
            var response = assertGetSucceeds(student.client(), ApiRoutes.ACTIVITIES + "?page=1&size=100");
            for (var activity : json.readTree(response.body())) {
                assertFalse(activity.path("id").asLong() == id, response.body());
            }
        } finally {
            jdbc.update("UPDATE hoat_dong SET trangthai='MO_DANG_KY' WHERE mahoatdong='ACT-03'");
        }
    }

    /** Regression: history survives login again and never includes another student's registrations. */
    @Test
    void communityHistoryIsPersistentAndOwnedOverHttp() throws Exception {
        var student = login(STUDENT);
        var sessionResponse = get(student, ApiRoutes.AUTH_SESSION);
        assertEquals("21110001", json.readTree(sessionResponse.body()).path("studentId").asString());
        var response = get(student, ApiRoutes.MY_ACTIVITY_REGISTRATIONS);
        assertEquals(200, response.statusCode(), response.body());
        var tickets = json.readTree(response.body());
        int expected = jdbc.queryForObject(
                "SELECT count(*) FROM dang_ky_cong_ich WHERE masinhvien='21110001'", Integer.class);
        assertTrue(expected > 0);
        assertEquals(expected, tickets.size());
        for (var ticket : tickets) {
            int id = ticket.path("receipt").path("id").asInt();
            assertEquals("21110001", jdbc.queryForObject(
                    "SELECT masinhvien FROM dang_ky_cong_ich WHERE madangkycongich=?", String.class, id));
            assertEquals("", ticket.path("receipt").path("qrToken").asString());
            assertFalse(ticket.path("activity").path("title").asString().isBlank());
        }
        var loggedInAgain = get(login(STUDENT), ApiRoutes.MY_ACTIVITY_REGISTRATIONS);
        assertEquals(tickets, json.readTree(loggedInAgain.body()));
        int otherId = jdbc.queryForObject(
                "SELECT min(madangkycongich) FROM dang_ky_cong_ich WHERE masinhvien<>'21110001'", Integer.class);
        var denied = post(student, ApiRoutes.REGISTRATION_TOKEN.replace("{id}", Integer.toString(otherId)), "");
        assertEquals(404, denied.statusCode(), denied.body());
    }

    /** Vi phạm seed thuộc đúng lượt cổng; chỉ chủ lượt và bảo vệ xem được nội dung. */
    @Test
    void gateEntryIncludesOwnedViolationDetailsOverHttp() throws Exception {
        String eventId = jdbc.queryForObject("""
                SELECT e.event_id::text FROM svc_gate_event e JOIN ra_vao r ON r.maravao=e.entry_id
                JOIN vi_pham v ON v.maravao=r.maravao AND v.masinhvien=r.masinhvien
                WHERE v.mavipham=1
                """, String.class);
        String path = "/api/v1/entries/" + eventId;
        var own = get(login(STUDENT), path);
        assertEquals(200, own.statusCode(), own.body());
        var detail = json.readTree(own.body()).path("violation");
        assertEquals(1, detail.path("id").asInt());
        assertEquals("Về KTX muộn sau 23h không xin phép", detail.path("content").asString());
        assertEquals("LOW", detail.path("level").asString());
        assertEquals("DA_XU_LY", detail.path("status").asString());
        assertFalse(detail.path("occurredAt").asString().isBlank());
        assertEquals(404, get(login("sv002"), path).statusCode());
        assertEquals(200, get(login("baove1"), path).statusCode());
    }

    /** Sends a read-only request through the actual HTTP server. */
    private HttpResponse<String> get(ApiSession session, String path) throws Exception {
        return send(session.client(), HttpRequest.newBuilder(uri(path)).GET().build());
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
    void administratorAndGuardCanReadBookingVisitorAndGateListsOverHttp() throws Exception {
        int adminId = jdbc.queryForObject(
                "SELECT mataikhoan FROM tai_khoan WHERE tendangnhap='admin'", Integer.class);
        jdbc.update("INSERT INTO svc_visitor(host_student_id,name_encrypted,identity_encrypted,valid_from,valid_to,state)"
                        + " VALUES('21110001','test-name','API GET read test',now()-interval '1 hour',"
                        + "now()+interval '1 hour','APPROVED')");
        int entryId = jdbc.queryForObject("INSERT INTO ra_vao(masinhvien,loaisukien,ghichu)"
                + " VALUES('21110001','VAO','API GET read test entry') RETURNING maravao", Integer.class);
        UUID eventId = UUID.randomUUID();
        jdbc.update("INSERT INTO svc_gate_event(event_id,entry_id,device_id,payload_hash,actor_id,barrier_status)"
                        + " VALUES(?,?, 'API-GET-READ','test-hash',?,'OPENED')",
                eventId, entryId, adminId);

        var admin = login("admin");
        assertGetSucceeds(admin.client(), ApiRoutes.BOOKINGS + "?page=1&pageSize=20");
        var adminVisitors = assertGetSucceeds(admin.client(), ApiRoutes.VISITORS + "?page=1&size=20");
        assertVisitorIsPresent(adminVisitors, "APPROVED");
        var adminEntries = assertGetSucceeds(admin.client(), ApiRoutes.ENTRIES + "?page=1&size=20");
        assertTrue(adminEntries.body().contains(eventId.toString()));

        var guard = login("baove1");
        var guardVisitors = assertGetSucceeds(guard.client(), ApiRoutes.VISITORS + "?page=1&size=20");
        assertVisitorIsPresent(guardVisitors, "APPROVED");
        var guardEntries = assertGetSucceeds(guard.client(), ApiRoutes.ENTRIES + "?page=1&size=20");
        assertTrue(guardEntries.body().contains(eventId.toString()));
    }

    private void assertVisitorIsPresent(HttpResponse<String> response, String expectedState) throws Exception {
        var visitors = json.readTree(response.body());
        assertTrue(visitors.isArray(), response.body());
        assertTrue(visitors.valueStream().anyMatch(visitor ->
                "21110001".equals(visitor.path("hostStudentId").asString())
                        && expectedState.equals(visitor.path("state").asString())), response.body());
    }

    private HttpResponse<String> assertGetSucceeds(HttpClient client, String path) throws Exception {
        var response = send(client, HttpRequest.newBuilder(uri(path)).GET().build());
        assertEquals(200, response.statusCode(), response.body());
        return response;
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
