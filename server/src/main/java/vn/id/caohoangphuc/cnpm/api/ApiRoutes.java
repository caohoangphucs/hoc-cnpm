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
    public static final String PAYMENT_CALLBACK = "/api/v1/payment-callbacks/{gateway}";
    public static final String REPAIRS = "/api/v1/repairs";
    public static final String REPAIR_ASSIGNMENT = "/api/v1/repairs/{id}/assignment";
    public static final String REPAIR_SCHEDULE = "/api/v1/repairs/{id}/schedule";
    public static final String REPAIR_COMPLETE = "/api/v1/repairs/{id}/complete";
    public static final String ENTRIES = "/api/v1/entries";
    public static final String VISITORS = "/api/v1/visitors";
    public static final String VISITOR_APPROVAL = "/api/v1/visitors/{id}/approval";
    public static final String VISITOR_CHECK_IN = "/api/v1/visitors/{id}/check-in";
    public static final String VISITOR_CHECK_OUT = "/api/v1/visitors/{id}/check-out";
    public static final String VIOLATIONS = "/api/v1/violations";
    public static final String VIOLATION_DECISIONS = "/api/v1/violations/decisions";
    public static final String DISCIPLINE_APPEALS = "/api/v1/decisions/{id}/appeals";
    public static final String APPEAL_RESPONSE = "/api/v1/appeals/{id}/response";
    public static final String ACTIVITIES = "/api/v1/activities";
    public static final String ACTIVITY_REGISTRATION = "/api/v1/activities/{id}/registrations";
    public static final String ACTIVITY_ATTENDANCE = "/api/v1/activities/attendance";
    public static final String ACTIVITY_CONFIRMATIONS = "/api/v1/activities/completion";
    public static final String ACTIVITY_POINTS = "/api/v1/activities/points";
    public static final String ACTIVITY_CONVERSION = "/api/v1/activities/conversions";
    public static final String ACTIVITY_CERTIFICATES = "/api/v1/activities/certificates";

    /** Maps each declared path and HTTP method to its controller handler. */
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
            .POST(PAYMENT_CALLBACK, payments::callback)
            .POST(REPAIRS, repairs::create)
            .GET(REPAIRS, repairs::list)
            .POST(REPAIR_ASSIGNMENT, repairs::assign)
            .PUT(REPAIR_SCHEDULE, repairs::reschedule)
            .POST(REPAIR_COMPLETE, repairs::complete)
            .POST(ENTRIES, gate::record)
            .POST(VISITORS, gate::declareVisitor)
            .POST(VISITOR_APPROVAL, gate::approveVisitor)
            .POST(VISITOR_CHECK_IN, gate::checkVisitorIn)
            .POST(VISITOR_CHECK_OUT, gate::checkVisitorOut)
            .GET(VIOLATIONS, discipline::pending)
            .POST(VIOLATION_DECISIONS, discipline::decide)
            .POST(DISCIPLINE_APPEALS, discipline::appeal)
            .POST(APPEAL_RESPONSE, discipline::respond)
            .GET(ACTIVITIES, community::activities)
            .POST(ACTIVITIES, community::createActivity)
            .POST(ACTIVITY_REGISTRATION, community::register)
            .DELETE(ACTIVITY_REGISTRATION, community::cancel)
            .POST(ACTIVITY_ATTENDANCE, community::attend)
            .POST(ACTIVITY_CONFIRMATIONS, community::confirm)
            .GET(ACTIVITY_POINTS, community::points)
            .POST(ACTIVITY_CONVERSION, community::convert)
            .POST(ACTIVITY_CERTIFICATES, community::requestCertificate)
            .build();
    }
}
