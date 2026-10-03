package vn.id.caohoangphuc.cnpm.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.Validator;
import java.time.Clock;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tools.jackson.databind.json.JsonMapper;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentialsRepository;
import vn.id.caohoangphuc.cnpm.persistence.service.*;
import vn.id.caohoangphuc.cnpm.service.*;

@Configuration
@EnableJpaRepositories(
    basePackages = "vn.id.caohoangphuc.cnpm.persistence.read",
    transactionManagerRef = "jpaReadTransactionManager")
@EnableTransactionManagement
public class DatabaseConfiguration {
  @Bean(name = "transactionManager")
  @Primary
  JdbcTransactionManager jdbcTransactionManager(DataSource dataSource) {
    return new JdbcTransactionManager(dataSource);
  }

  @Bean
  AccountCredentialsRepository accountCredentialsRepository(JdbcTemplate jdbc) {
    return new AccountCredentialsRepository(jdbc);
  }

  @Bean
  Clock serviceClock() {
    return Clock.systemUTC();
  }

  @Bean
  ServiceRepository serviceRepository(JdbcTemplate jdbc) {
    return new ServiceRepository(jdbc);
  }

  @Bean
  BookingRepository bookingRepository(JdbcTemplate jdbc, tools.jackson.databind.json.JsonMapper json) {
    return new BookingRepository(jdbc, json);
  }

  @Bean
  RoomCatalogRepository roomCatalogRepository(JdbcTemplate jdbc) {
    return new RoomCatalogRepository(jdbc);
  }

  @Bean
  InvoiceRepository invoiceRepository(JdbcTemplate jdbc) {
    return new InvoiceRepository(jdbc);
  }

  @Bean
  RepairRepository repairRepository(JdbcTemplate jdbc) {
    return new RepairRepository(jdbc);
  }

  @Bean
  GateRepository gateRepository(JdbcTemplate jdbc) {
    return new GateRepository(jdbc);
  }

  @Bean
  DisciplineRepository disciplineRepository(JdbcTemplate jdbc) {
    return new DisciplineRepository(jdbc);
  }

  @Bean
  PaymentRepository paymentRepository(JdbcTemplate jdbc) {
    return new PaymentRepository(jdbc);
  }

  @Bean
  CommunityRepository communityRepository(JdbcTemplate jdbc) {
    return new CommunityRepository(jdbc);
  }

  @Bean
  ServiceContext serviceContext(
      ServiceRepository repository,
      Validator validator,
      JsonMapper json,
      Clock clock,
      PlatformTransactionManager manager) {
    return new ServiceContext(repository, validator, json, clock, manager);
  }

  @Bean
  BookingService bookingService(BookingRepository repository, ServiceContext context) {
    return new BookingService(repository, context);
  }

  @Bean
  RoomCatalogService roomCatalogService(RoomCatalogRepository repository, ServiceContext context) {
    return new RoomCatalogService(repository, context);
  }

  @Bean
  RegistrationService registrationService(BookingRepository repository, ServiceContext context) {
    return new RegistrationService(repository, context);
  }

  @Bean
  InvoiceService invoiceService(InvoiceRepository repository, ServiceContext context) {
    return new InvoiceService(repository, context);
  }

  @Bean
  RepairService repairService(RepairRepository repository, ServiceContext context) {
    return new RepairService(repository, context);
  }

  @Bean
  GateService gateService(GateRepository repository, ServiceContext context) {
    return new GateService(repository, context);
  }

  @Bean
  DisciplineService disciplineService(DisciplineRepository repository, ServiceContext context) {
    return new DisciplineService(repository, context);
  }

  @Bean
  PaymentService paymentService(
      PaymentRepository repository, ServiceContext context, List<PaymentGateway> gateways) {
    return new PaymentService(repository, context, gateways);
  }

  @Bean
  CommunityService communityService(CommunityRepository repository, ServiceContext context) {
    return new CommunityService(repository, context);
  }

  // Read repositories use a dedicated JPA transaction manager on the same pool.
  // Service writes use the primary JDBC manager; do not mix it with the JPA read boundary.
  @Bean
  JpaTransactionManager jpaReadTransactionManager(EntityManagerFactory factory) {
    return new JpaTransactionManager(factory);
  }
}
