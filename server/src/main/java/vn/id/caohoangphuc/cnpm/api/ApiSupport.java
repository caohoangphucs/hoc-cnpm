package vn.id.caohoangphuc.cnpm.api;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.ErrorDto;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentialsRepository;
import vn.id.caohoangphuc.cnpm.service.Actor;
import vn.id.caohoangphuc.cnpm.service.RequestContext;
import vn.id.caohoangphuc.cnpm.service.ServiceException;

/** Shares only HTTP actor, request-id and error handling used by the thin UC controllers. */
@Component
public class ApiSupport {
    @FunctionalInterface
    public interface Operation {
        Object run(Actor actor, RequestContext context) throws Exception;
    }

    private final AccountCredentialsRepository accounts;

    /** Supplies account lookup for authenticated controller requests. */
    public ApiSupport(AccountCredentialsRepository accounts) {
        this.accounts = accounts;
    }

    /** Reads an integer query parameter or returns its default when it is absent. */
    public int integerParameter(ServerRequest request, String name, int defaultValue) {
        return request.param(name).map(Integer::parseInt).orElse(defaultValue);
    }

    /** Runs one controller action and returns the API error contract for expected failures. */
    public ServerResponse handle(ServerRequest request, Operation operation) throws Exception {
        UUID requestId = UUID.randomUUID();
        try {
            requestId = requestId(request);
            var result = operation.run(actor(request), requestContext(request, requestId));
            if (result instanceof ServerResponse response) return response;
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(result);
        } catch (ServiceException exception) {
            String field = switch (exception.getCode()) {
                case "INVALID_VISITOR_NAME" -> "name";
                case "INVALID_VISITOR_IDENTITY" -> "identity";
                case "INVALID_BUILDING_ID" -> "buildingId";
                case "INVALID_ROOM_TYPE_ID" -> "roomTypeId";
                default -> null;
            };
            if (field != null) return ServerResponse.status(exception.getStatus()).contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorDto(exception.getCode(), exception.getMessage(), requestId,
                            Map.of(field, exception.getMessage())));
            return error(exception.getStatus(), exception.getCode(), exception.getMessage(), requestId);
        } catch (ConstraintViolationException exception) {
            var fields = exception.getConstraintViolations().stream().collect(
                    java.util.stream.Collectors.toMap(
                            violation -> violation.getPropertyPath().toString(),
                            violation -> "Invalid value",
                            (first, ignored) -> first));
            return ServerResponse.status(422).contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorDto("INVALID", "Request is invalid", requestId, fields));
        } catch (HttpMessageNotReadableException exception) {
            return error(400, "INVALID_JSON", "Request body is invalid", requestId);
        } catch (IllegalArgumentException exception) {
            return error(400, "INVALID_REQUEST", "Request contains an invalid identifier or header", requestId);
        }
    }

    /** Blocks business actions until an enrolled administrator or accountant verifies the session OTP. */
    private Actor actor(ServerRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new ServiceException(401, "UNAUTHENTICATED", "Login required");
        var account = accounts.findByUsername(authentication.getName()).orElse(null);
        if (account == null) throw new ServiceException(401, "UNAUTHENTICATED", "Login required");
        var session = request.servletRequest().getSession(false);
        boolean verified = session != null
                && Boolean.TRUE.equals(session.getAttribute(AuthController.TWO_FACTOR_ATTRIBUTE));
        boolean needsCode = ("ADMIN".equals(account.role()) || "KETOAN".equals(account.role()))
                && accounts.findTotpSecret(account.accountId()).isPresent();
        if (needsCode && !verified) {
            throw new ServiceException(403, "TWO_FACTOR_REQUIRED", "Authenticator verification required");
        }
        return new Actor(account.accountId(), !needsCode || verified);
    }

    /** Uses caller idempotency headers when supplied and creates safe defaults otherwise. */
    private RequestContext requestContext(ServerRequest request, UUID requestId) {
        String key = request.headers().firstHeader("Idempotency-Key");
        return new RequestContext(requestId, key == null ? UUID.randomUUID() : UUID.fromString(key), request.servletRequest().getRemoteAddr());
    }

    /** Reads a supplied request ID or generates one for this request. */
    private UUID requestId(ServerRequest request) {
        String value = request.headers().firstHeader("X-Request-Id");
        return value == null ? UUID.randomUUID() : UUID.fromString(value);
    }

    /** Builds the common JSON error response returned by all UC controllers. */
    private ServerResponse error(int status, String code, String message, UUID requestId) {
        return ServerResponse.status(status).contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorDto(code, message, requestId, Map.of()));
    }
}
