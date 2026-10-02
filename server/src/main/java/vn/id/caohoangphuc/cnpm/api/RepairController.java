package vn.id.caohoangphuc.cnpm.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.RepairDto;
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
}
