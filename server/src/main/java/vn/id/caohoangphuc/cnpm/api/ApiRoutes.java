package vn.id.caohoangphuc.cnpm.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/** Central list of HTTP endpoints. Handler methods stay free of route annotations. */
@Configuration
public class ApiRoutes {
    public static final String AUTH_SESSION = "/api/v1/auth/session";
    public static final String AUTH_LOGIN = "/api/v1/auth/login";
    public static final String AUTH_TWO_FACTOR = "/api/v1/auth/2fa";
    public static final String AUTH_LOGOUT = "/api/v1/auth/logout";
    public static final String BOOKINGS = "/api/v1/bookings";

    @Bean
    RouterFunction<ServerResponse> apiRoutes(AuthController auth, BookingController bookings) {
        return RouterFunctions.route()
                .GET(AUTH_SESSION, auth::session)
                .POST(AUTH_LOGIN, auth::login)
                .POST(AUTH_TWO_FACTOR, auth::twoFactor)
                .POST(AUTH_LOGOUT, auth::logout)
                .POST(BOOKINGS, bookings::create)
                .build();
    }
}
