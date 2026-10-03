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
public record RepairDto(
    @NotBlank @Size(min = 1, max = 1000) String title,
    @NotBlank @Size(min = 1, max = 1000) String description,
    @NotNull Priority priority,
    @Valid @Size(max = 5) List<@NotNull UUID> fileIds
) {
    public enum Priority { THAP, TRUNG_BINH, CAO, KHAN_CAP }
}
