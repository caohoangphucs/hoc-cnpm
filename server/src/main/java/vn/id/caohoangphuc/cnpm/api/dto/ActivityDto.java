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
public record ActivityDto(
    @NotNull @DecimalMin("1") Long semesterId,
    @NotBlank @Size(min = 1, max = 255) String title,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @NotNull @DecimalMin("1") @DecimalMax("2147483647") Long capacity,
    @NotNull @DecimalMin("1") @DecimalMax("2147483647") Long points
) {

    /** Ensures a complete activity time range ends after it begins. */
    @AssertTrue(message = "Activity end must follow start")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return startAt == null || endAt == null || endAt.isAfter(startAt);
    }
}
