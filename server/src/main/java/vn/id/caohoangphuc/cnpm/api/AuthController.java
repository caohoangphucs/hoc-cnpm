package vn.id.caohoangphuc.cnpm.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.api.dto.ErrorDto;
import vn.id.caohoangphuc.cnpm.api.dto.LoginDto;
import vn.id.caohoangphuc.cnpm.api.dto.OtpDto;
import vn.id.caohoangphuc.cnpm.api.dto.SessionDto;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentialsRepository;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentials;
import vn.id.caohoangphuc.cnpm.service.TotpService;

/** Handles authentication requests; URL definitions live together in ApiRoutes. */
@Component
public class AuthController {
    private static final String TWO_FACTOR_ATTRIBUTE = "ktx.twoFactorVerified";
    private static final String PENDING_TOTP_ATTRIBUTE = "ktx.pendingTotpSecret";
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(15);

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AccountCredentialsRepository accounts;
    private final Validator validator;
    private final Clock clock;
    private final TotpService totp;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            CsrfTokenRepository csrfTokenRepository,
            AccountCredentialsRepository accounts,
            Validator validator,
            Clock clock,
            TotpService totp) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
        this.accounts = accounts;
        this.validator = validator;
        this.clock = clock;
        this.totp = totp;
    }

    public ServerResponse session(ServerRequest request) {
        var servletRequest = request.servletRequest();
        var csrfToken = csrfToken(servletRequest, response());
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return json(new SessionDto(false, null, false, csrfToken, null));
        }

        var account = accounts.findByUsername(authentication.getName()).orElse(null);
        if (account == null || !"ACTIVE".equals(account.status())) {
            return json(new SessionDto(false, null, false, csrfToken, null));
        }

        var session = servletRequest.getSession(false);
        boolean twoFactorVerified = !requiresTwoFactor(account.role())
                || (session != null && Boolean.TRUE.equals(session.getAttribute(TWO_FACTOR_ATTRIBUTE)));
        var expiresAt = OffsetDateTime.ofInstant(clock.instant().plus(SESSION_TIMEOUT), ZoneOffset.UTC);
        return json(
                new SessionDto(true, account.role(), twoFactorVerified, csrfToken, expiresAt));
    }

    public ServerResponse login(ServerRequest request) throws Exception {
        final LoginDto input;
        try {
            input = request.body(LoginDto.class);
        } catch (HttpMessageNotReadableException exception) {
            return error(HttpStatus.BAD_REQUEST, "INVALID_JSON", "Login request body is invalid", Map.of());
        }
        if (input == null) {
            return error(HttpStatus.BAD_REQUEST, "INVALID_JSON", "Login request body is required", Map.of());
        }
        var errors = validator.validate(input);
        if (!errors.isEmpty()) {
            var fields = errors.stream().collect(java.util.stream.Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    violation -> "Invalid value",
                    (first, ignored) -> first));
            return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid login request", fields);
        }

        final Authentication authenticated;
        try {
            authenticated = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(input.username(), input.password()));
        } catch (AuthenticationException exception) {
            return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                    "Username or password is incorrect", Map.of());
        }

        var account = accounts.findByUsername(authenticated.getName()).orElse(null);
        if (account == null || !"ACTIVE".equals(account.status())) {
            return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                    "Username or password is incorrect", Map.of());
        }

        HttpServletRequest servletRequest = request.servletRequest();
        HttpServletResponse servletResponse = response();
        servletRequest.getSession(true);
        servletRequest.changeSessionId();
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticated);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, servletResponse);

        boolean twoFactorVerified = !requiresTwoFactor(account.role());
        servletRequest.getSession(true).setAttribute(TWO_FACTOR_ATTRIBUTE, twoFactorVerified);
        csrfTokenRepository.saveToken(null, servletRequest, servletResponse);
        String csrfToken = csrfToken(servletRequest, servletResponse);
        var expiresAt = OffsetDateTime.ofInstant(clock.instant().plus(SESSION_TIMEOUT), ZoneOffset.UTC);
        return json(
                new SessionDto(true, account.role(), twoFactorVerified, csrfToken, expiresAt));
    }

    /** Verifies a code generated by an authenticator app for an enrolled account. */
    public ServerResponse twoFactor(ServerRequest request) throws Exception {
        final OtpDto input;
        try {
            input = request.body(OtpDto.class);
        } catch (HttpMessageNotReadableException exception) {
            return error(HttpStatus.BAD_REQUEST, "INVALID_JSON", "Verification request body is invalid", Map.of());
        }
        if (input == null) {
            return error(HttpStatus.BAD_REQUEST, "INVALID_JSON", "Verification request body is required", Map.of());
        }
        if (!validator.validate(input).isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid verification code",
                    Map.of("code", "Must contain six digits"));
        }
        var account = currentAccount();
        if (account == null || !requiresTwoFactor(account.role())) {
            return error(HttpStatus.FORBIDDEN, "TWO_FACTOR_NOT_REQUIRED", "Two-factor setup is not available", Map.of());
        }
        var encryptedSecret = accounts.findTotpSecret(account.accountId()).orElse(null);
        if (encryptedSecret == null) {
            return error(HttpStatus.CONFLICT, "TWO_FACTOR_SETUP_REQUIRED",
                    "Set up an authenticator app before verifying a code", Map.of());
        }
        if (!totp.matches(totp.decrypt(encryptedSecret), input.code(), clock.instant().getEpochSecond())) {
            return error(HttpStatus.UNAUTHORIZED, "INVALID_OTP", "Verification code is incorrect", Map.of());
        }

        request.servletRequest().getSession(true).setAttribute(TWO_FACTOR_ATTRIBUTE, true);
        return currentSession(request);
    }

    /** Creates an enrollment key for an account that has not configured two-factor authentication. */
    public ServerResponse beginTwoFactorSetup(ServerRequest request) {
        var account = currentAccount();
        if (account == null || !requiresTwoFactor(account.role())) {
            return error(HttpStatus.FORBIDDEN, "TWO_FACTOR_NOT_REQUIRED", "Two-factor setup is not available", Map.of());
        }
        if (accounts.findTotpSecret(account.accountId()).isPresent()) {
            return error(HttpStatus.CONFLICT, "TWO_FACTOR_ALREADY_CONFIGURED",
                    "An authenticator app is already configured", Map.of());
        }

        var session = request.servletRequest().getSession(true);
        var secret = (String) session.getAttribute(PENDING_TOTP_ATTRIBUTE);
        if (secret == null) {
            secret = totp.newSecret();
            session.setAttribute(PENDING_TOTP_ATTRIBUTE, secret);
        }
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .header("Cache-Control", "no-store")
                .body(new TwoFactorSetupDto(secret, totp.provisioningUri("KTX Smart Hub", account.username(), secret)));
    }

    /** Activates enrollment only after the account owner enters a valid app-generated code. */
    public ServerResponse confirmTwoFactorSetup(ServerRequest request) throws Exception {
        final OtpDto input;
        try {
            input = request.body(OtpDto.class);
        } catch (HttpMessageNotReadableException exception) {
            return error(HttpStatus.BAD_REQUEST, "INVALID_JSON", "Verification request body is invalid", Map.of());
        }
        if (input == null || !validator.validate(input).isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Enter a six-digit verification code",
                    Map.of("code", "Must contain six digits"));
        }
        var account = currentAccount();
        if (account == null || !requiresTwoFactor(account.role())) {
            return error(HttpStatus.FORBIDDEN, "TWO_FACTOR_NOT_REQUIRED", "Two-factor setup is not available", Map.of());
        }
        var session = request.servletRequest().getSession(false);
        var secret = session == null ? null : (String) session.getAttribute(PENDING_TOTP_ATTRIBUTE);
        if (secret == null) {
            return error(HttpStatus.CONFLICT, "TWO_FACTOR_SETUP_NOT_STARTED", "Start setup before confirming it", Map.of());
        }
        if (!totp.matches(secret, input.code(), clock.instant().getEpochSecond())) {
            return error(HttpStatus.UNAUTHORIZED, "INVALID_OTP", "Verification code is incorrect", Map.of());
        }
        if (!accounts.saveTotpSecret(account.accountId(), totp.encrypt(secret))) {
            return error(HttpStatus.CONFLICT, "TWO_FACTOR_ALREADY_CONFIGURED", "Authenticator setup could not be saved", Map.of());
        }
        session.removeAttribute(PENDING_TOTP_ATTRIBUTE);
        session.setAttribute(TWO_FACTOR_ATTRIBUTE, true);
        return currentSession(request);
    }

    private AccountCredentials currentAccount() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : accounts.findByUsername(authentication.getName()).orElse(null);
    }

    private ServerResponse currentSession(ServerRequest request) {
        var account = currentAccount();
        var session = request.servletRequest().getSession(false);
        String token = csrfToken(request.servletRequest(), response());
        var expiresAt = OffsetDateTime.ofInstant(clock.instant().plus(SESSION_TIMEOUT), ZoneOffset.UTC);
        return json(new SessionDto(true, account.role(),
                session != null && Boolean.TRUE.equals(session.getAttribute(TWO_FACTOR_ATTRIBUTE)), token, expiresAt));
    }

    public record TwoFactorSetupDto(String secret, String provisioningUri) {}

    public ServerResponse logout(ServerRequest request) {
        var servletRequest = request.servletRequest();
        var servletResponse = response();
        SecurityContextHolder.clearContext();
        var session = servletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Bootstrap the next anonymous session with a fresh CSRF token.
        csrfTokenRepository.saveToken(null, servletRequest, servletResponse);
        return json(
                new CommandResultDto(null, "LOGGED_OUT", 1L, UUID.randomUUID()));
    }

    private String csrfToken(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken token = csrfTokenRepository.loadToken(request);
        if (token == null) {
            token = csrfTokenRepository.generateToken(request);
            csrfTokenRepository.saveToken(token, request, response);
        }
        return token.getToken();
    }

    private static boolean requiresTwoFactor(String role) {
        return "ADMIN".equals(role) || "KETOAN".equals(role);
    }

    private static HttpServletResponse response() {
        var attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getResponse();
    }

    private static ServerResponse json(Object body) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    private static ServerResponse error(
            HttpStatus status, String code, String message, Map<String, String> fieldErrors) {
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorDto(code, message, UUID.randomUUID(), fieldErrors));
    }
}
