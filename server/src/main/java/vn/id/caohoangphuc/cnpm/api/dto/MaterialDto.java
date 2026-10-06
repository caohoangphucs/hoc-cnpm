package vn.id.caohoangphuc.cnpm.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Danh mục vật tư mới và tồn kho ban đầu. */
public record MaterialDto(
    @NotBlank @Size(max = 20) @Pattern(regexp = "[A-Za-z0-9_-]+") String id,
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 20) String unit,
    @Min(0) int stock,
    @NotNull @DecimalMin("0") @Digits(integer = 15, fraction = 0) BigDecimal price) {}
