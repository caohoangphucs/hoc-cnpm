package vn.id.caohoangphuc.cnpm.api.dto;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Trạng thái phiên đăng nhập và MSSV đã được server xác nhận. */
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
