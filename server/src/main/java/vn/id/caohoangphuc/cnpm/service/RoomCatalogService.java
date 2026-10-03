package vn.id.caohoangphuc.cnpm.service;

import vn.id.caohoangphuc.cnpm.api.dto.BedListDto;
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
