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
public record SessionDto(
    @NotNull Boolean authenticated,
     String role,
     Boolean twoFactorVerified,
    @NotBlank String csrfToken,
     OffsetDateTime idleExpiresAt,
     String studentId
) {
    /** Giữ tương thích các response/test không có thông tin sinh viên. */
    public SessionDto(Boolean authenticated, String role, Boolean twoFactorVerified,
            String csrfToken, OffsetDateTime idleExpiresAt) {
        this(authenticated, role, twoFactorVerified, csrfToken, idleExpiresAt, null);
    }
}

