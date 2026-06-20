package dev.javarush.youtube.auth_server.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;

public class AuthenticationConfig {
    @Bean
    @Order(2)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers("/auth/login/*", "/css/*", "/images/*", "/error").permitAll()
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
                );
        return http.build();
    }

    @Bean
    public OneTimeTokenGenerationSuccessHandler ottSuccessHandler() {
        return new ConsoleOTTGenerationSuccessHandler();
    }
}
