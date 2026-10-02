package vn.id.caohoangphuc.cnpm.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        when(bookings.create(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(registrations.decide(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(invoices.preview(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(invoices.issue(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(payments.create(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(payments.status(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(repairs.create(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(repairs.list(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(gate.record(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(discipline.pending(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(community.activities(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());

        mvc = MockMvcBuilders.routerFunctions(new ApiRoutes().apiRouter(
                auth, bookings, registrations, invoices, payments, repairs, gate, discipline, community)).build();
    }

    @Test
    void declaresAuthenticationAndFirstBusinessRoutesInOnePlace() throws Exception {
        mvc.perform(get(ApiRoutes.AUTH_SESSION)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_LOGIN)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_TWO_FACTOR)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_TWO_FACTOR_SETUP)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_TWO_FACTOR_CONFIRM)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_LOGOUT)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.BOOKINGS)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.BOOKING_DECISION.replace("{id}", "1"))).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.INVOICE_DRAFTS)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.INVOICES_ISSUE)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.PAYMENT_SESSIONS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.PAYMENT_SESSION.replace("{id}", "00000000-0000-0000-0000-000000000001")))
                .andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.REPAIRS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.REPAIRS)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.ENTRIES)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.VIOLATIONS)).andExpect(status().isOk());
        mvc.perform(get(ApiRoutes.ACTIVITIES)).andExpect(status().isOk());
    }

    @Test
    void doesNotExposeRoutesThatAreNotDeclared() throws Exception {
        mvc.perform(get("/api/v1/not-declared")).andExpect(status().isNotFound());
    }
}
