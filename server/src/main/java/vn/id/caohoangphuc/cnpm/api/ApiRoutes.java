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
    public static final String AUTH_TWO_FACTOR_SETUP = "/api/v1/auth/2fa/setup";
    public static final String AUTH_TWO_FACTOR_CONFIRM = "/api/v1/auth/2fa/setup/confirm";
    public static final String AUTH_LOGOUT = "/api/v1/auth/logout";
    public static final String BOOKINGS = "/api/v1/bookings";
    public static final String BOOKING_DECISION = "/api/v1/bookings/{id}/decision";
    public static final String INVOICE_DRAFTS = "/api/v1/invoice-drafts";
    public static final String INVOICES_ISSUE = "/api/v1/invoices/issue";
    public static final String PAYMENT_SESSIONS = "/api/v1/payment-sessions";
    public static final String PAYMENT_SESSION = "/api/v1/payment-sessions/{id}";
    public static final String REPAIRS = "/api/v1/repairs";
    public static final String ENTRIES = "/api/v1/entries";
    public static final String VIOLATIONS = "/api/v1/violations";
    public static final String ACTIVITIES = "/api/v1/activities";

    @Bean
    RouterFunction<ServerResponse> apiRouter(
            AuthController auth,
            BookingController bookings,
            RegistrationController registrations,
            InvoiceController invoices,
            PaymentController payments,
            RepairController repairs,
            GateController gate,
            DisciplineController discipline,
            CommunityController community) {
        return RouterFunctions.route()
                .GET(AUTH_SESSION, auth::session)
                .POST(AUTH_LOGIN, auth::login)
                .POST(AUTH_TWO_FACTOR, auth::twoFactor)
                .POST(AUTH_TWO_FACTOR_SETUP, auth::beginTwoFactorSetup)
                .POST(AUTH_TWO_FACTOR_CONFIRM, auth::confirmTwoFactorSetup)
                .POST(AUTH_LOGOUT, auth::logout)
                .POST(BOOKINGS, bookings::create)
                .POST(BOOKING_DECISION, registrations::decide)
                .POST(INVOICE_DRAFTS, invoices::preview)
                .POST(INVOICES_ISSUE, invoices::issue)
                .POST(PAYMENT_SESSIONS, payments::create)
                .GET(PAYMENT_SESSION, payments::status)
                .POST(REPAIRS, repairs::create)
                .GET(REPAIRS, repairs::list)
                .POST(ENTRIES, gate::record)
                .GET(VIOLATIONS, discipline::pending)
                .GET(ACTIVITIES, community::activities)
                .build();
    }
}
