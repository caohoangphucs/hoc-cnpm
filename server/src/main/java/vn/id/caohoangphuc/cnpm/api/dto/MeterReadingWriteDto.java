package vn.id.caohoangphuc.cnpm.api.dto;

import jakarta.validation.constraints.*;

/** Số đo thực tế và phiên bản đã xem trước khi chỉnh sửa. */
public record MeterReadingWriteDto(
    @NotNull @Min(0) @Max(2147483647) Long oldElectricity,
    @NotNull @Min(0) @Max(2147483647) Long newElectricity,
    @NotNull @Min(0) @Max(2147483647) Long oldWater,
    @NotNull @Min(0) @Max(2147483647) Long newWater,
    @NotBlank @Size(max = 64) String revision) {}
