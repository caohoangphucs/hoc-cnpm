package vn.id.caohoangphuc.cnpm.api;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the public booking HTTP API against a disposable PostgreSQL fixture. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingApiBlackBoxIT {
    private static final String PASSWORD = "test-password";
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JsonMapper json;

    private String suffix;
    private String semesterId;
    private String buildingId;
    private String roomId;
    private String bedId;
    private int windowId;
    private final List<StudentFixture> students = new ArrayList<>();

    private record StudentFixture(int accountId, String username, String studentId, List<UUID> files) {}
    private record ApiSession(HttpClient client, String csrfToken) {}

    @BeforeEach
    void createFixture() {
        suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        semesterId = "T" + suffix;
        buildingId = "TB" + suffix;
        roomId = "TR" + suffix;
        bedId = "TG" + suffix;

        createRoomFixture();
        createWindowFixture();
        createStudentFixture("NAM");
    }

    @AfterEach
    void removeFixture() {
        for (var student : students) {
            jdbc.update("DELETE FROM svc_profile WHERE booking_id IN"
                    + " (SELECT madangky FROM dang_ky_phong WHERE masinhvien=?)", student.studentId());
            jdbc.update("DELETE FROM svc_idempotency WHERE actor_id=?", student.accountId());
            jdbc.update("DELETE FROM svc_outbox WHERE payload->>'studentId'=?", student.studentId());
            jdbc.update("DELETE FROM audit_log WHERE nguoithuchien=?", Integer.toString(student.accountId()));
            jdbc.update("DELETE FROM svc_file WHERE owner_id=?", student.accountId());
            jdbc.update("DELETE FROM luu_tru WHERE masinhvien=?", student.studentId());
            jdbc.update("DELETE FROM sinh_vien WHERE masinhvien=?", student.studentId());
            jdbc.update("DELETE FROM tai_khoan WHERE mataikhoan=?", student.accountId());
        }

        jdbc.update("DELETE FROM giuong WHERE magiuong=?", bedId);
        jdbc.update("DELETE FROM phong WHERE maphong=?", roomId);
        jdbc.update("DELETE FROM toa_nha WHERE matoanha=?", buildingId);
        jdbc.update("DELETE FROM dot_dang_ky WHERE madot=?", windowId);
        jdbc.update("DELETE FROM hoc_ky WHERE mahocky=?", semesterId);
    }

    @Test
    void happyPathCreatesBookingHoldAndProfileThroughHttp() throws Exception {
        var student = onlyStudent();
        var response = postBooking(login(student), bookingBody(student.files().subList(0, 3), true), UUID.randomUUID());

        assertEquals(200, response.statusCode());
        var result = json.readTree(response.body());
        assertEquals("CHO_DUYET", result.path("status").asString());

        int bookingId = Integer.parseInt(result.path("id").asString());
        assertEquals("CHO_DUYET", jdbc.queryForObject(
                "SELECT trangthai FROM dang_ky_phong WHERE madangky=?", String.class, bookingId));
        assertEquals("GIU_CHO", jdbc.queryForObject(
                "SELECT trangthai FROM giuong WHERE magiuong=?", String.class, bedId));
        assertEquals("VALID", jdbc.queryForObject(
                "SELECT state FROM svc_profile WHERE booking_id=?", String.class, bookingId));
        assertEquals(48L, jdbc.queryForObject(
                "SELECT EXTRACT(EPOCH FROM (ngayhethan-ngaydangky))/3600 FROM dang_ky_phong WHERE madangky=?",
                Double.class, bookingId).longValue());
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_log WHERE tenbang='BOOKING_CREATED' AND nguoithuchien=?",
                Integer.class, Integer.toString(student.accountId())));
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM svc_outbox WHERE event_type='BOOKING_CREATED' AND aggregate_id=?",
                Integer.class, Integer.toString(bookingId)));
    }

    @Test
    void unauthenticatedHttpRequestIsRejected() throws Exception {
        var anonymous = sessionClient();
        var response = postBooking(anonymous.client(), anonymous.csrfToken(),
                bookingBody(onlyStudent().files().subList(0, 3), true), UUID.randomUUID());

        assertEquals(401, response.statusCode());
        assertEquals("UNAUTHENTICATED", json.readTree(response.body()).path("code").asString());
        assertEquals(0, bookingCount());
    }

    @Test
    void authenticatedNonStudentCannotCreateBooking() throws Exception {
        var guard = login("baove1", "123456");
        var response = postBooking(guard, bookingBody(onlyStudent().files().subList(0, 3), true), UUID.randomUUID());

        assertEquals(403, response.statusCode());
        assertEquals("FORBIDDEN", json.readTree(response.body()).path("code").asString());
        assertEquals(0, bookingCount());
    }

    @Test
    void missingCsrfTokenIsRejectedBeforeBookingIsCreated() throws Exception {
        var student = onlyStudent();
        var session = login(student);
        var response = send(session.client(), HttpRequest.newBuilder(uri(ApiRoutes.BOOKINGS))
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(bookingBody(student.files().subList(0, 3), true)))
                .build());

        assertEquals(403, response.statusCode());
        assertEquals("FORBIDDEN", json.readTree(response.body()).path("code").asString());
        assertEquals(0, bookingCount());
    }

    @Test
    void administratorOtpEndpointRequiresEnrollmentBeforeVerification() throws Exception {
        var admin = login("admin", "123456");
        var response = send(admin.client(), HttpRequest.newBuilder(uri(ApiRoutes.AUTH_TWO_FACTOR))
                .header("Content-Type", "application/json")
                .header("X-CSRF-TOKEN", admin.csrfToken())
                .POST(HttpRequest.BodyPublishers.ofString("{\"code\":\"123456\"}"))
                .build());

        assertEquals(409, response.statusCode());
        assertEquals("TWO_FACTOR_SETUP_REQUIRED", json.readTree(response.body()).path("code").asString());
    }

    @Test
    void invalidConsentAndTooFewFileIdsReturnValidationError() throws Exception {
        var student = onlyStudent();
        var session = login(student);
        var noConsent = postBooking(session, bookingBody(student.files().subList(0, 3), false), UUID.randomUUID());
        var oneFile = postBooking(session, bookingBody(student.files().subList(0, 1), true), UUID.randomUUID());

        assertEquals(422, noConsent.statusCode());
        assertEquals(422, oneFile.statusCode());
        assertEquals(0, bookingCount());
    }

    @Test
    void lowerAllowedFileCountFailsBusinessRuleWhenRequiredDocumentIsMissing() throws Exception {
        var student = onlyStudent();
        var response = postBooking(login(student), bookingBody(student.files().subList(0, 2), true), UUID.randomUUID());

        assertEquals(422, response.statusCode());
        assertEquals("INVALID", json.readTree(response.body()).path("code").asString());
        assertEquals(0, bookingCount());
    }

    @Test
    void fileNotOwnedByTheAuthenticatedStudentIsNotAccepted() throws Exception {
        var student = onlyStudent();
        var requestFiles = List.of(student.files().get(0), student.files().get(1), UUID.randomUUID());
        var response = postBooking(login(student), bookingBody(requestFiles, true), UUID.randomUUID());

        assertEquals(404, response.statusCode());
        assertEquals("NOT_FOUND", json.readTree(response.body()).path("code").asString());
        assertEquals(0, bookingCount());
    }

    @Test
    void expiredDocumentIsRejectedWithoutCreatingBooking() throws Exception {
        var student = onlyStudent();
        jdbc.update("UPDATE svc_file SET expires_at=now()-interval '1 second' WHERE id=?", student.files().get(0));
        var response = postBooking(login(student), bookingBody(student.files().subList(0, 3), true), UUID.randomUUID());

        assertEquals(422, response.statusCode());
        assertEquals("TRONG", bedStatus());
        assertEquals(0, bookingCount());
    }

    @Test
    void maximumFiveFileIdsAreAccepted() throws Exception {
        var student = onlyStudent();
        var session = login(student);
        var fiveFiles = postBooking(session, bookingBody(student.files(), true), UUID.randomUUID());

        assertEquals(200, fiveFiles.statusCode());
        assertEquals(1, bookingCount());
        assertEquals("ATTACHED", jdbc.queryForObject(
                "SELECT state FROM svc_file WHERE id=?", String.class, student.files().get(4)));
    }

    @Test
    void sixFileIdsExceedTheMaximumAndLeaveNoBooking() throws Exception {
        var student = onlyStudent();
        var response = postBooking(login(student), bookingBody(
                List.of(student.files().get(0), student.files().get(1), student.files().get(2),
                        student.files().get(3), student.files().get(4), UUID.randomUUID()), true), UUID.randomUUID());

        assertEquals(422, response.statusCode());
        assertEquals(0, bookingCount());
        assertEquals("TRONG", bedStatus());
    }

    @Test
    void closedRegistrationWindowReturnsInvalidWithoutCreatingBooking() throws Exception {
        jdbc.update("UPDATE dot_dang_ky SET trangthai='CLOSED' WHERE madot=?", windowId);
        var student = onlyStudent();
        var response = postBooking(login(student), bookingBody(student.files().subList(0, 3), true), UUID.randomUUID());

        assertEquals(422, response.statusCode());
        assertEquals(0, bookingCount());
        assertEquals("TRONG", bedStatus());
    }

    @Test
    void wrongBuildingGenderReturnsInvalidWithoutHoldingBed() throws Exception {
        jdbc.update("UPDATE toa_nha SET gioitinhapdung='NU' WHERE matoanha=?", buildingId);
        var student = onlyStudent();
        var response = postBooking(login(student), bookingBody(student.files().subList(0, 3), true), UUID.randomUUID());

        assertEquals(422, response.statusCode());
        assertEquals(0, bookingCount());
        assertEquals("TRONG", bedStatus());
    }

    @Test
    void unavailableBedReturnsConflictWithoutCreatingBooking() throws Exception {
        jdbc.update("UPDATE giuong SET trangthai='GIU_CHO' WHERE magiuong=?", bedId);
        var student = onlyStudent();
        var response = postBooking(login(student), bookingBody(student.files().subList(0, 3), true), UUID.randomUUID());

        assertEquals(409, response.statusCode());
        assertEquals(0, bookingCount());
    }

    @Test
    void studentWithActiveResidencyCannotBookAgain() throws Exception {
        var student = onlyStudent();
        jdbc.update("INSERT INTO luu_tru(masinhvien,magiuong,ngaybatdau,trangthai) VALUES(?,?,?, 'DANG_O')",
                student.studentId(), bedId, LocalDate.now(BUSINESS_ZONE));
        var response = postBooking(login(student), bookingBody(student.files().subList(0, 3), true), UUID.randomUUID());

        assertEquals(409, response.statusCode());
        assertEquals(0, bookingCount());
        assertEquals("TRONG", bedStatus());
    }

    @Test
    void idempotentRetryReturnsSameBookingAndDifferentBodyConflicts() throws Exception {
        var student = onlyStudent();
        var session = login(student);
        var key = UUID.randomUUID();
        var firstBody = bookingBody(student.files().subList(0, 3), true);
        var first = postBooking(session, firstBody, key);
        var retry = postBooking(session, firstBody, key);
        var changed = postBooking(session, bookingBody(student.files(), true), key);

        assertEquals(200, first.statusCode());
        assertEquals(200, retry.statusCode());
        assertEquals(json.readTree(first.body()).path("id").asString(),
                json.readTree(retry.body()).path("id").asString());
        assertEquals(409, changed.statusCode());
        assertEquals(1, bookingCount());
    }

    @Test
    void concurrentRequestsForOneBedAllowOnlyOneStudentToHoldIt() throws Exception {
        var firstStudent = onlyStudent();
        var secondStudent = createStudentFixture("NAM");
        var firstSession = login(firstStudent);
        var secondSession = login(secondStudent);
        var start = new CountDownLatch(1);

        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = CompletableFuture.supplyAsync(() -> concurrentPost(
                    start, firstSession, bookingBody(firstStudent.files().subList(0, 3), true)), pool);
            var second = CompletableFuture.supplyAsync(() -> concurrentPost(
                    start, secondSession, bookingBody(secondStudent.files().subList(0, 3), true)), pool);
            start.countDown();
            var statuses = List.of(first.join().statusCode(), second.join().statusCode());

            assertTrue(statuses.contains(200));
            assertTrue(statuses.contains(409));
            assertEquals(1, bookingCount());
            assertNotEquals("TRONG", bedStatus());
        } finally {
            pool.shutdownNow();
        }
    }

    private void createRoomFixture() {
        jdbc.update("INSERT INTO toa_nha(matoanha,tentoa,sotang,gioitinhapdung) VALUES(?,?,1,'NAM')",
                buildingId, "API test " + suffix);
        jdbc.update("INSERT INTO phong(maphong,matoanha,maloaiphong,sophong,tang,succhua,dango,giaphongcoban,trangthai)"
                        + " VALUES(?,?,'LP-4','1',1,4,0,500000,'CON_CHO')",
                roomId, buildingId);
        jdbc.update("INSERT INTO giuong(magiuong,maphong,sogiuong,trangthai) VALUES(?,?,'1','TRONG')",
                bedId, roomId);
    }

    private void createWindowFixture() {
        var now = LocalDateTime.now(BUSINESS_ZONE);
        jdbc.update("INSERT INTO hoc_ky(mahocky,tenhocky,ngaybatdau,ngayketthuc,trangthai) VALUES(?,?,?,?,'ACTIVE')",
                semesterId, "API test " + suffix, LocalDate.now(BUSINESS_ZONE).minusDays(1),
                LocalDate.now(BUSINESS_ZONE).plusMonths(3));
        windowId = jdbc.queryForObject("INSERT INTO dot_dang_ky(mahocky,tendot,thoigianmo,thoigiandong,trangthai)"
                        + " VALUES(?,?,?,?, 'ACTIVE') RETURNING madot",
                Integer.class, semesterId, "API test " + suffix, now.minusHours(1), now.plusDays(1));
    }

    private StudentFixture createStudentFixture(String gender) {
        String studentSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String username = "api" + studentSuffix;
        String studentId = "AS" + studentSuffix;
        String email = username + "@example.test";
        int accountId = jdbc.queryForObject("INSERT INTO tai_khoan(tendangnhap,matkhauhash,hoten,email,vaitro,trangthai)"
                        + " VALUES(?,?,?,?,'SINHVIEN','ACTIVE') RETURNING mataikhoan",
                Integer.class, username, passwordEncoder.encode(PASSWORD), "API Student", email);
        jdbc.update("INSERT INTO sinh_vien(masinhvien,mataikhoan,hoten,ngaysinh,gioitinh,cccd,email,sodienthoai,"
                        + "lop,khoa,quequan,trangthai) VALUES(?,?,?,'2004-01-01',?,?,?,'0900000000','API','API','API','CHO_DUYET')",
                studentId, accountId, "API Student " + studentSuffix, gender,
                "9" + studentSuffix, email);

        var fileIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        var kinds = List.of("CCCD_FRONT", "CCCD_BACK", "STUDENT_CARD", "POLICY", "POLICY");
        for (int index = 0; index < fileIds.size(); index++) {
            jdbc.update("INSERT INTO svc_file(id,owner_id,kind,object_key,mime,size_bytes,sha256,key_id,nonce,tag,"
                            + "state,scanned_at,expires_at) VALUES(?,?,?,?, 'image/png',100,'test-sha','test-key',"
                            + "decode('00','hex'),decode('00','hex'),'DRAFT',now(),now()+interval '1 day')",
                    fileIds.get(index), accountId, kinds.get(index), "api-test/" + studentSuffix + "/" + index);
        }

        var fixture = new StudentFixture(accountId, username, studentId, fileIds);
        students.add(fixture);
        return fixture;
    }

    private ApiSession login(StudentFixture student) throws Exception {
        return login(student.username(), PASSWORD);
    }

    private ApiSession login(String username, String password) throws Exception {
        var anonymous = sessionClient();
        var response = send(anonymous.client(), HttpRequest.newBuilder(uri("/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .header("X-CSRF-TOKEN", anonymous.csrfToken())
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .build());
        assertEquals(200, response.statusCode(), response.body());
        return new ApiSession(anonymous.client(), json.readTree(response.body()).path("csrfToken").asString());
    }

    private ApiSession sessionClient() throws Exception {
        var cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        var client = HttpClient.newBuilder().cookieHandler(cookies).connectTimeout(Duration.ofSeconds(5)).build();
        var response = send(client, HttpRequest.newBuilder(uri(ApiRoutes.AUTH_SESSION)).GET().build());
        assertEquals(200, response.statusCode());
        return new ApiSession(client, json.readTree(response.body()).path("csrfToken").asString());
    }

    private HttpResponse<String> postBooking(ApiSession session, String body, UUID idempotencyKey) throws Exception {
        return postBooking(session.client(), session.csrfToken(), body, idempotencyKey);
    }

    private HttpResponse<String> postBooking(HttpClient client, String csrf, String body, UUID idempotencyKey)
            throws Exception {
        return send(client, HttpRequest.newBuilder(uri(ApiRoutes.BOOKINGS))
                .header("Content-Type", "application/json")
                .header("X-CSRF-TOKEN", csrf)
                .header("Idempotency-Key", idempotencyKey.toString())
                .header("X-Request-Id", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build());
    }

    private HttpResponse<String> send(HttpClient client, HttpRequest request) throws Exception {
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }

    private String bookingBody(List<UUID> fileIds, boolean acceptedTerms) {
        String files = fileIds.stream().map(id -> "\"" + id + "\"").collect(Collectors.joining(","));
        return "{\"bedId\":\"" + bedId + "\",\"windowId\":" + windowId
                + ",\"fileIds\":[" + files + "],\"acceptedTerms\":" + acceptedTerms + "}";
    }

    private StudentFixture onlyStudent() {
        return students.get(0);
    }

    private HttpResponse<String> concurrentPost(CountDownLatch start, ApiSession session, String body) {
        try {
            start.await();
            return postBooking(session, body, UUID.randomUUID());
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private int bookingCount() {
        return students.stream()
                .mapToInt(student -> jdbc.queryForObject(
                        "SELECT COUNT(*) FROM dang_ky_phong WHERE masinhvien=?", Integer.class, student.studentId()))
                .sum();
    }

    private String bedStatus() {
        return jdbc.queryForObject("SELECT trangthai FROM giuong WHERE magiuong=?", String.class, bedId);
    }
}
