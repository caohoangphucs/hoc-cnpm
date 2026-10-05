package vn.id.caohoangphuc.cnpm.service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.id.caohoangphuc.cnpm.api.dto.PaymentDto;

/** VNPAY 2.1.0: tạo URL ký HMAC-SHA512 và kiểm tra thông báo từ provider. */
@Component
@ConditionalOnProperty(name = "payment.vnpay.enabled", havingValue = "true")
public class VnpayGateway implements PaymentGateway {
  private final String terminal;
  private final String secret;
  private final URI paymentUrl;
  private final URI returnUrl;
  private final Clock clock;
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
      .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

  /** Khóa đọc từ môi trường, không ghi log hoặc đưa vào URL. */
  public VnpayGateway(@Value("${payment.vnpay.terminal:}") String terminal,
      @Value("${payment.vnpay.secret:}") String secret,
      @Value("${payment.vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}") URI paymentUrl,
      @Value("${payment.vnpay.return-url:}") URI returnUrl, Clock clock) {
    if (!terminal.matches("[A-Za-z0-9]{8}") || secret.length() < 16)
      throw new IllegalArgumentException("VNPAY credentials are missing");
    if (!"https".equals(paymentUrl.getScheme()) || paymentUrl.getHost() == null
        || paymentUrl.getRawQuery() != null || paymentUrl.getUserInfo() != null)
      throw new IllegalArgumentException("VNPAY payment URL must be HTTPS");
    if (returnUrl.getHost() == null || returnUrl.getUserInfo() != null
        || !java.util.Set.of("http", "https").contains(returnUrl.getScheme()))
      throw new IllegalArgumentException("VNPAY return URL is invalid");
    this.terminal = terminal; this.secret = secret; this.paymentUrl = paymentUrl;
    this.returnUrl = returnUrl; this.clock = clock;
  }

  @Override
  public PaymentDto.Gateway gateway() { return PaymentDto.Gateway.VNPAY; }

  /** Giữ tương thích với interface hiện có; HTTP gọi overload có IP và hạn phiên. */
  @Override
  public Created create(String reference, BigDecimal amount, UUID requestId) {
    return create(reference, amount, requestId, clock.instant().plusSeconds(900), "127.0.0.1");
  }

  /** Số tiền VND nhân 100, thời gian GMT+7, TxnRef ổn định khi retry. */
  @Override
  public Created create(String reference, BigDecimal amount, UUID requestId, Instant expiresAt, String clientIp) {
    String scaled;
    try { scaled = amount.multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString(); }
    catch (ArithmeticException exception) { throw ServiceException.invalid("VNPAY amount must be whole VND"); }
    if (amount.signum() <= 0 || scaled.length() > 12) throw ServiceException.invalid("VNPAY amount exceeds provider limit");
    if (!expiresAt.isAfter(clock.instant())) throw ServiceException.conflict("Payment session has expired");
    var fields = new TreeMap<String, String>();
    fields.put("vnp_Version", "2.1.0"); fields.put("vnp_Command", "pay");
    fields.put("vnp_TmnCode", terminal); fields.put("vnp_Amount", scaled);
    fields.put("vnp_CurrCode", "VND"); fields.put("vnp_TxnRef", reference);
    fields.put("vnp_OrderInfo", "Thanh toan hoa don KTX " + reference.replace("-", ""));
    fields.put("vnp_OrderType", "other"); fields.put("vnp_Locale", "vn");
    fields.put("vnp_ReturnUrl", returnUrl.toString()); fields.put("vnp_IpAddr", clientIp);
    fields.put("vnp_CreateDate", TIME.format(expiresAt.minusSeconds(900)));
    fields.put("vnp_ExpireDate", TIME.format(expiresAt));
    String query = canonical(fields);
    return new Created(URI.create(paymentUrl + "?" + query + "&vnp_SecureHash=" + sign(query)));
  }

  /** Xác thực chữ ký và merchant trước khi diễn giải tiền hoặc trạng thái. */
  @Override
  public Verified verify(Map<String, String> callback) {
    var fields = new TreeMap<String, String>();
    callback.forEach((key, value) -> {
      if (key.startsWith("vnp_") && !key.equals("vnp_SecureHash")
          && !key.equals("vnp_SecureHashType") && value != null && !value.isEmpty()) fields.put(key, value);
    });
    String supplied = callback.getOrDefault("vnp_SecureHash", "");
    String canonical = canonical(fields);
    if (!supplied.matches("[0-9a-fA-F]{128}") || !MessageDigest.isEqual(
        sign(canonical).getBytes(StandardCharsets.US_ASCII), supplied.toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.US_ASCII))
        || !terminal.equals(fields.get("vnp_TmnCode")))
      throw new ServiceException(400, "VNPAY_INVALID_SIGNATURE", "Invalid VNPAY signature or merchant");
    String reference = required(fields, "vnp_TxnRef");
    UUID.fromString(reference);
    String amount = required(fields, "vnp_Amount");
    if (!amount.matches("[0-9]{1,12}")) throw ServiceException.invalid("Invalid VNPAY amount");
    String response = required(fields, "vnp_ResponseCode");
    String status = required(fields, "vnp_TransactionStatus");
    String transaction = required(fields, "vnp_TransactionNo");
    if (!response.matches("[0-9]{2}") || !status.matches("[0-9]{2}") || !transaction.matches("[0-9]{1,20}"))
      throw ServiceException.invalid("Invalid VNPAY result");
    return new Verified("VNPAY", reference, reference + ":" + transaction + ":" + response + ":" + status,
        ServiceContext.hash(canonical), transaction, new BigDecimal(amount).movePointLeft(2),
        "00".equals(response) && "00".equals(status));
  }

  /** Chỉ ký tham số đã sắp xếp và URL-encode đúng một lần. */
  private String canonical(Map<String, String> fields) {
    return fields.entrySet().stream().map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
        .collect(Collectors.joining("&"));
  }

  private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }

  /** HMAC-SHA512 giống thuật toán trong tài liệu VNPAY 2.1.0. */
  private String sign(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA512");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
      return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (java.security.GeneralSecurityException exception) { throw new IllegalStateException("Cannot sign VNPAY request", exception); }
  }

  private String required(Map<String, String> fields, String key) {
    String value = fields.get(key);
    if (value == null || value.isBlank()) throw ServiceException.invalid("Missing VNPAY parameter");
    return value;
  }
}
