package vn.id.caohoangphuc.cnpm.service;

import static org.junit.jupiter.api.Assertions.*;
import java.net.URI;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Vector HMAC dựng độc lập bằng Python; không sử dụng khóa merchant thật. */
class VnpayGatewayTest {
  private final VnpayGateway gateway = new VnpayGateway("TEST1234", "sandbox-test-secret-only-12345678",
      URI.create("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"),
      URI.create("https://demo.example/api/v1/payments/vnpay/return"),
      Clock.fixed(Instant.parse("2026-10-04T10:00:00Z"), ZoneOffset.UTC));

  private Map<String, String> callback() {
    return new HashMap<>(Map.of("vnp_TmnCode", "TEST1234",
        "vnp_TxnRef", "12345678-1234-1234-1234-123456789012", "vnp_Amount", "50000000",
        "vnp_ResponseCode", "00", "vnp_TransactionStatus", "00", "vnp_TransactionNo", "12345",
        "vnp_SecureHash", "2437bac3aacd993489319445750af79390d3480c3fcf784be385095d4370aa1a108e72857ce6bd34ff2147817576d4ae7c6f3346daf07fd4792221d8dbd20b04"));
  }

  @Test
  void acceptsIndependentSignedVectorAndConvertsMinorUnits() {
    var verified = gateway.verify(callback());
    assertTrue(verified.successful());
    assertEquals(0, new BigDecimal("500000").compareTo(verified.amount()));
  }

  @Test
  void rejectsAlteredAmountMerchantAndStatus() {
    for (String key : new String[]{"vnp_Amount", "vnp_TmnCode", "vnp_TransactionStatus"}) {
      var fields = callback(); fields.put(key, "99");
      assertThrows(ServiceException.class, () -> gateway.verify(fields));
    }
  }

  @Test
  void signsUrlWithStableReferenceVietnamTimeAndExpiry() {
    var expiry = Instant.parse("2026-10-04T10:15:00Z");
    var id = UUID.fromString("12345678-1234-1234-1234-123456789012");
    var created = gateway.create(id.toString(), new BigDecimal("500000"), id, expiry, "203.0.113.1");
    String url = created.payUrl().toString();
    assertTrue(url.contains("vnp_Amount=50000000"));
    assertTrue(url.contains("vnp_TxnRef=12345678123412341234123456789012"));
    assertTrue(url.contains("vnp_CreateDate=20261004170000"));
    assertTrue(url.contains("vnp_ExpireDate=20261004171500"));
    assertEquals(created, gateway.create(id.toString(), new BigDecimal("500000"), id, expiry, "203.0.113.1"));
    assertThrows(ServiceException.class, () -> gateway.create(id.toString(), new BigDecimal("10000000000"), id));
  }
}
