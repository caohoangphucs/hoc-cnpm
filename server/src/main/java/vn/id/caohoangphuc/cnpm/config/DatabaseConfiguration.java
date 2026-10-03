package vn.id.caohoangphuc.cnpm.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.Validator;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentialsRepository;
import vn.id.caohoangphuc.cnpm.persistence.procedure.DormitoryProcedureRepository;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(
    basePackages = "vn.id.caohoangphuc.cnpm.persistence.read", transactionManagerRef = "jpaReadTransactionManager")
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
    DormitoryProcedureRepository dormitoryProcedureRepository(JdbcTemplate jdbc, Validator validator,
                                                              JdbcTransactionManager transactionManager) {
        return new DormitoryProcedureRepository(jdbc, validator, transactionManager);
    }

    // Read repositories use a dedicated JPA transaction manager on the same pool.
    // Procedure writes always use the primary JDBC manager; do not mix both boundaries.
    @Bean
    JpaTransactionManager jpaReadTransactionManager(EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }
}
