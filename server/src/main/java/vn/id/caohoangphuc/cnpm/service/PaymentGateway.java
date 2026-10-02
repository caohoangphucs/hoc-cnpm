package vn.id.caohoangphuc.cnpm.service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import vn.id.caohoangphuc.cnpm.api.dto.PaymentDto;

public interface PaymentGateway {
  record Created(URI payUrl) {}

  record Verified(
      String gateway,
      String providerRef,
      String eventKey,
      String payloadHash,
      String transactionId,
      BigDecimal amount,
      boolean successful) {}

  PaymentDto.Gateway gateway();

  Created create(String providerRef, BigDecimal amount, UUID requestId);

  Verified verify(Map<String, String> callback);
}
