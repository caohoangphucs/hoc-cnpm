package vn.id.caohoangphuc.cnpm.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Room fields accepted when an administrator creates or edits a room. */
public record RoomWriteDto(
        @NotBlank @jakarta.validation.constraints.Size(max = 20) String id,
        @NotBlank @jakarta.validation.constraints.Size(max = 20) String buildingId,
        @jakarta.validation.constraints.Size(max = 20) String roomTypeId,
        @NotBlank @jakarta.validation.constraints.Size(max = 20) String number,
        @NotNull @DecimalMin("1") Integer floor,
        @NotNull @DecimalMin("1") Integer capacity,
        @NotNull @DecimalMin("0") @jakarta.validation.constraints.DecimalMax("999999999999999") Long basePrice,
        @NotBlank String status) {}
