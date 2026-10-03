package vn.id.caohoangphuc.cnpm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import vn.id.caohoangphuc.cnpm.api.dto.DraftResourceDto;
import vn.id.caohoangphuc.cnpm.api.dto.InvoiceDraftDto;
import vn.id.caohoangphuc.cnpm.api.dto.IssueDto;
import vn.id.caohoangphuc.cnpm.api.dto.InvoiceResourceDto;
import vn.id.caohoangphuc.cnpm.api.dto.InvoiceResourceListDto;
import vn.id.caohoangphuc.cnpm.api.dto.InvoiceUpdateDto;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.api.dto.PreviewRoomDto;
import vn.id.caohoangphuc.cnpm.persistence.service.InvoiceRepository;

public class InvoiceService {
  public record RoomSnapshot(
      String roomId,
      String month,
      InvoiceRepository.Reading reading,
      InvoiceRepository.Price price,
      List<InvoiceRepository.Occupant> occupants,
      List<InvoiceRepository.Extra> extras,
      BigDecimal discount,
      BigDecimal total,
      List<String> warnings,
      String hash) {}

  public record IssueRoom(String roomId, UUID draftId, long version) {}

  public record IssueResult(String roomId, String status, String invoiceId, String message) {}

  private final InvoiceRepository repository;
  private final ServiceContext context;

  /** Khởi tạo dịch vụ xem trước và phát hành hóa đơn. */
  public InvoiceService(InvoiceRepository repository, ServiceContext context) {
    this.repository = repository;
    this.context = context;
  }

  /** Lists invoices visible to the student or to accounting and administrators. */
  public InvoiceResourceListDto list(Actor actor, int page, int size) {
    context.page(page, size);
    return context.transaction(() -> {
      var account = context.require(actor, "SINHVIEN", "KETOAN", "ADMIN");
      String studentId = "SINHVIEN".equals(account.role()) ? account.studentId() : null;
      var result = repository.invoices(studentId, page, size);
      return new InvoiceResourceListDto(result.items(), (long) page, (long) size, result.total());
    });
  }

  /** Reads an invoice while restricting student access to their occupied room and month. */
  public InvoiceResourceDto get(Actor actor, int id) {
    context.positive(id, "invoiceId");
    return context.transaction(() -> {
      var account = context.require(actor, "SINHVIEN", "KETOAN", "ADMIN");
      var invoice = repository.invoiceResource(id);
      if (invoice == null) throw ServiceException.notFound("Invoice");
      if ("SINHVIEN".equals(account.role()) && !repository.visibleToStudent(id, account.studentId()))
        throw ServiceException.notFound("Invoice");
      return invoice;
    });
  }

  /** Updates only the due date so calculated charges and payments remain intact. */
  public CommandResultDto update(Actor actor, RequestContext request, int id, InvoiceUpdateDto input) {
    context.validate(input);
    context.positive(id, "invoiceId");
    return context.write(actor, request, "PUT:/api/v1/invoices/" + id, input,
        CommandResultDto.class, () -> {
          context.require(actor, "KETOAN", "ADMIN");
          var invoice = repository.invoiceResource(id);
          if (invoice == null) throw ServiceException.notFound("Invoice");
          if (!"CHUA_TT".equals(invoice.status()) && !"QUA_HAN".equals(invoice.status()))
            throw ServiceException.conflict("Only unpaid invoices can be edited");
          if (!repository.dueDate(id, input.dueDate())) throw ServiceException.notFound("Invoice");
          context.event(actor, request, "INVOICE_UPDATED", Integer.toString(id),
              Map.of("dueDate", input.dueDate()));
          return new CommandResultDto(Integer.toString(id), invoice.status(), 1L, request.requestId());
        }, "KETOAN", "ADMIN");
  }

  /** Deletes an unpaid invoice before it has any associated payment. */
  public CommandResultDto delete(Actor actor, RequestContext request, int id) {
    context.positive(id, "invoiceId");
    return context.write(actor, request, "DELETE:/api/v1/invoices/" + id, id,
        CommandResultDto.class, () -> {
          context.require(actor, "KETOAN", "ADMIN");
          var invoice = repository.invoiceResource(id);
          if (invoice == null) throw ServiceException.notFound("Invoice");
          if (!"CHUA_TT".equals(invoice.status()) || repository.hasPayments(id))
            throw ServiceException.conflict("Only unpaid invoices without payments can be deleted");
          if (!repository.deleteUnpaid(id)) throw ServiceException.notFound("Invoice");
          context.event(actor, request, "INVOICE_DELETED", Integer.toString(id),
              Map.of("roomId", invoice.roomId(), "month", invoice.month()));
          return new CommandResultDto(Integer.toString(id), "DELETED", 1L, request.requestId());
        }, "KETOAN", "ADMIN");
  }

