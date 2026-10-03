package vn.id.caohoangphuc.cnpm.api;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Calls the room and bed read APIs over HTTP with a real PostgreSQL database. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomCatalogApiBlackBoxIT {
    private static final String PASSWORD = "123456";

    @LocalServerPort int port;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;

    private record ApiSession(HttpClient client, String csrfToken) {}

    @AfterEach
    void removeCrudTestData() {
        jdbc.update("DELETE FROM phong WHERE maphong='API-CRUD-ROOM'");
        jdbc.update("DELETE FROM AUDIT_LOG WHERE TenBang IN ('ROOM_CREATED','ROOM_UPDATED','ROOM_DELETED',"
                + "'BED_CREATED','BED_UPDATED','BED_DELETED')");
        jdbc.update("DELETE FROM svc_outbox WHERE event_type IN ('ROOM_CREATED','ROOM_UPDATED','ROOM_DELETED',"
                + "'BED_CREATED','BED_UPDATED','BED_DELETED')");
    }

    @Test
    void administratorCanCreateReadUpdateAndDeleteRoomAndBed() throws Exception {
        var admin = login("admin");
        var roomBody = "{\"id\":\"API-CRUD-ROOM\",\"buildingId\":\"TOA-C\","
                + "\"roomTypeId\":\"LP-4\",\"number\":\"API-CRUD\",\"floor\":1,"
                + "\"capacity\":1,\"basePrice\":100000,\"status\":\"TRONG\"}";
        var createdRoom = send(admin.client(), request(admin, ApiRoutes.ROOMS, "POST", roomBody));
        assertEquals(200, createdRoom.statusCode(), createdRoom.body());

        var updatedRoom = send(admin.client(), request(admin,
                ApiRoutes.ROOM.replace("{id}", "API-CRUD-ROOM"), "PUT", roomBody.replace("100000", "120000")));
        assertEquals(200, updatedRoom.statusCode(), updatedRoom.body());
        assertEquals(120000, jdbc.queryForObject(
                "SELECT giaphongcoban FROM phong WHERE maphong='API-CRUD-ROOM'", Long.class));

        var bedBody = "{\"id\":\"API-CRUD-BED\",\"roomId\":\"API-CRUD-ROOM\","
                + "\"number\":\"1\",\"position\":\"Tang 1\",\"status\":\"TRONG\"}";
        var createdBed = send(admin.client(), request(admin, ApiRoutes.BEDS, "POST", bedBody));
        assertEquals(200, createdBed.statusCode(), createdBed.body());
        var updatedBed = send(admin.client(), request(admin, ApiRoutes.BED.replace("{id}", "API-CRUD-BED"),
                "PUT", bedBody.replace("Tang 1", "Tang 2")));
        assertEquals(200, updatedBed.statusCode(), updatedBed.body());
        assertEquals("Tang 2", jdbc.queryForObject(
                "SELECT vitri FROM giuong WHERE magiuong='API-CRUD-BED'", String.class));

        var deletedBed = send(admin.client(), request(admin, ApiRoutes.BED.replace("{id}", "API-CRUD-BED"),
                "DELETE", ""));
        assertEquals(200, deletedBed.statusCode(), deletedBed.body());
        var deletedRoom = send(admin.client(), request(admin,
                ApiRoutes.ROOM.replace("{id}", "API-CRUD-ROOM"), "DELETE", ""));
        assertEquals(200, deletedRoom.statusCode(), deletedRoom.body());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM phong WHERE maphong='API-CRUD-ROOM'", Integer.class));
    }

    @Test
    void studentCannotChangeRoomOrBedCatalogue() throws Exception {
        var student = login("sv001");
        var body = "{\"id\":\"NO-ACCESS\",\"buildingId\":\"TOA-C\",\"number\":\"NO-ACCESS\","
                + "\"floor\":1,\"capacity\":1,\"basePrice\":100000,\"status\":\"TRONG\"}";

        var response = send(student.client(), request(student, ApiRoutes.ROOMS, "POST", body));

        assertEquals(403, response.statusCode(), response.body());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM phong WHERE maphong='NO-ACCESS'", Integer.class));
    }

    @Test
    void studentCanReadRoomsAndBedsWithPagination() throws Exception {
        var student = login("sv001");
        var roomsResponse = send(student.client(), HttpRequest.newBuilder(uri(ApiRoutes.ROOMS
                        + "?page=1&pageSize=5"))
                .GET().build());

        assertEquals(200, roomsResponse.statusCode(), roomsResponse.body());
        var rooms = json.readTree(roomsResponse.body());
        assertEquals(1, rooms.path("page").asInt());
        assertEquals(5, rooms.path("pageSize").asInt());
        assertTrue(rooms.path("total").asLong() > 0);
        assertEquals(5, rooms.path("items").size());

        String roomId = rooms.path("items").get(0).path("id").asString();
        var roomResponse = send(student.client(), HttpRequest.newBuilder(uri(
                        ApiRoutes.ROOM.replace("{id}", roomId)))
                .GET().build());
        assertEquals(200, roomResponse.statusCode(), roomResponse.body());
        assertEquals(roomId, json.readTree(roomResponse.body()).path("id").asString());

        var bedsResponse = send(student.client(), HttpRequest.newBuilder(uri(
                        ApiRoutes.ROOM_BEDS.replace("{id}", roomId) + "?page=1&pageSize=10"))
                .GET().build());

        assertEquals(200, bedsResponse.statusCode(), bedsResponse.body());
        var beds = json.readTree(bedsResponse.body());
        assertTrue(beds.path("total").asLong() > 0);
        assertFalse(beds.path("items").isEmpty());
        assertTrue(beds.path("items").get(0).hasNonNull("status"));
        assertTrue(beds.path("items").get(0).path("version").asLong() >= 1);
    }

    @Test
    void studentCanReadBedsForRoomA101OverHttp() throws Exception {
        var student = login("sv001");
        var response = send(student.client(), HttpRequest.newBuilder(uri(
                        ApiRoutes.ROOM_BEDS.replace("{id}", "A-101") + "?page=1&pageSize=20"))
                .GET().build());

        assertEquals(200, response.statusCode(), response.body());
        var beds = json.readTree(response.body());
        assertEquals(1, beds.path("page").asInt());
        assertEquals(20, beds.path("pageSize").asInt());
        assertTrue(beds.path("items").isArray());
        assertTrue(beds.path("total").asLong() >= beds.path("items").size());
    }

    @Test
    void roomApiReturnsVietnameseRoomTypeTextOverHttp() throws Exception {
        var student = login("sv001");
        var response = send(student.client(), HttpRequest.newBuilder(uri(
                ApiRoutes.ROOM.replace("{id}", "A-101"))).GET().build());

        assertEquals(200, response.statusCode(), response.body());
        assertEquals("Phòng Tiêu Chuẩn 4 Giường", json.readTree(response.body()).path("roomType").asString());
    }

    @Test
    void accountingRoleCannotReadRoomCatalogue() throws Exception {
        var accountant = login("ketoan1");
        var response = send(accountant.client(), HttpRequest.newBuilder(uri(ApiRoutes.ROOMS))
                .GET().build());

        assertEquals(403, response.statusCode(), response.body());
        assertEquals("FORBIDDEN", json.readTree(response.body()).path("code").asString());
    }

    @Test
    void unknownRoomReturnsNotFoundInsteadOfAnEmptyBedList() throws Exception {
        var student = login("sv001");
        var response = send(student.client(), HttpRequest.newBuilder(uri(
                        ApiRoutes.ROOM_BEDS.replace("{id}", "ROOM-DOES-NOT-EXIST")))
                .GET().build());

        assertEquals(404, response.statusCode(), response.body());
        assertEquals("NOT_FOUND", json.readTree(response.body()).path("code").asString());
    }

    @Test
    void unknownRoomDetailReturnsNotFound() throws Exception {
        var student = login("sv001");
        var response = send(student.client(), HttpRequest.newBuilder(uri(
                        ApiRoutes.ROOM.replace("{id}", "ROOM-DOES-NOT-EXIST")))
                .GET().build());

        assertEquals(404, response.statusCode(), response.body());
        assertEquals("NOT_FOUND", json.readTree(response.body()).path("code").asString());
    }

    private ApiSession login(String username) throws Exception {
        var cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        var client = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
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

    private HttpResponse<String> send(HttpClient client, HttpRequest request) throws Exception {
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpRequest request(ApiSession session, String path, String method, String body) {
        var builder = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .header("X-CSRF-TOKEN", session.csrfToken())
                .header("Idempotency-Key", java.util.UUID.randomUUID().toString());
        var publisher = body.isEmpty() ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        return builder.method(method, publisher).build();
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }
}
