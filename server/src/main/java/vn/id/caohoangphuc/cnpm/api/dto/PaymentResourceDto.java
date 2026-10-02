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
public record PaymentResourceDto(
    @NotNull UUID id,
    @NotBlank String gateway,
    @NotBlank String providerRef,
    @NotNull @DecimalMin("0") Long amount,
    @NotNull @Valid List<@NotNull @DecimalMin("0") Long> invoiceIds,
    @NotBlank String status,
     URI payUrl,
    @NotNull OffsetDateTime expiresAt
) {

}
