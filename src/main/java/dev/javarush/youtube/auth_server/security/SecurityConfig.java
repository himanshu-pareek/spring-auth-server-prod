package dev.javarush.youtube.auth_server.security;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import dev.javarush.youtube.auth_server.authorization.AuthorizationConfig;
import dev.javarush.youtube.auth_server.client.ClientConfig;
import dev.javarush.youtube.auth_server.consent.ConsentConfig;
import dev.javarush.youtube.auth_server.user.UserConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@Import({
        RSAKeyConfig.class, // Configuration related to RSA Keys
        ClientConfig.class, // Configuration related to OAuth2 clients
        UserConfig.class, // Configuration related to users
        AuthorizationConfig.class, // Configuration related to authorization tokens
        ConsentConfig.class // Configuration related to consent (scopes allowed for client by resource-owner)
})
public class SecurityConfig {
    @Bean
    @Order(1)
    SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) {
        OAuth2AuthorizationServerConfigurer oAuth2AuthorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(oAuth2AuthorizationServerConfigurer.getEndpointsMatcher())
                .with(
                        oAuth2AuthorizationServerConfigurer,
                        authServer -> authServer.oidc(Customizer.withDefaults())
                )
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/auth/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        ));

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers("/auth/login", "/css/*", "/images/*", "/error").permitAll()
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
                );
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
