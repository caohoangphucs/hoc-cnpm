package vn.id.caohoangphuc.cnpm.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.id.caohoangphuc.cnpm.KtxApplication;
import vn.id.caohoangphuc.cnpm.api.dto.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Black-box checks of service authorization against the CI PostgreSQL fixture. */
@SpringBootTest(classes = KtxApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UseCaseBlackBoxIT {
    private static final Actor STUDENT = new Actor(5, false);
    private static final Actor GUARD = new Actor(4, false);
    private static final OffsetDateTime NOW = OffsetDateTime.of(2026, 10, 3, 12, 0, 0, 0, ZoneOffset.UTC);

    @Autowired BookingService bookings;
    @Autowired RegistrationService registrations;
    @Autowired InvoiceService invoices;
    @Autowired PaymentService payments;
    @Autowired RepairService repairs;
    @Autowired GateService gate;
    @Autowired DisciplineService discipline;
    @Autowired CommunityService community;

    @Test
    void uc04BookingRejectsAUserOutsideStudentRole() {
        assertForbidden(() -> bookings.book(GUARD, request(),
                new BookingDto("G-A-101-4", 1L, List.of(UUID.randomUUID(), UUID.randomUUID()), true)));
    }

    @Test
    void uc12RegistrationDecisionRejectsAUserOutsideAdminRole() {
        assertForbidden(() -> registrations.decide(STUDENT, request(), 1,
                new ApprovalDto(ApprovalDto.Decision.DUYET, null, 1L)));
    }

    @Test
    void uc17InvoicePreviewRejectsAUserOutsideAccountingRole() {
        assertForbidden(() -> invoices.preview(STUDENT, request(), new InvoiceDraftDto("2026-10", List.of("A-101"))));
    }

    @Test
    void uc08PaymentLookupRejectsAUserOutsideStudentRole() {
        assertForbidden(() -> payments.payableInvoices(GUARD, List.of(1L)));
    }

    @Test
    void uc22RepairCreationRejectsAUserOutsideStudentRole() {
        assertForbidden(() -> repairs.create(GUARD, request(),
                new RepairDto("Broken light", "The room light does not work", RepairDto.Priority.TRUNG_BINH, List.of())));
    }

    @Test
    void uc25GateEntryRejectsAUserOutsideGuardRole() {
        assertForbidden(() -> gate.record(STUDENT, request(),
                new EntryDto(UUID.randomUUID(), "card-token", EntryDto.Type.VAO, "CI-GATE", null)));
    }

    @Test
    void uc14DisciplineQueueRejectsAUserOutsideAdminRole() {
        assertForbidden(() -> discipline.pending(STUDENT, 1, 20));
    }

    @Test
    void uc30ActivityListRejectsAUserOutsideAllowedRoles() {
        assertForbidden(() -> community.activities(GUARD, 1, 20));
    }

    private static RequestContext request() {
        return new RequestContext(UUID.randomUUID(), UUID.randomUUID());
    }

    private static void assertForbidden(Runnable call) {
        var error = assertThrows(ServiceException.class, call::run);
        assertEquals(403, error.getStatus());
        assertEquals("FORBIDDEN", error.getCode());
    }
}
