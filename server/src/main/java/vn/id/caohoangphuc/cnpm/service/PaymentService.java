package vn.id.caohoangphuc.cnpm.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import vn.id.caohoangphuc.cnpm.api.dto.PaymentDto;
import vn.id.caohoangphuc.cnpm.api.dto.PaymentResourceDto;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.persistence.service.PaymentRepository;
import vn.id.caohoangphuc.cnpm.persistence.service.ServiceRepository;

public class PaymentService {
  private final PaymentRepository repository;
  private final ServiceContext context;
  private final List<PaymentGateway> gateways;

  /** Khởi tạo dịch vụ thanh toán cùng các adapter cổng thanh toán. */
  public PaymentService(
      PaymentRepository repository, ServiceContext context, List<PaymentGateway> gateways) {
    this.repository = repository;
    this.context = context;
    this.gateways = gateways;
  }

  /** Đọc các hóa đơn người gọi có thể thanh toán. */
  public List<PaymentRepository.Invoice> payableInvoices(Actor actor, List<Long> ids) {
    return context.transaction(
        () -> {
          var account = context.require(actor, "SINHVIEN");
          return loadOwnedInvoices(ids, account.studentId(), false);
        });
  }

  /** Tạo phiên thanh toán và lấy URL provider sau khi phiên PENDING đã được lưu. */
  public PaymentResourceDto create(Actor actor, RequestContext request, PaymentDto input) {
    context.validate(input);
    PaymentGateway gateway = gateway(input.gateway());
    UUID id = request.idempotencyKey();
    String providerRef = id.toString();
    record CreatePayment(PaymentDto input, UUID id) {}
    var command = new CreatePayment(input, id);

    // Tạo phiên PENDING và phân bổ hóa đơn trong DB trước khi gọi nhà cung cấp.
    var session =
        context.write(
            actor,
            request,
            "payment-sessions",
            command,
            PaymentResourceDto.class,
            () -> createPendingSession(actor, request, input, id, providerRef),
            "SINHVIEN");
    // Retry dùng lại providerRef để nhà cung cấp nhận diện cùng một phiên thanh toán.
    if (session.payUrl() != null) return session;

    // Không giữ transaction DB khi chờ mạng; providerRef ổn định giúp retry an toàn.
    return createProviderPayment(actor, request, gateway, providerRef, session);
  }

  /** Đọc trạng thái phiên thanh toán nếu người gọi được phép xem. */
  public PaymentResourceDto status(Actor actor, UUID id) {
    return context.transaction(
        () -> {
          var account = context.require(actor, "SINHVIEN", "KETOAN", "ADMIN");
          var session = repository.session(id, false);
          if (!canViewSession(account, session)) throw ServiceException.notFound("Payment session");
          return resource(session);
        });
  }

  /** Lists payment sessions for the student, accountant, or administrator. */
  public List<PaymentResourceDto> list(Actor actor, int page, int size) {
    context.page(page, size);
    return context.transaction(() -> {
      var account = context.require(actor, "SINHVIEN", "KETOAN", "ADMIN");
      Integer ownerId = "SINHVIEN".equals(account.role()) ? account.id() : null;
      return repository.sessions(ownerId, page, size).stream().map(this::resource).toList();
    });
  }

  /** Deletes an unpaid session only when no provider URL or callback can still complete it. */
  public CommandResultDto delete(Actor actor, RequestContext request, UUID id) {
    if (id == null) throw ServiceException.invalid("Invalid payment session ID");
    return context.write(actor, request, "DELETE:/api/v1/payment-sessions/" + id, id,
        CommandResultDto.class, () -> {
          var account = context.require(actor, "SINHVIEN", "KETOAN", "ADMIN");
          var session = repository.session(id, true);
          if (!canViewSession(account, session)) throw ServiceException.notFound("Payment session");
          if (!"PENDING".equals(session.status()) || session.payUrl() != null || repository.hasCallback(id))
            throw ServiceException.conflict("Payment session cannot be deleted after provider processing starts");
          if (!repository.deletePending(id)) throw ServiceException.notFound("Payment session");
          context.event(actor, request, "PAYMENT_SESSION_DELETED", id.toString(),
              Map.of("providerRef", session.providerRef()));
          return new CommandResultDto(id.toString(), "DELETED", 1L, request.requestId());
        }, "SINHVIEN", "KETOAN", "ADMIN");
  }

