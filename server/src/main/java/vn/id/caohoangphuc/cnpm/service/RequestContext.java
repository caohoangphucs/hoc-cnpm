package vn.id.caohoangphuc.cnpm.service;

import java.util.Objects;
import java.util.UUID;

public record RequestContext(UUID requestId, UUID idempotencyKey) {
  public RequestContext {
    Objects.requireNonNull(requestId, "requestId");
    Objects.requireNonNull(idempotencyKey, "idempotencyKey");
  }
}
