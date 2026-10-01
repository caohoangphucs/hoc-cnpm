package vn.id.caohoangphuc.cnpm.persistence;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;
import vn.id.caohoangphuc.cnpm.KtxApplication;
import vn.id.caohoangphuc.cnpm.api.dto.ApprovalDto;
import vn.id.caohoangphuc.cnpm.api.dto.EntryDto;
import vn.id.caohoangphuc.cnpm.api.dto.MeterDto;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentialsRepository;
import vn.id.caohoangphuc.cnpm.persistence.procedure.*;
import vn.id.caohoangphuc.cnpm.persistence.read.*;
import static org.junit.jupiter.api.Assertions.*;

/** Runs only against a freshly prepared disposable PostgreSQL16 database. */
@SpringBootTest(classes = KtxApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DatabaseFoundationIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired DormitoryProcedureRepository procedures;
    @Autowired AccountCredentialsRepository credentials;
    @Autowired PasswordEncoder encoder;
    @Autowired ApplicationContext context;
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired @Qualifier("transactionManager") PlatformTransactionManager manager;
    private TransactionTemplate tx;
    private String student, bed, room, supply;
    private Integer repair;
    @BeforeEach void fixtures() {
        tx = new TransactionTemplate(manager);
        student = jdbc.queryForObject("SELECT MaSinhVien FROM SINH_VIEN WHERE MaSinhVien NOT IN (SELECT MaSinhVien FROM LUU_TRU WHERE TrangThai='DANG_O') AND MaSinhVien NOT IN (SELECT MaSinhVien FROM DANG_KY_PHONG WHERE TrangThai='CHO_DUYET') ORDER BY MaSinhVien LIMIT 1", String.class);
        bed = jdbc.queryForObject("SELECT MaGiuong FROM GIUONG WHERE TrangThai='TRONG' ORDER BY MaGiuong LIMIT 1", String.class);
        room = jdbc.queryForObject("SELECT MaPhong FROM PHONG ORDER BY MaPhong LIMIT 1", String.class);
        supply = jdbc.queryForObject("SELECT MaVatTu FROM VAT_TU WHERE SoLuongTon>0 ORDER BY MaVatTu LIMIT 1", String.class);
        repair = jdbc.queryForObject("SELECT MaYeuCau FROM YEU_CAU_SUA_CHUA WHERE TrangThai IN ('MOI','DANG_XU_LY') ORDER BY MaYeuCau LIMIT 1", Integer.class);
    }
    @Test void schemaAndEveryEntityCanBeReadWithoutSchemaMutation() {
        assertEquals(27, entityManagerFactory.getMetamodel().getEntities().size());
        var repositories = context.getBeansOfType(ReadRepository.class);
        assertEquals(27, repositories.size());
        for (var repository : repositories.values()) assertNotNull(repository.findAll(PageRequest.of(0,1)));
        assertEquals(27, jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'", Integer.class));
    }
    @Test void connectionsUseIntendedTimezone() {
        assertEquals("Asia/Ho_Chi_Minh", jdbc.queryForObject("SHOW TimeZone",String.class));
    }
    @Test void loginFunctionAndBcryptMatchAllFiveRolesAndRejectWrongPassword() {
        var roles = Map.of("admin","ADMIN","sv001","SINHVIEN","ketoan1","KETOAN","kythuat1","KYTHUAT","baove1","BAOVE");
        roles.forEach((username,role) -> {
            var user = credentials.findByUsername(username).orElseThrow();
            assertEquals(role,user.role());
            assertTrue(encoder.matches("123456",user.passwordHash()));
            assertFalse(encoder.matches("admin123",user.passwordHash()));
            assertFalse(encoder.matches("ci-wrong",user.passwordHash()));
        });
        assertTrue(credentials.findByUsername("ci-missing").isEmpty());
        assertTrue(credentials.findByUsername("admin' OR '1'='1").isEmpty());
        assertFalse(encoder.matches("123456","broken-hash"));
    }
    private void rollback(Runnable action) {
        tx.executeWithoutResult(status -> { action.run(); status.setRollbackOnly(); });
    }
    private void rejected(String expected, Runnable action) {
        var error = assertThrows(ProcedureRejectedException.class,
            () -> tx.executeWithoutResult(status -> action.run()));
        assertEquals(expected,error.getStatus());
    }
    @Test void allNineProcedureContractsHaveVerifiedStatusesAndOutputValues() {
        rollback(() -> {
            var booking = procedures.bookBed(student, bed);
            assertEquals("SUCCESS",booking.status());assertNotNull(booking.id());
            assertEquals("GIU_CHO",jdbc.queryForObject("SELECT TrangThai FROM GIUONG WHERE MaGiuong=?",String.class,bed));
            assertEquals("SUCCESS",procedures.decideBooking(booking.id(),ApprovalDto.Decision.TU_CHOI,"CI").status());
        });
        rollback(() -> assertEquals("SUCCESS",procedures.recordMeter(new MeterDto(room,"2099-01",0L,1L,0L,1L),"CI").status()));
        rejected("ERROR_50006", () -> procedures.issueInvoice(room,"2099-01"));
        rejected("ERROR_NOT_FOUND", () -> procedures.confirmPayment("CI_NO_TX",DormitoryProcedureRepository.PaymentMethod.TIEN_MAT,BigDecimal.ONE,"CI"));
        rollback(() -> assertEquals("SUCCESS",procedures.completeRepair(repair,supply,1,"CI","Done").status()));
        rollback(() -> assertEquals("SUCCESS",procedures.recordEntry(student,EntryDto.Type.RA,"CI","CI").status()));
        rejected("ERROR_NOT_FOUND", () -> procedures.handleDiscipline(Integer.MAX_VALUE,DormitoryProcedureRepository.ViolationLevel.LOW,"NHAC_NHO","CI"));
        rejected("ERROR_CLOSED", () -> procedures.registerCommunity(student,"CI_NO_ACTIVITY"));
    }
    @Test void invoiceAndCommunityCreationReturnIdsAndQrToken() {
        var activity = jdbc.queryForObject("SELECT MaHoatDong FROM HOAT_DONG WHERE TrangThai='MO_DANG_KY' AND DaDangKy<ChiTieu AND NOT EXISTS (SELECT 1 FROM DANG_KY_CONG_ICH d WHERE d.MaHoatDong=HOAT_DONG.MaHoatDong AND d.MaSinhVien=?) ORDER BY MaHoatDong LIMIT 1",String.class,student);
        rollback(() -> {
            var registration = procedures.registerCommunity(student,activity);
            assertNotNull(registration.id());assertNotNull(registration.token());
            assertFalse(registration.token().isBlank());
        });
        rollback(() -> {
            procedures.recordMeter(new MeterDto(room,"2099-01",0L,10L,0L,10L),"CI");
            var invoice = procedures.issueInvoice(room,"2099-01");
            assertNotNull(invoice.id());
            assertEquals("SUCCESS",invoice.status());
        });
    }
    @Test void writesRequireCallerTransaction() {
        assertThrows(IllegalTransactionStateException.class, () -> procedures.bookBed(student,bed));
    }
    @Test void procedureFailureRollsBackEarlierWritesInSameTransaction() {
        Integer before = jdbc.queryForObject("SELECT COUNT(*) FROM DANG_KY_PHONG",Integer.class);
        rejected("ERROR_NOT_FOUND", () -> {
            procedures.bookBed(student,bed);
            procedures.confirmPayment("CI_NO_TX",DormitoryProcedureRepository.PaymentMethod.TIEN_MAT,BigDecimal.ONE,"CI");
        });
        assertEquals(before,jdbc.queryForObject("SELECT COUNT(*) FROM DANG_KY_PHONG",Integer.class));
        assertEquals("TRONG",jdbc.queryForObject("SELECT TrangThai FROM GIUONG WHERE MaGiuong=?",String.class,bed));
    }
    @Test void invalidDtoIsRejectedBeforeProcedureWrites() {
        assertThrows(ConstraintViolationException.class, () -> tx.executeWithoutResult(status ->
            procedures.recordMeter(new MeterDto(room,"2026-13",10L,9L,0L,1L),"CI")));
    }
}
