package vn.id.caohoangphuc.cnpm.persistence.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

/** Internal authentication projection; not an API response. */
public record AccountCredentials(Integer accountId, String username, @JsonIgnore String passwordHash,
        String fullName, String email, String role, String status, String studentId, String roomId) {
    @Override public String toString() {
        return "AccountCredentials[accountId=" + accountId + ", role=" + role + ", passwordHash=REDACTED]";
    }
}
