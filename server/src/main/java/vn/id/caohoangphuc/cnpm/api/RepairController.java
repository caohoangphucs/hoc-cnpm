package vn.id.caohoangphuc.cnpm.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.RepairDto;
import vn.id.caohoangphuc.cnpm.api.dto.AssignmentDto;
import vn.id.caohoangphuc.cnpm.api.dto.RepairCompleteDto;
import vn.id.caohoangphuc.cnpm.api.dto.RepairScheduleDto;
import vn.id.caohoangphuc.cnpm.service.RepairService;

/** HTTP actions for reporting and listing repair requests. */
@Component
public class RepairController {
    private final RepairService repairs;
    private final ApiSupport api;

    /** Connects repair routes to the service and shared HTTP request handling. */
    public RepairController(RepairService repairs, ApiSupport api) {
        this.repairs = repairs;
        this.api = api;
    }

    /** Records a repair request for the authenticated student. */
    public ServerResponse create(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> repairs.create(
                actor, context, request.body(RepairDto.class)));
    }

    /** Lists repairs visible to the authenticated student, technician, or administrator. */
    public ServerResponse list(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> repairs.list(actor,
                api.integerParameter(request, "page", 1),
                api.integerParameter(request, "size", 20)));
    }

    /** Reads one repair request visible to this student, technician, or administrator. */
    public ServerResponse get(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> repairs.get(actor,
                Integer.parseInt(request.pathVariable("id"))));
    }

    /** Deletes a new repair request before assignment or materials are recorded. */
    public ServerResponse delete(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> repairs.delete(actor, context,
                Integer.parseInt(request.pathVariable("id"))));
    }

    /** Assigns an open repair request to a technician. */
    public ServerResponse assign(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> repairs.assign(actor, context,
                Integer.parseInt(request.pathVariable("id")), request.body(AssignmentDto.class)));
    }

    /** Changes the technician's scheduled time and warranty date. */
    public ServerResponse reschedule(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> {
            var input = request.body(RepairScheduleDto.class);
            return repairs.reschedule(actor, context, Integer.parseInt(request.pathVariable("id")),
                    input.version(), input.scheduledAt(), input.warrantyUntil(), input.note());
        });
    }

    /** Completes a repair and returns shortages when the required materials are unavailable. */
    public ServerResponse complete(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> repairs.complete(actor, context,
                Integer.parseInt(request.pathVariable("id")), request.body(RepairCompleteDto.class)));
    }
}
