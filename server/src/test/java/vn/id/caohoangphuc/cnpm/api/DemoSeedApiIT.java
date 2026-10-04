package vn.id.caohoangphuc.cnpm.api;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies the actual demo dataset through HTTP, after migration, on disposable PostgreSQL only. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoSeedApiIT {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    private record Session(HttpClient client, String csrf) {}

    /** Restore the two review fixtures after HTTP mutations so later tests keep the seed baseline. */
    @AfterEach
    void restoreReviewFixtures() {
        jdbc.update("DELETE FROM svc_invoice_draft WHERE inputs->>'roomId' IN ('DEMO-A','DEMO-B')");
        jdbc.update("DELETE FROM svc_contract WHERE residency_id IN"
                + " (SELECT maluutru FROM luu_tru WHERE madangky IN (900001,900003))");
        jdbc.update("DELETE FROM luu_tru WHERE madangky IN (900001,900003)");
        jdbc.update("UPDATE sinh_vien SET maphonghientai=NULL,trangthai='CHO_DUYET'"
                + " WHERE masinhvien IN ('DEMO-PM1','DEMO-PF1')");
        jdbc.update("UPDATE dang_ky_phong SET trangthai='CHO_DUYET',version=1,"
                + " ghichulydo='Demo: hồ sơ đăng ký phòng' WHERE madangky IN (900001,900003)");
        jdbc.update("UPDATE svc_profile SET approved_by=NULL WHERE booking_id IN (900001,900003)");
        jdbc.update("UPDATE giuong SET trangthai='GIU_CHO',version=1 WHERE magiuong IN ('DEMO-A-1','DEMO-B-1')");
        jdbc.update("UPDATE phong SET dango=1,trangthai='CON_CHO' WHERE maphong IN ('DEMO-A','DEMO-B')");
    }

    /** The administrator sees nonzero pending, approved and rejected cases with usable profiles. */
    @Test
    void reviewListContainsAllDemoStatesAndProfiles() throws Exception {
        var admin = login("admin");
        int pending = 0, approved = 0, rejected = 0, cancelled = 0;
        for (int id = 900001; id <= 900008; id++) {
            var response = get(admin, "/api/v1/bookings/" + id);
            assertEquals(200, response.statusCode(), response.body());
            var booking = json.readTree(response.body());
            assertEquals("VALID", booking.path("profileState").asString());
            assertEquals(3, booking.path("fileIds").size());
            switch (booking.path("status").asString()) {
                case "CHO_DUYET" -> pending++;
                case "DA_DUYET" -> approved++;
                case "TU_CHOI" -> rejected++;
                case "HUY" -> cancelled++;
                default -> fail(response.body());
            }
        }
        assertEquals(4, pending);
        assertEquals(2, approved);
        assertEquals(1, rejected);
        assertEquals(1, cancelled);
    }

    /** Main/alternative flow: seed requests can be approved and rejected through real HTTP. */
    @Test
    void seededPendingRequestsSupportApprovalAndRejection() throws Exception {
        var admin = login("admin");
        var approval = post(admin, "/api/v1/bookings/900001/decision", "{\"decision\":\"DUYET\",\"version\":1}");
        assertEquals(200, approval.statusCode(), approval.body());
        assertEquals("DA_DUYET", json.readTree(approval.body()).path("status").asString());
        assertEquals("DANG_O", jdbc.queryForObject("SELECT trangthai FROM giuong WHERE magiuong='DEMO-A-1'", String.class));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM luu_tru WHERE madangky=900001", Integer.class));

        var rejection = post(admin, "/api/v1/bookings/900003/decision",
                "{\"decision\":\"TU_CHOI\",\"reason\":\"Không đủ điều kiện demo\",\"version\":1}");
        assertEquals(200, rejection.statusCode(), rejection.body());
        assertEquals("TU_CHOI", json.readTree(rejection.body()).path("status").asString());
        assertEquals("TRONG", jdbc.queryForObject("SELECT trangthai FROM giuong WHERE magiuong='DEMO-B-1'", String.class));
    }

    /** Negative/boundary cases: students cannot review, stale versions and missing rejection reasons fail. */
    @Test
    void seedReviewStillEnforcesRoleVersionAndReason() throws Exception {
        assertEquals(403, post(login("demo_pending_m1"), "/api/v1/bookings/900001/decision",
                "{\"decision\":\"DUYET\",\"version\":1}").statusCode());
        var admin = login("admin");
        assertEquals(409, post(admin, "/api/v1/bookings/900001/decision",
                "{\"decision\":\"DUYET\",\"version\":2}").statusCode());
        assertEquals(422, post(admin, "/api/v1/bookings/900001/decision",
                "{\"decision\":\"TU_CHOI\",\"version\":1}").statusCode());
    }

    /** UC-04: the catalog provides free beds without hiding held or occupied beds. */
    @Test
    void newStudentCanSeeFreeHeldAndOccupiedDemoBeds() throws Exception {
        var response = get(login("sv041"), "/api/v1/rooms/DEMO-A/beds");
        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("TRONG"), response.body());
        assertTrue(response.body().contains("GIU_CHO"), response.body());
        assertTrue(response.body().contains("DANG_O"), response.body());
    }

    /** UC-17: seed readings, prices and residencies support an actual invoice preview request. */
    @Test
    void accountantCanPreviewSeededRooms() throws Exception {
        var response = post(login("ketoan1"), "/api/v1/invoice-drafts",
                "{\"month\":\"2026-10\",\"roomIds\":[\"DEMO-A\",\"DEMO-B\"]}");
        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("DEMO-A"), response.body());
        assertTrue(response.body().contains("DEMO-B"), response.body());
    }

    /** UC-08 and UC-22: residents see their own invoices and repair cases, with cross-room isolation. */
    @Test
    void residentsCanReadSeededInvoicesRepairsAndPaymentHistory() throws Exception {
        var male = login("demo_resident_m");
        var female = login("demo_resident_f");
        assertEquals(200, get(male, "/api/v1/invoices/900001").statusCode());
        assertEquals(404, get(male, "/api/v1/invoices/900002").statusCode());
        assertEquals(200, get(female, "/api/v1/invoices/900002").statusCode());
        assertEquals(200, get(male, "/api/v1/repairs/900001").statusCode());
        var history = get(female, "/api/v1/payment-sessions");
        assertEquals(200, history.statusCode(), history.body());
        assertTrue(history.body().contains("SUCCESS"), history.body());
    }

    /** UC-25/14/30: lists, ownership and earned points use the migrated demo records. */
    @Test
    void gateDisciplineAndCommunityHaveVisibleDemoCases() throws Exception {
        var guard = login("baove1");
        var visitors = get(guard, "/api/v1/visitors");
        assertEquals(200, visitors.statusCode(), visitors.body());
        assertFalse(visitors.body().contains("PENDING"));
        assertTrue(visitors.body().contains("APPROVED"));
        assertFalse(visitors.body().contains("REJECTED"));
        var entries = get(guard, "/api/v1/entries");
        assertEquals(200, entries.statusCode(), entries.body());
        assertFalse(json.readTree(entries.body()).path("items").isEmpty());
        assertEquals(200, get(login("admin"), "/api/v1/violations/900001").statusCode());
        long semester = jdbc.queryForObject("SELECT public_id FROM hoc_ky WHERE mahocky='HK1-2026'", Long.class);
        var points = get(login("demo_resident_f"), "/api/v1/activities/points?semesterId=" + semester);
        assertEquals(200, points.statusCode(), points.body());
        assertTrue(points.body().contains("57"), points.body());
    }

    /** Establish a genuine session and take the fresh CSRF token returned by password login. */
    private Session login(String username) throws Exception {
        var client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
        var anonymous = client.send(HttpRequest.newBuilder(uri(ApiRoutes.AUTH_SESSION)).GET().build(), HttpResponse.BodyHandlers.ofString());
        var session = new Session(client, json.readTree(anonymous.body()).path("csrfToken").asString());
        var response = post(session, ApiRoutes.AUTH_LOGIN, "{\"username\":\"" + username + "\",\"password\":\"123456\"}");
        assertEquals(200, response.statusCode(), response.body());
        return new Session(client, json.readTree(response.body()).path("csrfToken").asString());
    }

    /** Send reads through the network rather than invoking a controller directly. */
    private HttpResponse<String> get(Session session, String path) throws Exception {
        return session.client().send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    /** Give every command an independent request/idempotency key and its session's CSRF token. */
    private HttpResponse<String> post(Session session, String path, String body) throws Exception {
        return session.client().send(HttpRequest.newBuilder(uri(path)).header("Content-Type", "application/json")
                .header("X-CSRF-TOKEN", session.csrf()).header("X-Request-Id", UUID.randomUUID().toString())
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    /** Resolve the random test HTTP port; no request is sent to production. */
    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }
}
