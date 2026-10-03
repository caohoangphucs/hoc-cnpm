package vn.id.caohoangphuc.cnpm.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Bed fields accepted when an administrator creates or edits a bed. */
public record BedWriteDto(
        @NotBlank String id,
        @NotBlank String roomId,
        @NotBlank String number,
        String position,
        @NotBlank String status) {}
