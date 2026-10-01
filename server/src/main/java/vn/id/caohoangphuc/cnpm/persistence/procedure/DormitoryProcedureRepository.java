package vn.id.caohoangphuc.cnpm.persistence.procedure;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collections;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import vn.id.caohoangphuc.cnpm.api.dto.ApprovalDto;
import vn.id.caohoangphuc.cnpm.api.dto.EntryDto;
import vn.id.caohoangphuc.cnpm.api.dto.MeterDto;

/** Existing SQL contracts only. Actors/student identity must come from a trusted Service. */
@Repository
@Validated
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "transactionManager")
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
    public DormitoryProcedureRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public ProcedureResult bookBed(@NotBlank @Size(max=20) String studentId,
                                    @NotBlank @Size(max=20) String bedId) {
        return call(Procedure.BOOK, studentId, bedId);
    }
    public ProcedureResult decideBooking(@NotNull @Positive Integer registrationId,
            @NotNull ApprovalDto.Decision decision, @Size(max=1000) String reason) {
        if (decision == ApprovalDto.Decision.TU_CHOI && (reason == null || reason.isBlank()))
            throw new IllegalArgumentException("A rejection requires a reason");
        return call(Procedure.APPROVE, registrationId, decision.name(), reason);
    }
    public ProcedureResult recordMeter(@NotNull @Valid MeterDto reading,
                                       @NotBlank @Size(max=50) String actor) {
        // Current SQL columns are int4; do not silently truncate target API int64 values.
        return call(Procedure.METER, reading.roomId(), reading.month(),
            Math.toIntExact(reading.oldElectricity()), Math.toIntExact(reading.newElectricity()),
            Math.toIntExact(reading.oldWater()), Math.toIntExact(reading.newWater()), actor);
    }
    public ProcedureResult issueInvoice(@NotBlank @Size(max=20) String roomId,
            @NotNull @Pattern(regexp="^\\d{4}-(0[1-9]|1[0-2])$") String month) {
        return call(Procedure.INVOICE, roomId, month);
    }
    public ProcedureResult confirmPayment(@NotBlank @Size(max=100) String reference,
            @NotNull PaymentMethod method, @NotNull @Positive @Digits(integer=15, fraction=0) BigDecimal amount,
            @NotBlank @Size(max=50) String actor) {
        return call(Procedure.PAYMENT, reference, method.name(), amount, actor);
    }
    public ProcedureResult completeRepair(@NotNull @Positive Integer repairId,
            @NotBlank @Size(max=20) String supplyId, @NotNull @Positive Integer quantity,
            @NotBlank @Size(max=50) String actor, @Size(max=1000) String note) {
        return call(Procedure.REPAIR, repairId, supplyId, quantity, actor, note);
    }
    public ProcedureResult recordEntry(@NotBlank @Size(max=20) String studentId,
            @NotNull EntryDto.Type type, @NotBlank @Size(max=50) String actor, @Size(max=255) String note) {
        return call(Procedure.GATE, studentId, type.name(), actor, note);
    }
    public ProcedureResult handleDiscipline(@NotNull @Positive Integer violationId,
            @NotNull ViolationLevel level, @NotBlank @Size(max=255) String decision,
            @NotBlank @Size(max=50) String actor) {
        return call(Procedure.DISCIPLINE, violationId, level.name(), decision, actor);
    }
    public ProcedureResult registerCommunity(@NotBlank @Size(max=20) String studentId,
                                              @NotBlank @Size(max=20) String activityId) {
        return call(Procedure.COMMUNITY, studentId, activityId);
    }
    private ProcedureResult call(Procedure procedure, Object... inputs) {
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
