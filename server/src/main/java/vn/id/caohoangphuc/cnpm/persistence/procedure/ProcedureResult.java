package vn.id.caohoangphuc.cnpm.persistence.procedure;

public record ProcedureResult(Integer id, String token, String status, String message) {
    public ProcedureResult requireSuccess() {
        if (!"SUCCESS".equals(status) && !"VIOLATION_RECORDED".equals(status))
            throw new ProcedureRejectedException(status == null ? "MISSING_STATUS" : status, message);
        return this;
    }
}
