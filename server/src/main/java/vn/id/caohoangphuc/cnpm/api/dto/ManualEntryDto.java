package vn.id.caohoangphuc.cnpm.api.dto;

import java.util.UUID;
import jakarta.validation.constraints.*;

/** Bảo vệ xác minh trực tiếp sinh viên, không dùng MSSV như token thẻ. */
public record ManualEntryDto(
    @NotNull UUID eventId,
    @NotBlank @Size(max = 20) String studentId,
    @NotNull EntryDto.Type type,
    @NotBlank @Size(max = 100) String deviceId,
    @Size(max = 255) String note,
    @AssertTrue boolean identityVerified) {}
