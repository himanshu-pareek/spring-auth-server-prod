package dev.javarush.youtube.auth_server.client;

import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@EnableJdbcRepositories
public class ClientConfig {
    @Bean
    RegisteredClientRepository clientRepository(ClientEntityRepository entityRepository) {
        return new CustomJdbcRegisteredClientRepository(entityRepository);
    }
}
