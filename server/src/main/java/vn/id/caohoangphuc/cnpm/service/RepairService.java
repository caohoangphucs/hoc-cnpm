package vn.id.caohoangphuc.cnpm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vn.id.caohoangphuc.cnpm.api.dto.AssignmentDto;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.api.dto.RepairCompleteDto;
import vn.id.caohoangphuc.cnpm.api.dto.RepairDto;
import vn.id.caohoangphuc.cnpm.api.dto.CommandResultDto;
import vn.id.caohoangphuc.cnpm.persistence.service.RepairRepository;
import vn.id.caohoangphuc.cnpm.persistence.service.ServiceRepository;

public class RepairService {
  public record Completion(String status, CommandResultDto result, List<String> shortages) {}

  private final RepairRepository repository;
  private final ServiceContext context;

  /** Khởi tạo dịch vụ xử lý phiếu sửa chữa và vật tư. */
  public RepairService(RepairRepository repository, ServiceContext context) {
    this.repository = repository;
    this.context = context;
  }

  /** Tạo phiếu sửa chữa mới từ yêu cầu hợp lệ. */
  public CommandResultDto create(Actor actor, RequestContext request, RepairDto input) {
    context.validate(input);
    return context.write(
        actor,
        request,
        "repairs",
        input,
        CommandResultDto.class,
        () -> {
          var account = context.require(actor, "SINHVIEN");
          var fileIds = input.fileIds() == null ? List.<java.util.UUID>of() : input.fileIds();
          context.files(fileIds, account.id(), "REPAIR");
          int repairId =
              repository.create(
                  account.studentId(),
                  input.title(),
                  input.description(),
                  input.priority().name(),
                  context.now(),
                  context.json(fileIds));
          context.attach(fileIds);
          repository.history(
              repairId, actor.accountId(), "", "MOI", "Repair request received", context.now());
          context.event(
              actor,
              request,
              "REPAIR_CREATED",
              Integer.toString(repairId),
              Map.of("studentId", account.studentId()));
          return new CommandResultDto(Integer.toString(repairId), "MOI", 1L, request.requestId());
        },
        "SINHVIEN");
  }

  /** Liệt kê phiếu sửa chữa mà vai trò hiện tại được phép xem. */
  public List<RepairRepository.Repair> list(Actor actor, int page, int size) {
    context.page(page, size);
    return context.transaction(
        () -> {
          var account = context.require(actor, "KYTHUAT", "ADMIN", "SINHVIEN");
          if (account.role().equals("SINHVIEN"))
            return repository.listForStudent(account.studentId(), page, size);
          return repository.list(account.role(), account.id(), page, size);
        });
  }

  /** Reads one repair request while enforcing student ownership and technician assignment. */
  public RepairRepository.Repair get(Actor actor, int id) {
    context.positive(id, "repairId");
    return context.transaction(() -> {
      var account = context.require(actor, "SINHVIEN", "KYTHUAT", "ADMIN");
      var repair = repository.repair(id, false);
      if (repair == null) throw ServiceException.notFound("Repair");
      if ("SINHVIEN".equals(account.role()) && !account.studentId().equals(repair.studentId()))
        throw ServiceException.notFound("Repair");
      if ("KYTHUAT".equals(account.role())) {
        var assignment = repository.assignment(id, false);
        if (assignment == null || assignment.technicianId() != account.id())
          throw ServiceException.notFound("Repair");
      }
      return repair;
    });
  }

  /** Deletes an unassigned request owned by the student, or any eligible request for an admin. */
  public CommandResultDto delete(Actor actor, RequestContext request, int id) {
    context.positive(id, "repairId");
    return context.write(actor, request, "DELETE:/api/v1/repairs/" + id, id,
        CommandResultDto.class, () -> {
          var account = context.require(actor, "SINHVIEN", "ADMIN");
          var repair = repository.repair(id, true);
          if (repair == null) throw ServiceException.notFound("Repair");
          if ("SINHVIEN".equals(account.role()) && !account.studentId().equals(repair.studentId()))
            throw ServiceException.notFound("Repair");
          if (!"MOI".equals(repair.status()) || repository.hasHistory(id))
            throw ServiceException.conflict("Only unassigned repair requests can be deleted");
          if (!repository.deleteUnassigned(id)) throw ServiceException.notFound("Repair");
          context.event(actor, request, "REPAIR_DELETED", Integer.toString(id),
              java.util.Map.of("studentId", repair.studentId(), "roomId", repair.roomId()));
          return new CommandResultDto(Integer.toString(id), "DELETED", repair.version() + 1,
              request.requestId());
        }, "SINHVIEN", "ADMIN");
  }

