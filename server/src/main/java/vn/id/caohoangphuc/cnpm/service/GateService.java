package vn.id.caohoangphuc.cnpm.service;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Map;
import vn.id.caohoangphuc.cnpm.api.dto.StudentProfileDto;
import vn.id.caohoangphuc.cnpm.api.dto.ApprovalDto;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.api.dto.EntryDto;
import vn.id.caohoangphuc.cnpm.api.dto.EntryResourceDto;
import vn.id.caohoangphuc.cnpm.api.dto.EntryResourceListDto;
import vn.id.caohoangphuc.cnpm.api.dto.VisitorDto;
import vn.id.caohoangphuc.cnpm.persistence.service.GateRepository;

public class GateService {
  public record VisitorRequest(
      String hostStudentId,
      String encryptedName,
      String encryptedIdentity,
      Instant validFrom,
      Instant validTo) {}

  private final GateRepository repository;
  private final ServiceContext context;

  /** Khởi tạo dịch vụ ghi nhận ra vào và quản lý khách thăm. */
  public GateService(GateRepository repository, ServiceContext context) {
    this.repository = repository;
    this.context = context;
  }

  /** Ghi nhận lượt ra hoặc vào và xử lý lặp event an toàn. */
  public EntryResourceDto record(Actor actor, RequestContext request, EntryDto input) {
    context.validate(input);
    context.text(input.deviceId(), 100, "deviceId");
    String hash = entryFingerprint(input.eventId(), ServiceContext.hash(input.cardToken()),
        input.type(), input.deviceId(), input.note());
    EntryResourceDto entry =
        context.write(
            actor,
            request,
            "entries",
            hash,
            EntryResourceDto.class,
            () -> recordEntry(actor, request, input, hash),
            "BAOVE");
    return entry;
  }

  /** Ghi thủ công theo MSSV chỉ dành cho bảo vệ đã đối chiếu giấy tờ tại cổng. */
  public EntryResourceDto recordManual(Actor actor, RequestContext request,
      vn.id.caohoangphuc.cnpm.api.dto.ManualEntryDto input) {
    context.validate(input);
    String hash = entryFingerprint(input.eventId(), ServiceContext.hash("manual:" + input.studentId()),
        input.type(), input.deviceId(), input.note());
    return context.write(actor, request, "entries/manual", hash, EntryResourceDto.class, () -> {
      context.require(actor, "BAOVE");
      var previous = findPreviousEntry(input.eventId(), hash);
      if (previous != null) return previous;
      var now = context.now();
      var card = repository.cardForStudent(input.studentId(), now);
      requireActiveCard(card, now);
      var event = new EntryDto(input.eventId(), "manual:" + input.studentId(), input.type(), input.deviceId(), input.note());
      return saveEntry(actor, request, event, card, now, hash);
    }, "BAOVE");
  }

  /** Fingerprint chứa hash token và mọi trường, không serialize token thô WRITE_ONLY. */
  private String entryFingerprint(java.util.UUID eventId, String identityHash,
      EntryDto.Type type, String deviceId, String note) {
    record Fingerprint(java.util.UUID eventId, String identityHash, EntryDto.Type type, String deviceId, String note) {}
    return ServiceContext.hash(context.json(new Fingerprint(eventId, identityHash, type, deviceId, note)));
  }

  /** Reads the authenticated student's own profile without exposing other students. */
  public StudentProfileDto profile(Actor actor) {
    return context.transaction(() -> {
      var account = context.require(actor, "SINHVIEN");
      var profile = repository.profile(account.studentId());
      if (profile == null) throw ServiceException.notFound("Student");
      return new StudentProfileDto(
          profile.studentId(), profile.name(), profile.dateOfBirth(), profile.gender(),
          profile.email(), profile.phone(), profile.className(), profile.faculty(),
          profile.hometown(), profile.roomId(), profile.status());
    });
  }

  /** Sinh viên chỉ tra cứu chính mình; bảo vệ/admin được tìm danh sách để làm nghiệp vụ. */
  public java.util.List<GateRepository.StudentOption> students(Actor actor, String search) {
    if (search.length() > 100) throw ServiceException.invalid("Search exceeds 100 characters");
    return context.transaction(() -> {
      var account = context.require(actor, "SINHVIEN", "BAOVE", "ADMIN");
      return repository.students(search.trim(), "SINHVIEN".equals(account.role()) ? account.studentId() : null);
    });
  }

  /** Lists gate entries for a student or for authorized gate/admin staff. */
  public EntryResourceListDto entries(Actor actor, int page, int size) {
    context.page(page, size);
    return context.transaction(() -> {
      var account = context.require(actor, "SINHVIEN", "ADMIN", "BAOVE");
      String studentId = "SINHVIEN".equals(account.role()) ? account.studentId() : null;
      var result = repository.entries(studentId, page, size);
      return new EntryResourceListDto(result.items(), (long) page, (long) size, result.total());
    });
  }