  /** Xác thực callback bằng adapter của cổng thanh toán tương ứng. */
  public PaymentGateway.Verified verify(String gatewayName, Map<String, String> callback) {
    PaymentDto.Gateway type;
    try {
      type = PaymentDto.Gateway.valueOf(gatewayName.toUpperCase());
    } catch (RuntimeException exception) {
      throw ServiceException.invalid("Unsupported payment gateway");
    }
    return gateway(type).verify(callback);
  }

  /** Áp dụng kết quả callback đã được adapter xác thực và lưu để chống xử lý lặp. */
  public PaymentResourceDto accept(
      Actor provider, RequestContext request, PaymentGateway.Verified payment) {
    // Chỉ gọi sau khi adapter đã xác thực chữ ký và định danh merchant của provider.
    requireVerifiedCallback(payment);
    return context.transaction(() -> applyCallback(provider, request, payment));
  }

  /** Đóng các phiên thanh toán đã hết hạn theo giới hạn batch. */
  public int expire(Actor actor, int limit) {
    context.batchSize(limit);
    var ids =
        context.transaction(
            () -> {
              context.require(actor, "ADMIN");
              return repository.expired(context.now(), limit);
            });
    int count = 0;
    for (UUID id : ids)
      if (context.transaction(
          () -> {
            context.require(actor, "ADMIN");
            return repository.expire(id, context.now());
          })) count++;
    return count;
  }

  /** Tìm adapter đã cấu hình cho cổng được chọn. */
  private PaymentGateway gateway(PaymentDto.Gateway type) {
    return gateways.stream()
        .filter(g -> g.gateway() == type)
        .findFirst()
        .orElseThrow(
            () ->
                new ServiceException(
                    503, "PAYMENT_GATEWAY_UNAVAILABLE", "Payment gateway is not configured"));
  }

  /** Khóa hóa đơn, tính số phải thu và tạo phiên PENDING trong một transaction. */
  private PaymentResourceDto createPendingSession(
      Actor actor, RequestContext request, PaymentDto input, UUID id, String providerRef) {
    var account = context.require(actor, "SINHVIEN");
    BigDecimal total = BigDecimal.ZERO;
    var invoices = loadOwnedInvoices(input.invoiceIds(), account.studentId(), true);
    for (var invoice : invoices) total = total.add(outstandingAmount(invoice));

    requirePaymentAmount(total);
    var session =
        repository.create(
            id,
            account.id(),
            input.gateway().name(),
            providerRef,
            total,
            context.now().plusSeconds(900));
    allocateInvoices(id, input.gateway(), invoices);
    context.event(
        actor,
        request,
        "PAYMENT_CREATED",
        id.toString(),
        Map.of("amount", total, "invoiceIds", input.invoiceIds()));
    return resource(session);
  }

  /** Tạo URL ngoài transaction DB rồi lưu URL nếu phiên vẫn thuộc người gọi. */
  private PaymentResourceDto createProviderPayment(
      Actor actor,
      RequestContext request,
      PaymentGateway gateway,
      String providerRef,
      PaymentResourceDto session) {
    var created =
        gateway.create(providerRef, BigDecimal.valueOf(session.amount()), request.requestId(), session.expiresAt().toInstant(), request.clientIp());
    requireSecurePaymentUrl(created);

    context.transaction(
        () -> {
          context.require(actor, "SINHVIEN");
          var current = repository.session(session.id(), actor.accountId(), true);
          if (current == null) throw ServiceException.notFound("Payment session");
          requireSamePaymentUrl(current, created);
          repository.payUrl(session.id(), created.payUrl().toString());
          return null;
        });

    return new PaymentResourceDto(
        session.id(),
        session.gateway(),
        session.providerRef(),
        session.amount(),
        session.invoiceIds(),
        session.status(),
        created.payUrl(),
        session.expiresAt());
  }