  /** Giao phiếu sửa chữa cho kỹ thuật viên. */
  public CommandResultDto assign(
      Actor actor, RequestContext request, int repairId, AssignmentDto input) {
    context.validate(input);
    return context.write(
        actor,
        request,
        "repairs/" + repairId + "/assignment",
        input,
        CommandResultDto.class,
        () -> {
          var account = context.require(actor, "KYTHUAT", "ADMIN");
          var repair = loadRepair(repairId, input.version());
          int technician = Math.toIntExact(input.technicianAccountId());
          if (repository.technicianRole(technician) != 1)
            throw ServiceException.invalid("Account is not an active technician");
          requireCanAssign(account, technician);
          if (!repair.status().equals("MOI"))
            throw ServiceException.conflict("Repair is already being processed");
          repository.assign(repairId, technician, input.scheduledAt().toInstant());
          repository.history(
              repairId, account.id(), "MOI", "DANG_XU_LY", "Assigned", context.now());
          context.event(
              actor,
              request,
              "REPAIR_ASSIGNED",
              Integer.toString(repairId),
              Map.of("technicianId", technician));
          return new CommandResultDto(
              Integer.toString(repairId), "DANG_XU_LY", repair.version() + 1, request.requestId());
        },
        "KYTHUAT",
        "ADMIN");
  }

  /** Đổi lịch xử lý phiếu sửa chữa đang được giao. */
  public CommandResultDto reschedule(
      Actor actor,
      RequestContext request,
      int id,
      long version,
      java.time.OffsetDateTime scheduledAt,
      java.time.LocalDate warrantyUntil,
      String note) {
    context.text(note, 1000, "note");
    record Input(
        long version,
        java.time.OffsetDateTime scheduledAt,
        java.time.LocalDate warrantyUntil,
        String note) {}
    var input = new Input(version, scheduledAt, warrantyUntil, note);
    return context.write(
        actor,
        request,
        "repairs/" + id + "/schedule",
        input,
        CommandResultDto.class,
        () -> {
          context.require(actor, "KYTHUAT");
          var repair = loadRepair(id, version);
          requireAssignedInProgress(id, repair, actor.accountId());
          repository.schedule(id, scheduledAt.toInstant(), warrantyUntil, note);
          repository.history(
              id, actor.accountId(), repair.status(), repair.status(), note, context.now());
          context.event(
              actor,
              request,
              "REPAIR_SCHEDULED",
              Integer.toString(id),
              Map.of("schedule", scheduledAt));
          return new CommandResultDto(
              Integer.toString(id), repair.status(), version + 1, request.requestId());
        },
        "KYTHUAT");
  }

  /** Hoàn tất sửa chữa nếu kỹ thuật viên được giao và kho đủ toàn bộ vật tư. */
  public Completion complete(Actor actor, RequestContext request, int id, RepairCompleteDto input) {
    context.validate(input);
    return context.write(
        actor,
        request,
        "repairs/" + id + "/completion",
        input,
        Completion.class,
        () -> completeRepair(actor, request, id, input),
        "KYTHUAT");
  }

  /** Kiểm tra quyền sở hữu phiếu, tồn kho và chỉ hoàn tất khi đủ vật tư. */
  private Completion completeRepair(
      Actor actor, RequestContext request, int id, RepairCompleteDto input) {
    context.require(actor, "KYTHUAT");
    var repair = loadRepair(id, input.version());
    requireAssignedInProgress(id, repair, actor.accountId());

    List<RepairRepository.Line> lines = toSupplyLines(input);
    requireUniqueSupplies(lines);
    var shortages = findShortagesAndRequestPurchase(id, lines, request.requestId());
    if (!shortages.isEmpty())
      return reportShortages(actor, request, id, input.version(), shortages);

    finishRepair(actor, request, repair, id, input, lines);
    return new Completion(
        "HOAN_THANH",
        new CommandResultDto(
            Integer.toString(id), "HOAN_THANH", input.version() + 1, request.requestId()),
        List.of());
  }

