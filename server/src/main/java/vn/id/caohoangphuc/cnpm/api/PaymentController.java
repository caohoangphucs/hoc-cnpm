package vn.id.caohoangphuc.cnpm.api;

import java.util.UUID;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.PaymentDto;
import vn.id.caohoangphuc.cnpm.api.dto.ErrorDto;
import vn.id.caohoangphuc.cnpm.service.RequestContext;
import vn.id.caohoangphuc.cnpm.service.ServiceException;
import vn.id.caohoangphuc.cnpm.service.PaymentService;

/** HTTP actions for creating a payment session and checking its status. */
@Component
public class PaymentController {
    private final PaymentService payments;
    private final ApiSupport api;

    /** Connects payment routes to the service and shared HTTP request handling. */
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

    /** Lists the payment sessions visible to the authenticated user. */
    public ServerResponse list(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> payments.list(actor,
                api.integerParameter(request, "page", 1), api.integerParameter(request, "pageSize", 20)));
    }

    /** Deletes a pending session only before payment processing starts. */
    public ServerResponse delete(ServerRequest request) throws Exception {
        return api.handle(request, (actor, context) -> payments.delete(actor, context,
                UUID.fromString(request.pathVariable("id"))));
    }

    /** Verifies and applies a signed provider callback; callbacks do not require a browser session. */
    public ServerResponse callback(ServerRequest request) {
        var requestId = UUID.randomUUID();
        try {
            Map<String, String> parameters = request.params().toSingleValueMap();
            var verified = payments.verify(request.pathVariable("gateway"), parameters);
            var result = payments.accept(null,
                    new RequestContext(requestId, UUID.randomUUID()), verified);
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(result);
        } catch (ServiceException exception) {
            return ServerResponse.status(exception.getStatus()).contentType(MediaType.APPLICATION_JSON).body(
                    new ErrorDto(exception.getCode(), exception.getMessage(), requestId, Map.of()));
        } catch (IllegalArgumentException exception) {
            return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new ErrorDto("INVALID_CALLBACK", "Payment callback is invalid", requestId, Map.of()));
        }
    }
}