  /** Ghi các khoản phân bổ và bản ghi thanh toán chờ cho từng hóa đơn. */
  private void allocateInvoices(
      UUID sessionId, PaymentDto.Gateway gateway, List<PaymentRepository.Invoice> invoices) {
    for (var invoice : invoices) {
      BigDecimal amount = invoice.total().subtract(invoice.paid());
      repository.allocate(
          sessionId, invoice.id(), gateway.name(), amount, sessionId + ":" + invoice.id());
    }
  }

  /** Xử lý callback trong transaction và lưu kết quả để provider gửi lặp lại an toàn. */
  private PaymentResourceDto applyCallback(
      Actor provider, RequestContext request, PaymentGateway.Verified payment) {
    var session = repository.byReference(payment.gateway(), payment.providerRef(), true);
    if (session == null) throw ServiceException.notFound("Payment session");

    var previous = repository.callback(payment.gateway(), payment.eventKey());
    if (previous != null) return replayCallback(previous, session, payment);

    requireCallbackAmount(session, payment);
    var allocations = repository.allocations(session.id());
    updatePaymentState(provider, session, allocations, payment);

    var updated = repository.byReference(payment.gateway(), payment.providerRef(), false);
    var result = resource(updated);
    repository.saveCallback(
        payment.gateway(),
        payment.eventKey(),
        session.id(),
        payment.payloadHash(),
        context.json(result),
        context.now());
    context.event(
        provider == null ? new Actor(session.actorId(), false) : provider,
        request,
        "PAYMENT_CALLBACK",
        session.id().toString(),
        Map.of("status", updated.status()));
    return result;
  }

  /** Trả kết quả cũ nếu eventKey và nội dung callback giống hệt lần trước. */
  private PaymentResourceDto replayCallback(
      PaymentRepository.Callback previous,
      PaymentRepository.Session session,
      PaymentGateway.Verified payment) {
    if (!previous.hash().equals(payment.payloadHash())
        || !previous.sessionId().equals(session.id()))
      throw ServiceException.conflict("Provider event key was reused");

    return context.parse(previous.response(), PaymentResourceDto.class);
  }

  /** Chọn chuyển trạng thái; chỉ callback thành công còn khớp mới cập nhật công nợ. */
  private void updatePaymentState(
      Actor provider,
      PaymentRepository.Session session,
      List<PaymentRepository.Allocation> allocations,
      PaymentGateway.Verified payment) {
    if (session.status().equals("SUCCESS")) return;

    if (!payment.successful()) {
      if (session.status().equals("PENDING")) {
        repository.paymentState(session.id(), "FAILED", payment.transactionId());
        repository.childState(session.id(), "FAILED");
      }
      return;
    }

    if (needsManualReview(session, allocations)) {
      repository.paymentState(session.id(), "NEEDS_REVIEW", payment.transactionId());
      return;
    }

    for (var allocation : allocations)
      repository.confirmInvoice(allocation, provider == null ? 0 : provider.accountId());
    repository.paymentState(session.id(), "SUCCESS", payment.transactionId());
  }

  /** Từ chối callback nếu tổng tiền provider gửi không đúng với phiên đã tạo. */
  private void requireCallbackAmount(
      PaymentRepository.Session session, PaymentGateway.Verified payment) {
    if (session.amount().compareTo(payment.amount()) != 0)
      throw new ServiceException(409, "CALLBACK_AMOUNT_MISMATCH", "Callback amount does not match");
  }

  /** Không để sinh viên đọc hoặc thanh toán hóa đơn của người khác. */
  private void requireOwnedInvoice(PaymentRepository.Invoice invoice, String studentId) {
    if (invoice == null || !repository.belongsToStudent(invoice.id(), studentId))
      throw ServiceException.notFound("Invoice");
  }

