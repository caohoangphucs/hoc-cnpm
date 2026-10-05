package vn.id.caohoangphuc.cnpm.service;

import java.util.Objects;
import java.util.UUID;

public record RequestContext(UUID requestId, UUID idempotencyKey, String clientIp) {
  /** Các tác vụ nội bộ không có địa chỉ HTTP. */
  public RequestContext(UUID requestId, UUID idempotencyKey) {
    this(requestId, idempotencyKey, "127.0.0.1");
  }
  public RequestContext {
    Objects.requireNonNull(requestId, "requestId");
    Objects.requireNonNull(idempotencyKey, "idempotencyKey");
  }
}