  /** Reads one gate entry and hides it from students other than its owner. */
  public EntryResourceDto entry(Actor actor, java.util.UUID id) {
    if (id == null) throw ServiceException.invalid("Invalid eventId");
    return context.transaction(() -> {
      var account = context.require(actor, "SINHVIEN", "ADMIN", "BAOVE");
      var event = repository.event(id, false);
      if (event == null) throw ServiceException.notFound("Entry");
      if ("SINHVIEN".equals(account.role()) && !account.studentId().equals(event.studentId()))
        throw ServiceException.notFound("Entry");
      return new EntryResourceDto(event.eventId(), event.studentId(), event.type(),
          java.time.OffsetDateTime.ofInstant(event.time(), ServiceContext.ZONE),
          event.barrierStatus(), event.violationId(),
          repository.violation(event.violationId(), event.studentId()));
    });
  }

  /** Xử lý lượt ra/vào sau khi chống lặp event và xác nhận thẻ còn hiệu lực. */
  private EntryResourceDto recordEntry(
      Actor actor, RequestContext request, EntryDto input, String payloadHash) {
    context.require(actor, "BAOVE");

    // Cùng eventId và cùng nội dung trả kết quả cũ; eventId khác nội dung bị từ chối.
    var previous = findPreviousEntry(input.eventId(), payloadHash);
    if (previous != null) return previous;

    var card = repository.card(repository.hashCard(input.cardToken()));
    var now = context.now();
    requireActiveCard(card, now);
    return saveEntry(actor, request, input, card, now, payloadHash);
  }

  /** Trả lượt đã ghi trước đó hoặc từ chối khi cùng eventId nhưng payload khác. */
  private EntryResourceDto findPreviousEntry(java.util.UUID eventId, String payloadHash) {
    var previous = repository.event(eventId, true);
    if (previous == null) return null;
    if (!previous.payloadHash().equals(payloadHash))
      throw ServiceException.conflict("Event ID reused with different input");

    return new EntryResourceDto(
        previous.eventId(),
        previous.studentId(),
        previous.type(),
        java.time.OffsetDateTime.ofInstant(previous.time(), ServiceContext.ZONE),
        previous.barrierStatus(),
        previous.violationId());
  }

  /** Lưu lượt, vi phạm về muộn nếu có, và sự kiện chờ thiết bị kiểm soát cửa. */
  private EntryResourceDto saveEntry(
      Actor actor,
      RequestContext request,
      EntryDto input,
      GateRepository.Card card,
      Instant now,
      String payloadHash) {
    var local = now.atZone(ServiceContext.ZONE);
    boolean late = isLateReturn(input.type(), local.toLocalTime());
    int id =
        repository.entry(
            card.studentId(),
            input.type().name(),
            Integer.toString(actor.accountId()),
            input.note(),
            java.time.LocalDateTime.ofInstant(now, ServiceContext.ZONE));
    Integer violation = null;
    if (late)
      violation =
          repository.lateViolation(
              card.studentId(), id, "Về ký túc xá sau giờ quy định lúc " + local.toLocalTime(), now);
    repository.gateEvent(
        input.eventId(), id, input.deviceId(), payloadHash, actor.accountId(), "PENDING");

    // Bản ghi đang chờ adapter thiết bị xử lý; service chưa xác nhận barrier đã mở.
    context.event(
        actor,
        request,
        "GATE_ENTRY",
        input.eventId().toString(),
        Map.of("studentId", card.studentId(), "late", late));
    return new EntryResourceDto(
        input.eventId(),
        card.studentId(),
        input.type().name(),
        java.time.OffsetDateTime.ofInstant(now, ServiceContext.ZONE),
        "PENDING",
        violation,
        repository.violation(violation, card.studentId()));
  }

  /** Kiểm tra họ tên Unicode và số giấy tờ theo loại, trước khi mã hóa/lưu dữ liệu. */
  private void validateVisitorIdentity(VisitorDto input) {
    String name = input.name().strip().replaceAll("\\s+", " ");
    if (name.length() < 2 || name.length() > 100
        || !name.matches("[\\p{L}\\p{M} .’'\\-]+") || !name.matches(".*\\p{L}.*"))
      throw new ServiceException(422, "INVALID_VISITOR_NAME", "Visitor name must contain letters and have 2-100 characters");
    String identity = input.identity().strip().toUpperCase(java.util.Locale.ROOT);
    var type = input.identityType();
    if (type == null) type = identity.matches("[0-9]{12}") ? VisitorDto.IdentityType.CCCD
        : identity.matches("[0-9]{9}") ? VisitorDto.IdentityType.CMND : VisitorDto.IdentityType.PASSPORT;
    String pattern = switch (type) {
      case CCCD -> "[0-9]{12}";
      case CMND -> "[0-9]{9}";
      case PASSPORT -> "(?=.*[A-Z])(?=.*[0-9])[A-Z0-9]{6,9}";
    };
    if (!identity.matches(pattern))
      throw new ServiceException(422, "INVALID_VISITOR_IDENTITY", "Identity number does not match document type");
  }

