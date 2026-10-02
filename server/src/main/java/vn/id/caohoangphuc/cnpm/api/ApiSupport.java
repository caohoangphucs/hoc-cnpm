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

    public ApiSupport(AccountCredentialsRepository accounts) {
        this.accounts = accounts;
    }

    /** Runs one controller action and returns the API error contract for expected failures. */
    public ServerResponse handle(ServerRequest request, Operation operation) throws Exception {
        UUID requestId = UUID.randomUUID();
        try {
            requestId = requestId(request);
            var result = operation.run(actor(request), requestContext(request, requestId));
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
                    .body(new ErrorDto("INVALID", "Request is invalid", requestId, fields));
        } catch (HttpMessageNotReadableException exception) {
            return error(400, "INVALID_JSON", "Request body is invalid", requestId);
        } catch (IllegalArgumentException exception) {
            return error(400, "INVALID_REQUEST", "Request contains an invalid identifier or header", requestId);
        }
    }

    /** Builds the actor from the authenticated account and its server-side 2FA session flag. */
    private Actor actor(ServerRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new ServiceException(401, "UNAUTHENTICATED", "Login required");
        var account = accounts.findByUsername(authentication.getName()).orElse(null);
        if (account == null) throw new ServiceException(401, "UNAUTHENTICATED", "Login required");
        var session = request.servletRequest().getSession(false);
        boolean verified = session != null && Boolean.TRUE.equals(session.getAttribute("ktx.twoFactorVerified"));
        return new Actor(account.accountId(), verified);
    }

    /** Uses caller idempotency headers when supplied and creates safe defaults otherwise. */
    private RequestContext requestContext(ServerRequest request, UUID requestId) {
        String key = request.headers().firstHeader("Idempotency-Key");
        return new RequestContext(requestId, key == null ? UUID.randomUUID() : UUID.fromString(key));
    }

    private UUID requestId(ServerRequest request) {
        String value = request.headers().firstHeader("X-Request-Id");
        return value == null ? UUID.randomUUID() : UUID.fromString(value);
    }

    private ServerResponse error(int status, String code, String message, UUID requestId) {
        return ServerResponse.status(status).contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorDto(code, message, requestId, Map.of()));
    }
}