  /** Đọc danh sách hóa đơn theo thứ tự cố định và xác minh tất cả thuộc cùng sinh viên. */
  private List<PaymentRepository.Invoice> loadOwnedInvoices(
      List<Long> ids, String studentId, boolean lock) {
    var invoices = new ArrayList<PaymentRepository.Invoice>();
    for (long id : ids.stream().sorted().toList()) {
      var invoice = repository.invoice(Math.toIntExact(id), lock);
      requireOwnedInvoice(invoice, studentId);
      invoices.add(invoice);
    }
    return invoices;
  }

  /** Sinh viên chỉ xem phiên của mình; kế toán và quản trị được xem phiên chung. */
  private boolean canViewSession(
      ServiceRepository.Account account, PaymentRepository.Session session) {
    if (session == null) return false;

    boolean isStudent = account.role().equals("SINHVIEN");
    return !isStudent || session.actorId() == account.id();
  }

  /** Kiểm tra trạng thái hóa đơn và trả về số tiền còn phải thanh toán. */
  private BigDecimal outstandingAmount(PaymentRepository.Invoice invoice) {
    if (!List.of("CHUA_TT", "TT_MOT_PHAN", "QUA_HAN").contains(invoice.status()))
      throw ServiceException.conflict("Invoice is not payable");

    BigDecimal due = invoice.total().subtract(invoice.paid());
    if (due.signum() <= 0) throw ServiceException.conflict("Invoice has no outstanding balance");
    return due;
  }

  /** Giới hạn tổng tiền phiên theo phạm vi kiểu số tiền trong schema. */
  private void requirePaymentAmount(BigDecimal amount) {
    if (amount.compareTo(new BigDecimal("999999999999999")) > 0)
      throw ServiceException.invalid("Payment amount is too large");
  }

  /** Từ chối kết quả callback thiếu hoặc có số tiền không hợp lệ. */
  private void requireVerifiedCallback(PaymentGateway.Verified payment) {
    if (payment == null || payment.amount() == null || payment.amount().signum() <= 0)
      throw new ServiceException(401, "INVALID_CALLBACK", "Payment callback could not be verified");
  }

  /** Chỉ chấp nhận URL HTTPS do cổng thanh toán trả về. */
  private void requireSecurePaymentUrl(PaymentGateway.Created created) {
    if (created == null
        || created.payUrl() == null
        || !"https".equalsIgnoreCase(created.payUrl().getScheme()))
      throw new ServiceException(
          503, "PAYMENT_GATEWAY_UNAVAILABLE", "Payment provider did not return a secure URL");
  }

  /** Đảm bảo retry cùng providerRef không sinh URL thanh toán khác. */
  private void requireSamePaymentUrl(
      PaymentRepository.Session session, PaymentGateway.Created created) {
    if (session.payUrl() != null && !session.payUrl().equals(created.payUrl().toString()))
      throw ServiceException.conflict("Provider returned a different URL for the same payment");
  }

  /** Đối soát phiên và từng hóa đơn trước khi ghi nhận callback thành công. */
  private boolean needsManualReview(
      PaymentRepository.Session session, List<PaymentRepository.Allocation> allocations) {
    if (session.status().equals("NEEDS_REVIEW") || !session.expiresAt().isAfter(context.now()))
      return true;

    for (var allocation : allocations) {
      var invoice = repository.invoice(allocation.invoiceId(), true);
      if (invoice == null
          || invoice.status().equals("HUY")
          || invoice.total().subtract(invoice.paid()).compareTo(allocation.amount()) < 0)
        return true;
    }
    return false;
  }

  /** Chuyển phiên lưu trong DB sang response và tính trạng thái hết hạn khi đọc. */
  private PaymentResourceDto resource(PaymentRepository.Session s) {
    var url = s.payUrl() == null ? null : java.net.URI.create(s.payUrl());
    var invoices = repository.allocations(s.id()).stream().map(a -> (long) a.invoiceId()).toList();
    String status = s.status();
    if (status.equals("PENDING") && !s.expiresAt().isAfter(context.now())) status = "EXPIRED";
    return new PaymentResourceDto(
        s.id(),
        s.gateway(),
        s.providerRef(),
        s.amount().longValueExact(),
        invoices,
        status,
        url,
        OffsetDateTime.ofInstant(s.expiresAt(), ServiceContext.ZONE));
  }
}
