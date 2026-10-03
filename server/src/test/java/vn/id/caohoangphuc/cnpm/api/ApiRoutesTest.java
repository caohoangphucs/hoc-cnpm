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
        when(auth.session(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(auth.login(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(auth.twoFactor(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(auth.logout(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());
        when(bookings.create(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());

        mvc = MockMvcBuilders.routerFunctions(new ApiRoutes().apiRouter(auth, bookings)).build();
    }

    @Test
    void declaresAuthenticationAndFirstBusinessRoutesInOnePlace() throws Exception {
        mvc.perform(get(ApiRoutes.AUTH_SESSION)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_LOGIN)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_TWO_FACTOR)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.AUTH_LOGOUT)).andExpect(status().isOk());
        mvc.perform(post(ApiRoutes.BOOKINGS)).andExpect(status().isOk());
    }

    @Test
    void doesNotExposeRoutesThatAreNotDeclared() throws Exception {
        mvc.perform(get("/api/v1/not-declared")).andExpect(status().isNotFound());
    }
}
