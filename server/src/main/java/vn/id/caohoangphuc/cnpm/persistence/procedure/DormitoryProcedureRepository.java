package vn.id.caohoangphuc.cnpm.persistence.procedure;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collections;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import vn.id.caohoangphuc.cnpm.api.dto.ApprovalDto;
import vn.id.caohoangphuc.cnpm.api.dto.EntryDto;
import vn.id.caohoangphuc.cnpm.api.dto.MeterDto;

/** Existing SQL contracts only. Actors/student identity must come from a trusted Service. */
public class DormitoryProcedureRepository {
    public enum PaymentMethod { VNPAY, MOMO, VIETQR, TIEN_MAT }
    public enum ViolationLevel { LOW, MEDIUM, HIGH, CRITICAL }
    private enum Procedure {
        BOOK("usp_DatGiuong", new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR}),
        APPROVE("usp_DuyetDangKy", new int[]{Types.VARCHAR, Types.VARCHAR}),
        METER("usp_GhiChiSoDienNuoc", new int[]{Types.VARCHAR, Types.VARCHAR}),
        INVOICE("usp_PhatHanhHoaDon", new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR}),
        PAYMENT("usp_XacNhanThanhToan", new int[]{Types.VARCHAR, Types.VARCHAR}),
        REPAIR("usp_HoanThanhSuaChua", new int[]{Types.VARCHAR, Types.VARCHAR}),
        GATE("usp_GhiNhanRaVao", new int[]{Types.VARCHAR, Types.VARCHAR}),
        DISCIPLINE("usp_XuLyKyLuat", new int[]{Types.VARCHAR, Types.VARCHAR}),
        COMMUNITY("usp_DangKyCongIch", new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR});
        final String name;
        final int[] outputs;
        Procedure(String name, int[] outputs) { this.name = name; this.outputs = outputs; }
    }
    private final JdbcTemplate jdbc;
    private final Validator validator;
    private final TransactionTemplate transaction;

    public DormitoryProcedureRepository(JdbcTemplate jdbc, Validator validator,
                                        PlatformTransactionManager manager) {
        this.jdbc = jdbc;
        this.validator = validator;
        this.transaction = new TransactionTemplate(manager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
    }

    public ProcedureResult bookBed(String studentId, String bedId) {
        requiredText(studentId, 20, "studentId");
        requiredText(bedId, 20, "bedId");
        return call(Procedure.BOOK, studentId, bedId);
    }
    public ProcedureResult decideBooking(Integer registrationId,
            ApprovalDto.Decision decision, String reason) {
        positive(registrationId, "registrationId");
        required(decision, "decision");
        optionalText(reason, 1000, "reason");
        if (decision == ApprovalDto.Decision.TU_CHOI && (reason == null || reason.isBlank()))
            throw new IllegalArgumentException("A rejection requires a reason");
        return call(Procedure.APPROVE, registrationId, decision.name(), reason);
    }
    public ProcedureResult recordMeter(MeterDto reading, String actor) {
        required(reading, "reading");
        requiredText(actor, 50, "actor");
        var violations = validator.validate(reading);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        // Current SQL columns are int4; do not silently truncate target API int64 values.
        return call(Procedure.METER, reading.roomId(), reading.month(),
            Math.toIntExact(reading.oldElectricity()), Math.toIntExact(reading.newElectricity()),
            Math.toIntExact(reading.oldWater()), Math.toIntExact(reading.newWater()), actor);
    }
    public ProcedureResult issueInvoice(String roomId, String month) {
        requiredText(roomId, 20, "roomId");
        required(month, "month");
        if (!month.matches("^\\d{4}-(0[1-9]|1[0-2])$"))
            throw new IllegalArgumentException("month must use YYYY-MM");
        return call(Procedure.INVOICE, roomId, month);
    }
    public ProcedureResult confirmPayment(String reference,
            PaymentMethod method, BigDecimal amount,
            String actor) {
        requiredText(reference, 100, "reference");
        required(method, "method");
        required(amount, "amount");
        if (amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 0
                || amount.compareTo(new BigDecimal("999999999999999")) > 0)
            throw new IllegalArgumentException("amount must be a positive integer of at most 15 digits");
        requiredText(actor, 50, "actor");
        return call(Procedure.PAYMENT, reference, method.name(), amount, actor);
    }
    public ProcedureResult completeRepair(Integer repairId,
            String supplyId, Integer quantity,
            String actor, String note) {
        positive(repairId, "repairId");
        requiredText(supplyId, 20, "supplyId");
        positive(quantity, "quantity");
        requiredText(actor, 50, "actor");
        optionalText(note, 1000, "note");
        return call(Procedure.REPAIR, repairId, supplyId, quantity, actor, note);
    }
    public ProcedureResult recordEntry(String studentId,
            EntryDto.Type type, String actor, String note) {
        requiredText(studentId, 20, "studentId");
        required(type, "type");
        requiredText(actor, 50, "actor");
        optionalText(note, 255, "note");
        return call(Procedure.GATE, studentId, type.name(), actor, note);
    }
    public ProcedureResult handleDiscipline(Integer violationId,
            ViolationLevel level, String decision,
            String actor) {
        positive(violationId, "violationId");
        required(level, "level");
        requiredText(decision, 255, "decision");
        requiredText(actor, 50, "actor");
        return call(Procedure.DISCIPLINE, violationId, level.name(), decision, actor);
    }
    public ProcedureResult registerCommunity(String studentId, String activityId) {
        requiredText(studentId, 20, "studentId");
        requiredText(activityId, 20, "activityId");
        return call(Procedure.COMMUNITY, studentId, activityId);
    }
    private static void required(Object value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
    }

    private static void requiredText(String value, int max, String field) {
        required(value, field);
        if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        optionalText(value, max, field);
    }

    private static void optionalText(String value, int max, String field) {
        if (value != null && value.length() > max)
            throw new IllegalArgumentException(field + " must have at most " + max + " characters");
    }

    private static void positive(Integer value, String field) {
        required(value, field);
        if (value <= 0) throw new IllegalArgumentException(field + " must be positive");
    }

    private ProcedureResult call(Procedure procedure, Object... inputs) {
        // Join the caller's JDBC transaction; a failed procedure marks it for rollback.
        return transaction.execute(status -> execute(procedure, inputs));
    }

    private ProcedureResult execute(Procedure procedure, Object... inputs) {
        int count = inputs.length + procedure.outputs.length;
        String sql = "{call " + procedure.name + "(" + String.join(",", Collections.nCopies(count, "?")) + ")}";
        ProcedureResult result = jdbc.execute((CallableStatementCreator) connection -> {
            var statement = connection.prepareCall(sql);
            for (int i=0; i<inputs.length; i++) statement.setObject(i+1, inputs[i]);
            for (int i=0; i<procedure.outputs.length; i++)
                statement.registerOutParameter(inputs.length+i+1, procedure.outputs[i]);
            return statement;
        }, statement -> {
            statement.execute();
            Integer id = procedure.outputs[0] == Types.INTEGER
                ? (Integer) statement.getObject(inputs.length+1) : null;
            String token = procedure == Procedure.COMMUNITY ? statement.getString(inputs.length+2) : null;
            return new ProcedureResult(id, token, statement.getString(count-1), statement.getString(count));
        });
        if (result == null) throw new ProcedureRejectedException("MISSING_RESULT", "No procedure result");
        return result.requireSuccess();
    }
}
