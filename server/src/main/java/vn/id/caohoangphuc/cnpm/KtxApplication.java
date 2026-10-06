package vn.id.caohoangphuc.cnpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KtxApplication {
    /** Starts the Spring Boot application. */
    public static void main(String[] args) { SpringApplication.run(KtxApplication.class, args); }
}
