package dev.javarush.youtube.auth_server.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.webauthn.jackson.WebauthnJacksonModule;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.util.List;

public class AuthorizationConfig {
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
    OAuth2AuthorizationService oAuth2AuthorizationService(
            JdbcOperations jdbcOperations,
            RegisteredClientRepository registeredClientRepository
    ) {
        var service = new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);

        // 1. Create a builder and add WebAuthnAuthentication to the allowlist
        // Type Validator is used only to validate if given object should be deserialized or not
        // This is not used for actual serialization - deserialization
        BasicPolymorphicTypeValidator.Builder ptvBuilder = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("org.springframework.security.web.webauthn.");

        // 2. Pass the builder to Spring Security, allowing it to append all other necessary allowed types
        // Jackson Module is used for the actual serialization - deserialization
        List<JacksonModule> securityModules = SecurityJacksonModules
                .getModules(AuthorizationConfig.class.getClassLoader(), ptvBuilder);

        JsonMapper jsonMapper = JsonMapper.builder()
                .addModules(securityModules)
                .addModule(new OAuth2AuthorizationServerJacksonModule())
                // 3. Register the WebauthnJacksonModule so Jackson knows how to construct WebAuthn objects
                .addModule(new WebauthnJacksonModule())
                // 4. Set the fully populated validator!
                .polymorphicTypeValidator(ptvBuilder.build())
                .build();

        // RowMapper is used to read the row and convert it into the Java Object
        // That means, it is used for de-serialization
        var rowMapper = new JdbcOAuth2AuthorizationService
                .JsonMapperOAuth2AuthorizationRowMapper(registeredClientRepository, jsonMapper);
        service.setAuthorizationRowMapper(rowMapper);

        // ParametersMapper is used to convert the Java Object into the DB Row
        // That means, it is used for serialization
        var parametersMapper = new JdbcOAuth2AuthorizationService
                .JsonMapperOAuth2AuthorizationParametersMapper(jsonMapper);
        service.setAuthorizationParametersMapper(parametersMapper);
        
        return service;
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
