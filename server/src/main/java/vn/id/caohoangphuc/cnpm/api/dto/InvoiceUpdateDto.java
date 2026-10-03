package vn.id.caohoangphuc.cnpm.api.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

/** Editable invoice field that does not alter the calculated charges or payment history. */
public record InvoiceUpdateDto(@NotNull LocalDate dueDate) {}
