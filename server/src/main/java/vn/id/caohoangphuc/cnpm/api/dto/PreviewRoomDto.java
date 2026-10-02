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
public record PreviewRoomDto(
    @NotBlank String roomId,
    @NotNull @DecimalMin("0") Long oldElectricity,
    @NotNull @DecimalMin("0") Long newElectricity,
    @NotNull @DecimalMin("0") Long oldWater,
    @NotNull @DecimalMin("0") Long newWater,
    @NotNull @DecimalMin("0") Long electricityPrice,
    @NotNull @DecimalMin("0") Long waterPrice,
    @NotNull @DecimalMin("0") Long roomPrice,
    @NotNull @DecimalMin("0") Long discount,
    @NotNull @DecimalMin("0") Long extras,
    @NotNull @DecimalMin("0") Long total,
    @NotNull @Valid List<@NotBlank String> warnings
) {

}
