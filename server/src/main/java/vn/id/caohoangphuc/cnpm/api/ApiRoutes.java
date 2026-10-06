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
    public static final String FILES = "/api/v1/files";
    public static final String FILE = "/api/v1/files/{id}";
    public static final String FILE_CONTENT = "/api/v1/files/{id}/content";
    public static final String BOOKING_WINDOWS = "/api/v1/booking-windows";
    public static final String ROOMS = "/api/v1/rooms";
    public static final String ROOM = "/api/v1/rooms/{id}";
    public static final String ROOM_BEDS = "/api/v1/rooms/{id}/beds";
    public static final String BEDS = "/api/v1/beds";
    public static final String BED = "/api/v1/beds/{id}";
    public static final String BOOKINGS = "/api/v1/bookings";
    public static final String BOOKING_ELIGIBILITY = "/api/v1/bookings/eligibility";
    public static final String BOOKING = "/api/v1/bookings/{id}";
    public static final String BOOKING_DECISION = "/api/v1/bookings/{id}/decision";
    public static final String METER_READINGS = "/api/v1/rooms/{id}/meter-readings";
    public static final String UNBILLED_ROOMS = "/api/v1/invoices/unbilled-rooms";
    public static final String INVOICE_DRAFTS = "/api/v1/invoice-drafts";
    public static final String INVOICES = "/api/v1/invoices";
    public static final String INVOICE = "/api/v1/invoices/{id}";
    public static final String INVOICES_ISSUE = "/api/v1/invoices/issue";
    public static final String PAYMENT_SESSIONS = "/api/v1/payment-sessions";
    public static final String PAYMENT_SESSION = "/api/v1/payment-sessions/{id}";
    public static final String VNPAY_IPN = "/api/v1/payments/vnpay/ipn";
    public static final String VNPAY_RETURN = "/api/v1/payments/vnpay/return";
    public static final String PAYMENT_CALLBACK = "/api/v1/payment-callbacks/{gateway}";
    public static final String REPAIRS = "/api/v1/repairs";
    public static final String REPAIR = "/api/v1/repairs/{id}";
    public static final String REPAIR_PRIORITY = "/api/v1/repairs/{id}/priority";
    public static final String MATERIALS = "/api/v1/materials";
    public static final String MANUAL_ENTRY = "/api/v1/entries/manual";
    public static final String STUDENTS = "/api/v1/students";
    public static final String STUDENT_PROFILE = "/api/v1/students/me";
    public static final String REPAIR_ASSIGNMENT = "/api/v1/repairs/{id}/assignment";
    public static final String REPAIR_SCHEDULE = "/api/v1/repairs/{id}/schedule";
    public static final String REPAIR_COMPLETE = "/api/v1/repairs/{id}/complete";
    public static final String ENTRIES = "/api/v1/entries";
    public static final String ENTRY = "/api/v1/entries/{id}";
    public static final String VISITORS = "/api/v1/visitors";
    public static final String VISITOR = "/api/v1/visitors/{id}";
    public static final String VISITOR_APPROVAL = "/api/v1/visitors/{id}/approval";
    public static final String VISITOR_CHECK_IN = "/api/v1/visitors/{id}/check-in";
    public static final String VISITOR_CHECK_OUT = "/api/v1/visitors/{id}/check-out";
    public static final String VIOLATIONS = "/api/v1/violations";
    public static final String VIOLATION = "/api/v1/violations/{id}";
    public static final String VIOLATIONS_PENDING = "/api/v1/violations/pending";
    public static final String VIOLATION_DECISIONS = "/api/v1/violations/decisions";
    public static final String DISCIPLINE_APPEALS = "/api/v1/decisions/{id}/appeals";
    public static final String APPEAL_RESPONSE = "/api/v1/appeals/{id}/response";
    public static final String MY_ACTIVITY_REGISTRATIONS = "/api/v1/activity-registrations";
    public static final String REGISTRATION_TOKEN = "/api/v1/activity-registrations/{id}/token";
    public static final String ACTIVITIES = "/api/v1/activities";
    public static final String ACTIVITY = "/api/v1/activities/{id}";
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
            RoomController rooms,
            BookingController bookings,
            RegistrationController registrations,
            InvoiceController invoices,
            PaymentController payments,
            RepairController repairs,
            GateController gate,
            DisciplineController discipline,
            CommunityController community,
            FileController files) {
        return RouterFunctions.route()
                .GET(AUTH_SESSION, auth::session)
                .POST(AUTH_LOGIN, auth::login)
                .POST(AUTH_TWO_FACTOR, auth::twoFactor)
                .POST(AUTH_TWO_FACTOR_SETUP, auth::beginTwoFactorSetup)
                .POST(AUTH_TWO_FACTOR_CONFIRM, auth::confirmTwoFactorSetup)
                .POST(AUTH_LOGOUT, auth::logout)
                .POST(FILES, files::upload)
                .GET(FILE, files::metadata)
                .GET(FILE_CONTENT, files::content)
                .GET(BOOKING_WINDOWS, files::windows)
                .GET(ROOMS, rooms::list)
                .GET(ROOM, rooms::get)
                .POST(ROOMS, rooms::createRoom)
                .PUT(ROOM, rooms::updateRoom)
                .DELETE(ROOM, rooms::deleteRoom)
                .GET(ROOM_BEDS, rooms::beds)
                .POST(BEDS, rooms::createBed)
                .PUT(BED, rooms::updateBed)
                .DELETE(BED, rooms::deleteBed)
                .GET(BOOKING_ELIGIBILITY, bookings::eligibility)
                .GET(BOOKINGS, bookings::list)
                .POST(BOOKINGS, bookings::create)
                .GET(BOOKING, bookings::get)
                .DELETE(BOOKING, bookings::delete)
                .POST(BOOKING_DECISION, registrations::decide)
                .POST(INVOICE_DRAFTS, invoices::preview)
                .GET(INVOICES, invoices::list)
                .GET(UNBILLED_ROOMS, invoices::unbilledRooms)
                .GET(METER_READINGS, invoices::meter)
                .PUT(METER_READINGS, invoices::saveMeter)
                .POST(INVOICES_ISSUE, invoices::issue)
                .GET(INVOICE, invoices::get)
                .PUT(INVOICE, invoices::update)
                .DELETE(INVOICE, invoices::delete)
                .POST(PAYMENT_SESSIONS, payments::create)
                .GET(PAYMENT_SESSIONS, payments::list)
                .GET(PAYMENT_SESSION, payments::status)
                .DELETE(PAYMENT_SESSION, payments::delete)
                .POST(PAYMENT_CALLBACK, payments::callback)
                .GET(VNPAY_IPN, payments::vnpayIpn)
                .GET(VNPAY_RETURN, payments::vnpayReturn)
                .GET(MATERIALS, repairs::materials)
                .POST(MATERIALS, repairs::createMaterial)
                .POST(REPAIRS, repairs::create)
                .GET(REPAIRS, repairs::list)
                .GET("/api/v1/repairs/{id}/files", repairs::files)
                .GET(REPAIR, repairs::get)
                .DELETE(REPAIR, repairs::delete)
                .GET(STUDENT_PROFILE, gate::studentProfile)
                .GET(STUDENTS, gate::students)
                .PUT(REPAIR_PRIORITY, repairs::priority)
                .POST(REPAIR_ASSIGNMENT, repairs::assign)
                .PUT(REPAIR_SCHEDULE, repairs::reschedule)
                .POST(REPAIR_COMPLETE, repairs::complete)
                .POST(MANUAL_ENTRY, gate::recordManual)
                .POST(ENTRIES, gate::record)
                .GET(ENTRIES, gate::entries)
                .GET(ENTRY, gate::entry)
                .GET(VISITORS, gate::visitors)
                .POST(VISITORS, gate::declareVisitor)
                .GET(VISITOR, gate::visitor)
                .DELETE(VISITOR, gate::deleteVisitor)
                .POST(VISITOR_APPROVAL, gate::approveVisitor)
                .POST(VISITOR_CHECK_IN, gate::checkVisitorIn)
                .POST(VISITOR_CHECK_OUT, gate::checkVisitorOut)
                .GET(VIOLATIONS, discipline::all)
                .POST(VIOLATIONS, discipline::create)
                .GET(VIOLATIONS_PENDING, discipline::pending)
                .GET(VIOLATION, discipline::get)
                .PUT(VIOLATION, discipline::update)
                .DELETE(VIOLATION, discipline::delete)
                .POST(VIOLATION_DECISIONS, discipline::decide)
                .POST(DISCIPLINE_APPEALS, discipline::appeal)
                .POST(APPEAL_RESPONSE, discipline::respond)
                .GET(MY_ACTIVITY_REGISTRATIONS, community::registrations)
                .POST(REGISTRATION_TOKEN, community::attendanceToken)
                .GET(ACTIVITIES, community::activities)
                .POST(ACTIVITIES, community::createActivity)
                .PUT(ACTIVITY, community::updateActivity)
                .DELETE(ACTIVITY, community::deleteActivity)
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
