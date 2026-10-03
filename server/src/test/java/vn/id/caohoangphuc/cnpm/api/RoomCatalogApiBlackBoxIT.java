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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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

    private record ApiSession(HttpClient client, String csrfToken) {}

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

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }
}
