package vn.id.caohoangphuc.cnpm.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Editable fields for an existing violation. */
public record ViolationUpdateDto(
    @NotBlank @Size(max = 2000) String content,
    @NotBlank @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL") String level,
    @NotNull @Positive Long version) {}
