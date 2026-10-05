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
public record VisitorDto(
    @NotBlank @Size(min = 1, max = 1000) String hostStudentId,
    @NotBlank @Size(min = 1, max = 1000) String name,
    @NotBlank @Size(min = 1, max = 1000) String identity,
    @NotNull OffsetDateTime validFrom,
    @NotNull OffsetDateTime validTo,
    IdentityType identityType
) {
    public enum IdentityType { CCCD, CMND, PASSPORT }

    /** Request cũ có thể bỏ loại giấy tờ, service suy luận theo định dạng. */
    public VisitorDto(String hostStudentId, String name, String identity,
            OffsetDateTime validFrom, OffsetDateTime validTo) {
        this(hostStudentId,name,identity,validFrom,validTo,null);
    }
}
