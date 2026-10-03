package vn.id.caohoangphuc.cnpm.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.service.RoomCatalogService;

/** HTTP queries for the room catalogue and its beds. */
@Component
public class RoomController {
    private final RoomCatalogService rooms;
    private final ApiSupport api;

    /** Connects room lookup routes to the service and shared request handling. */
    public RoomController(RoomCatalogService rooms, ApiSupport api) {
        this.rooms = rooms;
        this.api = api;
    }

    /** Returns a page of rooms and the number of beds currently available in each room. */
    public ServerResponse list(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> rooms.list(actor,
                api.integerParameter(request, "page", 1),
                api.integerParameter(request, "pageSize", 20)));
    }

    /** Returns the current bed statuses for one room. */
    public ServerResponse beds(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> rooms.beds(actor,
                request.pathVariable("id"),
                api.integerParameter(request, "page", 1),
                api.integerParameter(request, "pageSize", 20)));
    }
}
