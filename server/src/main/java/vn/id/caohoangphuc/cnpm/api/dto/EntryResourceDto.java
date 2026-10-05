package vn.id.caohoangphuc.cnpm.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Lượt ra/vào và thông tin vi phạm phát sinh từ chính lượt đó. */
public record EntryResourceDto(
    @NotNull UUID eventId,
    @NotBlank String studentId,
    @NotBlank String type,
    @NotNull OffsetDateTime time,
    @NotBlank String barrierStatus,
    Integer violationId,
    GateViolation violation
) {
    /** Chi tiết giới hạn cho bảo vệ/sinh viên, không bao gồm hồ sơ kỷ luật khác. */
    public record GateViolation(int id, String content, String level, String status,
            OffsetDateTime occurredAt) {}

    /** Danh sách lượt cổng giữ response nhẹ; xem chi tiết để đọc nội dung vi phạm. */
    public EntryResourceDto(UUID eventId, String studentId, String type, OffsetDateTime time,
            String barrierStatus, Integer violationId) {
        this(eventId, studentId, type, time, barrierStatus, violationId, null);
    }
}
