package vn.id.caohoangphuc.cnpm.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/** Đánh giá ưu tiên bởi kỹ thuật viên được giao phiếu. */
public record RepairPriorityDto(@NotNull RepairDto.Priority priority, @Min(1) long version) {}
