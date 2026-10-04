package vn.id.caohoangphuc.cnpm.api;

import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

/** Tests multipart/image endpoints and the whole UC-04/12 flow over real HTTP and PostgreSQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EvidenceFileApiIT {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    @Autowired PasswordEncoder passwords;
    @Autowired vn.id.caohoangphuc.cnpm.service.TotpService totp;
    private record Session(HttpClient client, String csrf) {}
    private final List<UUID> uploaded = new ArrayList<>();
    private Integer orphanAccount;

    /** Restore business fixtures and remove uploaded ciphertext after each test. */
    @AfterEach
    void restoreSeed() {
        jdbc.update("DELETE FROM svc_contract WHERE residency_id IN (SELECT maluutru FROM luu_tru WHERE masinhvien='21110091')");
        jdbc.update("DELETE FROM luu_tru WHERE masinhvien='21110091'");
        jdbc.update("DELETE FROM svc_profile WHERE booking_id IN (SELECT madangky FROM dang_ky_phong WHERE masinhvien='21110091')");
        jdbc.update("DELETE FROM dang_ky_phong WHERE masinhvien='21110091'");
        jdbc.update("UPDATE sinh_vien SET maphonghientai=NULL,trangthai='CHO_DUYET' WHERE masinhvien='21110091'");
        jdbc.update("UPDATE giuong SET trangthai='TRONG',version=1 WHERE magiuong='DEMO-A-4'");
        jdbc.update("UPDATE phong SET dango=1,trangthai='CON_CHO' WHERE maphong='DEMO-A'");
        for (UUID id : uploaded) jdbc.update("DELETE FROM svc_file WHERE id=?", id);
        if (orphanAccount != null) jdbc.update("DELETE FROM tai_khoan WHERE mataikhoan=?",orphanAccount);
    }

    /** Main flow: upload three real images, book a bed, view evidence as admin and approve. */
    @Test
    void uploadBookViewAndApproveThroughHttp() throws Exception {
        var student = login("sv_new_nam");
        var admin = login("admin");
        var windows = get(student,"/api/v1/booking-windows");
        assertEquals(200,windows.statusCode());
        assertTrue(text(windows).contains("900001"));
        var ids = new ArrayList<String>();
        for (String kind : List.of("CCCD_FRONT","CCCD_BACK","STUDENT_CARD")) {
            var response = upload(student,kind,"image/png",sample(),true);
            assertEquals(200,response.statusCode(),text(response));
            String id = json.readTree(response.body()).path("id").asString();
            ids.add(id);
            assertEquals(404,get(admin,"/api/v1/files/"+id+"/content").statusCode());
        }
        var booking = post(student,"/api/v1/bookings",json.writeValueAsString(java.util.Map.of(
                "bedId","DEMO-A-4","windowId",900001,"fileIds",ids,"acceptedTerms",true)));
        assertEquals(200,booking.statusCode(),text(booking));
        int id = Integer.parseInt(json.readTree(booking.body()).path("id").asString());
        for (String file : ids) {
            var image = get(admin,"/api/v1/files/"+file+"/content");
            assertEquals(200,image.statusCode(),text(image));
            assertNotNull(ImageIO.read(new java.io.ByteArrayInputStream(image.body())));
            assertEquals(404,get(login("sv_new_nu"),"/api/v1/files/"+file+"/content").statusCode());
        }
        assertEquals(404,get(login("sv_new_nu"),"/api/v1/bookings/"+id).statusCode());
        var decision = post(admin,"/api/v1/bookings/"+id+"/decision","{\"decision\":\"DUYET\",\"version\":1}");
        assertEquals(200,decision.statusCode(),text(decision));
        assertEquals("DANG_O",jdbc.queryForObject("SELECT trangthai FROM giuong WHERE magiuong='DEMO-A-4'",String.class));
    }

    /** Alternative flow: JPEG and explicit downloads retain valid content, headers and checksum. */
    @Test
    void jpegContentIsEncryptedAndDownloadIsPrivate() throws Exception {
        var student = login("sv_new_nam");
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(12,8,BufferedImage.TYPE_INT_RGB),"JPEG",output);
        var response = upload(student,"CCCD_FRONT","image/jpeg",output.toByteArray(),true);
        assertEquals(200,response.statusCode(),text(response));
        var metadata = json.readTree(response.body());
        String id = metadata.path("id").asString();
        byte[] ciphertext = jdbc.queryForObject("SELECT encrypted_bytes FROM svc_file_content WHERE file_id=?",byte[].class,UUID.fromString(id));
        var content = get(student,"/api/v1/files/"+id+"/content?download=true");
        assertEquals(200,content.statusCode());
        assertFalse(java.util.Arrays.equals(ciphertext,content.body()));
        assertEquals("image/jpeg",content.headers().firstValue("Content-Type").orElse(""));
        assertTrue(content.headers().firstValue("Content-Disposition").orElse("").startsWith("attachment;"));
        assertTrue(content.headers().firstValue("Cache-Control").orElse("").contains("no-store"));
        assertEquals("nosniff",content.headers().firstValue("X-Content-Type-Options").orElse(""));
        String hash = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(content.body()));
        assertEquals(metadata.path("sha256").asString(),hash);
        assertEquals(200,get(student,"/api/v1/files/"+id).statusCode());
        assertEquals(404,get(login("sv_new_nu"),"/api/v1/files/"+id).statusCode());
    }

    /** Invalid files must fail based on bytes, not a trusted extension or submitted MIME. */
    @Test
    void rejectsEmptyFakeUnsupportedAndMismatchedImages() throws Exception {
        var student = login("sv_new_nam");
        assertEquals(422,upload(student,"CCCD_FRONT","image/png",new byte[0],true).statusCode());
        assertEquals(422,upload(student,"CCCD_FRONT","image/png","<script>bad</script>".getBytes(StandardCharsets.UTF_8),true).statusCode());
        assertEquals(422,upload(student,"CCCD_FRONT","application/pdf",sample(),true).statusCode());
        assertEquals(422,upload(student,"CCCD_FRONT","image/jpeg",sample(),true).statusCode());
        assertEquals(422,upload(student,"NOT_ALLOWED","image/png",sample(),true).statusCode());
    }

    /** Boundary values: exactly 5 MiB is accepted, one byte over and oversized dimensions are rejected. */
    @Test
    void enforcesByteAndPixelBoundaries() throws Exception {
        var student = login("sv_new_nam");
        byte[] large = java.util.Arrays.copyOf(sample(),5*1024*1024);
        assertEquals(200,upload(student,"CCCD_FRONT","image/png",large,true).statusCode());
        assertEquals(413,upload(student,"CCCD_BACK","image/png",java.util.Arrays.copyOf(large,large.length+1),true).statusCode());
        assertEquals(413,upload(student,"CCCD_BACK","image/png",new byte[7*1024*1024],true).statusCode());
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(6001,1,BufferedImage.TYPE_INT_RGB),"PNG",output);
        assertEquals(422,upload(student,"STUDENT_CARD","image/png",output.toByteArray(),true).statusCode());
    }

    /** Missing session/CSRF, unknown IDs and wrong roles do not expose or create a file. */
    @Test
    void authenticationCsrfAndIdentifiersAreEnforced() throws Exception {
        var anonymous = new Session(HttpClient.newHttpClient(),"");
        UUID id = UUID.randomUUID();
        assertEquals(401,get(anonymous,"/api/v1/files/"+id+"/content").statusCode());
        var student = login("sv_new_nam");
        assertEquals(403,upload(student,"CCCD_FRONT","image/png",sample(),false).statusCode());
        assertEquals(400,get(student,"/api/v1/files/not-a-uuid").statusCode());
        assertEquals(404,get(student,"/api/v1/files/"+id).statusCode());
        assertEquals(403,upload(login("admin"),"CCCD_FRONT","image/png",sample(),true).statusCode());
    }

    /** A student-role account lacking a profile must not turn a null filter into all bookings. */
    @Test
    void studentWithoutProfileCannotReadAllBookings() throws Exception {
        String username = "orphan_"+UUID.randomUUID().toString().substring(0,8);
        orphanAccount = jdbc.queryForObject("INSERT INTO tai_khoan(tendangnhap,matkhauhash,hoten,email,vaitro,trangthai)"
                + " VALUES (?,?,?,?,'SINHVIEN','ACTIVE') RETURNING mataikhoan",Integer.class,
                username,passwords.encode("123456"),"Orphan test",username+"@test.invalid");
        var response = get(login(username),"/api/v1/bookings");
        assertEquals(403,response.statusCode(),text(response));
        assertEquals("STUDENT_PROFILE_REQUIRED",json.readTree(response.body()).path("code").asString());
    }

    /** Enrolled admin must complete OTP before viewing even an attached seed image. */
    @Test
    void pendingAdminOtpCannotReadPrivateEvidence() throws Exception {
        String previous = jdbc.queryForObject("SELECT bimattotp FROM tai_khoan WHERE tendangnhap='admin'",String.class);
        try {
            jdbc.update("UPDATE tai_khoan SET bimattotp=? WHERE tendangnhap='admin'",totp.encrypt("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"));
            String id = jdbc.queryForObject("SELECT file_ids->>0 FROM svc_profile WHERE booking_id=900001",String.class);
            var response = get(login("admin"),"/api/v1/files/"+id+"/content");
            assertEquals(403,response.statusCode(),text(response));
            assertEquals("TWO_FACTOR_REQUIRED",json.readTree(response.body()).path("code").asString());
        } finally {
            jdbc.update("UPDATE tai_khoan SET bimattotp=? WHERE tendangnhap='admin'",previous);
        }
    }

    /** Seed metadata must resolve to actual viewable images, preserving the same booking IDs. */
    @Test
    void seededEvidenceCanBeViewedByOwnerAndAdministratorOnly() throws Exception {
        var owner = login("demo_pending_m1");
        var other = login("demo_pending_f1");
        var admin = login("admin");
        var booking = json.readTree(get(owner,"/api/v1/bookings/900001").body());
        for (var file : booking.path("fileIds")) {
            String path = "/api/v1/files/"+file.asString()+"/content";
            assertEquals(200,get(owner,path).statusCode());
            assertEquals(200,get(admin,path).statusCode());
            assertEquals(404,get(other,path).statusCode());
        }
    }

    /** Build standard multipart bytes with an explicit boundary; do not use MockMvc. */
    private HttpResponse<byte[]> upload(Session session, String kind, String mime, byte[] image, boolean csrf) throws Exception {
        String boundary = "cnpm-"+UUID.randomUUID();
        var output = new ByteArrayOutputStream();
        output.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"kind\"\r\n\r\n"+kind+"\r\n--"+boundary
                + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"demo.png\"\r\nContent-Type: "+mime+"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(image);output.write(("\r\n--"+boundary+"--\r\n").getBytes(StandardCharsets.UTF_8));
        var builder = HttpRequest.newBuilder(uri("/api/v1/files")).header("Content-Type","multipart/form-data; boundary="+boundary);
        if (csrf) builder.header("X-CSRF-TOKEN",session.csrf());
        var response = session.client().send(builder.POST(HttpRequest.BodyPublishers.ofByteArray(output.toByteArray())).build(),HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode()==200) uploaded.add(UUID.fromString(json.readTree(response.body()).path("id").asString()));
        return response;
    }

    /** Use committed demo passwords and real secure session cookies/CSRF exchange. */
    private Session login(String username) throws Exception {
        var client = HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
        var first = client.send(HttpRequest.newBuilder(uri(ApiRoutes.AUTH_SESSION)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
        var session = new Session(client,json.readTree(first.body()).path("csrfToken").asString());
        var response = post(session,ApiRoutes.AUTH_LOGIN,"{\"username\":\""+username+"\",\"password\":\"123456\"}");
        assertEquals(200,response.statusCode(),text(response));
        return new Session(client,json.readTree(response.body()).path("csrfToken").asString());
    }

    /** Get bytes through the actual HTTP server, including private image responses. */
    private HttpResponse<byte[]> get(Session session,String path) throws Exception {
        return session.client().send(HttpRequest.newBuilder(uri(path)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
    }

    /** Send JSON commands with fresh request/idempotency keys to avoid sharing mutation results. */
    private HttpResponse<byte[]> post(Session session,String path,String body) throws Exception {
        return session.client().send(HttpRequest.newBuilder(uri(path)).header("Content-Type","application/json")
                .header("X-CSRF-TOKEN",session.csrf()).header("X-Request-Id",UUID.randomUUID().toString())
                .header("Idempotency-Key",UUID.randomUUID().toString()).POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofByteArray());
    }

    /** Read the synthetic sample shipped in the same artifact that initializes demo evidence. */
    private byte[] sample() throws Exception {
        try (var stream = getClass().getResourceAsStream("/demo-evidence/CCCD_FRONT.png")) { return stream.readAllBytes(); }
    }

    /** Decode JSON error output only for assertions; never log uploaded personal content. */
    private String text(HttpResponse<byte[]> response) { return new String(response.body(),StandardCharsets.UTF_8); }

    /** Keep every test request on the disposable random-port server. */
    private URI uri(String path) { return URI.create("http://127.0.0.1:"+port+path); }
}
