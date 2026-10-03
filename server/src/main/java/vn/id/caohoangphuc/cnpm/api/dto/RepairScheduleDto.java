package vn.id.caohoangphuc.cnpm.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** New repair schedule and warranty information submitted by a technician. */
public record RepairScheduleDto(
        @NotNull Long version,
        @NotNull OffsetDateTime scheduledAt,
        LocalDate warrantyUntil,
        @NotBlank @Size(max = 1000) String note) {}
