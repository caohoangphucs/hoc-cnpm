package vn.id.caohoangphuc.cnpm.service;

import vn.id.caohoangphuc.cnpm.api.dto.BedListDto;
import vn.id.caohoangphuc.cnpm.api.dto.RoomDto;
import vn.id.caohoangphuc.cnpm.api.dto.RoomWriteDto;
import vn.id.caohoangphuc.cnpm.api.dto.BedWriteDto;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.api.dto.RoomListDto;
import vn.id.caohoangphuc.cnpm.persistence.service.RoomCatalogRepository;

/** Reads room and bed information for authenticated students and administrators. */
public class RoomCatalogService {
    private final RoomCatalogRepository repository;
    private final ServiceContext context;

    /** Connects the room catalogue to SQL reads and shared authorization checks. */
    public RoomCatalogService(RoomCatalogRepository repository, ServiceContext context) {
        this.repository = repository;
        this.context = context;
    }

    /** Lists rooms in a stable order with counts of beds that remain bookable. */
    public RoomListDto list(Actor actor, int page, int pageSize) {
        context.page(page, pageSize);
        return context.transaction(() -> {
            context.require(actor, "SINHVIEN", "ADMIN");
            long offset = (long) (page - 1) * pageSize;
            var result = repository.rooms(pageSize, offset);
            return new RoomListDto(result.items(), (long) page, (long) pageSize, result.total());
        });
    }

    /** Returns one room after checking the caller can access the room catalogue. */
    public RoomDto get(Actor actor, String roomId) {
        context.text(roomId, 20, "roomId");
        return context.transaction(() -> {
            context.require(actor, "SINHVIEN", "ADMIN");
            var room = repository.room(roomId);
            if (room == null) throw ServiceException.notFound("Room");
            return room;
        });
    }

    /** Creates a room after validating the submitted fields and requiring an administrator. */
    public CommandResultDto createRoom(Actor actor, RequestContext request, RoomWriteDto input) {
        context.validate(input);
        validateRoom(input);
        return context.write(actor, request, "POST:/api/v1/rooms", input, CommandResultDto.class, () -> {
            repository.createRoom(input);
            context.event(actor, request, "ROOM_CREATED", input.id(), input);
            return result(input.id(), request);
        }, "ADMIN");
    }

    /** Replaces editable room fields while leaving derived occupancy counts untouched. */
    public CommandResultDto updateRoom(Actor actor, RequestContext request, String id, RoomWriteDto input) {
        context.validate(input);
        validateRoom(input);
        if (!id.equals(input.id())) throw ServiceException.invalid("Room ID must match the URL");
        return context.write(actor, request, "PUT:/api/v1/rooms/" + id, input, CommandResultDto.class, () -> {
            if (!repository.updateRoom(id, input)) throw ServiceException.notFound("Room");
            context.event(actor, request, "ROOM_UPDATED", id, input);
            return result(id, request);
        }, "ADMIN");
    }

    /** Deletes a room using the database's declared cascade and restrict rules. */
    public CommandResultDto deleteRoom(Actor actor, RequestContext request, String id) {
        return context.write(actor, request, "DELETE:/api/v1/rooms/" + id, id, CommandResultDto.class, () -> {
            if (repository.roomHasHistory(id))
                throw ServiceException.conflict("Room has linked student or operational records");
            if (!repository.deleteRoom(id)) throw ServiceException.notFound("Room");
            context.event(actor, request, "ROOM_DELETED", id, java.util.Map.of("id", id));
            return result(id, request);
        }, "ADMIN");
    }

    /** Creates a bed in a room. */
    public CommandResultDto createBed(Actor actor, RequestContext request, BedWriteDto input) {
        context.validate(input);
        validateBed(input);
        return context.write(actor, request, "POST:/api/v1/beds", input, CommandResultDto.class, () -> {
            if (!repository.roomExists(input.roomId())) throw ServiceException.notFound("Room");
            repository.createBed(input);
            context.event(actor, request, "BED_CREATED", input.id(), input);
            return result(input.id(), request);
        }, "ADMIN");
    }

    /** Replaces editable fields on a bed. */
    public CommandResultDto updateBed(Actor actor, RequestContext request, String id, BedWriteDto input) {
        context.validate(input);
        validateBed(input);
        if (!id.equals(input.id())) throw ServiceException.invalid("Bed ID must match the URL");
        return context.write(actor, request, "PUT:/api/v1/beds/" + id, input, CommandResultDto.class, () -> {
            if (!repository.roomExists(input.roomId())) throw ServiceException.notFound("Room");
            if (!repository.updateBed(id, input)) throw ServiceException.notFound("Bed");
            context.event(actor, request, "BED_UPDATED", id, input);
            return result(id, request);
        }, "ADMIN");
    }

    /** Deletes a bed using the database's declared cascade and restrict rules. */
    public CommandResultDto deleteBed(Actor actor, RequestContext request, String id) {
        return context.write(actor, request, "DELETE:/api/v1/beds/" + id, id, CommandResultDto.class, () -> {
            if (repository.bedHasHistory(id))
                throw ServiceException.conflict("Bed has linked registration or residency records");
            if (!repository.deleteBed(id)) throw ServiceException.notFound("Bed");
            context.event(actor, request, "BED_DELETED", id, java.util.Map.of("id", id));
            return result(id, request);
        }, "ADMIN");
    }

    /** Checks required identifiers and prevents unsupported room or bed states. */
    private void validateRoom(RoomWriteDto room) {
        context.text(room.buildingId(), 20, "buildingId");
        context.text(room.number(), 20, "number");
        if (!java.util.List.of("TRONG", "CON_CHO", "DAY", "DANG_SUA", "KHOA").contains(room.status()))
            throw ServiceException.invalid("Invalid room status");
    }

    /** Checks required identifiers and prevents unsupported bed states. */
    private void validateBed(BedWriteDto bed) {
        context.text(bed.roomId(), 20, "roomId");
        context.text(bed.number(), 20, "number");
        if (!java.util.List.of("TRONG", "GIU_CHO", "DANG_O", "KHOA").contains(bed.status()))
            throw ServiceException.invalid("Invalid bed status");
    }

    /** Builds the standard result returned after a room or bed change. */
    private CommandResultDto result(String id, RequestContext request) {
        return new CommandResultDto(id, "COMPLETED", 1L, request.requestId());
    }

    /** Lists beds for one room and reports a not-found error for an unknown room. */
    public BedListDto beds(Actor actor, String roomId, int page, int pageSize) {
        context.page(page, pageSize);
        context.text(roomId, 20, "roomId");
        return context.transaction(() -> {
            context.require(actor, "SINHVIEN", "ADMIN");
            if (!repository.roomExists(roomId)) throw ServiceException.notFound("Room");

            long offset = (long) (page - 1) * pageSize;
            var result = repository.beds(roomId, pageSize, offset);
            return new BedListDto(result.items(), (long) page, (long) pageSize, result.total());
        });
    }
}
