package vn.id.caohoangphuc.cnpm.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(
    basePackages = "vn.id.caohoangphuc.cnpm.persistence.read", transactionManagerRef = "jpaReadTransactionManager")
@EnableTransactionManagement
public class DatabaseConfiguration {
    @Bean(name = "transactionManager")
    @Primary
    JdbcTransactionManager jdbcTransactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    // Read repositories use a dedicated JPA transaction manager on the same pool.
    // Procedure writes always use the primary JDBC manager; do not mix both boundaries.
    @Bean
    JpaTransactionManager jpaReadTransactionManager(EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }
}
