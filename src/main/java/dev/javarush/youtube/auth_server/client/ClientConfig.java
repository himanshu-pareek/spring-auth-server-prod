package dev.javarush.youtube.auth_server.client;

import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

public class ClientConfig {
    @Bean
    RegisteredClientRepository clientRepository(ClientEntityRepository entityRepository) {
        return new CustomJdbcRegisteredClientRepository(entityRepository);
    }
}