  /** Tính bản xem trước hóa đơn phòng cùng snapshot và cảnh báo dữ liệu. */
  public DraftResourceDto preview(Actor actor, RequestContext request, InvoiceDraftDto input) {
    context.validate(input);
    return context.write(
        actor,
        request,
        "invoice-drafts",
        input,
        DraftResourceDto.class,
        () -> {
          context.require(actor, "KETOAN");

          // Lưu ảnh chụp đầu vào để lúc phát hành phát hiện dữ liệu đã thay đổi.
          var snapshots = new ArrayList<RoomSnapshot>();
          for (String roomId : input.roomIds().stream().sorted().toList())
            snapshots.add(snapshot(roomId, input.month()));
          UUID id = UUID.randomUUID();
          String allHash = ServiceContext.hash(context.json(snapshots));
          var rooms = snapshots.stream().map(this::resource).toList();
          repository.saveDraft(
              id, actor.accountId(), input.month(), context.json(snapshots), allHash);
          return new DraftResourceDto(id, 1L, input.month(), rooms, allHash);
        },
        "KETOAN");
  }

  /** Phát hành hóa đơn cho các phòng sau khi đối chiếu bản xem trước. */
  public List<IssueResult> issue(Actor actor, RequestContext request, IssueDto input) {
    context.validate(input);
    var draft = repository.draft(input.draftId(), actor.accountId(), false);
    if (draft == null) throw ServiceException.notFound("Invoice draft");
    context.version(draft.version(), input.version());

    // Mỗi phòng phát hành độc lập, nhưng luôn được đối chiếu lại với snapshot.
    List<RoomSnapshot> rooms =
        java.util.Arrays.asList(context.parse(draft.rooms(), RoomSnapshot[].class));
    var result = new ArrayList<IssueResult>();
    for (RoomSnapshot expected : rooms) {
      var item = new IssueRoom(expected.roomId(), input.draftId(), input.version());
      var one = issueRoom(actor, request, item);
      result.add(one);
    }
    return result;
  }

  /** Đối chiếu snapshot và phát hành một hóa đơn trong cùng transaction. */
  private IssueResult issueRoom(Actor actor, RequestContext request, IssueRoom input) {
    return context.write(
        actor,
        request,
        "invoice-issue/" + input.roomId(),
        input,
        IssueResult.class,
        () -> {
          context.require(actor, "KETOAN");
          var draft = repository.draft(input.draftId(), actor.accountId(), true);
          if (draft == null) throw ServiceException.notFound("Invoice draft");
          context.version(draft.version(), input.version());
          RoomSnapshot expected = findSnapshot(draft.rooms(), input.roomId());
          if (expected == null) throw ServiceException.notFound("Room in draft");
          repository.lockRoom(input.roomId());

          // Không phát hành theo số liệu cũ nếu dữ liệu đã đổi sau preview.
          RoomSnapshot current = snapshot(input.roomId(), draft.month());
          requireIssuableSnapshot(current, expected);
          LocalDate due = LocalDate.ofInstant(context.now(), ServiceContext.ZONE).plusDays(15);
          var invoice =
              repository.issue(
                  current.roomId(),
                  current.month(),
                  current.reading(),
                  current.price(),
                  current.discount(),
                  current.extras(),
                  due);
          repository.snapshot(invoice.id(), current.hash(), context.json(current));
          context.event(
              actor,
              request,
              "INVOICE_ISSUED",
              Integer.toString(invoice.id()),
              Map.of("roomId", invoice.roomId(), "total", invoice.total()));
          return new IssueResult(
              input.roomId(), "SUCCESS", Integer.toString(invoice.id()), "Invoice issued");
        },
        "KETOAN");
  }

  /** Tìm snapshot của một phòng trong dữ liệu preview đã lưu. */
  private RoomSnapshot findSnapshot(String json, String roomId) {
    for (RoomSnapshot item : context.parse(json, RoomSnapshot[].class))
      if (item.roomId().equals(roomId)) return item;
    return null;
  }

