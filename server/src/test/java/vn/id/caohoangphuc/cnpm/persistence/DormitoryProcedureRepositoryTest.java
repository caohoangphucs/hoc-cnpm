package vn.id.caohoangphuc.cnpm.persistence;

import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import vn.id.caohoangphuc.cnpm.api.dto.ApprovalDto;
import vn.id.caohoangphuc.cnpm.api.dto.EntryDto;
import vn.id.caohoangphuc.cnpm.api.dto.MeterDto;
import vn.id.caohoangphuc.cnpm.persistence.procedure.DormitoryProcedureRepository;
import vn.id.caohoangphuc.cnpm.persistence.procedure.ProcedureRejectedException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DormitoryProcedureRepositoryTest {
    @Test void invalidArgumentsAreRejectedWithoutSpringProxyOrDatabaseAccess() {
        var jdbc = mock(JdbcTemplate.class);
        var manager = mock(PlatformTransactionManager.class);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var repository = new DormitoryProcedureRepository(jdbc, factory.getValidator(), manager);
            List<Runnable> invalidCalls = List.of(
                () -> repository.bookBed(" ", "BED"),
                () -> repository.bookBed("SV", "x".repeat(21)),
                () -> repository.decideBooking(0, ApprovalDto.Decision.DUYET, null),
                () -> repository.decideBooking(1, null, null),
                () -> repository.decideBooking(1, ApprovalDto.Decision.TU_CHOI, " "),
                () -> repository.issueInvoice("ROOM", "2026-13"),
                () -> repository.confirmPayment("REF", DormitoryProcedureRepository.PaymentMethod.MOMO,
                    new BigDecimal("1.5"), "actor"),
                () -> repository.confirmPayment("REF", DormitoryProcedureRepository.PaymentMethod.MOMO,
                    new BigDecimal("1000000000000000"), "actor"),
                () -> repository.completeRepair(1, "SUPPLY", -1, "actor", null),
                () -> repository.recordEntry("SV", EntryDto.Type.RA, "actor", "x".repeat(256)),
                () -> repository.handleDiscipline(1, null, "decision", "actor"),
                () -> repository.registerCommunity(null, "ACTIVITY")
            );
            for (var call : invalidCalls) assertThrows(IllegalArgumentException.class, call::run);
            assertThrows(ConstraintViolationException.class, () -> repository.recordMeter(
                new MeterDto("ROOM", "2026-01", 10L, 9L, 0L, 1L), "actor"));
            assertThrows(ArithmeticException.class, () -> repository.recordMeter(
                new MeterDto("ROOM", "2026-01", 0L, 2147483648L, 0L, 1L), "actor"));
            verifyNoInteractions(jdbc, manager);
        }
    }

    @Test void validWritesStillRequireCallerTransactionWithoutSpringProxy() {
        var jdbc = mock(JdbcTemplate.class);
        var dataSource = mock(DataSource.class);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var repository = new DormitoryProcedureRepository(jdbc, factory.getValidator(),
                new JdbcTransactionManager(dataSource));
            assertThrows(IllegalTransactionStateException.class, () -> repository.bookBed("SV", "BED"));
            verifyNoInteractions(jdbc, dataSource);
        }
    }

    @Test void missingProcedureResultRollsBackParticipatingTransaction() {
        var jdbc = mock(JdbcTemplate.class);
        var manager = mock(PlatformTransactionManager.class);
        var status = new SimpleTransactionStatus(false);
        when(manager.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var repository = new DormitoryProcedureRepository(jdbc, factory.getValidator(), manager);
            var error = assertThrows(ProcedureRejectedException.class, () -> repository.bookBed("SV", "BED"));
            assertEquals("MISSING_RESULT", error.getStatus());
            verify(manager).getTransaction(argThat(definition ->
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY));
            verify(manager).rollback(status);
            verify(manager, never()).commit(any());
        }
    }
}
