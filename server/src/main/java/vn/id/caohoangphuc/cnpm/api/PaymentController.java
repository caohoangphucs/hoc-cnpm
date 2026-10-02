package vn.id.caohoangphuc.cnpm.api;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.PaymentDto;
import vn.id.caohoangphuc.cnpm.service.PaymentService;

/** HTTP actions for creating a payment session and checking its status. */
@Component
public class PaymentController {
    private final PaymentService payments;
    private final ApiSupport api;

    public PaymentController(PaymentService payments, ApiSupport api) {
        this.payments = payments;
        this.api = api;
    }

    /** Starts a payment session through the configured gateway adapter. */
    public ServerResponse create(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> payments.create(
                actor, context, request.body(PaymentDto.class)));
    }

    /** Returns a payment session only when the service permits this account to see it. */
    public ServerResponse status(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> payments.status(
                actor, UUID.fromString(request.pathVariable("id"))));
    }
}
