package vn.id.caohoangphuc.cnpm.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/** Fields used to create or edit a disciplinary violation record. */
public record ViolationWriteDto(
    @NotBlank String studentId,
    @NotBlank @Size(max = 2000) String content,
    @NotBlank @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL") String level,
    OffsetDateTime occurredAt) {}
