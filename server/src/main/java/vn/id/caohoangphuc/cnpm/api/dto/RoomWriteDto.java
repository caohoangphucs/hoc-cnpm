package vn.id.caohoangphuc.cnpm.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Room fields accepted when an administrator creates or edits a room. */
public record RoomWriteDto(
        @NotBlank String id,
        @NotBlank String buildingId,
        String roomTypeId,
        @NotBlank String number,
        @NotNull @DecimalMin("1") Integer floor,
        @NotNull @DecimalMin("1") Integer capacity,
        @NotNull @DecimalMin("0") Long basePrice,
        @NotBlank String status) {}
