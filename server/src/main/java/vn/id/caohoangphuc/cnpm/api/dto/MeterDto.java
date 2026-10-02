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
public record MeterDto(
    @NotBlank @Size(min = 1, max = 1000) String roomId,
    @NotBlank @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$") String month,
    @NotNull @DecimalMin("0") @DecimalMax("999999999999999") Long oldElectricity,
    @NotNull @DecimalMin("0") @DecimalMax("999999999999999") Long newElectricity,
    @NotNull @DecimalMin("0") @DecimalMax("999999999999999") Long oldWater,
    @NotNull @DecimalMin("0") @DecimalMax("999999999999999") Long newWater
) {

    /** Ensures neither new utility meter reading is below its previous reading. */
    @AssertTrue(message = "New readings must be greater than or equal to old readings")
    @JsonIgnore
    public boolean isNonDecreasing() {
        return oldElectricity == null || newElectricity == null || oldWater == null || newWater == null
            || (newElectricity >= oldElectricity && newWater >= oldWater);
    }
}
