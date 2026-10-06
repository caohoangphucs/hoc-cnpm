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
        jdbc.update("DELETE FROM svc_repair_assignment WHERE repair_id IN (SELECT mayeucau FROM yeu_cau_sua_chua WHERE tieude='API test light')");
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

    /** #42: thông báo hạn phiên và idle timeout theo vai trò; session sau đăng nhập vẫn dùng được. */
    @Test
    void studentIdleSessionLastsEightHoursWhileGuardRemainsFifteenMinutes() throws Exception {
        var student = login(STUDENT);
        var response = get(student, ApiRoutes.AUTH_SESSION);
        long seconds = java.time.Duration.between(java.time.Instant.now(),
                java.time.OffsetDateTime.parse(json.readTree(response.body()).path("idleExpiresAt").asString()).toInstant()).toSeconds();
        assertTrue(seconds >= 28790 && seconds <= 28801, Long.toString(seconds));
        response = get(login("baove1"), ApiRoutes.AUTH_SESSION);
        seconds = java.time.Duration.between(java.time.Instant.now(),
                java.time.OffsetDateTime.parse(json.readTree(response.body()).path("idleExpiresAt").asString()).toInstant()).toSeconds();
        assertTrue(seconds >= 890 && seconds <= 901, Long.toString(seconds));
    }

    /** #41: tìm dữ liệu thật theo MSSV/họ tên và giới hạn sinh viên ở chính mình. */
    @Test
    void studentSearchUsesOwnedDatabaseResultsOverHttp() throws Exception {
        var student = login(STUDENT);
        var own = get(student, ApiRoutes.STUDENTS);
        assertEquals(200, own.statusCode(), own.body());
        assertEquals(1, json.readTree(own.body()).size());
        assertEquals("21110001", json.readTree(own.body()).get(0).path("id").asString());
        assertEquals(0, json.readTree(get(student, ApiRoutes.STUDENTS+"?search=21110002").body()).size());
        var guard = get(login("baove1"), ApiRoutes.STUDENTS+"?search=21110002");
        assertEquals("21110002",json.readTree(guard.body()).get(0).path("id").asString());
        assertEquals(403,get(login("kythuat1"),ApiRoutes.STUDENTS).statusCode());
    }

    /** #39/#40: đầu vào sai báo đúng trường; hợp lệ tạo phiếu, retry cùng key không tạo trùng. */
    @Test
    void visitorValidationAndIdempotencyWorkOverHttp() throws Exception {
        var student = login(STUDENT);
        String valid = "{\"hostStudentId\":\"21110001\",\"name\":\"  Nguyễn   Văn An  \","
                + "\"identity\":\"012345678901\",\"identityType\":\"CCCD\","
                + "\"validFrom\":\"2030-01-01T09:00:00+07:00\",\"validTo\":\"2030-01-01T10:00:00+07:00\"}";
        var invalid = post(student,ApiRoutes.VISITORS,valid.replace("  Nguyễn   Văn An  ","12345"));
        assertEquals(422,invalid.statusCode(),invalid.body());
        assertTrue(json.readTree(invalid.body()).path("fieldErrors").has("name"));
        invalid = post(student,ApiRoutes.VISITORS,valid.replace("012345678901","123"));
        assertEquals(422,invalid.statusCode(),invalid.body());
        assertTrue(json.readTree(invalid.body()).path("fieldErrors").has("identity"));
        invalid = post(student,ApiRoutes.VISITORS,valid.replace("  Nguyễn   Văn An  ","x"));
        assertEquals(422,invalid.statusCode());
        String key = UUID.randomUUID().toString();
        var request = HttpRequest.newBuilder(uri(ApiRoutes.VISITORS)).header("Content-Type","application/json")
                .header("X-CSRF-TOKEN",student.csrfToken()).header("Idempotency-Key",key)
                .POST(HttpRequest.BodyPublishers.ofString(valid)).build();
        var created = send(student.client(),request);
        assertEquals(200,created.statusCode(),created.body());
        long id = Long.parseLong(json.readTree(created.body()).path("id").asString());
        try {
            assertEquals(created.body(),send(student.client(),request).body());
            assertEquals("PENDING",json.readTree(get(student,"/api/v1/visitors/"+id).body()).path("state").asString());
        } finally {
            assertEquals(200,send(student.client(),mutate(student,"/api/v1/visitors/"+id,"DELETE","")).statusCode());
        }
    }

    /** #38: sinh viên không tự nâng ưu tiên; kỹ thuật viên được phân công cập nhật có version/audit. */
    @Test
    void technicianAssessesRepairPriorityOverHttp() throws Exception {
        var student=login(STUDENT);
        var create=post(student,ApiRoutes.REPAIRS,"{\"title\":\"API test light\",\"description\":\"Không sáng\",\"priority\":\"KHAN_CAP\",\"fileIds\":[]}");
        assertEquals(200,create.statusCode(),create.body());
        int id=Integer.parseInt(json.readTree(create.body()).path("id").asString());
        var detail=get(student,"/api/v1/repairs/"+id);
        assertEquals("TRUNG_BINH",json.readTree(detail.body()).path("priority").asString());
        String path=ApiRoutes.REPAIR_PRIORITY.replace("{id}",Integer.toString(id));
        assertEquals(403,send(student.client(),mutate(student,path,"PUT","{\"priority\":\"CAO\",\"version\":1}")).statusCode());
        var assign=post(login("admin"),"/api/v1/repairs/"+id+"/assignment","{\"technicianAccountId\":3,\"scheduledAt\":\"2030-01-01T09:00:00+07:00\",\"version\":1}");
        assertEquals(200,assign.statusCode(),assign.body());
        var technician=login("kythuat1");
        var updated=send(technician.client(),mutate(technician,path,"PUT","{\"priority\":\"CAO\",\"version\":2}"));
        assertEquals(200,updated.statusCode(),updated.body());
        assertEquals("CAO",json.readTree(get(student,"/api/v1/repairs/"+id).body()).path("priority").asString());
        assertEquals(409,send(technician.client(),mutate(technician,path,"PUT","{\"priority\":\"THAP\",\"version\":2}")).statusCode());
    }

    /** #47/#48: biên tiêu đề 254/255/256 và sinh viên chưa có phòng không sinh dữ liệu dở dang. */
    @Test
    void repairInputBoundariesAndRoomEligibilityOverHttp() throws Exception {
        var student = login(STUDENT);
        for (int length : new int[]{254, 255, 256}) {
            String title = "A".repeat(length);
            var response = post(student, ApiRoutes.REPAIRS,
                    json.writeValueAsString(java.util.Map.of("title",title,"description","Biên tiêu đề","fileIds",java.util.List.of())));
            assertEquals(length <= 255 ? 200 : 422, response.statusCode(),response.body());
            if (length <= 255) {
                String id = json.readTree(response.body()).path("id").asString();
                assertEquals(200,send(student.client(),mutate(student,"/api/v1/repairs/"+id,"DELETE","")).statusCode());
            } else {
                assertTrue(json.readTree(response.body()).path("fieldErrors").has("title"));
            }
        }
        var waiting = post(login("sv_new_nam"),ApiRoutes.REPAIRS,
                "{\"title\":\"API test no room\",\"description\":\"Chưa lưu trú\",\"fileIds\":[]}");
        assertEquals(422,waiting.statusCode(),waiting.body());
        assertEquals("NO_CURRENT_ROOM",json.readTree(waiting.body()).path("code").asString());
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM yeu_cau_sua_chua WHERE tieude='API test no room'",Integer.class));
    }

    /** #49: thiếu/null/version sai trả lỗi trường, không cập nhật phiếu trước request hợp lệ. */
    @Test
    void repairScheduleRejectsMissingFieldsBeforeMutationOverHttp() throws Exception {
        var created = post(login(STUDENT),ApiRoutes.REPAIRS,
                "{\"title\":\"API test light\",\"description\":\"Đổi lịch\",\"fileIds\":[]}");
        assertEquals(200,created.statusCode(),created.body());
        int id = Integer.parseInt(json.readTree(created.body()).path("id").asString());
        assertEquals(200,post(login("admin"),"/api/v1/repairs/"+id+"/assignment",
                "{\"technicianAccountId\":3,\"scheduledAt\":\"2030-01-01T09:00:00+07:00\",\"version\":1}").statusCode());
        var tech = login("kythuat1");
        String path="/api/v1/repairs/"+id+"/schedule";
        String valid="{\"version\":2,\"scheduledAt\":\"2030-01-02T09:00:00+07:00\",\"note\":\"Đổi lịch\"}";
        for (String body : new String[]{valid.replace("\"version\":2,",""),
                valid.replace("\"version\":2","\"version\":null"),
                valid.replace("\"version\":2","\"version\":0"),
                valid.replace("\"scheduledAt\":\"2030-01-02T09:00:00+07:00\",",""),
                valid.replace("\"2030-01-02T09:00:00+07:00\"","null"),
                valid.replace("Đổi lịch", "")}) {
            var response=send(tech.client(),mutate(tech,path,"PUT",body));
            assertEquals(422,response.statusCode(),response.body());
            assertTrue(json.readTree(response.body()).path("fieldErrors").size()>0);
            assertEquals(2,json.readTree(get(tech,"/api/v1/repairs/"+id).body()).path("version").asInt());
        }
        assertEquals(200,send(tech.client(),mutate(tech,path,"PUT",valid)).statusCode());
    }

    /** #46: danh mục thật, upload multipart, hoàn thành với ảnh và xuất kho đúng một lần. */
    @Test
    void completionUsesMaterialCatalogAndPrivateImageOverHttp() throws Exception {
        var student=login(STUDENT);
        var tech=login("kythuat1");
        String code="TEST-"+UUID.randomUUID().toString().substring(0,8);
        var material=post(tech,ApiRoutes.MATERIALS,json.writeValueAsString(java.util.Map.of(
                "id",code,"name","Bóng đèn kiểm thử","unit","cái","stock",3,"price",1000)));
        assertEquals(200,material.statusCode(),material.body());
        assertEquals(403,get(student,ApiRoutes.MATERIALS).statusCode());
        assertTrue(get(tech,ApiRoutes.MATERIALS).body().contains(code));
        String fileId=null;
        var evidenceIds=new java.util.ArrayList<String>();
        int id=0;
        try {
            var created=post(student,ApiRoutes.REPAIRS,"{\"title\":\"API test light\",\"description\":\"Đèn hỏng\",\"fileIds\":[]}");
            assertEquals(200,created.statusCode(),created.body());
            id=Integer.parseInt(json.readTree(created.body()).path("id").asString());
            assertEquals(200,post(login("admin"),"/api/v1/repairs/"+id+"/assignment",
                    "{\"technicianAccountId\":3,\"scheduledAt\":\"2030-01-01T09:00:00+07:00\",\"version\":1}").statusCode());
            var image=new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB);
            var png=new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image,"png",png);
            String boundary="test-"+UUID.randomUUID();
            var multipart=new java.io.ByteArrayOutputStream();
            multipart.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"kind\"\r\n\r\nREPAIR\r\n--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"after.png\"\r\nContent-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            multipart.write(png.toByteArray());
            multipart.write(("\r\n--"+boundary+"--\r\n").getBytes(StandardCharsets.UTF_8));
            var uploadRequest=HttpRequest.newBuilder(uri("/api/v1/files"))
                    .header("X-CSRF-TOKEN",tech.csrfToken()).header("Content-Type","multipart/form-data; boundary="+boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(multipart.toByteArray())).build();
            for (int imageIndex=0; imageIndex<10; imageIndex++) {
                var upload=send(tech.client(),uploadRequest);
                assertEquals(200,upload.statusCode(),upload.body());
                evidenceIds.add(json.readTree(upload.body()).path("id").asString());
            }
            fileId=evidenceIds.get(0);
            assertEquals(404,get(student,"/api/v1/files/"+fileId).statusCode());
            String body=json.writeValueAsString(java.util.Map.of("version",2,"note","Thay đèn", "fileIds",evidenceIds,
                    "supplies",java.util.List.of(java.util.Map.of("supplyId",code,"quantity",1))));
            var tooMany=new java.util.ArrayList<>(evidenceIds);
            tooMany.add(UUID.randomUUID().toString());
            assertEquals(422,post(tech,"/api/v1/repairs/"+id+"/complete",json.writeValueAsString(java.util.Map.of(
                    "version",2,"note","Quá giới hạn ảnh","fileIds",tooMany,"supplies",java.util.List.of()))).statusCode());
            var completeRequest=mutate(tech,"/api/v1/repairs/"+id+"/complete","POST",body);
            var complete=send(tech.client(),completeRequest);
            assertEquals(200,complete.statusCode(),complete.body());
            assertEquals("HOAN_THANH",json.readTree(complete.body()).path("status").asString());
            assertEquals(complete.body(),send(tech.client(),completeRequest).body());
            assertEquals(2,jdbc.queryForObject("SELECT soluongton FROM vat_tu WHERE mavattu=?",Integer.class,code));
            assertTrue(get(student,"/api/v1/repairs/"+id+"/files").body().contains(fileId));
            assertEquals(200,get(student,"/api/v1/files/"+fileId).statusCode());
            assertEquals(404,get(login("sv002"),"/api/v1/files/"+fileId).statusCode());
        } finally {
            jdbc.update("DELETE FROM xuat_vat_tu WHERE mavattu=?",code);
            jdbc.update("DELETE FROM vat_tu WHERE mavattu=?",code);
            if(id>0) jdbc.update("DELETE FROM svc_repair_files WHERE repair_id=?",id);
            for(String evidence:evidenceIds) {
                jdbc.update("DELETE FROM svc_file_content WHERE file_id=?",UUID.fromString(evidence));
                jdbc.update("DELETE FROM svc_file WHERE id=?",UUID.fromString(evidence));
            }
        }
    }

    /** #59/#61: fingerprint gồm token; ghi chú 255 hợp lệ, 256 trả lỗi trước INSERT. */
    @Test
    void gateFingerprintAndNoteBoundaryOverHttp() throws Exception {
        var guard=login("baove1");
        UUID cardId=UUID.randomUUID();
        String token="api-card-"+UUID.randomUUID();
        jdbc.update("INSERT INTO svc_card(id,student_id,token_hash,valid_from,valid_to,state) VALUES(?,'DEMO-AM',?,now()-interval '1 hour',now()+interval '1 hour','ACTIVE')",
                cardId,vn.id.caohoangphuc.cnpm.service.ServiceContext.hash(token));
        UUID event=UUID.randomUUID();
        try {
            String body=json.writeValueAsString(java.util.Map.of("eventId",event,"cardToken",token,"type","RA","deviceId","API-GATE-REGRESSION","note","A".repeat(255)));
            String key=UUID.randomUUID().toString();
            var first=gateRequest(guard,body,key);
            assertEquals(200,first.statusCode(),first.body());
            assertEquals(first.body(),gateRequest(guard,body,key).body());
            var changed=gateRequest(guard,body.replace(token,"invalid-card"),key);
            assertEquals(409,changed.statusCode(),changed.body());
            assertEquals(409,gateRequest(guard,body.replace(token,"invalid-card"),UUID.randomUUID().toString()).statusCode());
            String invalid=body.replace(event.toString(),UUID.randomUUID().toString()).replace("A".repeat(255),"A".repeat(256));
            var response=gateRequest(guard,invalid,UUID.randomUUID().toString());
            assertEquals(422,response.statusCode(),response.body());
            assertTrue(json.readTree(response.body()).path("fieldErrors").has("note"));
            assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM svc_gate_event WHERE device_id='API-GATE-REGRESSION'",Integer.class));
        } finally {
            cleanupGateDevice("API-GATE-REGRESSION");
            jdbc.update("DELETE FROM svc_card WHERE id=?",cardId);
        }
    }

    /** Gửi cùng key qua HTTP thật để kiểm tra replay và xung đột fingerprint. */
    private HttpResponse<String> gateRequest(ApiSession guard,String body,String key) throws Exception {
        return send(guard.client(),HttpRequest.newBuilder(uri(ApiRoutes.ENTRIES))
                .header("X-CSRF-TOKEN",guard.csrfToken()).header("Content-Type","application/json")
                .header("Idempotency-Key",key).POST(HttpRequest.BodyPublishers.ofString(body)).build());
    }

    /** Chỉ dọn event do fixture tạo, không đụng dữ liệu seed. */
    private void cleanupGateDevice(String device) {
        var ids=jdbc.queryForList("SELECT entry_id FROM svc_gate_event WHERE device_id=?",Integer.class,device);
        jdbc.update("DELETE FROM vi_pham WHERE maravao IN (SELECT entry_id FROM svc_gate_event WHERE device_id=?)",device);
        jdbc.update("DELETE FROM svc_gate_event WHERE device_id=?",device);
        for(int id:ids) jdbc.update("DELETE FROM ra_vao WHERE maravao=?",id);
    }

    /** #60: MSSV có endpoint riêng, xác minh bắt buộc, vẫn kiểm tra thẻ/lưu trú và quyền. */
    @Test
    void manualGateEntryRequiresGuardVerificationOverHttp() throws Exception {
        var guard=login("baove1");
        UUID card=UUID.randomUUID();
        jdbc.update("INSERT INTO svc_card(id,student_id,token_hash,valid_from,valid_to,state) VALUES(?,'DEMO-AM',?,now()-interval '1 hour',now()+interval '1 hour','ACTIVE')",
                card,vn.id.caohoangphuc.cnpm.service.ServiceContext.hash("manual-fixture-"+card));
        String body=json.writeValueAsString(java.util.Map.of("eventId",UUID.randomUUID(),"studentId","DEMO-AM","type","RA","deviceId","API-MANUAL-GATE","identityVerified",true));
        try {
            assertEquals(422,post(guard,ApiRoutes.MANUAL_ENTRY,body.replace("true","false")).statusCode());
            assertEquals(403,post(login(STUDENT),ApiRoutes.MANUAL_ENTRY,body).statusCode());
            assertEquals(409,post(guard,ApiRoutes.MANUAL_ENTRY,body.replace("DEMO-AM","21110091")).statusCode());
            var response=post(guard,ApiRoutes.MANUAL_ENTRY,body);
            assertEquals(200,response.statusCode(),response.body());
            assertEquals("DEMO-AM",json.readTree(response.body()).path("studentId").asString());
            assertFalse(response.body().contains("token"));
            assertFalse(get(guard,ApiRoutes.STUDENTS).body().contains("token"));
        } finally {
            cleanupGateDevice("API-MANUAL-GATE");
            jdbc.update("DELETE FROM svc_card WHERE id=?",card);
        }
    }

    /** #56–#58: lọc quyền trước phân trang, chỉ duyệt mới vào, version thật và dấu vết không bị xóa. */
    @Test
    void visitorApprovalScopeAndEntryVersionsOverHttp() throws Exception {
        var student=login(STUDENT);var guard=login("baove1");
        var ids=new java.util.ArrayList<Long>();
        try {
            for(String state:java.util.List.of("PENDING","REJECTED","APPROVED")) {
                long id=jdbc.queryForObject("INSERT INTO svc_visitor(host_student_id,name_encrypted,identity_encrypted,valid_from,valid_to,state) VALUES('21110001','fixture','API-VISITOR-SCOPE',now()-interval '1 hour',now()+interval '1 hour',?) RETURNING id",Long.class,state);
                ids.add(id);
            }
            var list=get(guard,ApiRoutes.VISITORS+"?page=1&size=8");
            assertEquals(200,list.statusCode(),list.body());
            assertTrue(json.readTree(list.body()).valueStream().allMatch(v->"APPROVED".equals(v.path("state").asString())));
            assertEquals(404,get(guard,ApiRoutes.VISITORS+"/"+ids.get(0)).statusCode());
            assertEquals(409,post(guard,ApiRoutes.VISITORS+"/"+ids.get(0)+"/check-in","").statusCode());
            assertEquals(409,post(guard,ApiRoutes.VISITORS+"/"+ids.get(1)+"/check-in","").statusCode());
            long approved=ids.get(2);
            assertEquals(403,post(student,ApiRoutes.VISITORS+"/"+approved+"/check-in","").statusCode());
            var checkIn=post(guard,ApiRoutes.VISITORS+"/"+approved+"/check-in","");
            assertEquals(200,checkIn.statusCode(),checkIn.body());
            assertEquals(2,json.readTree(checkIn.body()).path("version").asInt());
            assertEquals(2,json.readTree(get(guard,ApiRoutes.VISITORS+"/"+approved).body()).path("version").asInt());
            assertEquals(409,post(guard,ApiRoutes.VISITORS+"/"+approved+"/check-in","").statusCode());
            var checkOut=post(guard,ApiRoutes.VISITORS+"/"+approved+"/check-out","");
            assertEquals(200,checkOut.statusCode(),checkOut.body());
            assertEquals(3,json.readTree(checkOut.body()).path("version").asInt());
            assertEquals(3,json.readTree(get(student,ApiRoutes.VISITORS+"/"+approved).body()).path("version").asInt());
            // Hồ sơ lỗi cũ có lượt nhưng còn PENDING phải giữ lại để đối soát.
            jdbc.update("UPDATE svc_visitor SET checked_in_at=now() WHERE id=?",ids.get(0));
            assertEquals(409,send(student.client(),mutate(student,ApiRoutes.VISITORS+"/"+ids.get(0),"DELETE","")).statusCode());
            assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM svc_visitor WHERE id=?",Integer.class,ids.get(0)));
            jdbc.update("UPDATE svc_visitor SET valid_to=now()-interval '1 second' WHERE id=?",approved);
            assertEquals(409,post(guard,ApiRoutes.VISITORS+"/"+approved+"/check-in","").statusCode());
        } finally {
            for(long id:ids) jdbc.update("DELETE FROM svc_visitor WHERE id=?",id);
        }
    }

    /** #52: contract 1.000 ký tự và tiền tố REVIEW không gây lỗi SQL; vượt contract trả 422. */
    @Test
    void disciplineLongDecisionAndAppealReasonsOverHttp() throws Exception {
        var admin=login("admin");var student=login(STUDENT);
        for(int length:new int[]{255,256,1000}) {
            int id=jdbc.queryForObject("INSERT INTO vi_pham(masinhvien,noidungvipham,mucdo,ngayvipham,trangthai) VALUES('21110001','API CRUD violation long decision','LOW','2026-10-01','CHUA_XU_LY') RETURNING mavipham",Integer.class);
            UUID file=UUID.randomUUID();
            int owner=jdbc.queryForObject("SELECT mataikhoan FROM tai_khoan WHERE tendangnhap='admin'",Integer.class);
            jdbc.update("INSERT INTO svc_file(id,owner_id,kind,object_key,mime,size_bytes,sha256,key_id,nonce,tag,state,scanned_at,expires_at) VALUES(?,?,'EVIDENCE',?,'image/png',1,?,'api-fixture',?,?,'DRAFT',now(),now()+interval '1 hour')",file,owner,"test/"+file,"a".repeat(64),new byte[12],new byte[16]);
            try {
                String decision="D".repeat(length);
                String body=json.writeValueAsString(java.util.Map.of("violationIds",java.util.List.of(id),"decision",decision,"reason","Xác minh","evidenceFileId",file,"versions",java.util.Map.of(Integer.toString(id),1)));
                var invalid=post(admin,"/api/v1/violations/decisions",body.replace(decision,"D".repeat(1001)));
                assertEquals(422,invalid.statusCode(),invalid.body());
                var result=post(admin,"/api/v1/violations/decisions",body);
                assertEquals(200,result.statusCode(),result.body());
                assertEquals(json.readTree(get(admin,"/api/v1/violations/"+id).body()).path("semesterCount").asInt(),
                        json.readTree(result.body()).get(0).path("semesterCount").asInt());
                assertEquals(decision,jdbc.queryForObject("SELECT hinhthucxuly FROM vi_pham WHERE mavipham=?",String.class,id));
                long decisionId=jdbc.queryForObject("SELECT id FROM svc_discipline_decision WHERE violation_id=?",Long.class,id);
                for(int reasonLength:new int[]{247,248,1000}) {
                    var appeal=post(student,"/api/v1/decisions/"+decisionId+"/appeals","{\"content\":\"Đề nghị xem lại\"}");
                    assertEquals(200,appeal.statusCode(),appeal.body());
                    long appealId=Long.parseLong(json.readTree(appeal.body()).path("id").asString());
                    String reason="R".repeat(reasonLength);
                    var response=post(admin,"/api/v1/appeals/"+appealId+"/response",json.writeValueAsString(java.util.Map.of("accepted",true,"reason",reason,"version",1)));
                    assertEquals(200,response.statusCode(),response.body());
                    assertEquals("REVIEW: "+reason,jdbc.queryForObject("SELECT hinhthucxuly FROM vi_pham WHERE mavipham=?",String.class,id));
                }
            } finally {
                jdbc.update("DELETE FROM svc_appeal WHERE decision_id IN (SELECT id FROM svc_discipline_decision WHERE violation_id=?)",id);
                jdbc.update("DELETE FROM svc_discipline_decision WHERE violation_id=?",id);
                jdbc.update("DELETE FROM vi_pham WHERE mavipham=?",id);
                jdbc.update("DELETE FROM svc_file WHERE id=?",file);
            }
        }
    }

    /** #55: chỉ đếm đã xử lý trong học kỳ, kiểm tra biên 2/3 và hồ sơ chưa xác nhận. */
    @Test
    void disciplineCountUsesProcessedViolationsInSameSemesterOverHttp() throws Exception {
        var admin=login("admin");
        var ids=new java.util.ArrayList<Integer>();
        try {
            for(int n=0;n<3;n++) {
                int id=jdbc.queryForObject("INSERT INTO vi_pham(masinhvien,noidungvipham,mucdo,ngayvipham,trangthai) VALUES('21110091','API CRUD violation count','LOW','2026-10-01','CHUA_XU_LY') RETURNING mavipham",Integer.class);
                ids.add(id);
            }
            String path="/api/v1/violations/"+ids.get(0);
            assertEquals(0,json.readTree(get(admin,path).body()).path("semesterCount").asInt());
            jdbc.update("UPDATE vi_pham SET trangthai='DA_XU_LY',hinhthucxuly='Nhắc nhở' WHERE mavipham IN (?,?)",ids.get(0),ids.get(1));
            assertEquals(2,json.readTree(get(admin,path).body()).path("semesterCount").asInt());
            jdbc.update("UPDATE vi_pham SET trangthai='DA_XU_LY',hinhthucxuly='Nhắc nhở' WHERE mavipham=?",ids.get(2));
            assertEquals(3,json.readTree(get(admin,path).body()).path("semesterCount").asInt());
            jdbc.update("UPDATE vi_pham SET ngayvipham='2026-01-01' WHERE mavipham=?",ids.get(2));
            assertEquals(2,json.readTree(get(admin,path).body()).path("semesterCount").asInt());
            int pending=jdbc.queryForObject("INSERT INTO vi_pham(masinhvien,noidungvipham,mucdo,ngayvipham,trangthai) VALUES('21110091','API CRUD violation count','LOW','2026-10-01','CHUA_XU_LY') RETURNING mavipham",Integer.class);
            ids.add(pending);
            var queue=json.readTree(get(admin,"/api/v1/violations/pending?page=1&size=100").body());
            var row=queue.valueStream().filter(v->v.path("id").asInt()==pending).findFirst().orElseThrow();
            assertEquals(2,row.path("semesterCount").asInt());
            assertEquals("ACTIVE",jdbc.queryForObject("SELECT trangthai FROM tai_khoan WHERE tendangnhap='sv_new_nam'",String.class));
        } finally {
            for(int id:ids) jdbc.update("DELETE FROM vi_pham WHERE mavipham=?",id);
        }
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
