package dev.javarush.youtube.auth_server.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.authentication.ott.JdbcOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;

public class AuthenticationConfig {
    @Bean
    @Order(2)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers("/auth/login/*", "/css/*", "/js/*", "/images/*", "/error").permitAll()
                                        .anyRequest().authenticated()
                )
                .formLogin(
                        form -> form.loginPage("/auth/login")
                                .defaultSuccessUrl("/")
                                .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .oneTimeTokenLogin(ott -> ott
                        .tokenGenerationSuccessHandler(ottSuccessHandler())
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login/ott")
                        .permitAll()
                )
                .webAuthn(webAuthn -> webAuthn
                        .rpId("localhost")
                        .rpName("Demo Spring Auth Server")
                        .allowedOrigins("http://localhost:9000")
                );
        return http.build();
    }

    @Bean
    OneTimeTokenGenerationSuccessHandler ottSuccessHandler() {
        return new ConsoleOTTGenerationSuccessHandler();
    }

    @Bean
    OneTimeTokenService oneTimeTokenService(JdbcOperations jdbcOperations) {
        return new JdbcOneTimeTokenService(jdbcOperations);
    }

    @Bean
    PublicKeyCredentialUserEntityRepository publicKeyCredentialUserEntityRepository(JdbcOperations jdbcOperations) {
        return new JdbcPublicKeyCredentialUserEntityRepository(jdbcOperations);
    }

    @Bean
    UserCredentialRepository userCredentialRepository(JdbcOperations jdbcOperations) {
        return new JdbcUserCredentialRepository(jdbcOperations);
    }
}
