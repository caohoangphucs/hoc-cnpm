package vn.id.caohoangphuc.cnpm.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.function.RouterFunctions;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentials;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentialsRepository;
import vn.id.caohoangphuc.cnpm.service.BookingService;

class BookingControllerTest {
    private final BookingService bookings = mock(BookingService.class);
    private final AccountCredentialsRepository accounts = mock(AccountCredentialsRepository.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var controller = new BookingController(bookings, accounts);
        var routes = RouterFunctions.route().POST(ApiRoutes.BOOKINGS, controller::create).build();
        mvc = MockMvcBuilders.routerFunctions(routes).build();

        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "sv001", "", List.of(new SimpleGrantedAuthority("ROLE_SINHVIEN")));
        var security = SecurityContextHolder.createEmptyContext();
        security.setAuthentication(authentication);
        SecurityContextHolder.setContext(security);

        when(accounts.findByUsername("sv001")).thenReturn(Optional.of(new AccountCredentials(
                10, "sv001", "hash", "Sinh Vien", "sv@example.test", "SINHVIEN", "ACTIVE", "SV10", "P101")));
        when(bookings.book(any(), any(), any())).thenReturn(
                new CommandResultDto("25", "CHO_DUYET", 1L, UUID.randomUUID()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsBookingForTheAuthenticatedStudent() throws Exception {
        UUID key = UUID.randomUUID();
        mvc.perform(post(ApiRoutes.BOOKINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", key)
                        .content("""
                                {"bedId":"B-1","windowId":7,"fileIds":["%s","%s"],"acceptedTerms":true}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHO_DUYET"));

        verify(bookings).book(any(), any(), any());
    }
}
