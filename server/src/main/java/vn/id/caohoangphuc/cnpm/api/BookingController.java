package vn.id.caohoangphuc.cnpm.api;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.BookingDto;
import vn.id.caohoangphuc.cnpm.api.dto.ErrorDto;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentialsRepository;
import vn.id.caohoangphuc.cnpm.service.Actor;
import vn.id.caohoangphuc.cnpm.service.BookingService;
import vn.id.caohoangphuc.cnpm.service.RequestContext;
import vn.id.caohoangphuc.cnpm.service.ServiceException;

/** HTTP adapter for booking actions. The URL is declared in ApiRoutes. */
@Component
public class BookingController {
    private final BookingService bookings;
    private final AccountCredentialsRepository accounts;

    public BookingController(BookingService bookings, AccountCredentialsRepository accounts) {
        this.bookings = bookings;
        this.accounts = accounts;
    }

    /** Creates a room booking using the account established by the login middleware. */
    public ServerResponse create(ServerRequest request) throws Exception {
        UUID requestId;
        try {
            requestId = requestId(request);
        } catch (IllegalArgumentException exception) {
            return error(400, "INVALID_REQUEST_ID", "X-Request-Id must be a UUID", UUID.randomUUID());
        }
        UUID idempotencyKey;
        try {
            idempotencyKey = UUID.fromString(request.headers().firstHeader("Idempotency-Key"));
        } catch (RuntimeException exception) {
            return error(400, "INVALID_IDEMPOTENCY_KEY", "Idempotency-Key must be a UUID", requestId);
        }

        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            var account = accounts.findByUsername(authentication.getName()).orElse(null);
            if (account == null) {
                return error(401, "UNAUTHENTICATED", "Login required", requestId);
            }

            var result = bookings.book(
                    new Actor(account.accountId(), twoFactorVerified(request)),
                    new RequestContext(requestId, idempotencyKey),
                    request.body(BookingDto.class));
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(result);
        } catch (ServiceException exception) {
            return error(exception.getStatus(), exception.getCode(), exception.getMessage(), requestId);
        } catch (ConstraintViolationException exception) {
            var fields = exception.getConstraintViolations().stream().collect(
                    java.util.stream.Collectors.toMap(
                            violation -> violation.getPropertyPath().toString(),
                            violation -> "Invalid value",
                            (first, ignored) -> first));
            return ServerResponse.status(422).contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorDto("INVALID", "Booking request is invalid", requestId, fields));
        } catch (HttpMessageNotReadableException exception) {
            return error(400, "INVALID_JSON", "Booking request body is invalid", requestId);
        }
    }

    private static UUID requestId(ServerRequest request) {
        String header = request.headers().firstHeader("X-Request-Id");
        return header == null ? UUID.randomUUID() : UUID.fromString(header);
    }

    private static boolean twoFactorVerified(ServerRequest request) {
        var session = request.servletRequest().getSession(false);
        return session != null && Boolean.TRUE.equals(session.getAttribute("ktx.twoFactorVerified"));
    }

    private static ServerResponse error(int status, String code, String message, UUID requestId) {
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorDto(code, message, requestId, Map.of()));
    }
}
