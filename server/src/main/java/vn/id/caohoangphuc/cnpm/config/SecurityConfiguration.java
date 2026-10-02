package vn.id.caohoangphuc.cnpm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import vn.id.caohoangphuc.cnpm.api.ApiRoutes;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentialsRepository;

@Configuration
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            CsrfTokenRepository csrfTokenRepository) throws Exception {
        return http
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository)
                        .requireExplicitSave(true))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(ApiRoutes.AUTH_SESSION, ApiRoutes.AUTH_LOGIN).permitAll()
                        .requestMatchers(ApiRoutes.AUTH_TWO_FACTOR, ApiRoutes.AUTH_LOGOUT).authenticated()
                        .anyRequest().access((authentication, context) -> new AuthorizationDecision(
                                canAccessApi(authentication.get(), context.getRequest()))))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, 401, "UNAUTHENTICATED", "Login required"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(response, 403, "FORBIDDEN", "Request is not allowed")))
                .build();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    UserDetailsService userDetailsService(AccountCredentialsRepository repository) {
        return username -> {
            var account = repository.findByUsername(username).orElseThrow(() ->
                    new UsernameNotFoundException("Invalid credentials"));
            return User.withUsername(account.username())
                    .password(account.passwordHash())
                    .roles(account.role())
                    .disabled(!"ACTIVE".equals(account.status()))
                    .build();
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void writeError(
            HttpServletResponse response,
            int status,
            String code,
            String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String requestId = UUID.randomUUID().toString();
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message
                + "\",\"requestId\":\"" + requestId + "\",\"fieldErrors\":{}}");
    }

    private static boolean canAccessApi(Authentication authentication, HttpServletRequest request) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        boolean requiresTwoFactor = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_KETOAN"));
        if (!requiresTwoFactor) {
            return true;
        }

        var session = request.getSession(false);
        return session != null && Boolean.TRUE.equals(session.getAttribute("ktx.twoFactorVerified"));
    }
}