  /** Tạo phiếu mua bổ sung và để yêu cầu sửa chữa tiếp tục ở trạng thái đang xử lý. */
  private Completion reportShortages(
      Actor actor, RequestContext request, int repairId, long version, List<String> shortages) {
    context.event(
        actor,
        request,
        "REPAIR_STOCK_SHORTAGE",
        Integer.toString(repairId),
        Map.of("supplies", shortages));
    return new Completion(
        "INSUFFICIENT_STOCK",
        new CommandResultDto(
            Integer.toString(repairId), "DANG_XU_LY", version, request.requestId()),
        shortages);
  }

  /** Ghi xuất kho, hoàn tất phiếu, lưu lịch sử và phát sự kiện. */
  private void finishRepair(
      Actor actor,
      RequestContext request,
      RepairRepository.Repair repair,
      int id,
      RepairCompleteDto input,
      List<RepairRepository.Line> lines) {
    repository.complete(
        id, lines, actor.accountId(), input.note(), input.warrantyUntil(), context.now());
    repository.history(
        id, actor.accountId(), "DANG_XU_LY", "HOAN_THANH", input.note(), context.now());
    context.event(
        actor,
        request,
        "REPAIR_COMPLETED",
        Integer.toString(id),
        Map.of("studentId", repair.studentId()));
  }

  /** Đọc và khóa phiếu sửa chữa; báo lỗi nếu mã phiếu không tồn tại. */
  private RepairRepository.Repair requireRepair(int id) {
    var repair = repository.repair(id, true);
    if (repair == null) throw ServiceException.notFound("Repair");
    return repair;
  }

  /** Khóa phiếu và kiểm tra version để các lệnh cập nhật dùng chung quy tắc. */
  private RepairRepository.Repair loadRepair(int id, long expectedVersion) {
    var repair = requireRepair(id);
    context.version(repair.version(), expectedVersion);
    return repair;
  }

  /** Chuyển các dòng vật tư trong DTO sang kiểu dữ liệu repository dùng. */
  private List<RepairRepository.Line> toSupplyLines(RepairCompleteDto input) {
    return input.supplies().stream()
        .map(supply -> new RepairRepository.Line(supply.supplyId(), supply.quantity()))
        .toList();
  }

  /** Kỹ thuật viên chỉ được tự nhận phiếu cho chính mình. */
  private void requireCanAssign(ServiceRepository.Account account, int technicianId) {
    if (account.role().equals("KYTHUAT") && technicianId != account.id())
      throw new ServiceException(403, "FORBIDDEN", "Technicians may only assign themselves");
  }

  /** Kiểm tra người gọi đang được giao phiếu và phiếu vẫn đang xử lý. */
  private void requireAssignedInProgress(
      int repairId, RepairRepository.Repair repair, int actorId) {
    var assignment = repository.assignment(repairId, true);
    if (assignment == null || assignment.technicianId() != actorId)
      throw ServiceException.notFound("Assigned repair");

    if (!repair.status().equals("DANG_XU_LY"))
      throw ServiceException.conflict("Repair is not in progress");
  }

  /** Ngăn một vật tư xuất hiện nhiều lần trong cùng yêu cầu hoàn tất. */
  private void requireUniqueSupplies(List<RepairRepository.Line> lines) {
    long uniqueCount = lines.stream().map(RepairRepository.Line::supplyId).distinct().count();
    if (uniqueCount != lines.size()) throw ServiceException.invalid("Duplicate supplies");
  }

  /** Khóa tồn kho, tìm vật tư thiếu và tạo yêu cầu mua bù cho từng món. */
  private List<String> findShortagesAndRequestPurchase(
      int repairId, List<RepairRepository.Line> lines, java.util.UUID requestId) {
    var shortages = new ArrayList<String>();
    var orderedLines =
        lines.stream()
            .sorted(java.util.Comparator.comparing(RepairRepository.Line::supplyId))
            .toList();

    for (var line : orderedLines) {
      context.positive(line.quantity(), "quantity");
      var supply = repository.supply(line.supplyId(), true);
      if (supply == null) throw ServiceException.notFound("Supply");

      if (supply.stock() < line.quantity()) {
        shortages.add(line.supplyId());
        repository.purchase(repairId, line.supplyId(), line.quantity() - supply.stock(), requestId);
      }
    }
    return shortages;
  }
}
