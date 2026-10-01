package vn.id.caohoangphuc.cnpm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // No business HTTP endpoints are exposed in the database foundation stage.
        return http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .anyRequest().denyAll()).build();
    }

    @Bean
    org.springframework.security.core.userdetails.UserDetailsService userDetailsService(
            vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentialsRepository repository) {
        return username -> {
            var account = repository.findByUsername(username).orElseThrow(() ->
                new org.springframework.security.core.userdetails.UsernameNotFoundException("Invalid credentials"));
            return org.springframework.security.core.userdetails.User.withUsername(account.username())
                .password(account.passwordHash()).roles(account.role())
                .disabled(!"ACTIVE".equals(account.status())).build();
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
