package vn.id.caohoangphuc.cnpm.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import vn.id.caohoangphuc.cnpm.api.dto.ConvertDto;
import vn.id.caohoangphuc.cnpm.persistence.service.CommunityRepository;
import vn.id.caohoangphuc.cnpm.persistence.service.ServiceRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CommunityServiceTest {
  @Test
  void conversionUsesInternalSemesterForLedgerAndPublicSemesterForWrites() {
    var repository = mock(CommunityRepository.class);
    var context = mock(ServiceContext.class);
    var actor = new Actor(5, true);
    var request = new RequestContext(UUID.randomUUID(), UUID.randomUUID());
    var input = new ConvertDto(7L, 2);
    when(context.require(actor, "SINHVIEN"))
        .thenReturn(new ServiceRepository.Account(5, "SINHVIEN", "ACTIVE", "SV01"));
    when(context.write(eq(actor), eq(request), eq("point-conversions"), eq(input),
        eq(CommunityService.ConversionReceipt.class), any(), eq("SINHVIEN")))
        .thenAnswer(call -> {
          Supplier<CommunityService.ConversionReceipt> work = call.getArgument(5);
          return work.get();
        });
    when(repository.studentSemester("SV01", 7L)).thenReturn("HK1-2026");
    when(repository.ledger("SV01", "HK1-2026")).thenReturn(List.of(
        new CommunityRepository.Ledger(1, "SV01", "HK1-2026", 57, "CONG_DIEM", Instant.EPOCH)));
    when(repository.createConversion(any(UUID.class), eq("SV01"), eq("7"), eq(20), eq(2)))
        .thenReturn(42);

    var receipt = new CommunityService(repository, context).convert(actor, request, input);

    assertEquals("PENDING", receipt.status());
    assertEquals(20, receipt.pointsSpent());
    verify(repository).ledger("SV01", "HK1-2026");
    verify(repository).createConversion(any(UUID.class), eq("SV01"), eq("7"), eq(20), eq(2));
    verify(repository).debit("SV01", "7", 20, 42);
  }
}
