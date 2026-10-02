package vn.id.caohoangphuc.cnpm.service;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;
import vn.id.caohoangphuc.cnpm.persistence.service.ServiceRepository;

/** Tiện ích giao dịch dùng chung; quy tắc từng UC vẫn nằm trong service tương ứng. */
public class ServiceContext {
  public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
  private final ServiceRepository repository;
  private final Validator validator;
  private final JsonMapper json;
  private final Clock clock;
  private final TransactionTemplate transaction;

  public ServiceContext(
      ServiceRepository repository,
      Validator validator,
      JsonMapper json,
      Clock clock,
      PlatformTransactionManager manager) {
    this.repository = repository;
    this.validator = validator;
    this.json = json;
    this.clock = clock;
    this.transaction = new TransactionTemplate(manager);
  }

  /** Trả về thời điểm hiện tại từ clock được cấu hình cho ứng dụng. */
  public Instant now() {
    return clock.instant();
  }

  /** Chạy thao tác trong transaction quản lý bởi Spring. */
  public <T> T transaction(Supplier<T> work) {
    return transaction.execute(status -> work.get());
  }

  /** Xác thực tài khoản đang hoạt động, vai trò được phép và yêu cầu 2FA. */
  public ServiceRepository.Account require(Actor actor, String... roles) {
    if (actor == null) throw new ServiceException(401, "UNAUTHENTICATED", "Login required");
    var account = repository.account(actor.accountId());
    if (account == null || !"ACTIVE".equals(account.status()))
      throw new ServiceException(401, "UNAUTHENTICATED", "Account is not active");
    if (!Arrays.asList(roles).contains(account.role()))
      throw new ServiceException(403, "FORBIDDEN", "Role is not allowed");
    requireTwoFactorForSensitiveRole(account.role(), actor);
    return account;
  }

  /** Bắt buộc xác thực hai lớp cho tài khoản quản trị và kế toán. */
  private void requireTwoFactorForSensitiveRole(String role, Actor actor) {
    boolean sensitiveRole = role.equals("ADMIN") || role.equals("KETOAN");
    if (sensitiveRole && !actor.twoFactorVerified())
      throw new ServiceException(401, "TWO_FACTOR_REQUIRED", "Two-factor verification required");
  }

  /** Chạy kiểm tra bean validation cho dữ liệu đầu vào. */
  public void validate(Object dto) {
    if (dto == null) throw ServiceException.invalid("Request body is required");
    var violations = validator.validate(dto);
    if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
  }

  /** Báo xung đột nếu version dữ liệu khác version caller đã đọc. */
  public void version(long actual, long expected) {
    if (actual != expected)
      throw ServiceException.conflict("Version has changed; reload the resource");
  }

  /** Kiểm tra chuỗi bắt buộc có nội dung và không vượt quá độ dài cho phép. */
  public void text(String value, int max, String field) {
    if (value == null || value.isBlank() || value.length() > max)
      throw ServiceException.invalid("Invalid " + field);
  }

  /** Kiểm tra một mã hoặc số lượng phải lớn hơn không. */
  public void positive(long value, String field) {
    if (value <= 0) throw ServiceException.invalid(field + " must be positive");
  }

  /** Kiểm tra giới hạn phân trang dùng chung trong các danh sách. */
  public void page(int page, int size) {
    if (page < 1 || size < 1 || size > 100) throw ServiceException.invalid("Invalid page");
  }

  /** Giới hạn số bản ghi xử lý trong một lần chạy nền. */
  public void batchSize(int size) {
    if (size < 1 || size > 1000) throw ServiceException.invalid("Invalid batch size");
  }

  /** Chuyển giá trị Java thành JSON bằng mapper dùng chung. */
  public String json(Object value) {
    return json.writeValueAsString(value);
  }

  /** Đọc JSON thành kiểu dữ liệu Java được chỉ định. */
  public <T> T parse(String value, Class<T> type) {
    return json.readValue(value, type);
  }

  /** Tạo SHA-256 dạng hex để so sánh payload mà không lưu nội dung gốc. */
  public static String hash(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }

  /** Chạy lệnh ghi với quyền, idempotency, transaction và lưu response để retry an toàn. */
  public <T> T write(
      Actor actor,
      RequestContext request,
      String route,
      Object input,
      Class<T> type,
      Supplier<T> work,
      String... roles) {
    if (request == null) throw ServiceException.invalid("Request context is required");

    // Gộp quyền, idempotency và nghiệp vụ trong một transaction để retry không tạo bản ghi lặp.
    String hash = hash(json(input));
    for (int attempt = 0; ; attempt++) {
      try {
        return transaction(
            () -> {
              require(actor, roles);
              repository.lockRequest(actor.accountId(), route, request.idempotencyKey());
              var saved = repository.request(actor.accountId(), route, request.idempotencyKey());
              if (saved != null && saved.expiresAt().isAfter(now())) {
                if (!saved.hash().equals(hash))
                  throw ServiceException.conflict("Idempotency key was used with different input");
                return parse(saved.response(), type);
              }
              T result = work.get();
              repository.saveRequest(
                  actor.accountId(),
                  route,
                  request.idempotencyKey(),
                  hash,
                  json(result),
                  now().plusSeconds(86400));
              return result;
            });
      } catch (TransientDataAccessException exception) {
        if (attempt >= 2)
          throw new ServiceException(
              503, "DATABASE_BUSY", "Database is busy; retry with the same key");
      }
    }
  }

  /** Kiểm tra file nháp thuộc người gọi, đúng loại và chưa hết hạn. */
  public void files(List<UUID> ids, int ownerId, String... kinds) {
    if (ids == null || ids.size() > 5 || new HashSet<>(ids).size() != ids.size())
      throw ServiceException.invalid("Invalid file list");
    for (UUID id : ids.stream().sorted().toList()) {
      var file = repository.file(id);
      if (file == null || file.ownerId() != ownerId) throw ServiceException.notFound("File");
      if (!Arrays.asList(kinds).contains(file.kind())
          || !file.state().equals("DRAFT")
          || !file.expiresAt().isAfter(now()))
        throw ServiceException.invalid("File is not available for this purpose");
    }
  }

  /** Gắn các file đã kiểm tra vào bản ghi nghiệp vụ. */
  public void attach(List<UUID> ids) {
    ids.forEach(repository::attachFile);
  }

  /** Đọc loại file để áp dụng quy tắc theo mục đích sử dụng. */
  public String fileKind(UUID id) {
    return repository.file(id).kind();
  }

  /** Ghi audit và đưa sự kiện vào outbox trong transaction hiện tại. */
  public void event(Actor actor, RequestContext request, String type, String id, Object details) {
    String payload = json(details);
    repository.audit(actor.accountId(), request.requestId(), type, "UPDATE", payload);
    repository.enqueue(type, id, payload, type + ":" + id + ":" + request.requestId());
  }
}
