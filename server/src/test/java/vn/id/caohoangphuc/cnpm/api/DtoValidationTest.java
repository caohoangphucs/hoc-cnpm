package vn.id.caohoangphuc.cnpm.api;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import vn.id.caohoangphuc.cnpm.api.dto.*;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentials;
import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();
    @AfterAll static void closeFactory() { factory.close(); }
    @Test void bookingRequiresConsentAndAtLeastTwoFiles() {
        assertFalse(validator.validate(new BookingDto("BED", 1L, List.of(UUID.randomUUID()), false)).isEmpty());
        assertTrue(validator.validate(new BookingDto("BED", 1L,
            List.of(UUID.randomUUID(), UUID.randomUUID()), true)).isEmpty());
    }
    @Test void meterRejectsInvalidMonthNegativeAndDecreasingValues() {
        assertFalse(validator.validate(new MeterDto("ROOM", "2026-13", -1L, 1L, 5L, 4L)).isEmpty());
        assertFalse(validator.validate(new MeterDto("ROOM", "2026-01", 10L, 9L, 0L, 1L)).isEmpty());
        assertTrue(validator.validate(new MeterDto("ROOM", "2026-01", 10L, 11L, 0L, 1L)).isEmpty());
    }
    @Test void rejectionNeedsReason() {
        assertFalse(validator.validate(new ApprovalDto(ApprovalDto.Decision.TU_CHOI, " ", 1L)).isEmpty());
        assertTrue(validator.validate(new ApprovalDto(ApprovalDto.Decision.DUYET, null, 1L)).isEmpty());
    }
    @Test void paymentRejectsDuplicateInvoiceIds() {
        assertFalse(validator.validate(new PaymentDto(List.of(1L, 1L), PaymentDto.Gateway.VNPAY)).isEmpty());
        assertFalse(validator.validate(new PaymentDto(List.of(0L), PaymentDto.Gateway.VNPAY)).isEmpty());
        assertTrue(validator.validate(new PaymentDto(List.of(1L, 2L), PaymentDto.Gateway.VNPAY)).isEmpty());
    }
    @Test void nestedSupplyQuantityMustBePositive() {
        var request = new RepairCompleteDto(1L, "Done", List.of(new SupplyLineDto("SUPPLY", 0)), null);
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test void everyUseCaseRejectsMalformedPublicRequestData() {
        var now = OffsetDateTime.of(2026, 10, 3, 12, 0, 0, 0, ZoneOffset.UTC);
        var id = UUID.randomUUID();

        assertInvalid(new BookingDto(" ", 0L, List.of(id, id), false));
        assertInvalid(new ApprovalDto(ApprovalDto.Decision.TU_CHOI, " ", 1L));
        assertInvalid(new InvoiceDraftDto("2026-13", List.of("ROOM", "ROOM")));
        assertInvalid(new IssueDto(null, 0L));
        assertInvalid(new PaymentDto(List.of(1L, 1L), null));
        assertInvalid(new RepairDto("", "", null, List.of(id, id, id, id, id, id)));
        assertInvalid(new AssignmentDto(0L, now, 1L));
        assertInvalid(new RepairCompleteDto(1L, "Done", List.of(new SupplyLineDto("PART", 0)), null));
        assertInvalid(new EntryDto(null, "", null, "", null));
        assertInvalid(new VisitorDto("", "", "", now, now));
        assertInvalid(new ActivityDto(1L, " ", now, now, 0L, 1L));
        assertInvalid(new ConfirmDto(List.of(1L, 1L)));
        assertInvalid(new ConvertDto(1L, 6));
        assertInvalid(new DisciplineDto(List.of(1L, 1L), "", "", id, Map.of("V-1", 0L)));
    }

    private void assertInvalid(Object request) {
        assertFalse(validator.validate(request).isEmpty(), request.getClass().getSimpleName());
    }
    @Test void secretsAreAbsentFromJsonAndToString() {
        var mapper = JsonMapper.builder().build();
        var login = new LoginDto("admin", "ci-secret");
        assertFalse(mapper.writeValueAsString(login).contains("ci-secret"));
        assertFalse(login.toString().contains("ci-secret"));
        assertEquals("ci-secret", mapper.readValue("{\"username\":\"admin\",\"password\":\"ci-secret\"}", LoginDto.class).password());
        var credentials = new AccountCredentials(1,"admin","private-hash","Admin","a@example.test","ADMIN","ACTIVE",null,null);
        assertFalse(mapper.writeValueAsString(credentials).contains("private-hash"));
        assertFalse(credentials.toString().contains("private-hash"));
    }
}
