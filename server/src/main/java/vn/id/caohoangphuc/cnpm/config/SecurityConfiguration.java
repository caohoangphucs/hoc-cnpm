package vn.id.caohoangphuc.cnpm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import vn.id.caohoangphuc.cnpm.persistence.auth.AccountCredentialsRepository;

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
    UserDetailsService userDetailsService(AccountCredentialsRepository repository) {
        return username -> {
            var account = repository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Invalid credentials"));
            return User.withUsername(account.username())
                .password(account.passwordHash()).roles(account.role())
                .disabled(!"ACTIVE".equals(account.status())).build();
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
