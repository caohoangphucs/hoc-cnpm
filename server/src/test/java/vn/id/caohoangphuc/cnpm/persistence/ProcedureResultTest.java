package vn.id.caohoangphuc.cnpm.persistence;

import org.junit.jupiter.api.Test;
import vn.id.caohoangphuc.cnpm.persistence.procedure.*;
import static org.junit.jupiter.api.Assertions.*;

class ProcedureResultTest {
    @Test void successAndRecordedViolationAreAccepted() {
        assertDoesNotThrow(() -> new ProcedureResult(1,null,"SUCCESS","ok").requireSuccess());
        assertDoesNotThrow(() -> new ProcedureResult(null,null,"VIOLATION_RECORDED","late").requireSuccess());
    }
    @Test void businessErrorThrowsToTriggerRollback() {
        var failure = assertThrows(ProcedureRejectedException.class,
            () -> new ProcedureResult(null,null,"ERROR_FULL","Full").requireSuccess());
        assertEquals("ERROR_FULL", failure.getStatus());
    }
    @Test void absentOrUnknownStatusCannotMeanSuccess() {
        assertThrows(ProcedureRejectedException.class,
            () -> new ProcedureResult(null,null,null,null).requireSuccess());
        assertThrows(ProcedureRejectedException.class,
            () -> new ProcedureResult(null,null,"UNKNOWN","?").requireSuccess());
    }
}
