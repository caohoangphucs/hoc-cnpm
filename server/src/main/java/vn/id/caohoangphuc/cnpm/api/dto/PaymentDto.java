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
public record PaymentDto(
    @NotNull @Valid @Size(min = 1, max = 100) @UniqueElements List<@NotNull @DecimalMin("1") @DecimalMax("2147483647") Long> invoiceIds,
    @NotNull Gateway gateway
) {
    public enum Gateway { VNPAY, MOMO }
}
