package vn.id.caohoangphuc.cnpm.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.RoomWriteDto;
import vn.id.caohoangphuc.cnpm.api.dto.BedWriteDto;
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

    /** Returns one room by its public identifier. */
    public ServerResponse get(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> rooms.get(actor, request.pathVariable("id")));
    }

    /** Creates a room from administrator-managed catalogue data. */
    public ServerResponse createRoom(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> rooms.createRoom(actor, context, request.body(RoomWriteDto.class)));
    }

    /** Replaces editable fields for an existing room. */
    public ServerResponse updateRoom(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> rooms.updateRoom(actor, context,
                request.pathVariable("id"), request.body(RoomWriteDto.class)));
    }

    /** Deletes a room by its identifier. */
    public ServerResponse deleteRoom(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> rooms.deleteRoom(actor, context, request.pathVariable("id")));
    }

    /** Creates a bed in the selected room. */
    public ServerResponse createBed(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> rooms.createBed(actor, context, request.body(BedWriteDto.class)));
    }

    /** Replaces editable fields for an existing bed. */
    public ServerResponse updateBed(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> rooms.updateBed(actor, context,
                request.pathVariable("id"), request.body(BedWriteDto.class)));
    }

    /** Deletes a bed by its identifier. */
    public ServerResponse deleteBed(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> rooms.deleteBed(actor, context, request.pathVariable("id")));
    }

    /** Returns the current bed statuses for one room. */
    public ServerResponse beds(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> rooms.beds(actor,
                request.pathVariable("id"),
                api.integerParameter(request, "page", 1),
                api.integerParameter(request, "pageSize", 20)));
    }
}
