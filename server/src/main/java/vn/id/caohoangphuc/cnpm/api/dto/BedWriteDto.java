package vn.id.caohoangphuc.cnpm.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Bed fields accepted when an administrator creates or edits a bed. */
public record BedWriteDto(
        @NotBlank @jakarta.validation.constraints.Size(max = 20) String id,
        @NotBlank @jakarta.validation.constraints.Size(max = 20) String roomId,
        @NotBlank @jakarta.validation.constraints.Size(max = 20) String number,
        @jakarta.validation.constraints.Size(max = 50) String position,
        @NotBlank String status) {}