  /** Đọc dữ liệu hóa đơn hiện tại và tạo snapshot để preview hoặc so sánh trước khi phát hành. */
  private RoomSnapshot snapshot(String roomId, String month) {
    if (!repository.roomExists(roomId)) throw ServiceException.notFound("Room");
    var warnings = new ArrayList<String>();

    // Thiếu dữ liệu được giữ thành cảnh báo để kế toán bổ sung trước khi phát hành.
    var reading = loadReading(roomId, month, warnings);
    var price = loadPrice(roomId, month, warnings);
    if (repository.issued(roomId, month)) warnings.add("ALREADY_ISSUED");
    var occupants = repository.occupants(roomId, month, price.room());

    // Tính tổng tiền tại service; giảm giá được làm tròn xuống đơn vị đồng.
    BigDecimal discount = calculateDiscount(occupants);
    var extras = repository.extras(roomId, month);
    BigDecimal total = calculateTotal(reading, price, extras, discount);
    requireValidTotal(total);
    RoomSnapshot value =
        new RoomSnapshot(
            roomId, month, reading, price, occupants, extras, discount, total, warnings, "");
    return new RoomSnapshot(
        roomId,
        month,
        reading,
        price,
        occupants,
        extras,
        discount,
        total,
        warnings,
        ServiceContext.hash(context.json(value)));
  }

  /** Đọc chỉ số; thiếu dữ liệu được ghi thành cảnh báo và dùng giá trị 0 cho preview. */
  private InvoiceRepository.Reading loadReading(
      String roomId, String month, List<String> warnings) {
    var reading = repository.reading(roomId, month);
    if (reading == null) {
      warnings.add("MISSING_METER");
      return new InvoiceRepository.Reading(0, 0, 0, 0);
    }

    requireValidReading(reading);
    return reading;
  }

  /** Đọc giá áp dụng; thiếu giá được ghi thành cảnh báo để chặn phát hành sau này. */
  private InvoiceRepository.Price loadPrice(String roomId, String month, List<String> warnings) {
    var price = repository.prices(roomId, month);
    if (price != null) return price;

    warnings.add("MISSING_PRICE");
    return new InvoiceRepository.Price(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  /** Cộng phần giảm giá của từng người ở, làm tròn xuống đến đồng. */
  private BigDecimal calculateDiscount(List<InvoiceRepository.Occupant> occupants) {
    return occupants.stream()
        .map(
            occupant ->
                occupant
                    .roomShare()
                    .multiply(occupant.discountPercent())
                    .divide(BigDecimal.valueOf(100))
                    .setScale(0, java.math.RoundingMode.FLOOR))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /** Tính tiền phòng, điện, nước và phụ phí rồi trừ giảm giá. */
  private BigDecimal calculateTotal(
      InvoiceRepository.Reading reading,
      InvoiceRepository.Price price,
      List<InvoiceRepository.Extra> extras,
      BigDecimal discount) {
    BigDecimal electricity =
        BigDecimal.valueOf(reading.newElectricity() - reading.oldElectricity())
            .multiply(price.electricity());
    BigDecimal water =
        BigDecimal.valueOf(reading.newWater() - reading.oldWater()).multiply(price.water());
    BigDecimal extraTotal =
        extras.stream()
            .map(InvoiceRepository.Extra::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return price.room().add(electricity).add(water).add(extraTotal).subtract(discount);
  }

  /** Yêu cầu dữ liệu phát hành vẫn khớp preview và không còn cảnh báo. */
  private void requireIssuableSnapshot(RoomSnapshot current, RoomSnapshot expected) {
    if (!current.hash().equals(expected.hash()))
      throw ServiceException.conflict("Invoice inputs changed; create a new preview");

    if (!current.warnings().isEmpty())
      throw ServiceException.conflict(String.join(", ", current.warnings()));
  }

  /** Không chấp nhận chỉ số điện hoặc nước giảm so với kỳ trước. */
  private void requireValidReading(InvoiceRepository.Reading reading) {
    boolean electricityWentBackwards = reading.newElectricity() < reading.oldElectricity();
    boolean waterWentBackwards = reading.newWater() < reading.oldWater();
    if (electricityWentBackwards || waterWentBackwards)
      throw ServiceException.invalid("Meter readings cannot decrease");
  }

  /** Kiểm tra tổng hóa đơn nằm trong phạm vi số tiền lưu được. */
  private void requireValidTotal(BigDecimal total) {
    if (total.signum() < 0 || total.compareTo(new BigDecimal("999999999999999")) > 0)
      throw ServiceException.invalid("Invoice total is out of range");
  }

  /** Chuyển snapshot nội bộ thành dữ liệu preview trả cho caller. */
  private PreviewRoomDto resource(RoomSnapshot value) {
    return new PreviewRoomDto(
        value.roomId(),
        value.reading().oldElectricity(),
        value.reading().newElectricity(),
        value.reading().oldWater(),
        value.reading().newWater(),
        value.price().electricity().longValueExact(),
        value.price().water().longValueExact(),
        value.price().room().longValueExact(),
        value.discount().longValueExact(),
        value.extras().stream()
            .map(InvoiceRepository.Extra::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .longValueExact(),
        value.total().longValueExact(),
        value.warnings());
  }
}
