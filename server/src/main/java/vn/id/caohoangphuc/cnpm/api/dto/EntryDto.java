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
public record EntryDto(
    @NotNull UUID eventId,
    @NotBlank @Size(min = 1, max = 1000) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String cardToken,
    @NotNull Type type,
    @NotBlank @Size(min = 1, max = 100) String deviceId,
    @Size(min = 1, max = 255) String note
) {
    public enum Type { VAO, RA }
    @Override public String toString() { return "EntryDto[eventId=" + eventId + ", cardToken=REDACTED]"; }
}
