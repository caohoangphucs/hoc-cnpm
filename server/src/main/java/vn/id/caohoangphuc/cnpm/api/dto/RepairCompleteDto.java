package vn.id.caohoangphuc.cnpm.api.dto;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import vn.id.caohoangphuc.cnpm.api.validation.UniqueElements;

/** Target API contract; not proof that the associated endpoint is implemented. */
public record RepairCompleteDto(
    @NotNull @DecimalMin("1") Long version,
    @NotBlank @Size(min = 1, max = 1000) String note,
    @NotNull @Valid @Size(max = 100) List<@NotNull @Valid SupplyLineDto> supplies,
     LocalDate warrantyUntil,
    @Size(max = 10) List<@NotNull UUID> fileIds
) {
    /** Giữ tương thích request cũ không có ảnh nghiệm thu. */
    public RepairCompleteDto(Long version, String note, List<SupplyLineDto> supplies, LocalDate warrantyUntil) {
        this(version, note, supplies, warrantyUntil, List.of());
    }
}
