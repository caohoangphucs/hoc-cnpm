package vn.id.caohoangphuc.cnpm.persistence.procedure;

/** Runtime exception ensures Spring rolls back even when SQL returns a business error. */
public class ProcedureRejectedException extends RuntimeException {
    private final String status;
    public ProcedureRejectedException(String status, String message) {
        super(message == null ? "Procedure rejected the operation" : message);
        this.status = status;
    }
    public String getStatus() { return status; }
}
