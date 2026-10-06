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

    /** IPN của VNPAY dùng GET và phản hồi RspCode, không cần phiên trình duyệt. */
    public ServerResponse vnpayIpn(ServerRequest request) {
        try {
            var verified = payments.verify("VNPAY", vnpayParameters(request));
            var receipt = payments.acceptCallback(null,
                    new RequestContext(UUID.randomUUID(), UUID.randomUUID()), verified);
            return receipt.replayed()
                    ? vnpayAck("02", "Order already confirmed")
                    : vnpayAck("00", "Confirm Success");
        } catch (ServiceException exception) {
            String code = switch (exception.getCode()) {
                case "VNPAY_INVALID_SIGNATURE" -> "97";
                case "CALLBACK_AMOUNT_MISMATCH" -> "04";
                case "NOT_FOUND" -> "01";
                default -> "99";
            };
            return vnpayAck(code, "Cannot confirm payment");
        } catch (Exception exception) { return vnpayAck("99", "Cannot confirm payment"); }
    }

    /** ReturnURL chỉ xác minh rồi mở trang trạng thái; tuyệt đối không cộng tiền. */
    public ServerResponse vnpayReturn(ServerRequest request) {
        try {
            var verified = payments.verify("VNPAY", vnpayParameters(request));
            return ServerResponse.status(303).header("Location", "/student/invoices?paymentSession="
                    + UUID.fromString(verified.providerRef())).header("Cache-Control", "no-store").build();
        } catch (Exception exception) {
            return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("code", "INVALID_VNPAY_RETURN", "message", "Invalid payment return signature"));
        }
    }

    /** Từ chối tham số lặp để không có cách hiểu chữ ký khác nhau giữa các hệ thống. */
    private Map<String, String> vnpayParameters(ServerRequest request) {
        if (request.params().entrySet().stream().anyMatch(entry -> entry.getValue().size() != 1))
            throw new ServiceException(400, "VNPAY_INVALID_SIGNATURE", "Duplicate callback parameters");
        return request.params().toSingleValueMap();
    }

    private ServerResponse vnpayAck(String code, String message) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .header("Cache-Control", "no-store").body(Map.of("RspCode", code, "Message", message));
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