  /** Sinh viên khai báo khách đến thăm trong một khoảng thời gian. */
  public CommandResultDto declareVisitor(Actor actor, RequestContext request, VisitorDto input) {
    context.validate(input);
    requireValidVisitorPeriod(input);
    validateVisitorIdentity(input);
    return context.write(
        actor,
        request,
        "visitors",
        input,
        CommandResultDto.class,
        () -> {
          var owner = context.require(actor, "SINHVIEN");
          if (!repository.ownsStudent(owner.id(), input.hostStudentId()))
            throw ServiceException.notFound("Student");

          // Thông tin khách được mã hóa trước khi ghi xuống cơ sở dữ liệu.
          int ownerId = repository.studentAccount(input.hostStudentId());
          if (ownerId <= 0) throw ServiceException.notFound("Student");
          String encryptedName = encrypt(input.name().strip().replaceAll("\\s+", " ")),
              encryptedIdentity = encrypt(input.identity().strip().toUpperCase(java.util.Locale.ROOT));
          long id =
              repository.visitor(
                  input.hostStudentId(),
                  encryptedName,
                  encryptedIdentity,
                  input.validFrom().toInstant(),
                  input.validTo().toInstant());
          context.event(
              actor,
              request,
              "VISITOR_DECLARED",
              Long.toString(id),
              Map.of("hostStudentId", input.hostStudentId()));
          return new CommandResultDto(Long.toString(id), "PENDING", 1L, request.requestId());
        },
        "SINHVIEN");
  }

  /** Lists visitor requests for the authenticated student or all requests for an administrator. */
  public java.util.List<GateRepository.Visitor> visitors(Actor actor, int page, int size) {
    context.page(page, size);
    return context.transaction(() -> {
      var account = context.require(actor, "SINHVIEN", "ADMIN", "BAOVE");
      String host = "SINHVIEN".equals(account.role()) ? account.studentId() : null;
      return repository.visitors(host, "BAOVE".equals(account.role()), page, size);
    });
  }

  /** Reads one visitor request while hiding requests from other students. */
  public GateRepository.Visitor visitor(Actor actor, long id) {
    context.positive(id, "visitorId");
    return context.transaction(() -> {
      var account = context.require(actor, "SINHVIEN", "ADMIN", "BAOVE");
      var visitor = repository.visitor(id, false);
      if (visitor == null) throw ServiceException.notFound("Visitor");
      if ("SINHVIEN".equals(account.role())
          && !repository.ownsStudent(account.id(), visitor.hostStudentId()))
        throw ServiceException.notFound("Visitor");
      if ("BAOVE".equals(account.role()) && !"APPROVED".equals(visitor.state()))
        throw ServiceException.notFound("Visitor");
      return visitor;
    });
  }

  /** Deletes a pending visitor request owned by the student or an administrator. */
  public CommandResultDto deleteVisitor(Actor actor, RequestContext request, long id) {
    context.positive(id, "visitorId");
    return context.write(actor, request, "DELETE:/api/v1/visitors/" + id, id,
        CommandResultDto.class, () -> {
          var account = context.require(actor, "SINHVIEN", "ADMIN");
          var visitor = repository.visitor(id, true);
          if (visitor == null) throw ServiceException.notFound("Visitor");
          if ("SINHVIEN".equals(account.role())
              && !repository.ownsStudent(account.id(), visitor.hostStudentId()))
            throw ServiceException.notFound("Visitor");
          if (!"PENDING".equals(visitor.state()) || repository.hasVisitorEntries(id))
            throw ServiceException.conflict("Only pending visitor requests can be deleted");
          if (!repository.deletePendingVisitor(id)) throw ServiceException.notFound("Visitor");
          context.event(actor, request, "VISITOR_DELETED", Long.toString(id),
              Map.of("hostStudentId", visitor.hostStudentId()));
          return new CommandResultDto(Long.toString(id), "DELETED", visitor.version() + 1,
              request.requestId());
        }, "SINHVIEN", "ADMIN");
  }

