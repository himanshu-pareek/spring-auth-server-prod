package dev.javarush.youtube.auth_server.security;

import dev.javarush.youtube.auth_server.authentication.AuthenticationConfig;
import dev.javarush.youtube.auth_server.authorization.AuthorizationConfig;
import dev.javarush.youtube.auth_server.client.ClientConfig;
import dev.javarush.youtube.auth_server.consent.ConsentConfig;
import dev.javarush.youtube.auth_server.jwt.JWTConfig;
import dev.javarush.youtube.auth_server.user.UserConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        JWTConfig.class, // Configuration related to RSA Keys
        ClientConfig.class, // Configuration related to OAuth2 clients
        UserConfig.class, // Configuration related to users
        AuthorizationConfig.class, // Configuration related to authorization tokens
        ConsentConfig.class, // Configuration related to consent (scopes allowed for client by resource-owner)
        AuthenticationConfig.class, // Configuration for authentication mechanisms
})
public class SecurityConfig {}
