package vn.id.caohoangphuc.cnpm.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

class ApiRoutesTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() throws Exception {
        var auth = mock(AuthController.class);
        var rooms = mock(RoomController.class);
        var bookings = mock(BookingController.class);
        var registrations = mock(RegistrationController.class);
        var invoices = mock(InvoiceController.class);
        var payments = mock(PaymentController.class);
        var repairs = mock(RepairController.class);
        var gate = mock(GateController.class);
        var discipline = mock(DisciplineController.class);
        var community = mock(CommunityController.class);
        when(auth.session(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(auth.login(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(auth.twoFactor(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(auth.beginTwoFactorSetup(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(auth.confirmTwoFactorSetup(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(auth.logout(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(rooms.list(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(rooms.get(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(rooms.createRoom(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(rooms.updateRoom(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(rooms.deleteRoom(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(rooms.beds(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(rooms.createBed(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(rooms.updateBed(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(rooms.deleteBed(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(bookings.create(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(bookings.list(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(bookings.get(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(bookings.delete(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(registrations.decide(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(invoices.preview(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(invoices.issue(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(invoices.list(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(invoices.get(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(invoices.update(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(invoices.delete(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(payments.create(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(payments.status(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(payments.list(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(payments.delete(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(payments.callback(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(repairs.create(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(repairs.list(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(repairs.get(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(repairs.delete(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(repairs.assign(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(repairs.reschedule(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(repairs.complete(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(gate.record(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(gate.entries(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(gate.entry(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(gate.declareVisitor(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(gate.visitors(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(gate.visitor(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(gate.deleteVisitor(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(gate.approveVisitor(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(gate.checkVisitorIn(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(gate.checkVisitorOut(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(discipline.pending(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(discipline.all(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(discipline.get(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(discipline.create(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(discipline.update(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(discipline.delete(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(discipline.decide(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(discipline.appeal(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(discipline.respond(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.activities(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.createActivity(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.updateActivity(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.deleteActivity(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.register(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.cancel(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.attend(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.confirm(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.points(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.convert(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.requestCertificate(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());

        mvc = MockMvcBuilders.routerFunctions(new ApiRoutes().apiRouter(
                auth, rooms, bookings, registrations, invoices, payments, repairs, gate, discipline, community, mock(FileController.class))).build();
    }

    @Test
    void declaresAuthenticationAndBusinessRoutesInOnePlace() throws Exception {
        mvc.perform(get(ApiRoutes.AUTH_SESSION)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_LOGIN)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_TWO_FACTOR)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_TWO_FACTOR_SETUP)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_TWO_FACTOR_CONFIRM)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_LOGOUT)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.ROOMS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.ROOM.replace("{id}", "A-101"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.ROOMS)).andExpect(status().isOk());
        mvc.perform(put(ApiRoutes.ROOM.replace("{id}", "A-101"))).andExpect(status().isOk());
        mvc.perform(delete(ApiRoutes.ROOM.replace("{id}", "A-101"))).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.ROOM_BEDS.replace("{id}", "A-101"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.BEDS)).andExpect(status().isOk());
        mvc.perform(put(ApiRoutes.BED.replace("{id}", "A-101-1"))).andExpect(status().isOk());
        mvc.perform(delete(ApiRoutes.BED.replace("{id}", "A-101-1"))).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.BOOKINGS)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.BOOKINGS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.BOOKING.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(delete(ApiRoutes.BOOKING.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.BOOKING_DECISION.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.INVOICE_DRAFTS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.INVOICES)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.INVOICES_ISSUE)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.INVOICE.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(put(ApiRoutes.INVOICE.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(delete(ApiRoutes.INVOICE.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.PAYMENT_SESSIONS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.PAYMENT_SESSIONS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.PAYMENT_SESSION.replace("{id}", "00000000-0000-0000-0000-000000000001")))
                .andExpect(status().isOk());
        mvc.perform(delete(ApiRoutes.PAYMENT_SESSION.replace("{id}", "00000000-0000-0000-0000-000000000001")))
                .andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.PAYMENT_CALLBACK.replace("{gateway}", "vnpay"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.REPAIRS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.REPAIRS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.REPAIR.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(delete(ApiRoutes.REPAIR.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.REPAIR_ASSIGNMENT.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(put(ApiRoutes.REPAIR_SCHEDULE.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.REPAIR_COMPLETE.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.ENTRIES)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.ENTRIES)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.ENTRY.replace("{id}", "00000000-0000-0000-0000-000000000001")))
                .andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.VISITORS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.VISITORS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.VISITOR.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(delete(ApiRoutes.VISITOR.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.VISITOR_APPROVAL.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.VISITOR_CHECK_IN.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.VISITOR_CHECK_OUT.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.VIOLATIONS)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.VIOLATIONS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.VIOLATIONS_PENDING)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.VIOLATION.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(put(ApiRoutes.VIOLATION.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(delete(ApiRoutes.VIOLATION.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.VIOLATION_DECISIONS)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.DISCIPLINE_APPEALS.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.APPEAL_RESPONSE.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.ACTIVITIES)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.ACTIVITIES)).andExpect(status().isOk());
        mvc.perform(put(ApiRoutes.ACTIVITY.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(delete(ApiRoutes.ACTIVITY.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.ACTIVITY_REGISTRATION.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(delete(ApiRoutes.ACTIVITY_REGISTRATION.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.ACTIVITY_ATTENDANCE)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.ACTIVITY_CONFIRMATIONS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.ACTIVITY_POINTS + "?semesterId=1")).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.ACTIVITY_CONVERSION)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.ACTIVITY_CERTIFICATES)).andExpect(status().isOk());
    }

    @Test
    void doesNotExposeRoutesThatAreNotDeclared() throws Exception {
        mvc.perform(get("/api/v1/not-declared")).andExpect(status().isNotFound());
    }
}