  /** Nhân viên có quyền duyệt hoặc từ chối đăng ký khách. */
  public CommandResultDto approveVisitor(
      Actor actor, RequestContext request, long id, ApprovalDto input) {
    context.validate(input);
    return context.write(
        actor,
        request,
        "visitors/" + id + "/approval",
        input,
        CommandResultDto.class,
        () -> {
          context.require(actor, "ADMIN");
          var visitor = repository.visitor(id, true);
          if (visitor == null) throw ServiceException.notFound("Visitor");
          context.version(visitor.version(), input.version());
          if (!visitor.state().equals("PENDING"))
            throw ServiceException.conflict("Visitor request is already decided");

          String state = input.decision() == ApprovalDto.Decision.DUYET ? "APPROVED" : "REJECTED";
          repository.visitorState(id, state, actor.accountId());
          context.event(
              actor, request, "VISITOR_DECIDED", Long.toString(id), Map.of("status", state));
          return new CommandResultDto(
              Long.toString(id), state, visitor.version() + 1, request.requestId());
        },
        "ADMIN");
  }

  /** Bảo vệ kiểm tra khách đã đăng ký khi ra vào ký túc xá. */
  public CommandResultDto checkVisitor(
      Actor actor, RequestContext request, long id, boolean entering) {
    record Input(long id, boolean entering) {}
    var input = new Input(id, entering);
    return context.write(
        actor,
        request,
        "visitors/check",
        input,
        CommandResultDto.class,
        () -> {
          context.require(actor, "BAOVE");
          var visitor = repository.visitor(id, true);
          Instant now = context.now();
          requireVisitorAllowed(visitor, now);
          repository.visitorEntry(id, entering, now);
          context.event(
              actor, request, "VISITOR_ENTRY", Long.toString(id), Map.of("entering", entering));
          return new CommandResultDto(
              Long.toString(id),
              entering ? "CHECKED_IN" : "CHECKED_OUT",
              visitor.version() + 1,
              request.requestId());
        },
        "BAOVE");
  }

  /** Mã hóa dữ liệu khách bằng AES-GCM trước khi lưu. */
  private String encrypt(String value) {
    try {
      String encoded = System.getenv("KTX_PERSONAL_DATA_KEY");
      if (encoded == null)
        throw new ServiceException(
            503, "ENCRYPTION_UNAVAILABLE", "Personal-data encryption is not configured");
      byte[] key = java.util.Base64.getDecoder().decode(encoded);
      if (key.length != 32) throw new IllegalArgumentException("Invalid key size");
      byte[] nonce = new byte[12];
      new java.security.SecureRandom().nextBytes(nonce);
      var cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(
          javax.crypto.Cipher.ENCRYPT_MODE,
          new javax.crypto.spec.SecretKeySpec(key, "AES"),
          new javax.crypto.spec.GCMParameterSpec(128, nonce));
      byte[] encrypted = cipher.doFinal(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return java.util.Base64.getEncoder().encodeToString(nonce)
          + "."
          + java.util.Base64.getEncoder().encodeToString(encrypted);
    } catch (ServiceException exception) {
      throw exception;
    } catch (Exception exception) {
      throw ServiceException.invalid("Personal data could not be encrypted");
    }
  }

  /** Kiểm tra thẻ còn hiệu lực và chủ thẻ vẫn đang lưu trú. */
  private void requireActiveCard(GateRepository.Card card, Instant now) {
    if (card == null
        || !card.state().equals("ACTIVE")
        || now.isBefore(card.validFrom())
        || !now.isBefore(card.validTo())
        || !"DANG_O".equals(card.residencyState()))
      throw ServiceException.conflict("Card or residency is inactive");
  }

  /** Chỉ lượt vào ngoài khung giờ 05:00–23:00 bị tính là về muộn. */
  private boolean isLateReturn(EntryDto.Type type, LocalTime time) {
    return type == EntryDto.Type.VAO
        && (time.isAfter(LocalTime.of(23, 0)) || time.isBefore(LocalTime.of(5, 0)));
  }

  /** Xác nhận khách đã được duyệt và đang trong khung giờ thăm. */
  private void requireVisitorAllowed(GateRepository.Visitor visitor, Instant now) {
    if (visitor == null) throw ServiceException.notFound("Visitor");

    if (!"APPROVED".equals(visitor.state()))
      throw new ServiceException(409, "VISITOR_NOT_APPROVED", "Visitor request is not approved");

    if (now.isBefore(visitor.validFrom()))
      throw new ServiceException(409, "VISITOR_NOT_STARTED", "Visitor visiting period has not started");

    if (!now.isBefore(visitor.validTo()))
      throw new ServiceException(409, "VISITOR_EXPIRED", "Visitor visiting period has expired");
  }

  /** Kiểm tra thời điểm kết thúc đăng ký khách nằm sau thời điểm bắt đầu. */
  private void requireValidVisitorPeriod(VisitorDto input) {
    if (!input.validTo().isAfter(input.validFrom()))
      throw ServiceException.invalid("Visitor validity must end after it starts");
  }
}
