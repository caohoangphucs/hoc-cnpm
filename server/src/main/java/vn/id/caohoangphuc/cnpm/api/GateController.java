package vn.id.caohoangphuc.cnpm.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.EntryDto;
import vn.id.caohoangphuc.cnpm.service.GateService;

/** HTTP actions for security staff recording student entry and exit. */
@Component
public class GateController {
    private final GateService gate;
    private final ApiSupport api;

    public GateController(GateService gate, ApiSupport api) {
        this.gate = gate;
        this.api = api;
    }

    /** Records a card event; the service owns duplicate and card-validity checks. */
    public ServerResponse record(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> gate.record(
                actor, context, request.body(EntryDto.class)));
    }
}
