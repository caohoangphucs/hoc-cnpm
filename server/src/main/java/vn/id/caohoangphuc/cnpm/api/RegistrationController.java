package vn.id.caohoangphuc.cnpm.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.ApprovalDto;
import vn.id.caohoangphuc.cnpm.service.RegistrationService;

/** HTTP actions for reviewing student room registrations. */
@Component
public class RegistrationController {
    private final RegistrationService registrations;
    private final ApiSupport api;

    public RegistrationController(RegistrationService registrations, ApiSupport api) {
        this.registrations = registrations;
        this.api = api;
    }

    /** Approves or rejects one booking using the submitted version. */
    public ServerResponse decide(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> registrations.decide(actor, context,
                Integer.parseInt(request.pathVariable("id")), request.body(ApprovalDto.class)));
    }
}
